package com.estructura.datos.cletter.modelo;

/**
 * Representa un logro local desbloqueable dentro de Cletter.
 */
public class Logro {
    private final String id;
    private final String tituloEs;
    private final String tituloEn;
    private final String descripcionEs;
    private final String descripcionEn;
    private final boolean desbloqueado;

    public Logro(
            String id,
            String tituloEs,
            String tituloEn,
            String descripcionEs,
            String descripcionEn,
            boolean desbloqueado
    ) {
        this.id = id;
        this.tituloEs = tituloEs;
        this.tituloEn = tituloEn;
        this.descripcionEs = descripcionEs;
        this.descripcionEn = descripcionEn;
        this.desbloqueado = desbloqueado;
    }

    public String getId() {
        return id;
    }

    public String getTitulo(boolean esIngles) {
        return esIngles ? tituloEn : tituloEs;
    }

    public String getDescripcion(boolean esIngles) {
        return esIngles ? descripcionEn : descripcionEs;
    }

    public boolean isDesbloqueado() {
        return desbloqueado;
    }
}
