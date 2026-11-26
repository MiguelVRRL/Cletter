package com.estructura.datos.cletter.datos;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Singleton totalmente no-bloqueante para audio y vibración.
 * Todas las operaciones de SoundPool, MediaPlayer, disco y Vibrator se ejecutan
 * serialmente en un único HandlerThread de fondo, por lo que el hilo principal (UI)
 * nunca adquiere locks ni hace IPC síncrono al servidor de audio.
 */
public final class GestorFeedback {

    private static final int TASA_EFECTOS = 22050;
    private static final int TASA_AMBIENTE = 11025;

    private static final int MSG_INICIAR_AUDIO = 1;
    private static final int MSG_MUSICA_ON = 2;
    private static final int MSG_MUSICA_OFF = 3;

    // Relaciones de tono (pitch) para la escala pentatónica a partir de Do4 (261.63 Hz)
    private static final float[] PITCH_ESCALA = new float[]{
            1.00f, 1.122f, 1.260f, 1.498f, 1.682f, 2.00f, 2.00f
    };

    private static volatile GestorFeedback instancia;

    private final Context contexto;
    private final AlmacenamientoJuego almacenamiento;
    private final Vibrator vibrador;
    private final Handler manejadorAudio;

    // Estado tocado únicamente dentro del HandlerThread (sin contención de locks)
    private SoundPool soundPool;
    private final Set<Integer> sonidosCargados = new HashSet<>(8);
    private int idNota = 0;
    private int idAcierto = 0;
    private int idError = 0;
    private int idVictoria = 0;
    private boolean audioPreparado = false;

    private MediaPlayer reproductorAmbiente;
    private File archivoAmbiente;
    private boolean deseaMusica = false;
    private long ultimaVibracionMs = 0L;

    private GestorFeedback(Context contexto) {
        this.contexto = contexto.getApplicationContext();
        this.almacenamiento = new AlmacenamientoJuego(this.contexto);
        this.vibrador = resolverVibrador(this.contexto);

        HandlerThread hiloAudio = new HandlerThread("Cletter-Audio", Process.THREAD_PRIORITY_BACKGROUND);
        hiloAudio.start();
        this.manejadorAudio = new Handler(hiloAudio.getLooper(), msg -> {
            if (msg.what == MSG_INICIAR_AUDIO) {
                prepararAudioEnHilo();
                return true;
            } else if (msg.what == MSG_MUSICA_ON) {
                deseaMusica = true;
                actualizarEstadoMusicaEnHilo();
                return true;
            } else if (msg.what == MSG_MUSICA_OFF) {
                deseaMusica = false;
                pausarMusicaEnHilo();
                return true;
            }
            return false;
        });

        manejadorAudio.sendEmptyMessage(MSG_INICIAR_AUDIO);
    }

    public static GestorFeedback obtenerInstancia(Context contexto) {
        if (instancia == null) {
            synchronized (GestorFeedback.class) {
                if (instancia == null) {
                    instancia = new GestorFeedback(contexto);
                }
            }
        }
        return instancia;
    }

    public void reproducirPasoLetra(int indicePaso) {
        final boolean sonido = almacenamiento.estaSonidoActivo();
        final boolean vibracion = almacenamiento.estaVibracionActiva();
        if (!sonido && !vibracion) {
            return;
        }
        final int idx = Math.max(0, Math.min(PITCH_ESCALA.length - 1, indicePaso));
        manejadorAudio.post(() -> {
            if (sonido) {
                reproducirEfectoEnHilo(idNota, 0.35f, PITCH_ESCALA[idx]);
            }
            if (vibracion) {
                vibrarSuaveEnHilo(12);
            }
        });
    }

    public void reproducirPalabraValida() {
        final boolean sonido = almacenamiento.estaSonidoActivo();
        final boolean vibracion = almacenamiento.estaVibracionActiva();
        if (!sonido && !vibracion) {
            return;
        }
        manejadorAudio.post(() -> {
            if (sonido) {
                reproducirEfectoEnHilo(idAcierto, 0.45f, 1.0f);
            }
            if (vibracion) {
                vibrarSuaveEnHilo(24);
            }
        });
    }

    public void reproducirPalabraInvalida() {
        final boolean sonido = almacenamiento.estaSonidoActivo();
        final boolean vibracion = almacenamiento.estaVibracionActiva();
        if (!sonido && !vibracion) {
            return;
        }
        manejadorAudio.post(() -> {
            if (sonido) {
                reproducirEfectoEnHilo(idError, 0.30f, 1.0f);
            }
            if (vibracion) {
                vibrarSuaveEnHilo(30);
            }
        });
    }

    public void reproducirVictoria() {
        final boolean sonido = almacenamiento.estaSonidoActivo();
        final boolean vibracion = almacenamiento.estaVibracionActiva();
        if (!sonido && !vibracion) {
            return;
        }
        manejadorAudio.post(() -> {
            if (sonido) {
                reproducirEfectoEnHilo(idVictoria, 0.50f, 1.0f);
            }
            if (vibracion) {
                vibrarSuaveEnHilo(50);
            }
        });
    }

    public void iniciarMusicaAmbiente() {
        manejadorAudio.removeMessages(MSG_MUSICA_OFF);
        manejadorAudio.removeMessages(MSG_MUSICA_ON);
        manejadorAudio.sendEmptyMessage(MSG_MUSICA_ON);
    }

    public void detenerMusicaAmbiente() {
        manejadorAudio.removeMessages(MSG_MUSICA_ON);
        manejadorAudio.removeMessages(MSG_MUSICA_OFF);
        manejadorAudio.sendEmptyMessage(MSG_MUSICA_OFF);
    }

    // ---- Métodos internos ejecutados EXCLUSIVAMENTE en el HandlerThread ----

    private void prepararAudioEnHilo() {
        if (audioPreparado) {
            return;
        }
        try {
            File carpeta = new File(contexto.getCacheDir(), "audio_cletter_v1");
            if (!carpeta.exists()) {
                //noinspection ResultOfMethodCallIgnored
                carpeta.mkdirs();
            }

            File fNota = asegurarWavNota(new File(carpeta, "nota.wav"));
            File fAcierto = asegurarWavAcierto(new File(carpeta, "acierto.wav"));
            File fError = asegurarWavError(new File(carpeta, "error.wav"));
            File fVictoria = asegurarWavVictoria(new File(carpeta, "victoria.wav"));
            archivoAmbiente = asegurarWavAmbiente(new File(carpeta, "ambiente.wav"));

            AudioAttributes atributos = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool pool = new SoundPool.Builder()
                    .setMaxStreams(4)
                    .setAudioAttributes(atributos)
                    .build();

            pool.setOnLoadCompleteListener((sp, sampleId, status) -> {
                if (status == 0) {
                    manejadorAudio.post(() -> sonidosCargados.add(sampleId));
                }
            });

            idNota = pool.load(fNota.getAbsolutePath(), 1);
            idAcierto = pool.load(fAcierto.getAbsolutePath(), 1);
            idError = pool.load(fError.getAbsolutePath(), 1);
            idVictoria = pool.load(fVictoria.getAbsolutePath(), 1);

            soundPool = pool;
            audioPreparado = true;

            if (deseaMusica) {
                actualizarEstadoMusicaEnHilo();
            }
        } catch (Throwable ignored) {
            // En entornos sin salida de audio (tests/emuladores mudos) se ignora sin afectar la app
        }
    }

    private void reproducirEfectoEnHilo(int sampleId, float volumen, float pitch) {
        if (soundPool != null && sampleId != 0 && sonidosCargados.contains(sampleId)) {
            try {
                soundPool.play(sampleId, volumen, volumen, 1, 0, pitch);
            } catch (Throwable ignored) {
            }
        }
    }

    private void actualizarEstadoMusicaEnHilo() {
        if (!deseaMusica || !almacenamiento.estaMusicaActiva()) {
            pausarMusicaEnHilo();
            return;
        }
        if (!audioPreparado) {
            prepararAudioEnHilo();
            return;
        }
        if (archivoAmbiente == null || !archivoAmbiente.exists()) {
            return;
        }
        try {
            if (reproductorAmbiente == null) {
                MediaPlayer mp = new MediaPlayer();
                mp.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
                mp.setOnErrorListener((player, what, extra) -> {
                    manejadorAudio.post(this::liberarReproductorAmbienteEnHilo);
                    return true;
                });
                mp.setDataSource(archivoAmbiente.getAbsolutePath());
                mp.setLooping(true);
                mp.setVolume(0.14f, 0.14f);
                mp.prepare();
                reproductorAmbiente = mp;
            }
            if (!reproductorAmbiente.isPlaying()) {
                reproductorAmbiente.start();
            }
        } catch (Throwable ignored) {
            liberarReproductorAmbienteEnHilo();
        }
    }

    private void pausarMusicaEnHilo() {
        if (reproductorAmbiente != null) {
            try {
                if (reproductorAmbiente.isPlaying()) {
                    reproductorAmbiente.pause();
                }
            } catch (Throwable ignored) {
                liberarReproductorAmbienteEnHilo();
            }
        }
    }

    private void liberarReproductorAmbienteEnHilo() {
        if (reproductorAmbiente != null) {
            try {
                reproductorAmbiente.release();
            } catch (Throwable ignored) {
            }
            reproductorAmbiente = null;
        }
    }

    private void vibrarSuaveEnHilo(long milisegundos) {
        if (vibrador == null) {
            return;
        }
        long ahora = SystemClock.uptimeMillis();
        if (ahora - ultimaVibracionMs < 28L) {
            return;
        }
        ultimaVibracionMs = ahora;
        try {
            if (!vibrador.hasVibrator()) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrador.vibrate(VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //noinspection deprecation
                vibrador.vibrate(milisegundos);
            }
        } catch (Throwable ignored) {
        }
    }

    private static Vibrator resolverVibrador(Context ctx) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager gestor = (VibratorManager) ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                return gestor != null ? gestor.getDefaultVibrator() : null;
            }
            return (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private File asegurarWavNota(File destino) throws IOException {
        if (destino.exists() && destino.length() > 44) return destino;
        short[] pcm = sintetizarTono(261.63, 75, 0.55f);
        escribirWav(destino, pcm, TASA_EFECTOS);
        return destino;
    }

    private File asegurarWavAcierto(File destino) throws IOException {
        if (destino.exists() && destino.length() > 44) return destino;
        short[] n1 = sintetizarTono(523.25, 80, 0.50f);
        short[] n2 = sintetizarTono(659.25, 130, 0.55f);
        escribirWav(destino, concatenar(n1, n2), TASA_EFECTOS);
        return destino;
    }

    private File asegurarWavError(File destino) throws IOException {
        if (destino.exists() && destino.length() > 44) return destino;
        short[] pcm = sintetizarTono(185.0, 95, 0.40f);
        escribirWav(destino, pcm, TASA_EFECTOS);
        return destino;
    }

    private File asegurarWavVictoria(File destino) throws IOException {
        if (destino.exists() && destino.length() > 44) return destino;
        short[] n1 = sintetizarTono(523.25, 85, 0.50f);
        short[] n2 = sintetizarTono(659.25, 85, 0.50f);
        short[] n3 = sintetizarTono(783.99, 95, 0.52f);
        short[] n4 = sintetizarTono(1046.50, 210, 0.55f);
        escribirWav(destino, concatenar(concatenar(n1, n2), concatenar(n3, n4)), TASA_EFECTOS);
        return destino;
    }

    private File asegurarWavAmbiente(File destino) throws IOException {
        if (destino.exists() && destino.length() > 44) return destino;
        double[] notas = new double[]{220.0, 261.63, 293.66, 329.63, 261.63, 196.0};
        int muestrasPorPaso = TASA_AMBIENTE;
        short[] pcm = new short[notas.length * muestrasPorPaso];
        for (int p = 0; p < notas.length; p++) {
            double freq = notas[p];
            int inicio = p * muestrasPorPaso;
            int largoNota = (int) (TASA_AMBIENTE * 0.55);
            for (int i = 0; i < largoNota; i++) {
                double t = (double) i / TASA_AMBIENTE;
                double env = Math.min(1.0, i / 180.0) * Math.exp(-4.0 * i / largoNota);
                pcm[inicio + i] = (short) (Math.sin(2.0 * Math.PI * freq * t) * env * 0.35 * Short.MAX_VALUE);
            }
        }
        escribirWav(destino, pcm, TASA_AMBIENTE);
        return destino;
    }

    private static short[] sintetizarTono(double frecuencia, int duracionMs, float volumen) {
        int n = (TASA_EFECTOS * duracionMs) / 1000;
        short[] muestras = new short[n];
        for (int i = 0; i < n; i++) {
            double t = (double) i / TASA_EFECTOS;
            double env = Math.min(1.0, i / 90.0) * Math.exp(-3.4 * i / n);
            muestras[i] = (short) (Math.sin(2.0 * Math.PI * frecuencia * t) * env * volumen * Short.MAX_VALUE);
        }
        return muestras;
    }

    private static short[] concatenar(short[] a, short[] b) {
        short[] res = new short[a.length + b.length];
        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        return res;
    }

    private static void escribirWav(File destino, short[] muestras, int tasa) throws IOException {
        int bytesDatos = muestras.length * 2;
        byte[] buffer = new byte[44 + bytesDatos];
        escribirAscii(buffer, 0, "RIFF");
        escribirIntLE(buffer, 4, 36 + bytesDatos);
        escribirAscii(buffer, 8, "WAVE");
        escribirAscii(buffer, 12, "fmt ");
        escribirIntLE(buffer, 16, 16);
        escribirShortLE(buffer, 20, (short) 1);
        escribirShortLE(buffer, 22, (short) 1);
        escribirIntLE(buffer, 24, tasa);
        escribirIntLE(buffer, 28, tasa * 2);
        escribirShortLE(buffer, 32, (short) 2);
        escribirShortLE(buffer, 34, (short) 16);
        escribirAscii(buffer, 36, "data");
        escribirIntLE(buffer, 40, bytesDatos);

        int pos = 44;
        for (short s : muestras) {
            buffer[pos++] = (byte) (s & 0xFF);
            buffer[pos++] = (byte) ((s >> 8) & 0xFF);
        }
        try (FileOutputStream fos = new FileOutputStream(destino)) {
            fos.write(buffer);
        }
    }

    private static void escribirAscii(byte[] b, int offset, String texto) {
        for (int i = 0; i < texto.length(); i++) {
            b[offset + i] = (byte) texto.charAt(i);
        }
    }

    private static void escribirIntLE(byte[] b, int offset, int v) {
        b[offset] = (byte) (v & 0xFF);
        b[offset + 1] = (byte) ((v >> 8) & 0xFF);
        b[offset + 2] = (byte) ((v >> 16) & 0xFF);
        b[offset + 3] = (byte) ((v >> 24) & 0xFF);
    }

    private static void escribirShortLE(byte[] b, int offset, short v) {
        b[offset] = (byte) (v & 0xFF);
        b[offset + 1] = (byte) ((v >> 8) & 0xFF);
    }
}
