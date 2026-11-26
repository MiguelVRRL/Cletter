package com.estructura.datos.cletter.modelo;

/**
 * Representa una palabra válida junto con el camino de índices de celdas en el tablero.
 * Se almacena en ColaCircular<PistaTablero> para ofrecer pistas rotativas al jugador.
 */
public class PistaTablero {
    private final String palabra;
    private final int[] caminoIndices;

    public PistaTablero(String palabra, int[] caminoIndices) {
        this.palabra = palabra;
        this.caminoIndices = caminoIndices;
    }

    public String getPalabra() {
        return palabra;
    }

    public int[] getCaminoIndices() {
        return caminoIndices;
    }
}
