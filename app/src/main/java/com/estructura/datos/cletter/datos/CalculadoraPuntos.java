package com.estructura.datos.cletter.datos;

/**
 * Calcula los puntos de una palabra en función de la complejidad de cada letra
 * y un multiplicador creciente según su longitud.
 */
public final class CalculadoraPuntos {

    private CalculadoraPuntos() {
    }

    public static int obtenerPesoLetra(char letra) {
        char c = Character.toUpperCase(letra);
        switch (c) {
            case 'A':
            case 'E':
            case 'I':
            case 'O':
            case 'S':
            case 'N':
            case 'R':
            case 'L':
            case 'T':
                return 1;
            case 'C':
            case 'D':
            case 'M':
            case 'P':
            case 'U':
            case 'B':
            case 'G':
                return 2;
            case 'F':
            case 'H':
            case 'V':
            case 'Y':
            case 'Q':
                return 3;
            case 'J':
            case 'Ñ':
            case 'X':
            case 'Z':
            case 'K':
            case 'W':
                return 4;
            default:
                return 1;
        }
    }

    public static double obtenerMultiplicadorLongitud(int longitud) {
        if (longitud <= 3) {
            return 1.0;
        } else if (longitud == 4) {
            return 1.25;
        } else if (longitud == 5) {
            return 1.5;
        } else if (longitud == 6) {
            return 2.0;
        } else {
            return 2.5 + (longitud - 7) * 0.5;
        }
    }

    public static int calcularPuntos(String palabra) {
        if (palabra == null || palabra.length() < 3) {
            return 0;
        }
        int sumaBase = 0;
        for (int i = 0; i < palabra.length(); i++) {
            sumaBase += obtenerPesoLetra(palabra.charAt(i));
        }
        double multiplicador = obtenerMultiplicadorLongitud(palabra.length());
        return (int) Math.round(sumaBase * multiplicador);
    }

    /**
     * Calcula la meta de puntos progresiva para un nivel entre 1 y 200.
     * Escala suavemente desde 20 pts (Nivel 1) hasta 155 pts (Nivel 200).
     */
    public static int calcularMetaNivel(int numeroNivel) {
        int nivelSeguro = Math.max(1, Math.min(200, numeroNivel));
        int piso = (nivelSeguro - 1) / 10;       // 0..19
        int subNivel = (nivelSeguro - 1) % 10;   // 0..9
        int basePiso = 20 + piso * 6;
        int incrementoSubnivel = (subNivel * 2);
        return Math.min(160, basePiso + incrementoSubnivel);
    }

    /**
     * Recompensa en monedas al superar un nivel (bono extra al conquistar un piso).
     */
    public static int calcularMonedasRecompensa(int numeroNivel, boolean conquistoPiso) {
        int base = 15 + ((numeroNivel - 1) / 10) * 2;
        return conquistoPiso ? base + 50 : base;
    }
}
