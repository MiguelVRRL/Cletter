package com.estructura.datos.cletter.estructuras.grafo;

/**
 * Implementación de Grafo con matriz de adyacencia basada en la clase universitaria.
 * En Cletter modela el tablero hexagonal de 18 celdas (<= MAXIMO_VERTICES = 20)
 * y valida las adyacencias entre letras al deslizar el dedo.
 */
public class Grafo {
    private final int MAXIMO_VERTICES = 20;
    private int[][] matriz;
    private int nroVertices;
    private String[] vertices;

    public Grafo() {
        nroVertices = 0;
        matriz = new int[MAXIMO_VERTICES][MAXIMO_VERTICES];
        vertices = new String[MAXIMO_VERTICES];
    }

    public void NuevoVertice(String nombreVertice) {
        if (nroVertices < MAXIMO_VERTICES) {
            if (!ExisteVertice(nombreVertice)) {
                vertices[nroVertices] = nombreVertice;
                nroVertices++;
            }
        }
    }

    private boolean ExisteVertice(String nombreVertice) {
        for (int i = 0; i < nroVertices; i++) {
            if (nombreVertice.equals(vertices[i])) {
                return true;
            }
        }
        return false;
    }

    public void Union(String vertice1, String vertice2, int valor) {
        int i = ObtenerIndice(vertice1);
        int j = ObtenerIndice(vertice2);
        if (i != -1 && j != -1) {
            matriz[i][j] = valor;
            matriz[j][i] = valor;
        }
    }

    public int ObtenerIndice(String nombreVertice) {
        for (int i = 0; i < nroVertices; i++) {
            if (nombreVertice.equals(vertices[i])) {
                return i;
            }
        }
        return -1;
    }

    public void BorrarArista(String vertice1, String vertice2) {
        Union(vertice1, vertice2, 0);
    }

    public boolean Adyacente(String vertice1, String vertice2) {
        int indice1 = ObtenerIndice(vertice1);
        int indice2 = ObtenerIndice(vertice2);
        if (indice1 == -1 || indice2 == -1) {
            return false;
        }
        return matriz[indice1][indice2] > 0;
    }

    public boolean AdyacentePorIndice(int indice1, int indice2) {
        if (indice1 < 0 || indice1 >= nroVertices || indice2 < 0 || indice2 >= nroVertices) {
            return false;
        }
        return matriz[indice1][indice2] > 0;
    }

    public int getNroVertices() {
        return nroVertices;
    }

    public String getVertice(int indice) {
        if (indice < 0 || indice >= nroVertices) {
            return null;
        }
        return vertices[indice];
    }

    public int[] obtenerIndicesVecinos(int indiceVertice) {
        if (indiceVertice < 0 || indiceVertice >= nroVertices) {
            return new int[0];
        }
        int cantidad = 0;
        for (int j = 0; j < nroVertices; j++) {
            if (matriz[indiceVertice][j] > 0) {
                cantidad++;
            }
        }
        int[] vecinos = new int[cantidad];
        int k = 0;
        for (int j = 0; j < nroVertices; j++) {
            if (matriz[indiceVertice][j] > 0) {
                vecinos[k++] = j;
            }
        }
        return vecinos;
    }

    public void MostrarGrafo() {
        for (int i = 0; i < nroVertices; i++) {
            System.out.print(vertices[i] + " -> ");
            for (int j = 0; j < nroVertices; j++) {
                if (matriz[i][j] > 0) {
                    System.out.print(vertices[j] + " ");
                }
            }
            System.out.println();
        }
    }

    public void BorrarVertice(String nombreVertice) {
        int indice = ObtenerIndice(nombreVertice);
        if (indice == -1) {
            return;
        }
        for (int i = indice; i < nroVertices - 1; i++) {
            vertices[i] = vertices[i + 1];
        }
        for (int i = indice; i < nroVertices - 1; i++) {
            for (int j = 0; j < nroVertices; j++) {
                matriz[i][j] = matriz[i + 1][j];
            }
        }
        for (int j = indice; j < nroVertices - 1; j++) {
            for (int i = 0; i < nroVertices - 1; i++) {
                matriz[i][j] = matriz[i][j + 1];
            }
        }
        nroVertices--;
    }
}
