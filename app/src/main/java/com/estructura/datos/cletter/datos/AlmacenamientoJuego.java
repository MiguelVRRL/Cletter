package com.estructura.datos.cletter.datos;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.estructura.datos.cletter.R;
import com.estructura.datos.cletter.estructuras.cola.ColaCircular;
import com.estructura.datos.cletter.estructuras.lista.ListaSimple;
import com.estructura.datos.cletter.modelo.Logro;
import com.estructura.datos.cletter.modelo.PalabraEncontrada;
import com.estructura.datos.cletter.modelo.PerfilJugador;
import com.estructura.datos.cletter.modelo.ProgresoNivelGuardado;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Administra la persistencia local de progreso, estadísticas del perfil,
 * configuración y logros en SharedPreferences.
 */
public class AlmacenamientoJuego {

    private static final String PREFS = "cletter_preferencias";

    private static final String CLAVE_NOMBRE = "jugador_nombre";
    private static final String CLAVE_AVATAR = "jugador_avatar";
    private static final String CLAVE_FECHA_REGISTRO = "jugador_fecha_registro";
    private static final String CLAVE_MONEDAS = "jugador_monedas";
    private static final String CLAVE_NIVEL_ACTUAL = "jugador_nivel_actual";
    private static final String CLAVE_NIVELES_COMPLETADOS = "jugador_niveles_completados";

    private static final String CLAVE_MAS_NIVELES_DIA = "stat_mas_niveles_dia";
    private static final String CLAVE_HOY_FECHA = "stat_hoy_fecha";
    private static final String CLAVE_HOY_NIVELES = "stat_hoy_niveles";
    private static final String CLAVE_DIAS_JUGADOS = "stat_dias_jugados";
    private static final String CLAVE_TOTAL_PALABRAS = "stat_total_palabras";
    private static final String CLAVE_PALABRA_MAS_LARGA = "stat_palabra_larga";
    private static final String CLAVE_MEJOR_PALABRA = "stat_mejor_palabra";
    private static final String CLAVE_PUNTOS_TOTALES = "stat_puntos_totales";
    private static final String CLAVE_LOGROS = "stat_logros_ids";

    private static final String CLAVE_MUSICA = "cfg_musica";
    private static final String CLAVE_SONIDO = "cfg_sonido";
    private static final String CLAVE_VIBRACION = "cfg_vibracion";
    private static final String CLAVE_IDIOMA = "cfg_idioma"; // "es" o "en"
    private static final String CLAVE_TEMA = "cfg_tema";     // 0=sistema, 1=claro, 2=oscuro

    // Progreso parcial del nivel en curso (se conserva al salir a Inicio)
    private static final String CLAVE_PARTIDA_NIVEL = "partida_nivel";
    private static final String CLAVE_PARTIDA_IDIOMA = "partida_idioma";
    private static final String CLAVE_PARTIDA_LETRAS = "partida_letras";
    private static final String CLAVE_PARTIDA_PALABRAS = "partida_palabras";

    public static final int TEMA_SISTEMA = 0;
    public static final int TEMA_CLARO = 1;
    public static final int TEMA_OSCURO = 2;

    private static volatile String ultimoDiaRegistrado = null;

    private final Context contexto;
    private final SharedPreferences prefs;

    public AlmacenamientoJuego(Context contexto) {
        this.contexto = contexto.getApplicationContext();
        this.prefs = this.contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        asegurarInicializacion();
    }

    private void asegurarInicializacion() {
        if (!prefs.contains(CLAVE_FECHA_REGISTRO)) {
            String hoyTexto = formatearFechaLegible(new Date());
            String hoyClave = formatearClaveDia(new Date());
            Set<String> dias = new HashSet<>();
            dias.add(hoyClave);

            prefs.edit()
                    .putString(CLAVE_NOMBRE, contexto.getString(R.string.nombre_jugador_defecto))
                    .putInt(CLAVE_AVATAR, 0)
                    .putString(CLAVE_FECHA_REGISTRO, hoyTexto)
                    .putInt(CLAVE_MONEDAS, 120)
                    .putInt(CLAVE_NIVEL_ACTUAL, 1)
                    .putInt(CLAVE_NIVELES_COMPLETADOS, 0)
                    .putStringSet(CLAVE_DIAS_JUGADOS, dias)
                    .putBoolean(CLAVE_MUSICA, true)
                    .putBoolean(CLAVE_SONIDO, true)
                    .putBoolean(CLAVE_VIBRACION, true)
                    .putString(CLAVE_IDIOMA, Locale.getDefault().getLanguage().equals("en") ? "en" : "es")
                    .putInt(CLAVE_TEMA, TEMA_CLARO)
                    .apply();
        } else {
            registrarActividadHoy();
        }
    }

    private void registrarActividadHoy() {
        String hoyClave = formatearClaveDia(new Date());
        if (hoyClave.equals(ultimoDiaRegistrado)) {
            return;
        }
        ultimoDiaRegistrado = hoyClave;
        Set<String> dias = new HashSet<>(prefs.getStringSet(CLAVE_DIAS_JUGADOS, new HashSet<>()));
        if (!dias.contains(hoyClave)) {
            dias.add(hoyClave);
            prefs.edit().putStringSet(CLAVE_DIAS_JUGADOS, dias).apply();
        }
    }

    public PerfilJugador obtenerPerfil() {
        int nivelesCompletados = prefs.getInt(CLAVE_NIVELES_COMPLETADOS, 0);
        int pisosConquistados = Math.min(CatalogoPisos.TOTAL_PISOS, nivelesCompletados / CatalogoPisos.NIVELES_POR_PISO);
        Set<String> dias = prefs.getStringSet(CLAVE_DIAS_JUGADOS, new HashSet<>());
        Set<String> logros = prefs.getStringSet(CLAVE_LOGROS, new HashSet<>());

        return new PerfilJugador(
                prefs.getString(CLAVE_NOMBRE, contexto.getString(R.string.nombre_jugador_defecto)),
                prefs.getInt(CLAVE_AVATAR, 0),
                prefs.getString(CLAVE_FECHA_REGISTRO, formatearFechaLegible(new Date())),
                prefs.getInt(CLAVE_MONEDAS, 120),
                prefs.getInt(CLAVE_NIVEL_ACTUAL, 1),
                pisosConquistados,
                prefs.getInt(CLAVE_MAS_NIVELES_DIA, 0),
                Math.max(1, dias.size()),
                prefs.getInt(CLAVE_TOTAL_PALABRAS, 0),
                prefs.getInt(CLAVE_PALABRA_MAS_LARGA, 0),
                prefs.getInt(CLAVE_MEJOR_PALABRA, 0),
                prefs.getInt(CLAVE_PUNTOS_TOTALES, 0),
                logros.size()
        );
    }

    public int obtenerNivelesCompletados() {
        return prefs.getInt(CLAVE_NIVELES_COMPLETADOS, 0);
    }

    public int obtenerNivelActual() {
        return prefs.getInt(CLAVE_NIVEL_ACTUAL, 1);
    }

    public int obtenerMonedas() {
        return prefs.getInt(CLAVE_MONEDAS, 120);
    }

    public boolean gastarMonedas(int cantidad) {
        int actuales = obtenerMonedas();
        if (actuales < cantidad) {
            return false;
        }
        prefs.edit().putInt(CLAVE_MONEDAS, actuales - cantidad).apply();
        return true;
    }

    public void actualizarPerfil(String nuevoNombre, int indiceAvatar) {
        String nombreLimpio = (nuevoNombre == null || nuevoNombre.trim().isEmpty())
                ? contexto.getString(R.string.nombre_jugador_defecto)
                : nuevoNombre.trim();
        prefs.edit()
                .putString(CLAVE_NOMBRE, nombreLimpio)
                .putInt(CLAVE_AVATAR, indiceAvatar)
                .apply();
    }

    /**
     * Registra la superación de un nivel, actualiza estadísticas y devuelve en una
     * ColaCircular todos los logros recién desbloqueados.
     */
    public ColaCircular<Logro> registrarNivelCompletado(
            int numeroNivel,
            int puntosNivel,
            int palabrasEnNivel,
            int longitudMaximaNivel,
            int mejorPalabraNivel,
            int monedasGanadas
    ) {
        String hoyClave = formatearClaveDia(new Date());
        String ultimaFecha = prefs.getString(CLAVE_HOY_FECHA, "");
        int nivelesHoy = hoyClave.equals(ultimaFecha) ? prefs.getInt(CLAVE_HOY_NIVELES, 0) + 1 : 1;
        int mejorDia = Math.max(prefs.getInt(CLAVE_MAS_NIVELES_DIA, 0), nivelesHoy);

        Set<String> dias = new HashSet<>(prefs.getStringSet(CLAVE_DIAS_JUGADOS, new HashSet<>()));
        dias.add(hoyClave);

        int completadosPrevios = prefs.getInt(CLAVE_NIVELES_COMPLETADOS, 0);
        int nuevosCompletados = Math.max(completadosPrevios, numeroNivel);
        int siguienteNivel = Math.min(CatalogoPisos.TOTAL_NIVELES, Math.max(prefs.getInt(CLAVE_NIVEL_ACTUAL, 1), numeroNivel + 1));

        int totalPalabras = prefs.getInt(CLAVE_TOTAL_PALABRAS, 0) + palabrasEnNivel;
        int palabraLarga = Math.max(prefs.getInt(CLAVE_PALABRA_MAS_LARGA, 0), longitudMaximaNivel);
        int mejorPalabra = Math.max(prefs.getInt(CLAVE_MEJOR_PALABRA, 0), mejorPalabraNivel);
        int puntosTotales = prefs.getInt(CLAVE_PUNTOS_TOTALES, 0) + puntosNivel;
        int nuevasMonedas = obtenerMonedas() + monedasGanadas;

        prefs.edit()
                .putInt(CLAVE_NIVELES_COMPLETADOS, nuevosCompletados)
                .putInt(CLAVE_NIVEL_ACTUAL, siguienteNivel)
                .putInt(CLAVE_MONEDAS, nuevasMonedas)
                .putString(CLAVE_HOY_FECHA, hoyClave)
                .putInt(CLAVE_HOY_NIVELES, nivelesHoy)
                .putInt(CLAVE_MAS_NIVELES_DIA, mejorDia)
                .putStringSet(CLAVE_DIAS_JUGADOS, dias)
                .putInt(CLAVE_TOTAL_PALABRAS, totalPalabras)
                .putInt(CLAVE_PALABRA_MAS_LARGA, palabraLarga)
                .putInt(CLAVE_MEJOR_PALABRA, mejorPalabra)
                .putInt(CLAVE_PUNTOS_TOTALES, puntosTotales)
                .apply();

        // Evaluar nuevos logros y encolarlos en ColaCircular<Logro>
        Set<String> logrosActuales = new HashSet<>(prefs.getStringSet(CLAVE_LOGROS, new HashSet<>()));
        ListaSimple<String> nuevosIds = CatalogoLogros.evaluarNuevosLogros(obtenerPerfil(), logrosActuales);
        ColaCircular<Logro> colaNuevosLogros = new ColaCircular<>();

        if (!nuevosIds.EsVacia()) {
            for (int i = 1; i <= nuevosIds.Cantidad(); i++) {
                String id = nuevosIds.Obtener(i);
                if (id != null) {
                    logrosActuales.add(id);
                    Logro logro = CatalogoLogros.buscarPorId(id, true);
                    if (logro != null) {
                        colaNuevosLogros.encolar(logro);
                    }
                }
            }
            prefs.edit().putStringSet(CLAVE_LOGROS, logrosActuales).apply();
        }
        return colaNuevosLogros;
    }

    public ListaSimple<Logro> obtenerTodosLosLogros() {
        Set<String> logros = prefs.getStringSet(CLAVE_LOGROS, new HashSet<>());
        return CatalogoLogros.construirLista(logros);
    }

    public boolean estaMusicaActiva() {
        return prefs.getBoolean(CLAVE_MUSICA, true);
    }

    public void setMusicaActiva(boolean activa) {
        prefs.edit().putBoolean(CLAVE_MUSICA, activa).apply();
    }

    public boolean estaSonidoActivo() {
        return prefs.getBoolean(CLAVE_SONIDO, true);
    }

    public void setSonidoActivo(boolean activo) {
        prefs.edit().putBoolean(CLAVE_SONIDO, activo).apply();
    }

    public boolean estaVibracionActiva() {
        return prefs.getBoolean(CLAVE_VIBRACION, true);
    }

    public void setVibracionActiva(boolean activa) {
        prefs.edit().putBoolean(CLAVE_VIBRACION, activa).apply();
    }

    public String obtenerIdioma() {
        return prefs.getString(CLAVE_IDIOMA, "es");
    }

    public boolean esIngles() {
        return "en".equals(obtenerIdioma());
    }

    public void guardarProgresoNivelEnCurso(
            int numeroNivel,
            String idioma,
            String letrasTablero,
            ListaSimple<PalabraEncontrada> palabras
    ) {
        if (letrasTablero == null || letrasTablero.length() != GeneradorTablero.TOTAL_CELDAS) {
            return;
        }
        StringBuilder serializado = new StringBuilder();
        if (palabras != null) {
            for (int i = 1; i <= palabras.Cantidad(); i++) {
                PalabraEncontrada palabra = palabras.Obtener(i);
                if (palabra != null) {
                    if (serializado.length() > 0) {
                        serializado.append('|');
                    }
                    serializado.append(palabra.getTexto())
                            .append(':')
                            .append(palabra.getPuntos());
                }
            }
        }
        prefs.edit()
                .putInt(CLAVE_PARTIDA_NIVEL, numeroNivel)
                .putString(CLAVE_PARTIDA_IDIOMA, idioma)
                .putString(CLAVE_PARTIDA_LETRAS, letrasTablero)
                .putString(CLAVE_PARTIDA_PALABRAS, serializado.toString())
                .apply();
    }

    public ProgresoNivelGuardado obtenerProgresoNivelEnCurso(int numeroNivel, String idioma) {
        int nivelGuardado = prefs.getInt(CLAVE_PARTIDA_NIVEL, -1);
        if (nivelGuardado != numeroNivel) {
            return null;
        }
        String idiomaGuardado = prefs.getString(CLAVE_PARTIDA_IDIOMA, "");
        if (!idioma.equals(idiomaGuardado)) {
            return null;
        }
        String letras = prefs.getString(CLAVE_PARTIDA_LETRAS, "");
        if (letras.length() != GeneradorTablero.TOTAL_CELDAS) {
            return null;
        }

        ListaSimple<PalabraEncontrada> lista = new ListaSimple<>();
        String palabrasCrud = prefs.getString(CLAVE_PARTIDA_PALABRAS, "");
        if (!palabrasCrud.isEmpty()) {
            String[] entradas = palabrasCrud.split("\\|");
            for (String entrada : entradas) {
                int sep = entrada.indexOf(':');
                if (sep > 0 && sep < entrada.length() - 1) {
                    String texto = entrada.substring(0, sep);
                    try {
                        int puntos = Integer.parseInt(entrada.substring(sep + 1));
                        PalabraEncontrada item = new PalabraEncontrada(texto, puntos);
                        if (!lista.Contiene(item)) {
                            lista.InsertarAlFinal(item);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        return new ProgresoNivelGuardado(nivelGuardado, idiomaGuardado, letras, lista);
    }

    public void limpiarProgresoNivelEnCurso() {
        prefs.edit()
                .remove(CLAVE_PARTIDA_NIVEL)
                .remove(CLAVE_PARTIDA_IDIOMA)
                .remove(CLAVE_PARTIDA_LETRAS)
                .remove(CLAVE_PARTIDA_PALABRAS)
                .apply();
    }

    public void guardarYAplicarIdioma(String codigoIdioma) {
        String seguro = "en".equals(codigoIdioma) ? "en" : "es";
        prefs.edit().putString(CLAVE_IDIOMA, seguro).apply();
        limpiarProgresoNivelEnCurso();
        aplicarIdiomaGuardado();
    }

    public void aplicarIdiomaGuardado() {
        String idioma = obtenerIdioma();
        LocaleListCompat actuales = AppCompatDelegate.getApplicationLocales();
        if (!actuales.isEmpty() && idioma.equals(actuales.toLanguageTags())) {
            return;
        }
        if (actuales.isEmpty() && idioma.equals(Locale.getDefault().getLanguage())) {
            return;
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(idioma));
    }

    public int obtenerTema() {
        return prefs.getInt(CLAVE_TEMA, TEMA_CLARO);
    }

    public void guardarYAplicarTema(int tema) {
        prefs.edit().putInt(CLAVE_TEMA, tema).apply();
        aplicarTemaGuardado();
    }

    public void aplicarTemaGuardado() {
        int tema = obtenerTema();
        int modoObjetivo;
        if (tema == TEMA_CLARO) {
            modoObjetivo = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (tema == TEMA_OSCURO) {
            modoObjetivo = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            modoObjetivo = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        if (AppCompatDelegate.getDefaultNightMode() != modoObjetivo) {
            AppCompatDelegate.setDefaultNightMode(modoObjetivo);
        }
    }

    public void reiniciarProgreso() {
        String nombre = prefs.getString(CLAVE_NOMBRE, contexto.getString(R.string.nombre_jugador_defecto));
        int avatar = prefs.getInt(CLAVE_AVATAR, 0);
        boolean musica = estaMusicaActiva();
        boolean sonido = estaSonidoActivo();
        boolean vibracion = estaVibracionActiva();
        String idioma = obtenerIdioma();
        int tema = obtenerTema();

        String hoyTexto = formatearFechaLegible(new Date());
        String hoyClave = formatearClaveDia(new Date());
        Set<String> dias = new HashSet<>();
        dias.add(hoyClave);

        prefs.edit().clear()
                .putString(CLAVE_NOMBRE, nombre)
                .putInt(CLAVE_AVATAR, avatar)
                .putString(CLAVE_FECHA_REGISTRO, hoyTexto)
                .putInt(CLAVE_MONEDAS, 120)
                .putInt(CLAVE_NIVEL_ACTUAL, 1)
                .putInt(CLAVE_NIVELES_COMPLETADOS, 0)
                .putStringSet(CLAVE_DIAS_JUGADOS, dias)
                .putBoolean(CLAVE_MUSICA, musica)
                .putBoolean(CLAVE_SONIDO, sonido)
                .putBoolean(CLAVE_VIBRACION, vibracion)
                .putString(CLAVE_IDIOMA, idioma)
                .putInt(CLAVE_TEMA, tema)
                .apply();
    }

    private String formatearFechaLegible(Date fecha) {
        Locale locale = esIngles() ? Locale.ENGLISH : new Locale("es", "ES");
        return new SimpleDateFormat("d MMM yyyy", locale).format(fecha);
    }

    private String formatearClaveDia(Date fecha) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(fecha);
    }
}
