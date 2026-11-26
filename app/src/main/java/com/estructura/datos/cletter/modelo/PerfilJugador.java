package com.estructura.datos.cletter.modelo;

/**
 * Modelo del perfil y estadísticas locales del jugador.
 */
public class PerfilJugador {
    private final String nombre;
    private final int indiceAvatar;
    private final String fechaRegistro;
    private final int monedas;
    private final int nivelActual;
    private final int pisosConquistados;
    private final int masNivelesDia;
    private final int totalDias;
    private final int totalPalabras;
    private final int palabraMasLarga;
    private final int mejorPalabraPuntos;
    private final int puntosTotales;
    private final int cantidadLogros;

    public PerfilJugador(
            String nombre,
            int indiceAvatar,
            String fechaRegistro,
            int monedas,
            int nivelActual,
            int pisosConquistados,
            int masNivelesDia,
            int totalDias,
            int totalPalabras,
            int palabraMasLarga,
            int mejorPalabraPuntos,
            int puntosTotales,
            int cantidadLogros
    ) {
        this.nombre = nombre;
        this.indiceAvatar = indiceAvatar;
        this.fechaRegistro = fechaRegistro;
        this.monedas = monedas;
        this.nivelActual = nivelActual;
        this.pisosConquistados = pisosConquistados;
        this.masNivelesDia = masNivelesDia;
        this.totalDias = totalDias;
        this.totalPalabras = totalPalabras;
        this.palabraMasLarga = palabraMasLarga;
        this.mejorPalabraPuntos = mejorPalabraPuntos;
        this.puntosTotales = puntosTotales;
        this.cantidadLogros = cantidadLogros;
    }

    public String getNombre() { return nombre; }
    public int getIndiceAvatar() { return indiceAvatar; }
    public String getFechaRegistro() { return fechaRegistro; }
    public int getMonedas() { return monedas; }
    public int getNivelActual() { return nivelActual; }
    public int getPisosConquistados() { return pisosConquistados; }
    public int getMasNivelesDia() { return masNivelesDia; }
    public int getTotalDias() { return totalDias; }
    public int getTotalPalabras() { return totalPalabras; }
    public int getPalabraMasLarga() { return palabraMasLarga; }
    public int getMejorPalabraPuntos() { return mejorPalabraPuntos; }
    public int getPuntosTotales() { return puntosTotales; }
    public int getCantidadLogros() { return cantidadLogros; }
}
