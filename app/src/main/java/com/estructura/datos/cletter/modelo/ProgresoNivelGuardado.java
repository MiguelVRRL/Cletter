package com.estructura.datos.cletter.modelo;

import com.estructura.datos.cletter.estructuras.lista.ListaSimple;

/**
 * Representa el progreso parcial guardado de un nivel en curso para poder
 * retomarlo exactamente donde lo dejó el jugador al regresar desde Inicio.
 */
public class ProgresoNivelGuardado {
    private final int numeroNivel;
    private final String idioma;
    private final String letrasTablero;
    private final ListaSimple<PalabraEncontrada> palabras;

    public ProgresoNivelGuardado(
            int numeroNivel,
            String idioma,
            String letrasTablero,
            ListaSimple<PalabraEncontrada> palabras
    ) {
        this.numeroNivel = numeroNivel;
        this.idioma = idioma;
        this.letrasTablero = letrasTablero;
        this.palabras = palabras;
    }

    public int getNumeroNivel() {
        return numeroNivel;
    }

    public String getIdioma() {
        return idioma;
    }

    public String getLetrasTablero() {
        return letrasTablero;
    }

    public ListaSimple<PalabraEncontrada> getPalabras() {
        return palabras;
    }
}
