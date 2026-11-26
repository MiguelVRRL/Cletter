package com.estructura.datos.cletter.modelo;

/**
 * Representa una palabra válida formada por el jugador durante un nivel.
 * Se almacena dentro de ListaSimple<PalabraEncontrada>.
 */
public class PalabraEncontrada {
    private final String texto;
    private final int puntos;

    public PalabraEncontrada(String texto, int puntos) {
        this.texto = texto;
        this.puntos = puntos;
    }

    public String getTexto() {
        return texto;
    }

    public int getPuntos() {
        return puntos;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) return true;
        if (!(otro instanceof PalabraEncontrada)) return false;
        PalabraEncontrada otraPalabra = (PalabraEncontrada) otro;
        return texto.equals(otraPalabra.texto);
    }

    @Override
    public int hashCode() {
        return texto.hashCode();
    }
}
