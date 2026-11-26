package com.estructura.datos.cletter.datos;

import com.estructura.datos.cletter.estructuras.arbol.ArbolBinario;
import com.estructura.datos.cletter.estructuras.cola.ColaCircular;
import com.estructura.datos.cletter.estructuras.grafo.Grafo;
import com.estructura.datos.cletter.estructuras.lista.ListaSimple;
import com.estructura.datos.cletter.modelo.CeldaHexagonal;
import com.estructura.datos.cletter.modelo.PisoInfo;
import com.estructura.datos.cletter.modelo.PistaTablero;
import com.estructura.datos.cletter.modelo.TableroNivel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Construye el panal hexagonal fijo de 18 celdas (columnas 4-3-4-3-4),
 * arma su Grafo de adyacencias, siembra palabras temáticas del piso,
 * verifica con DFS + ArbolBinario que el nivel sea resoluble y carga
 * la ColaCircular de pistas.
 */
public final class GeneradorTablero {

    public static final int TOTAL_CELDAS = 18;
    // Alturas de cada columna del panal 4-3-4-3-4
    public static final int[] FILAS_POR_COLUMNA = new int[]{4, 3, 4, 3, 4};

    private static final int MAX_PROFUNDIDAD_SOLVER = 7;
    private static final int MAX_PALABRAS_SOLVER = 70;
    private static final int MAX_INTENTOS_GENERACION = 6;

    // Bolsas ponderadas de letras por idioma para rellenar huecos naturales
    private static final char[] BOLSA_ES = (
            "AAAAAAAAAAAAEEEEEEEEEEEEIIIIIIOOOOOOOOUUUU" +
            "SSSSSSNNNNNNRRRRRRLLLLLLTTTTTT" +
            "CCCCDDDDMMMMPPPPBBGG" +
            "FFHHVVYJQXZ"
    ).toCharArray();

    private static final char[] BOLSA_EN = (
            "AAAAAAAAAEEEEEEEEEEEEIIIIIIIIOOOOOOOOUUUU" +
            "SSSSSSNNNNNNRRRRRRLLLLLLTTTTTTT" +
            "CCCCDDDDMMMMPPPPBBGGHH" +
            "FFWWVVYYKJXZQ"
    ).toCharArray();

    private GeneradorTablero() {
    }

    /**
     * Crea la estructura base de las 18 celdas hexagonales y conecta todas sus
     * adyacencias (hasta 6 vecinos por celda) dentro de un Grafo.
     */
    public static Grafo construirGrafoHexagonal(CeldaHexagonal[] celdasSalida) {
        Grafo grafo = new Grafo();
        int[][] mapaIndice = new int[5][4];
        for (int[] fila : mapaIndice) {
            Arrays.fill(fila, -1);
        }

        int idx = 0;
        for (int col = 0; col < FILAS_POR_COLUMNA.length; col++) {
            for (int fila = 0; fila < FILAS_POR_COLUMNA[col]; fila++) {
                CeldaHexagonal celda = new CeldaHexagonal(idx, col, fila, 'A');
                celdasSalida[idx] = celda;
                mapaIndice[col][fila] = idx;
                grafo.NuevoVertice(celda.getIdVertice());
                idx++;
            }
        }

        // Conectar vecinos en malla hexagonal tipo "flat-top" con columnas intercaladas:
        // Columnas pares (0, 2, 4) tienen 4 filas (0..3).
        // Columnas impares (1, 3) tienen 3 filas (0..2), desplazadas +0.5 hacia abajo.
        for (int col = 0; col < FILAS_POR_COLUMNA.length; col++) {
            for (int fila = 0; fila < FILAS_POR_COLUMNA[col]; fila++) {
                int origen = mapaIndice[col][fila];
                if (fila + 1 < FILAS_POR_COLUMNA[col]) {
                    conectar(grafo, celdasSalida, origen, mapaIndice[col][fila + 1]);
                }
                if (col + 1 < FILAS_POR_COLUMNA.length) {
                    if (col % 2 == 0) {
                        if (fila - 1 >= 0 && fila - 1 < FILAS_POR_COLUMNA[col + 1]) {
                            conectar(grafo, celdasSalida, origen, mapaIndice[col + 1][fila - 1]);
                        }
                        if (fila < FILAS_POR_COLUMNA[col + 1]) {
                            conectar(grafo, celdasSalida, origen, mapaIndice[col + 1][fila]);
                        }
                    } else {
                        if (fila < FILAS_POR_COLUMNA[col + 1]) {
                            conectar(grafo, celdasSalida, origen, mapaIndice[col + 1][fila]);
                        }
                        if (fila + 1 < FILAS_POR_COLUMNA[col + 1]) {
                            conectar(grafo, celdasSalida, origen, mapaIndice[col + 1][fila + 1]);
                        }
                    }
                }
            }
        }
        return grafo;
    }

    private static void conectar(Grafo grafo, CeldaHexagonal[] celdas, int a, int b) {
        if (a >= 0 && b >= 0 && a != b) {
            grafo.Union(celdas[a].getIdVertice(), celdas[b].getIdVertice(), 1);
        }
    }

    private static int[][] extraerTablaVecinos(Grafo grafo) {
        int[][] tabla = new int[TOTAL_CELDAS][];
        for (int i = 0; i < TOTAL_CELDAS; i++) {
            tabla[i] = grafo.obtenerIndicesVecinos(i);
        }
        return tabla;
    }

    /**
     * Genera un tablero determinista y garantizadamente resoluble para el nivel indicado.
     */
    public static TableroNivel generarNivel(
            int numeroNivel,
            boolean esIngles,
            ArbolBinario<String> diccionario,
            long semillaExtra
    ) {
        int nivelSeguro = Math.max(1, Math.min(CatalogoPisos.TOTAL_NIVELES, numeroNivel));
        PisoInfo piso = CatalogoPisos.obtenerPorNivel(nivelSeguro);
        int metaPuntos = CalculadoraPuntos.calcularMetaNivel(nivelSeguro);

        CeldaHexagonal[] mejorCeldas = null;
        Grafo mejorGrafo = null;
        Map<String, int[]> mejorSoluciones = null;
        int mejorPuntajeTotal = -1;

        String[] tematicas = piso.getPalabrasClave(esIngles);
        ListaSimple<String> poolBase = new ListaSimple<>();
        for (String palabra : tematicas) {
            String limpia = GestorDiccionario.normalizarPalabra(palabra);
            if (limpia.length() >= 3 && limpia.length() <= 7) {
                poolBase.InsertarAlFinal(limpia);
            }
        }

        char[] bolsa = esIngles ? BOLSA_EN : BOLSA_ES;
        char[] vocales = new char[]{'A', 'E', 'I', 'O', 'U'};

        for (int intento = 0; intento < MAX_INTENTOS_GENERACION; intento++) {
            long semilla = (nivelSeguro * 7919L) + (esIngles ? 104729L : 31337L)
                    + (intento * 997L) + semillaExtra;
            Random azar = new Random(semilla);

            CeldaHexagonal[] celdas = new CeldaHexagonal[TOTAL_CELDAS];
            Grafo grafo = construirGrafoHexagonal(celdas);
            int[][] vecinosTabla = extraerTablaVecinos(grafo);
            char[] letras = new char[TOTAL_CELDAS];
            Arrays.fill(letras, '\0');

            ListaSimple<String> pool = poolBase.Copiar();
            pool.Mezclar(azar);

            int sembradas = 0;
            for (int i = 1; i <= pool.Cantidad() && sembradas < 3; i++) {
                String palabra = pool.Obtener(i);
                if (sembrarPalabraEnGrafo(vecinosTabla, letras, palabra, azar)) {
                    sembradas++;
                }
            }

            int vocalesActuales = contarVocales(letras);
            for (int i = 0; i < TOTAL_CELDAS; i++) {
                if (letras[i] == '\0') {
                    if (vocalesActuales < 7) {
                        letras[i] = vocales[azar.nextInt(vocales.length)];
                        vocalesActuales++;
                    } else {
                        letras[i] = bolsa[azar.nextInt(bolsa.length)];
                        if (esVocal(letras[i])) {
                            vocalesActuales++;
                        }
                    }
                }
                celdas[i].setLetra(letras[i]);
            }

            Map<String, int[]> soluciones = resolverTableroConVecinos(celdas, vecinosTabla, diccionario);
            int puntajeDisponible = calcularPuntajeTotal(soluciones);

            if (puntajeDisponible > mejorPuntajeTotal) {
                mejorPuntajeTotal = puntajeDisponible;
                mejorCeldas = celdas;
                mejorGrafo = grafo;
                mejorSoluciones = soluciones;
            }

            if (puntajeDisponible >= (int) (metaPuntos * 1.8) && soluciones.size() >= 8) {
                break;
            }
        }

        int metaFinal = metaPuntos;
        if (mejorPuntajeTotal > 0 && mejorPuntajeTotal < metaFinal) {
            metaFinal = Math.max(12, (int) (mejorPuntajeTotal * 0.65));
        }

        ColaCircular<PistaTablero> colaPistas = construirColaPistas(mejorSoluciones);

        return new TableroNivel(
                nivelSeguro,
                piso.getNumero(),
                metaFinal,
                mejorCeldas,
                mejorGrafo,
                colaPistas,
                mejorSoluciones != null ? mejorSoluciones.size() : 0,
                Math.max(0, mejorPuntajeTotal)
        );
    }

    /**
     * Reordena las letras actuales del tablero manteniendo intacta la topología del Grafo,
     * recalculando las pistas válidas en ColaCircular.
     */
    public static TableroNivel mezclarTablero(
            TableroNivel actual,
            ArbolBinario<String> diccionario,
            long semilla
    ) {
        Random azar = new Random(semilla);
        CeldaHexagonal[] nuevasCeldas = new CeldaHexagonal[TOTAL_CELDAS];
        Grafo grafo = construirGrafoHexagonal(nuevasCeldas);
        int[][] vecinosTabla = extraerTablaVecinos(grafo);

        ListaSimple<Character> letras = new ListaSimple<>();
        for (CeldaHexagonal c : actual.getCeldas()) {
            letras.InsertarAlFinal(c.getLetra());
        }
        letras.Mezclar(azar);
        for (int i = 0; i < TOTAL_CELDAS; i++) {
            nuevasCeldas[i].setLetra(letras.Obtener(i + 1));
        }

        Map<String, int[]> soluciones = resolverTableroConVecinos(nuevasCeldas, vecinosTabla, diccionario);
        int puntajeTotal = calcularPuntajeTotal(soluciones);
        ColaCircular<PistaTablero> colaPistas = construirColaPistas(soluciones);

        return new TableroNivel(
                actual.getNumeroNivel(),
                actual.getNumeroPiso(),
                actual.getPuntajeMeta(),
                nuevasCeldas,
                grafo,
                colaPistas,
                soluciones.size(),
                puntajeTotal
        );
    }

    /**
     * Reconstruye exactamente un tablero guardado a partir de sus 18 letras,
     * recreando su Grafo de adyacencias y su ColaCircular de pistas.
     */
    public static TableroNivel restaurarTableroDesdeLetras(
            int numeroNivel,
            String letras18,
            ArbolBinario<String> diccionario
    ) {
        int nivelSeguro = Math.max(1, Math.min(CatalogoPisos.TOTAL_NIVELES, numeroNivel));
        PisoInfo piso = CatalogoPisos.obtenerPorNivel(nivelSeguro);
        int metaPuntos = CalculadoraPuntos.calcularMetaNivel(nivelSeguro);

        CeldaHexagonal[] celdas = new CeldaHexagonal[TOTAL_CELDAS];
        Grafo grafo = construirGrafoHexagonal(celdas);
        int[][] vecinosTabla = extraerTablaVecinos(grafo);

        for (int i = 0; i < TOTAL_CELDAS; i++) {
            celdas[i].setLetra(letras18.charAt(i));
        }

        Map<String, int[]> soluciones = resolverTableroConVecinos(celdas, vecinosTabla, diccionario);
        int puntajeTotal = calcularPuntajeTotal(soluciones);
        int metaFinal = metaPuntos;
        if (puntajeTotal > 0 && puntajeTotal < metaFinal) {
            metaFinal = Math.max(12, (int) (puntajeTotal * 0.65));
        }
        ColaCircular<PistaTablero> colaPistas = construirColaPistas(soluciones);

        return new TableroNivel(
                nivelSeguro,
                piso.getNumero(),
                metaFinal,
                celdas,
                grafo,
                colaPistas,
                soluciones.size(),
                Math.max(0, puntajeTotal)
        );
    }

    /**
     * Serializa las 18 letras actuales del tablero para guardar la partida en curso.
     */
    public static String serializarLetras(TableroNivel tablero) {
        if (tablero == null || tablero.getCeldas() == null || tablero.getCeldas().length != TOTAL_CELDAS) {
            return "";
        }
        char[] letras = new char[TOTAL_CELDAS];
        CeldaHexagonal[] celdas = tablero.getCeldas();
        for (int i = 0; i < TOTAL_CELDAS; i++) {
            letras[i] = celdas[i].getLetra();
        }
        return new String(letras);
    }

    private static boolean sembrarPalabraEnGrafo(int[][] vecinosTabla, char[] letras, String palabra, Random azar) {
        int[] inicios = new int[TOTAL_CELDAS];
        for (int i = 0; i < TOTAL_CELDAS; i++) {
            inicios[i] = i;
        }
        for (int i = TOTAL_CELDAS - 1; i > 0; i--) {
            int j = azar.nextInt(i + 1);
            int tmp = inicios[i];
            inicios[i] = inicios[j];
            inicios[j] = tmp;
        }

        boolean[] visitado = new boolean[TOTAL_CELDAS];
        int[] camino = new int[palabra.length()];

        for (int inicio : inicios) {
            if (letras[inicio] != '\0' && letras[inicio] != palabra.charAt(0)) {
                continue;
            }
            Arrays.fill(visitado, false);
            if (buscarCaminoSiembra(vecinosTabla, letras, palabra, 0, inicio, visitado, camino, azar)) {
                for (int k = 0; k < palabra.length(); k++) {
                    letras[camino[k]] = palabra.charAt(k);
                }
                return true;
            }
        }
        return false;
    }

    private static boolean buscarCaminoSiembra(
            int[][] vecinosTabla,
            char[] letras,
            String palabra,
            int pos,
            int celdaActual,
            boolean[] visitado,
            int[] camino,
            Random azar
    ) {
        char objetivo = palabra.charAt(pos);
        if (letras[celdaActual] != '\0' && letras[celdaActual] != objetivo) {
            return false;
        }
        visitado[celdaActual] = true;
        camino[pos] = celdaActual;

        if (pos == palabra.length() - 1) {
            return true;
        }

        int[] vecinosBase = vecinosTabla[celdaActual];
        int[] vecinos = Arrays.copyOf(vecinosBase, vecinosBase.length);
        for (int i = vecinos.length - 1; i > 0; i--) {
            int j = azar.nextInt(i + 1);
            int tmp = vecinos[i];
            vecinos[i] = vecinos[j];
            vecinos[j] = tmp;
        }

        for (int vecino : vecinos) {
            if (!visitado[vecino]) {
                if (buscarCaminoSiembra(vecinosTabla, letras, palabra, pos + 1, vecino, visitado, camino, azar)) {
                    return true;
                }
            }
        }
        visitado[celdaActual] = false;
        return false;
    }

    /**
     * Recorre el Grafo hexagonal mediante DFS con poda por prefijo en ArbolBinario
     * para descubrir palabras válidas del tablero y sus caminos exactos.
     */
    public static Map<String, int[]> resolverTablero(
            CeldaHexagonal[] celdas,
            Grafo grafo,
            ArbolBinario<String> diccionario
    ) {
        return resolverTableroConVecinos(celdas, extraerTablaVecinos(grafo), diccionario);
    }

    private static Map<String, int[]> resolverTableroConVecinos(
            CeldaHexagonal[] celdas,
            int[][] vecinosTabla,
            ArbolBinario<String> diccionario
    ) {
        Map<String, int[]> encontradas = new HashMap<>(MAX_PALABRAS_SOLVER);
        if (diccionario == null || diccionario.Vacio()) {
            return encontradas;
        }
        boolean[] visitado = new boolean[TOTAL_CELDAS];
        int[] camino = new int[MAX_PROFUNDIDAD_SOLVER];
        StringBuilder actual = new StringBuilder(MAX_PROFUNDIDAD_SOLVER);

        for (int i = 0; i < TOTAL_CELDAS && encontradas.size() < MAX_PALABRAS_SOLVER; i++) {
            dfsResolver(celdas, vecinosTabla, diccionario, i, visitado, camino, 0, actual, encontradas);
        }
        return encontradas;
    }

    private static void dfsResolver(
            CeldaHexagonal[] celdas,
            int[][] vecinosTabla,
            ArbolBinario<String> diccionario,
            int indice,
            boolean[] visitado,
            int[] camino,
            int profundidad,
            StringBuilder actual,
            Map<String, int[]> encontradas
    ) {
        if (profundidad >= MAX_PROFUNDIDAD_SOLVER || encontradas.size() >= MAX_PALABRAS_SOLVER) {
            return;
        }
        visitado[indice] = true;
        camino[profundidad] = indice;
        actual.append(celdas[indice].getLetra());

        if (profundidad >= 1) {
            String cadena = actual.toString();
            int estado = diccionario.evaluarPrefijoYPalabra(cadena);
            if (estado == 0) {
                actual.deleteCharAt(actual.length() - 1);
                visitado[indice] = false;
                return;
            }
            if (profundidad >= 2 && estado == 2 && !encontradas.containsKey(cadena)) {
                encontradas.put(cadena, Arrays.copyOf(camino, profundidad + 1));
            }
        }

        if (profundidad + 1 < MAX_PROFUNDIDAD_SOLVER && encontradas.size() < MAX_PALABRAS_SOLVER) {
            int[] vecinos = vecinosTabla[indice];
            for (int vecino : vecinos) {
                if (!visitado[vecino]) {
                    dfsResolver(celdas, vecinosTabla, diccionario, vecino, visitado, camino,
                            profundidad + 1, actual, encontradas);
                }
            }
        }

        actual.deleteCharAt(actual.length() - 1);
        visitado[indice] = false;
    }

    private static int calcularPuntajeTotal(Map<String, int[]> soluciones) {
        int total = 0;
        for (String palabra : soluciones.keySet()) {
            total += CalculadoraPuntos.calcularPuntos(palabra);
        }
        return total;
    }

    private static ColaCircular<PistaTablero> construirColaPistas(Map<String, int[]> soluciones) {
        ColaCircular<PistaTablero> cola = new ColaCircular<>();
        if (soluciones == null || soluciones.isEmpty()) {
            return cola;
        }
        ListaSimple<PistaTablero> ordenadas = new ListaSimple<>();
        for (Map.Entry<String, int[]> entrada : soluciones.entrySet()) {
            ordenadas.InsertarAlFinal(new PistaTablero(entrada.getKey(), entrada.getValue()));
        }
        ordenadas.Ordenar((a, b) -> {
            int pA = CalculadoraPuntos.calcularPuntos(a.getPalabra());
            int pB = CalculadoraPuntos.calcularPuntos(b.getPalabra());
            return Integer.compare(pB, pA);
        });
        int limite = Math.min(35, ordenadas.Cantidad());
        for (int i = 1; i <= limite; i++) {
            cola.encolar(ordenadas.Obtener(i));
        }
        return cola;
    }

    private static int contarVocales(char[] letras) {
        int total = 0;
        for (char c : letras) {
            if (esVocal(c)) {
                total++;
            }
        }
        return total;
    }

    private static boolean esVocal(char c) {
        return c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U';
    }
}
