package com.estructura.datos.cletter.modelo;

/**
 * Representa una celda individual dentro del panal hexagonal 4-3-4-3-4 (18 celdas).
 */
public class CeldaHexagonal {
    private final int indice;
    private final String idVertice;
    private final int columna;
    private final int fila;
    private char letra;
    private float centroX;
    private float centroY;

    public CeldaHexagonal(int indice, int columna, int fila, char letra) {
        this.indice = indice;
        this.idVertice = "H" + indice;
        this.columna = columna;
        this.fila = fila;
        this.letra = letra;
    }

    public int getIndice() {
        return indice;
    }

    public String getIdVertice() {
        return idVertice;
    }

    public int getColumna() {
        return columna;
    }

    public int getFila() {
        return fila;
    }

    public char getLetra() {
        return letra;
    }

    public void setLetra(char letra) {
        this.letra = letra;
    }

    public float getCentroX() {
        return centroX;
    }

    public void setCentroX(float centroX) {
        this.centroX = centroX;
    }

    public float getCentroY() {
        return centroY;
    }

    public void setCentroY(float centroY) {
        this.centroY = centroY;
    }
}
