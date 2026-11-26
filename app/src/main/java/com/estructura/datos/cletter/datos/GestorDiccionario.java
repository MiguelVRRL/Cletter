package com.estructura.datos.cletter.datos;

import android.content.Context;

import com.estructura.datos.cletter.estructuras.arbol.ArbolBinario;
import com.estructura.datos.cletter.modelo.PisoInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Carga y mantiene el diccionario del idioma activo (Español o Inglés) dentro
 * de un ArbolBinario de búsqueda balanceado (O(log N)), incluyendo el vocabulario
 * curado de Videojuegos y Ciencias de la Computación de los 20 pisos.
 */
public final class GestorDiccionario {

    private static GestorDiccionario instancia;

    private final ArbolBinario<String> arbolEspanol = new ArbolBinario<>();
    private final ArbolBinario<String> arbolIngles = new ArbolBinario<>();
    private volatile boolean cargadoEspanol = false;
    private volatile boolean cargadoIngles = false;

    private GestorDiccionario() {
    }

    public static synchronized GestorDiccionario obtenerInstancia() {
        if (instancia == null) {
            instancia = new GestorDiccionario();
        }
        return instancia;
    }

    public ArbolBinario<String> obtenerArbol(Context contexto, boolean esIngles) {
        if (esIngles) {
            if (!cargadoIngles) {
                synchronized (arbolIngles) {
                    if (!cargadoIngles) {
                        cargarDesdeAsset(contexto, "diccionario_en.txt", arbolIngles, true);
                        cargadoIngles = true;
                    }
                }
            }
            return arbolIngles;
        } else {
            if (!cargadoEspanol) {
                synchronized (arbolEspanol) {
                    if (!cargadoEspanol) {
                        cargarDesdeAsset(contexto, "diccionario_es.txt", arbolEspanol, false);
                        cargadoEspanol = true;
                    }
                }
            }
            return arbolEspanol;
        }
    }

    /**
     * Precarga el diccionario del idioma activo en un hilo de baja prioridad
     * para que al abrir una partida el tablero esté listo al instante.
     */
    public void precargarAsync(Context contexto, boolean esIngles) {
        if ((esIngles && cargadoIngles) || (!esIngles && cargadoEspanol)) {
            return;
        }
        final Context appCtx = contexto.getApplicationContext();
        Thread hilo = new Thread(() -> {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            obtenerArbol(appCtx, esIngles);
        }, "Cletter-PrecargaDic");
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * Construye un ArbolBinario balanceado a partir de una colección directa de palabras
     * (útil para pruebas unitarias y cargas sin contexto Android).
     */
    public static ArbolBinario<String> construirArbolDesdePalabras(String[] palabrasCrudas, boolean esIngles) {
        Set<String> conjunto = new HashSet<>(palabrasCrudas.length + 200);
        for (String palabra : palabrasCrudas) {
            String limpia = normalizarPalabra(palabra);
            if (limpia.length() >= 3 && limpia.length() <= 12) {
                conjunto.add(limpia);
            }
        }
        agregarVocabularioPisos(conjunto, esIngles);
        String[] ordenadas = conjunto.toArray(new String[0]);
        Arrays.sort(ordenadas);
        ArbolBinario<String> arbol = new ArbolBinario<>();
        arbol.InsertarBalanceado(ordenadas);
        return arbol;
    }

    private void cargarDesdeAsset(Context contexto, String archivo, ArbolBinario<String> destino, boolean esIngles) {
        int capacidadInicial = esIngles ? 16384 : 8192;
        Set<String> palabras = new HashSet<>(capacidadInicial);
        try (InputStream entrada = contexto.getAssets().open(archivo);
             BufferedReader lector = new BufferedReader(
                     new InputStreamReader(entrada, StandardCharsets.UTF_8), 16384)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String limpia = normalizarPalabra(linea);
                if (limpia.length() >= 3 && limpia.length() <= 12) {
                    palabras.add(limpia);
                }
            }
        } catch (IOException ignored) {
            // Si falta el asset se respalda con el léxico temático de los pisos.
        }
        agregarVocabularioPisos(palabras, esIngles);

        String[] ordenadas = palabras.toArray(new String[0]);
        Arrays.sort(ordenadas);
        destino.Limpiar();
        destino.InsertarBalanceado(ordenadas);
    }

    private static void agregarVocabularioPisos(Set<String> destino, boolean esIngles) {
        for (PisoInfo piso : CatalogoPisos.obtenerTodos()) {
            for (String clave : piso.getPalabrasClave(esIngles)) {
                String limpia = normalizarPalabra(clave);
                if (limpia.length() >= 3) {
                    destino.add(limpia);
                }
            }
        }
    }

    /**
     * Normaliza tildes (conservando la Ñ en español) y convierte a mayúsculas puras.
     * Ruta rápida O(N) sin ICU Normalizer para ASCII puro y vocales acentuadas comunes.
     */
    public static String normalizarPalabra(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        int largo = texto.length();
        // Comprobación ultrarrápida: si ya es A..Z o Ñ puro, devolver directamente sin copiar
        boolean yaLimpia = true;
        for (int i = 0; i < largo; i++) {
            char c = texto.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || c == 'Ñ')) {
                yaLimpia = false;
                break;
            }
        }
        if (yaLimpia) {
            return texto;
        }

        StringBuilder resultado = new StringBuilder(largo);
        for (int i = 0; i < largo; i++) {
            char c = texto.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                resultado.append(c);
            } else if (c >= 'a' && c <= 'z') {
                resultado.append((char) (c - 32));
            } else if (c == 'Ñ' || c == 'ñ') {
                resultado.append('Ñ');
            } else if (c == 'Á' || c == 'á' || c == 'À' || c == 'à' || c == 'Ä' || c == 'ä') {
                resultado.append('A');
            } else if (c == 'É' || c == 'é' || c == 'È' || c == 'è' || c == 'Ë' || c == 'ë') {
                resultado.append('E');
            } else if (c == 'Í' || c == 'í' || c == 'Ì' || c == 'ì' || c == 'Ï' || c == 'ï') {
                resultado.append('I');
            } else if (c == 'Ó' || c == 'ó' || c == 'Ò' || c == 'ò' || c == 'Ö' || c == 'ö') {
                resultado.append('O');
            } else if (c == 'Ú' || c == 'ú' || c == 'Ù' || c == 'ù' || c == 'Ü' || c == 'ü') {
                resultado.append('U');
            } else if (c > 127) {
                String nfd = Normalizer.normalize(String.valueOf(Character.toUpperCase(c)), Normalizer.Form.NFD);
                for (int j = 0; j < nfd.length(); j++) {
                    char base = nfd.charAt(j);
                    if (base >= 'A' && base <= 'Z') {
                        resultado.append(base);
                    }
                }
            }
        }
        return resultado.toString();
    }
}
