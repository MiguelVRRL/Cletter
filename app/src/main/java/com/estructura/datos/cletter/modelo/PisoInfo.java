package com.estructura.datos.cletter.modelo;

/**
 * Representa la información temática e histórica de cada uno de los 20 pisos de Cletter.
 */
public class PisoInfo {
    public static final int AREA_CS = 0;
    public static final int AREA_VIDEOJUEGOS = 1;
    public static final int AREA_MIXTO = 2;

    private final int numero;
    private final String tituloEs;
    private final String tituloEn;
    private final String subtituloEs;
    private final String subtituloEn;
    private final int area;
    private final String anio;
    private final String historiaEs;
    private final String historiaEn;
    private final String nombreRecursoPortada;
    private final int colorPrimarioFondo;
    private final int colorSecundarioFondo;
    private final String[] palabrasClaveEs;
    private final String[] palabrasClaveEn;

    public PisoInfo(
            int numero,
            String tituloEs,
            String tituloEn,
            String subtituloEs,
            String subtituloEn,
            int area,
            String anio,
            String historiaEs,
            String historiaEn,
            String nombreRecursoPortada,
            int colorPrimarioFondo,
            int colorSecundarioFondo,
            String[] palabrasClaveEs,
            String[] palabrasClaveEn
    ) {
        this.numero = numero;
        this.tituloEs = tituloEs;
        this.tituloEn = tituloEn;
        this.subtituloEs = subtituloEs;
        this.subtituloEn = subtituloEn;
        this.area = area;
        this.anio = anio;
        this.historiaEs = historiaEs;
        this.historiaEn = historiaEn;
        this.nombreRecursoPortada = nombreRecursoPortada;
        this.colorPrimarioFondo = colorPrimarioFondo;
        this.colorSecundarioFondo = colorSecundarioFondo;
        this.palabrasClaveEs = palabrasClaveEs;
        this.palabrasClaveEn = palabrasClaveEn;
    }

    public int getNumero() {
        return numero;
    }

    public String getTitulo(boolean esIngles) {
        return esIngles ? tituloEn : tituloEs;
    }

    public String getSubtitulo(boolean esIngles) {
        return esIngles ? subtituloEn : subtituloEs;
    }

    public int getArea() {
        return area;
    }

    public String getAnio() {
        return anio;
    }

    public String getHistoria(boolean esIngles) {
        return esIngles ? historiaEn : historiaEs;
    }

    public String getNombreRecursoPortada() {
        return nombreRecursoPortada;
    }

    public int getColorPrimarioFondo() {
        return colorPrimarioFondo;
    }

    public int getColorSecundarioFondo() {
        return colorSecundarioFondo;
    }

    public String[] getPalabrasClave(boolean esIngles) {
        return esIngles ? palabrasClaveEn : palabrasClaveEs;
    }

    public int getNivelInicio() {
        return (numero - 1) * 10 + 1;
    }

    public int getNivelFin() {
        return numero * 10;
    }
}
