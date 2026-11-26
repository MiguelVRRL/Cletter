package com.estructura.datos.cletter;

import com.estructura.datos.cletter.datos.CalculadoraPuntos;
import com.estructura.datos.cletter.datos.CatalogoPisos;
import com.estructura.datos.cletter.datos.GeneradorTablero;
import com.estructura.datos.cletter.datos.GestorDiccionario;
import com.estructura.datos.cletter.estructuras.arbol.ArbolBinario;
import com.estructura.datos.cletter.estructuras.cola.ColaCircular;
import com.estructura.datos.cletter.estructuras.grafo.Grafo;
import com.estructura.datos.cletter.estructuras.lista.ListaSimple;
import com.estructura.datos.cletter.estructuras.pila.Pila;
import com.estructura.datos.cletter.modelo.CeldaHexagonal;
import com.estructura.datos.cletter.modelo.PalabraEncontrada;
import com.estructura.datos.cletter.modelo.PisoInfo;
import com.estructura.datos.cletter.modelo.PistaTablero;
import com.estructura.datos.cletter.modelo.TableroNivel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Pruebas unitarias de las 5 estructuras de datos aplicadas y del motor
 * de generación/puntuación de Cletter.
 */
public class PruebasMotorCletter {

    @Test
    public void pruebaGrafoTopologiaHexagonal18Celdas() {
        CeldaHexagonal[] celdas = new CeldaHexagonal[GeneradorTablero.TOTAL_CELDAS];
        Grafo grafo = GeneradorTablero.construirGrafoHexagonal(celdas);

        assertEquals(18, grafo.getNroVertices());

        // La celda central de la columna 2 (col=2, fila=1 -> índice 8) debe tener 6 vecinos
        int indiceCentro = -1;
        for (CeldaHexagonal c : celdas) {
            if (c.getColumna() == 2 && c.getFila() == 1) {
                indiceCentro = c.getIndice();
                break;
            }
        }
        assertTrue(indiceCentro >= 0);
        assertEquals(6, grafo.obtenerIndicesVecinos(indiceCentro).length);

        // La esquina superior izquierda (col=0, fila=0 -> índice 0) tiene 2 vecinos: (0,1) y (1,0)
        assertEquals(2, grafo.obtenerIndicesVecinos(0).length);
        assertTrue(grafo.Adyacente("H0", "H1"));
        assertTrue(grafo.Adyacente("H0", "H4"));
        assertFalse(grafo.Adyacente("H0", "H17"));
    }

    @Test
    public void pruebaPilaTrazoYRetroceso() {
        Pila<Integer> pila = new Pila<>();
        assertTrue(pila.Vacia());

        pila.Apilar(0);
        pila.Apilar(4);
        pila.Apilar(8);

        assertEquals(3, pila.Tamaño());
        assertEquals(Integer.valueOf(8), pila.LeerUltimo());
        assertEquals(Integer.valueOf(4), pila.LeerPenultimo());
        assertTrue(pila.Contiene(4));
        assertFalse(pila.Contiene(12));

        // Retroceso (backtracking)
        assertEquals(Integer.valueOf(8), pila.Desapilar());
        assertEquals(Integer.valueOf(4), pila.LeerUltimo());
        assertEquals(2, pila.Tamaño());

        Object[] orden = pila.AArregloOrdenado();
        assertEquals(2, orden.length);
        assertEquals(0, orden[0]);
        assertEquals(4, orden[1]);

        pila.Limpiar();
        assertTrue(pila.Vacia());
    }

    @Test
    public void pruebaListaSimpleInsertarObtenerEliminar() {
        ListaSimple<PalabraEncontrada> lista = new ListaSimple<>();
        assertTrue(lista.EsVacia());

        lista.InsertarAlFinal(new PalabraEncontrada("BIT", 4));
        lista.InsertarAlFinal(new PalabraEncontrada("BYTE", 10));
        lista.InsertarAlFinal(new PalabraEncontrada("GRAFO", 15));

        assertEquals(3, lista.Cantidad());
        assertTrue(lista.Contiene(new PalabraEncontrada("BYTE", 0)));
        assertEquals("BIT", lista.Obtener(1).getTexto());
        assertEquals("BYTE", lista.Obtener(2).getTexto());
        assertEquals("GRAFO", lista.Obtener(3).getTexto());
        assertNull(lista.Obtener(4));

        PalabraEncontrada eliminada = lista.Eliminar(2);
        assertNotNull(eliminada);
        assertEquals("BYTE", eliminada.getTexto());
        assertEquals(2, lista.Cantidad());
        assertEquals("GRAFO", lista.Obtener(2).getTexto());

        // Eliminar el último elemento y volver a insertar al final valida el puntero ultimo
        PalabraEncontrada ultimoEliminado = lista.Eliminar(2);
        assertEquals("GRAFO", ultimoEliminado.getTexto());
        lista.InsertarAlFinal(new PalabraEncontrada("ARBOL", 12));
        assertEquals(2, lista.Cantidad());
        assertEquals("ARBOL", lista.Obtener(2).getTexto());

        // Copiar, Mezclar y Ordenar
        ListaSimple<PalabraEncontrada> copia = lista.Copiar();
        assertEquals(2, copia.Cantidad());
        copia.Mezclar(new java.util.Random(42L));
        copia.Ordenar((a, b) -> Integer.compare(b.getPuntos(), a.getPuntos()));
        assertEquals("ARBOL", copia.Obtener(1).getTexto());
        assertEquals("BIT", copia.Obtener(2).getTexto());

        lista.Anula();
        assertTrue(lista.EsVacia());
    }

    @Test
    public void pruebaColaCircularRotacionYUnicoElemento() {
        ColaCircular<String> cola = new ColaCircular<>();
        assertTrue(cola.Vacia());
        assertNull(cola.desencolar());

        cola.encolar("SOLO");
        assertEquals(1, cola.getTamaño());
        assertEquals("SOLO", cola.frente());
        assertEquals("SOLO", cola.desencolar());
        assertTrue(cola.Vacia());
        assertNull(cola.frente());

        cola.encolar("A");
        cola.encolar("B");
        cola.encolar("C");

        // Rotación circular
        String primero = cola.desencolar();
        assertEquals("A", primero);
        cola.encolar(primero);
        assertEquals("B", cola.frente());
        assertEquals("A", cola.ultimo());
        assertEquals(3, cola.getTamaño());
    }

    @Test
    public void pruebaArbolBinarioBalanceadoYPrefijos() {
        String[] palabras = new String[]{
                "ALGORITMO", "ARBOL", "BIT", "BYTE", "CODIGO", "COLA", "DATO", "GRAFO", "PILA", "PIXEL"
        };
        ArbolBinario<String> arbol = GestorDiccionario.construirArbolDesdePalabras(palabras, false);

        assertFalse(arbol.Vacio());
        assertTrue(arbol.existeValor("GRAFO"));
        assertTrue(arbol.existeValor("ALGORITMO"));
        assertFalse(arbol.existeValor("INEXISTENTE"));

        assertTrue(arbol.existePrefijo("ALG"));
        assertTrue(arbol.existePrefijo("PIX"));
        assertFalse(arbol.existePrefijo("ZZZ"));

        // Prueba genérica del Árbol Binario con Integer
        ArbolBinario<Integer> arbolEnteros = new ArbolBinario<>();
        arbolEnteros.Insertar(50);
        arbolEnteros.Insertar(20);
        arbolEnteros.Insertar(80);
        arbolEnteros.Insertar(10);
        assertEquals(4, arbolEnteros.ContarNodos());
        assertEquals(Integer.valueOf(10), arbolEnteros.MenorValor());
        assertTrue(arbolEnteros.existe(20));
        assertFalse(arbolEnteros.existe(99));
        ListaSimple<Integer> ordenados = arbolEnteros.ListaOrdenada();
        assertEquals(4, ordenados.Cantidad());
        assertEquals(Integer.valueOf(10), ordenados.Obtener(1));
        assertEquals(Integer.valueOf(80), ordenados.Obtener(4));
    }

    @Test
    public void pruebaCalculadoraPuntosYMetas() {
        assertEquals(0, CalculadoraPuntos.calcularPuntos("AB"));
        int ptsCorta = CalculadoraPuntos.calcularPuntos("SOL");     // 1+1+1 = 3 * 1.0 = 3
        int ptsLarga = CalculadoraPuntos.calcularPuntos("JUEGO");   // 4+2+1+2+1 = 10 * 1.5 = 15
        assertEquals(3, ptsCorta);
        assertEquals(15, ptsLarga);
        assertTrue(ptsLarga > ptsCorta);

        int meta1 = CalculadoraPuntos.calcularMetaNivel(1);
        int meta100 = CalculadoraPuntos.calcularMetaNivel(100);
        int meta200 = CalculadoraPuntos.calcularMetaNivel(200);
        assertTrue(meta1 >= 15);
        assertTrue(meta100 > meta1);
        assertTrue(meta200 > meta100);
    }

    @Test
    public void pruebaCatalogo20PisosYGeneradorBilingue() {
        PisoInfo[] pisos = CatalogoPisos.obtenerTodos();
        assertEquals(20, pisos.length);
        assertEquals("floor_01", pisos[0].getNombreRecursoPortada());
        assertEquals("floor_20", pisos[19].getNombreRecursoPortada());
        assertEquals(1, CatalogoPisos.calcularNumeroPiso(1));
        assertEquals(1, CatalogoPisos.calcularNumeroPiso(10));
        assertEquals(2, CatalogoPisos.calcularNumeroPiso(11));
        assertEquals(20, CatalogoPisos.calcularNumeroPiso(200));

        String[] muestraEs = new String[]{
                "CASA", "MESA", "SOL", "LUZ", "MAR", "RED", "BIT", "DATO", "PASO", "NOTA",
                "CICLO", "ORDEN", "MOTOR", "TABLA", "LOGICA", "CINTA", "REGLA", "CLAVE",
                "VALOR", "CERO", "NAVE", "BOLA", "RAYO", "DUELO", "GIRO", "PUNTO", "JUEGO",
                "NIVEL", "SALTO", "MAPA", "RUTA", "NODO", "GRAFO", "PILA", "COLA", "ARBOL"
        };
        ArbolBinario<String> dicEs = GestorDiccionario.construirArbolDesdePalabras(muestraEs, false);

        TableroNivel nivel1 = GeneradorTablero.generarNivel(1, false, dicEs, 0L);
        assertEquals(18, nivel1.getCeldas().length);
        assertTrue(nivel1.getTotalPalabrasDisponibles() >= 3);
        assertFalse(nivel1.getColaPistas().Vacia());

        PistaTablero pista = nivel1.getColaPistas().frente();
        assertNotNull(pista);
        assertTrue(pista.getPalabra().length() >= 3);
        assertEquals(pista.getPalabra().length(), pista.getCaminoIndices().length);

        TableroNivel mezclado = GeneradorTablero.mezclarTablero(nivel1, dicEs, 12345L);
        assertEquals(18, mezclado.getCeldas().length);
        assertEquals(nivel1.getPuntajeMeta(), mezclado.getPuntajeMeta());

        String[] muestraEn = new String[]{
                "CODE", "DATA", "LOOP", "NOTE", "STEP", "LOGIC", "TABLE", "ENGINE", "TAPE",
                "STATE", "RULE", "MIND", "ZERO", "READ", "HEAD", "BIT", "BYTE", "CHIP",
                "GATE", "WIRE", "GAME", "PLAY", "SCORE", "PIXEL", "GRAPH", "STACK", "QUEUE", "TREE"
        };
        ArbolBinario<String> dicEn = GestorDiccionario.construirArbolDesdePalabras(muestraEn, true);
        TableroNivel nivel10En = GeneradorTablero.generarNivel(10, true, dicEn, 0L);
        assertEquals(1, nivel10En.getNumeroPiso());
        assertTrue(nivel10En.getTotalPalabrasDisponibles() >= 3);
    }

    @Test
    public void pruebaRendimientoDiccionariosCompletos() throws Exception {
        java.io.File dicEsFile = new java.io.File("src/main/assets/diccionario_es.txt");
        java.io.File dicEnFile = new java.io.File("src/main/assets/diccionario_en.txt");
        if (!dicEsFile.exists() || !dicEnFile.exists()) {
            return;
        }

        long t0 = System.nanoTime();
        String[] palabrasEs = java.nio.file.Files.readAllLines(dicEsFile.toPath()).toArray(new String[0]);
        ArbolBinario<String> arbolEs = GestorDiccionario.construirArbolDesdePalabras(palabrasEs, false);
        long tEsCargaMs = (System.nanoTime() - t0) / 1_000_000L;

        long t1 = System.nanoTime();
        for (int nivel : new int[]{1, 50, 100, 150, 200}) {
            TableroNivel t = GeneradorTablero.generarNivel(nivel, false, arbolEs, 0L);
            assertTrue(t.getTotalPalabrasDisponibles() >= 5);
        }
        long tEsGenMs = (System.nanoTime() - t1) / 1_000_000L;

        long t2 = System.nanoTime();
        String[] palabrasEn = java.nio.file.Files.readAllLines(dicEnFile.toPath()).toArray(new String[0]);
        ArbolBinario<String> arbolEn = GestorDiccionario.construirArbolDesdePalabras(palabrasEn, true);
        long tEnCargaMs = (System.nanoTime() - t2) / 1_000_000L;

        long t3 = System.nanoTime();
        for (int nivel : new int[]{1, 50, 100, 150, 200}) {
            TableroNivel t = GeneradorTablero.generarNivel(nivel, true, arbolEn, 0L);
            assertTrue(t.getTotalPalabrasDisponibles() >= 5);
        }
        long tEnGenMs = (System.nanoTime() - t3) / 1_000_000L;

        assertTrue("Carga ES muy lenta: " + tEsCargaMs + " ms", tEsCargaMs < 1000);
        assertTrue("Generación 5 niveles ES muy lenta: " + tEsGenMs + " ms", tEsGenMs < 1500);
        assertTrue("Carga EN muy lenta: " + tEnCargaMs + " ms", tEnCargaMs < 1200);
        assertTrue("Generación 5 niveles EN muy lenta: " + tEnGenMs + " ms", tEnGenMs < 1500);
    }

    @Test
    public void pruebaSerializacionYRestauracionDeTablero() {
        String[] muestra = new String[]{
                "CASA", "MESA", "SOL", "LUZ", "MAR", "RED", "BIT", "DATO", "PASO", "NOTA",
                "CICLO", "ORDEN", "MOTOR", "TABLA", "LOGICA"
        };
        ArbolBinario<String> dic = GestorDiccionario.construirArbolDesdePalabras(muestra, false);
        TableroNivel original = GeneradorTablero.generarNivel(1, false, dic, 0L);

        String letras = GeneradorTablero.serializarLetras(original);
        assertEquals(GeneradorTablero.TOTAL_CELDAS, letras.length());

        TableroNivel restaurado = GeneradorTablero.restaurarTableroDesdeLetras(1, letras, dic);
        assertEquals(letras, GeneradorTablero.serializarLetras(restaurado));
        assertEquals(original.getPuntajeMeta(), restaurado.getPuntajeMeta());
        assertEquals(original.getTotalPalabrasDisponibles(), restaurado.getTotalPalabrasDisponibles());
        assertFalse(restaurado.getColaPistas().Vacia());
    }
}
