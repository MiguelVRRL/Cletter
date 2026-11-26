package com.estructura.datos.cletter.modelo;

import com.estructura.datos.cletter.estructuras.cola.ColaCircular;
import com.estructura.datos.cletter.estructuras.grafo.Grafo;

/**
 * Contiene el estado completo generado para un nivel específico (1..200),
 * incluyendo las 18 celdas hexagonales, su Grafo de adyacencias y la ColaCircular de pistas.
 */
public class TableroNivel {
    private final int numeroNivel;
    private final int numeroPiso;
    private final int puntajeMeta;
    private final CeldaHexagonal[] celdas;
    private final Grafo grafo;
    private final ColaCircular<PistaTablero> colaPistas;
    private final int totalPalabrasDisponibles;
    private final int puntajeMaximoDisponible;

    public TableroNivel(
            int numeroNivel,
            int numeroPiso,
            int puntajeMeta,
            CeldaHexagonal[] celdas,
            Grafo grafo,
            ColaCircular<PistaTablero> colaPistas,
            int totalPalabrasDisponibles,
            int puntajeMaximoDisponible
    ) {
        this.numeroNivel = numeroNivel;
        this.numeroPiso = numeroPiso;
        this.puntajeMeta = puntajeMeta;
        this.celdas = celdas;
        this.grafo = grafo;
        this.colaPistas = colaPistas;
        this.totalPalabrasDisponibles = totalPalabrasDisponibles;
        this.puntajeMaximoDisponible = puntajeMaximoDisponible;
    }

    public int getNumeroNivel() {
        return numeroNivel;
    }

    public int getNumeroPiso() {
        return numeroPiso;
    }

    public int getPuntajeMeta() {
        return puntajeMeta;
    }

    public CeldaHexagonal[] getCeldas() {
        return celdas;
    }

    public Grafo getGrafo() {
        return grafo;
    }

    public ColaCircular<PistaTablero> getColaPistas() {
        return colaPistas;
    }

    public int getTotalPalabrasDisponibles() {
        return totalPalabrasDisponibles;
    }

    public int getPuntajeMaximoDisponible() {
        return puntajeMaximoDisponible;
    }
}
