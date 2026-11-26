package com.estructura.datos.cletter.interfaz.juego;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.estructura.datos.cletter.R;
import com.estructura.datos.cletter.datos.AlmacenamientoJuego;
import com.estructura.datos.cletter.datos.CalculadoraPuntos;
import com.estructura.datos.cletter.datos.CatalogoPisos;
import com.estructura.datos.cletter.datos.GeneradorTablero;
import com.estructura.datos.cletter.datos.GestorDiccionario;
import com.estructura.datos.cletter.datos.GestorFeedback;
import com.estructura.datos.cletter.datos.GestorPortadas;
import com.estructura.datos.cletter.estructuras.arbol.ArbolBinario;
import com.estructura.datos.cletter.estructuras.cola.ColaCircular;
import com.estructura.datos.cletter.estructuras.lista.ListaSimple;
import com.estructura.datos.cletter.interfaz.archivo.ActividadDetallePiso;
import com.estructura.datos.cletter.modelo.Logro;
import com.estructura.datos.cletter.modelo.PalabraEncontrada;
import com.estructura.datos.cletter.modelo.PisoInfo;
import com.estructura.datos.cletter.modelo.PistaTablero;
import com.estructura.datos.cletter.modelo.ProgresoNivelGuardado;
import com.estructura.datos.cletter.modelo.TableroNivel;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pantalla principal de partida de Cletter.
 * Integra las 5 estructuras de datos obligatorias en tiempo real:
 * - Grafo + Pila<Integer> dentro de VistaTableroHexagonal.
 * - ArbolBinario balanceado para validar palabras del idioma activo.
 * - ListaSimple<PalabraEncontrada> para registrar y consultar las palabras del nivel.
 * - ColaCircular<PistaTablero> para pistas rotativas y ColaCircular<Logro> para logros.
 */
public class ActividadJuego extends AppCompatActivity implements VistaTableroHexagonal.EscuchaTablero {

    public static final String EXTRA_NIVEL = "extra_nivel";
    private static final int COSTO_PISTA = 15;
    private static final int COSTO_MEZCLAR = 10;

    private AlmacenamientoJuego almacenamiento;
    private GestorFeedback feedback;
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();
    private final AtomicInteger tokenCarga = new AtomicInteger(0);

    private TextView txtMonedas;
    private TextView txtNumeroNivel;
    private ProgressBar barraPuntos;
    private TextView txtPuntosNivel;
    private TextView txtPisoEnJuego;
    private View contenedorPreviewPalabra;
    private TextView txtPalabraActual;
    private TextView txtPuntosPreview;
    private TextView txtEstadoPalabra;
    private VistaTableroHexagonal vistaTablero;

    private ArbolBinario<String> diccionario;
    private TableroNivel tableroActual;
    private final ListaSimple<PalabraEncontrada> palabrasEncontradas = new ListaSimple<>();
    private ObjectAnimator animadorSacudida;
    private ObjectAnimator animadorBarra;
    private AlertDialog dialogoActivo;

    private int numeroNivel;
    private int puntosNivel = 0;
    private int longitudMaximaNivel = 0;
    private int mejorPalabraNivel = 0;
    private int longitudTrazoAnterior = 0;
    private boolean nivelSuperado = false;
    private boolean cargando = false;
    private long contadorMezclas = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        almacenamiento = new AlmacenamientoJuego(this);
        feedback = GestorFeedback.obtenerInstancia(this);
        setContentView(R.layout.actividad_juego);

        vincularVistas();

        numeroNivel = getIntent().getIntExtra(EXTRA_NIVEL, almacenamiento.obtenerNivelActual());
        numeroNivel = Math.max(1, Math.min(CatalogoPisos.TOTAL_NIVELES, numeroNivel));

        cargarNivel(numeroNivel);
    }

    private void vincularVistas() {
        txtMonedas = findViewById(R.id.txt_monedas_juego);
        txtNumeroNivel = findViewById(R.id.txt_numero_nivel_juego);
        barraPuntos = findViewById(R.id.barra_puntos_nivel);
        txtPuntosNivel = findViewById(R.id.txt_puntos_nivel);
        txtPisoEnJuego = findViewById(R.id.txt_piso_en_juego);
        contenedorPreviewPalabra = findViewById(R.id.contenedor_preview_palabra);
        txtPalabraActual = findViewById(R.id.txt_palabra_actual);
        txtPuntosPreview = findViewById(R.id.txt_puntos_preview);
        txtEstadoPalabra = findViewById(R.id.txt_estado_palabra);
        TextView txtBtnPista = findViewById(R.id.txt_btn_pista);
        TextView txtBtnMezclar = findViewById(R.id.txt_btn_mezclar);
        vistaTablero = findViewById(R.id.tablero_hexagonal);

        vistaTablero.setEscuchaTablero(this);

        txtBtnPista.setText(getString(R.string.boton_pista, COSTO_PISTA));
        txtBtnMezclar.setText(getString(R.string.boton_mezclar, COSTO_MEZCLAR));

        findViewById(R.id.btn_palabras_encontradas).setOnClickListener(v -> mostrarDialogoPalabras());
        findViewById(R.id.btn_pausa_juego).setOnClickListener(v -> mostrarDialogoPausa());
        findViewById(R.id.btn_pista).setOnClickListener(v -> usarPista());
        findViewById(R.id.btn_mezclar).setOnClickListener(v -> mezclarTableroActual());
    }

    private void cargarNivel(int nivel) {
        final int token = tokenCarga.incrementAndGet();
        this.numeroNivel = Math.max(1, Math.min(CatalogoPisos.TOTAL_NIVELES, nivel));
        this.puntosNivel = 0;
        this.longitudMaximaNivel = 0;
        this.mejorPalabraNivel = 0;
        this.longitudTrazoAnterior = 0;
        this.nivelSuperado = false;
        this.cargando = true;
        this.contadorMezclas = 0;
        this.palabrasEncontradas.Anula();

        PisoInfo piso = CatalogoPisos.obtenerPorNivel(numeroNivel);
        final String idiomaActual = almacenamiento.obtenerIdioma();
        final boolean esIngles = "en".equals(idiomaActual);

        txtNumeroNivel.setText(String.valueOf(numeroNivel));
        txtMonedas.setText(String.valueOf(almacenamiento.obtenerMonedas()));
        txtPisoEnJuego.setText(getString(R.string.etiqueta_piso_completo, piso.getNumero(), piso.getTitulo(esIngles)));
        ocultarPreviewPalabra();
        txtEstadoPalabra.setTextColor(ContextCompat.getColor(this, R.color.texto_tenue));
        txtEstadoPalabra.setText(R.string.cargando_diccionario);
        vistaTablero.setBloqueado(true);

        final int nivelObjetivo = numeroNivel;
        final ProgresoNivelGuardado guardado = almacenamiento.obtenerProgresoNivelEnCurso(nivelObjetivo, idiomaActual);

        ejecutor.execute(() -> {
            ArbolBinario<String> arbolCargado = GestorDiccionario.obtenerInstancia()
                    .obtenerArbol(getApplicationContext(), esIngles);
            TableroNivel generado;
            if (guardado != null) {
                generado = GeneradorTablero.restaurarTableroDesdeLetras(
                        nivelObjetivo,
                        guardado.getLetrasTablero(),
                        arbolCargado
                );
            } else {
                generado = GeneradorTablero.generarNivel(nivelObjetivo, esIngles, arbolCargado, 0L);
            }

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || token != tokenCarga.get()) {
                    return;
                }
                diccionario = arbolCargado;
                tableroActual = generado;
                cargando = false;

                palabrasEncontradas.Anula();
                puntosNivel = 0;
                longitudMaximaNivel = 0;
                mejorPalabraNivel = 0;

                if (guardado != null && guardado.getPalabras() != null) {
                    ListaSimple<PalabraEncontrada> previas = guardado.getPalabras();
                    for (int i = 1; i <= previas.Cantidad(); i++) {
                        PalabraEncontrada p = previas.Obtener(i);
                        if (p != null && !palabrasEncontradas.Contiene(p)) {
                            palabrasEncontradas.InsertarAlFinal(p);
                            puntosNivel += p.getPuntos();
                            if (p.getTexto().length() > longitudMaximaNivel) {
                                longitudMaximaNivel = p.getTexto().length();
                            }
                            if (p.getPuntos() > mejorPalabraNivel) {
                                mejorPalabraNivel = p.getPuntos();
                            }
                        }
                    }
                    contadorMezclas = 1; // Asegura conservar el tablero exacto si se sale de nuevo
                }

                vistaTablero.setTablero(generado);
                vistaTablero.setBloqueado(false);
                txtEstadoPalabra.setText("");
                barraPuntos.setMax(generado.getPuntajeMeta());
                barraPuntos.setProgress(Math.min(generado.getPuntajeMeta(), puntosNivel));
                actualizarBarraPuntos(false);
            });
        });
    }

    @Override
    public void alCambiarTrazo(String palabraParcial, int longitud) {
        if (longitud > longitudTrazoAnterior && longitud > 0) {
            feedback.reproducirPasoLetra(longitud - 1);
        }
        longitudTrazoAnterior = longitud;

        if (palabraParcial == null || palabraParcial.isEmpty()) {
            vistaTablero.setEstadoRepetida(false);
            ocultarPreviewPalabra();
            return;
        }

        cancelarSacudidaPreview();
        contenedorPreviewPalabra.setVisibility(View.VISIBLE);
        txtPalabraActual.setText(palabraParcial);
        txtPalabraActual.setTextColor(ContextCompat.getColor(this, R.color.texto_primario));
        txtEstadoPalabra.setText("");

        boolean yaEncontrada = false;
        boolean esPalabraValidaNueva = false;
        if (longitud >= 3 && diccionario != null) {
            String limpia = GestorDiccionario.normalizarPalabra(palabraParcial);
            if (diccionario.existeValor(limpia)) {
                yaEncontrada = palabrasEncontradas.Contiene(new PalabraEncontrada(limpia, 0));
                esPalabraValidaNueva = !yaEncontrada;
            }
        }

        // Indicador visual naranja antes de soltar si la palabra ya fue encontrada
        vistaTablero.setEstadoRepetida(yaEncontrada);
        txtPalabraActual.setBackgroundResource(
                yaEncontrada ? R.drawable.fondo_preview_palabra_repetida : R.drawable.fondo_preview_palabra
        );

        // Solo mostrar la burbuja de puntaje cuando realmente forma una palabra válida nueva
        if (esPalabraValidaNueva) {
            int puntosEstimados = CalculadoraPuntos.calcularPuntos(palabraParcial);
            txtPuntosPreview.setText(String.valueOf(puntosEstimados));
            txtPuntosPreview.setVisibility(View.VISIBLE);
        } else {
            txtPuntosPreview.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void alCompletarTrazo(String palabraFormada) {
        longitudTrazoAnterior = 0;
        vistaTablero.setEstadoRepetida(false);

        if (cargando || nivelSuperado || diccionario == null || tableroActual == null) {
            ocultarPreviewPalabra();
            return;
        }
        if (palabraFormada == null || palabraFormada.length() < 3) {
            ocultarPreviewPalabra();
            return;
        }

        String limpia = GestorDiccionario.normalizarPalabra(palabraFormada);
        if (!diccionario.existeValor(limpia)) {
            feedback.reproducirPalabraInvalida();
            sacudirYOcultarPreview(false);
            return;
        }

        PalabraEncontrada candidata = new PalabraEncontrada(limpia, 0);
        if (palabrasEncontradas.Contiene(candidata)) {
            feedback.reproducirPalabraInvalida();
            sacudirYOcultarPreview(true);
            return;
        }

        int puntos = CalculadoraPuntos.calcularPuntos(limpia);
        palabrasEncontradas.InsertarAlFinal(new PalabraEncontrada(limpia, puntos));

        puntosNivel += puntos;
        if (limpia.length() > longitudMaximaNivel) {
            longitudMaximaNivel = limpia.length();
        }
        if (puntos > mejorPalabraNivel) {
            mejorPalabraNivel = puntos;
        }

        ocultarPreviewPalabra();
        vistaTablero.limpiarPista();
        txtEstadoPalabra.setText("");
        feedback.reproducirPalabraValida();
        actualizarBarraPuntos(true);

        if (puntosNivel >= tableroActual.getPuntajeMeta()) {
            completarNivel();
        } else {
            guardarProgresoActual();
        }
    }

    private void guardarProgresoActual() {
        if (nivelSuperado || tableroActual == null || almacenamiento == null) {
            return;
        }
        if (puntosNivel <= 0 && contadorMezclas == 0 && palabrasEncontradas.EsVacia()) {
            return;
        }
        String letras = GeneradorTablero.serializarLetras(tableroActual);
        if (letras.length() == GeneradorTablero.TOTAL_CELDAS) {
            almacenamiento.guardarProgresoNivelEnCurso(
                    numeroNivel,
                    almacenamiento.obtenerIdioma(),
                    letras,
                    palabrasEncontradas
            );
        }
    }

    private void cancelarSacudidaPreview() {
        if (animadorSacudida != null) {
            animadorSacudida.cancel();
            animadorSacudida = null;
        }
        if (contenedorPreviewPalabra != null) {
            contenedorPreviewPalabra.setTranslationX(0f);
        }
    }

    private void ocultarPreviewPalabra() {
        cancelarSacudidaPreview();
        if (contenedorPreviewPalabra != null) {
            contenedorPreviewPalabra.setVisibility(View.INVISIBLE);
        }
        txtPalabraActual.setText("");
        txtPalabraActual.setBackgroundResource(R.drawable.fondo_preview_palabra);
        txtPuntosPreview.setVisibility(View.INVISIBLE);
    }

    private void sacudirYOcultarPreview(boolean marcoNaranja) {
        cancelarSacudidaPreview();
        contenedorPreviewPalabra.setVisibility(View.VISIBLE);
        txtPalabraActual.setBackgroundResource(
                marcoNaranja ? R.drawable.fondo_preview_palabra_repetida : R.drawable.fondo_preview_palabra
        );
        txtPuntosPreview.setVisibility(View.INVISIBLE);

        float d = getResources().getDisplayMetrics().density;
        animadorSacudida = ObjectAnimator.ofFloat(
                contenedorPreviewPalabra,
                "translationX",
                0f, -10f * d, 10f * d, -7f * d, 7f * d, -3f * d, 3f * d, 0f
        );
        animadorSacudida.setDuration(280);
        animadorSacudida.addListener(new AnimatorListenerAdapter() {
            private boolean cancelada = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelada = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelada) {
                    animadorSacudida = null;
                    ocultarPreviewPalabra();
                }
            }
        });
        animadorSacudida.start();
    }

    private void mostrarMensajeEstado(String mensaje, int colorRes) {
        txtEstadoPalabra.setTextColor(ContextCompat.getColor(this, colorRes));
        txtEstadoPalabra.setText(mensaje);
    }

    private void actualizarBarraPuntos(boolean animar) {
        if (tableroActual == null) {
            return;
        }
        int meta = tableroActual.getPuntajeMeta();
        int progresoAcotado = Math.min(meta, puntosNivel);
        if (animadorBarra != null) {
            animadorBarra.cancel();
            animadorBarra = null;
        }
        if (animar) {
            animadorBarra = ObjectAnimator.ofInt(barraPuntos, "progress", barraPuntos.getProgress(), progresoAcotado);
            animadorBarra.setDuration(260);
            animadorBarra.start();
        } else {
            barraPuntos.setProgress(progresoAcotado);
        }
        txtPuntosNivel.setText(getString(R.string.progreso_puntos, puntosNivel, meta));
    }

    private void usarPista() {
        if (cargando || nivelSuperado || tableroActual == null) {
            return;
        }
        if (almacenamiento.obtenerMonedas() < COSTO_PISTA) {
            Toast.makeText(this, getString(R.string.monedas_insuficientes, COSTO_PISTA), Toast.LENGTH_SHORT).show();
            return;
        }

        ColaCircular<PistaTablero> cola = tableroActual.getColaPistas();
        if (cola == null || cola.Vacia()) {
            Toast.makeText(this, R.string.sin_pistas_disponibles, Toast.LENGTH_SHORT).show();
            return;
        }

        PistaTablero pistaElegida = null;
        int totalEnCola = cola.getTamaño();
        for (int i = 0; i < totalEnCola; i++) {
            PistaTablero candidata = cola.desencolar();
            if (candidata == null) {
                break;
            }
            cola.encolar(candidata);
            if (!palabrasEncontradas.Contiene(new PalabraEncontrada(candidata.getPalabra(), 0))) {
                pistaElegida = candidata;
                break;
            }
        }

        if (pistaElegida == null) {
            Toast.makeText(this, R.string.sin_pistas_disponibles, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!almacenamiento.gastarMonedas(COSTO_PISTA)) {
            return;
        }
        txtMonedas.setText(String.valueOf(almacenamiento.obtenerMonedas()));

        int[] caminoCompleto = pistaElegida.getCaminoIndices();
        int largoMostrar = Math.min(3, caminoCompleto.length);
        int[] caminoParcial = Arrays.copyOf(caminoCompleto, largoMostrar);
        vistaTablero.mostrarPista(caminoParcial);
        feedback.reproducirPasoLetra(4);
    }

    private void mezclarTableroActual() {
        if (cargando || nivelSuperado || tableroActual == null || diccionario == null) {
            return;
        }
        if (almacenamiento.obtenerMonedas() < COSTO_MEZCLAR) {
            Toast.makeText(this, getString(R.string.monedas_insuficientes, COSTO_MEZCLAR), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!almacenamiento.gastarMonedas(COSTO_MEZCLAR)) {
            return;
        }

        txtMonedas.setText(String.valueOf(almacenamiento.obtenerMonedas()));
        cargando = true;
        vistaTablero.setBloqueado(true);
        contadorMezclas++;

        final int token = tokenCarga.get();
        final TableroNivel base = tableroActual;
        final long semilla = System.currentTimeMillis() + (contadorMezclas * 131L);

        ejecutor.execute(() -> {
            TableroNivel mezclado = GeneradorTablero.mezclarTablero(base, diccionario, semilla);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || token != tokenCarga.get()) {
                    return;
                }
                tableroActual = mezclado;
                cargando = false;
                vistaTablero.setTablero(mezclado);
                vistaTablero.setBloqueado(false);
                mostrarMensajeEstado(getString(R.string.tablero_mezclado), R.color.texto_tenue);
                feedback.reproducirPasoLetra(2);
                guardarProgresoActual();
            });
        });
    }

    private void completarNivel() {
        nivelSuperado = true;
        almacenamiento.limpiarProgresoNivelEnCurso();
        vistaTablero.setBloqueado(true);
        feedback.reproducirVictoria();

        boolean conquistoPiso = (numeroNivel % CatalogoPisos.NIVELES_POR_PISO == 0);
        int monedasGanadas = CalculadoraPuntos.calcularMonedasRecompensa(numeroNivel, conquistoPiso);

        ColaCircular<Logro> colaNuevosLogros = almacenamiento.registrarNivelCompletado(
                numeroNivel,
                puntosNivel,
                palabrasEncontradas.Cantidad(),
                longitudMaximaNivel,
                mejorPalabraNivel,
                monedasGanadas
        );
        txtMonedas.setText(String.valueOf(almacenamiento.obtenerMonedas()));

        boolean esIngles = almacenamiento.esIngles();
        while (!colaNuevosLogros.Vacia()) {
            Logro logro = colaNuevosLogros.desencolar();
            if (logro != null) {
                Toast.makeText(
                        this,
                        getString(R.string.logro_desbloqueado_toast, logro.getTitulo(esIngles)),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        mostrarDialogoVictoria(conquistoPiso, monedasGanadas);
    }

    private void mostrarDialogoPalabras() {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialogo_palabras, null);
        LinearLayout contenedor = vista.findViewById(R.id.contenedor_lista_palabras);
        TextView btnCerrar = vista.findViewById(R.id.btn_cerrar_palabras);

        if (palabrasEncontradas.EsVacia()) {
            TextView vacio = new TextView(this);
            vacio.setText(R.string.sin_palabras_aun);
            vacio.setTextColor(ContextCompat.getColor(this, R.color.texto_tenue));
            vacio.setTextSize(14f);
            vacio.setGravity(Gravity.CENTER);
            vacio.setPadding(16, 48, 16, 48);
            contenedor.addView(vacio);
        } else {
            int densidad = Math.round(getResources().getDisplayMetrics().density);
            for (int i = 1; i <= palabrasEncontradas.Cantidad(); i++) {
                PalabraEncontrada palabra = palabrasEncontradas.Obtener(i);
                if (palabra == null) {
                    continue;
                }
                LinearLayout fila = new LinearLayout(this);
                fila.setOrientation(LinearLayout.HORIZONTAL);
                fila.setGravity(Gravity.CENTER_VERTICAL);
                fila.setBackgroundResource(R.drawable.fondo_pildora_suave);
                fila.setPadding(16 * densidad, 11 * densidad, 16 * densidad, 11 * densidad);

                LinearLayout.LayoutParams paramsFila = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                paramsFila.bottomMargin = 8 * densidad;
                fila.setLayoutParams(paramsFila);

                TextView txtPalabra = new TextView(this);
                txtPalabra.setText(palabra.getTexto());
                txtPalabra.setTextColor(ContextCompat.getColor(this, R.color.texto_primario));
                txtPalabra.setTextSize(15f);
                txtPalabra.setTypeface(null, android.graphics.Typeface.BOLD);
                txtPalabra.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                TextView txtPts = new TextView(this);
                txtPts.setText(getString(R.string.palabra_valida_puntos, palabra.getPuntos()));
                txtPts.setTextColor(ContextCompat.getColor(this, R.color.marca_primario));
                txtPts.setTextSize(13f);
                txtPts.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);

                fila.addView(txtPalabra);
                fila.addView(txtPts);
                contenedor.addView(fila);
            }
        }

        AlertDialog dialogo = crearDialogoTransparente(vista, true);
        btnCerrar.setOnClickListener(v -> dialogo.dismiss());
        mostrarDialogoUnico(dialogo);
    }

    private void mostrarDialogoPausa() {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialogo_pausa, null);

        MaterialSwitch swMusica = vista.findViewById(R.id.switch_pausa_musica);
        MaterialSwitch swSonido = vista.findViewById(R.id.switch_pausa_sonido);
        MaterialSwitch swVibracion = vista.findViewById(R.id.switch_pausa_vibracion);
        View filaTabla = vista.findViewById(R.id.fila_pausa_tabla);
        TextView txtDetalleTabla = vista.findViewById(R.id.txt_pausa_tabla_detalle);
        ImageView icoChevron = vista.findViewById(R.id.ico_pausa_chevron);
        View btnInicio = vista.findViewById(R.id.btn_pausa_inicio);
        View btnCerrar = vista.findViewById(R.id.btn_cerrar_pausa);

        swMusica.setChecked(almacenamiento.estaMusicaActiva());
        swSonido.setChecked(almacenamiento.estaSonidoActivo());
        swVibracion.setChecked(almacenamiento.estaVibracionActiva());

        swMusica.setOnCheckedChangeListener((b, activo) -> {
            almacenamiento.setMusicaActiva(activo);
            if (activo) {
                feedback.iniciarMusicaAmbiente();
            } else {
                feedback.detenerMusicaAmbiente();
            }
        });
        swSonido.setOnCheckedChangeListener((b, activo) -> almacenamiento.setSonidoActivo(activo));
        swVibracion.setOnCheckedChangeListener((b, activo) -> almacenamiento.setVibracionActiva(activo));

        filaTabla.setOnClickListener(v -> {
            boolean mostrar = txtDetalleTabla.getVisibility() != View.VISIBLE;
            txtDetalleTabla.setVisibility(mostrar ? View.VISIBLE : View.GONE);
            icoChevron.animate().rotation(mostrar ? 180f : 0f).setDuration(180).start();
        });

        AlertDialog dialogo = crearDialogoTransparente(vista, true);
        btnInicio.setOnClickListener(v -> {
            guardarProgresoActual();
            dialogo.dismiss();
            finish();
        });
        btnCerrar.setOnClickListener(v -> dialogo.dismiss());
        mostrarDialogoUnico(dialogo);
    }

    private void mostrarDialogoVictoria(boolean conquistoPiso, int monedasGanadas) {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialogo_victoria, null);

        TextView txtTitulo = vista.findViewById(R.id.txt_titulo_victoria);
        TextView txtResumen = vista.findViewById(R.id.txt_resumen_victoria);
        View bloquePiso = vista.findViewById(R.id.bloque_piso_desbloqueado);
        ImageView imgPortada = vista.findViewById(R.id.img_portada_victoria);
        TextView txtObra = vista.findViewById(R.id.txt_obra_victoria);
        TextView btnSiguiente = vista.findViewById(R.id.btn_siguiente_nivel);
        TextView btnVerHistoria = vista.findViewById(R.id.btn_ver_historia_victoria);
        TextView btnVolverInicio = vista.findViewById(R.id.btn_volver_inicio_victoria);

        PisoInfo piso = CatalogoPisos.obtenerPorNivel(numeroNivel);
        boolean esIngles = almacenamiento.esIngles();

        if (conquistoPiso) {
            txtTitulo.setText(getString(R.string.piso_conquistado, piso.getNumero()));
            bloquePiso.setVisibility(View.VISIBLE);
            GestorPortadas.aplicarPortada(imgPortada, piso, false, esIngles);
            txtObra.setText(piso.getTitulo(esIngles));
            btnVerHistoria.setVisibility(View.VISIBLE);
        } else {
            txtTitulo.setText(getString(R.string.nivel_superado, numeroNivel));
            bloquePiso.setVisibility(View.GONE);
            btnVerHistoria.setVisibility(View.GONE);
        }

        txtResumen.setText(getString(
                R.string.resumen_victoria,
                puntosNivel,
                palabrasEncontradas.Cantidad(),
                monedasGanadas
        ));

        AlertDialog dialogo = crearDialogoTransparente(vista, false);

        if (numeroNivel < CatalogoPisos.TOTAL_NIVELES) {
            btnSiguiente.setText(getString(R.string.siguiente_nivel, numeroNivel + 1));
            btnSiguiente.setOnClickListener(v -> {
                dialogo.dismiss();
                cargarNivel(numeroNivel + 1);
            });
        } else {
            btnSiguiente.setText(R.string.estado_todos_completados);
            btnSiguiente.setOnClickListener(v -> {
                dialogo.dismiss();
                finish();
            });
        }

        btnVerHistoria.setOnClickListener(v -> {
            dialogo.dismiss();
            Intent intent = new Intent(this, ActividadDetallePiso.class);
            intent.putExtra(ActividadDetallePiso.EXTRA_NUMERO_PISO, piso.getNumero());
            startActivity(intent);
            finish();
        });

        btnVolverInicio.setOnClickListener(v -> {
            dialogo.dismiss();
            finish();
        });

        mostrarDialogoUnico(dialogo);
    }

    private AlertDialog crearDialogoTransparente(View vista, boolean cancelable) {
        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setView(vista)
                .setCancelable(cancelable)
                .create();
        if (dialogo.getWindow() != null) {
            dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialogo;
    }

    private void mostrarDialogoUnico(AlertDialog nuevo) {
        cerrarDialogoActivo();
        dialogoActivo = nuevo;
        dialogoActivo.setOnDismissListener(d -> {
            if (dialogoActivo == d) {
                dialogoActivo = null;
            }
        });
        dialogoActivo.show();
    }

    private void cerrarDialogoActivo() {
        if (dialogoActivo != null) {
            try {
                if (dialogoActivo.isShowing()) {
                    dialogoActivo.dismiss();
                }
            } catch (Throwable ignored) {
            }
            dialogoActivo = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        feedback.iniciarMusicaAmbiente();
    }

    @Override
    protected void onPause() {
        super.onPause();
        guardarProgresoActual();
        feedback.detenerMusicaAmbiente();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tokenCarga.incrementAndGet();
        cancelarSacudidaPreview();
        if (animadorBarra != null) {
            animadorBarra.cancel();
            animadorBarra = null;
        }
        cerrarDialogoActivo();
        if (vistaTablero != null) {
            vistaTablero.setEscuchaTablero(null);
        }
        ejecutor.shutdownNow();
    }
}
