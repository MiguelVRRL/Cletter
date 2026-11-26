package com.estructura.datos.cletter.estructuras.pila;

/**
 * Pila genérica LIFO basada en la clase universitaria.
 * En Cletter gestiona el trazo activo de celdas al deslizar sobre el tablero
 * y el retroceso (backtracking) cuando el jugador regresa a la letra anterior.
 */
public class Pila<T> {
    private NodoPila<T> ultimo;
    private int n;

    public Pila() {
        ultimo = null;
        n = 0;
    }

    public int Tamaño() {
        return n;
    }

    public boolean Vacia() {
        return ultimo == null;
    }

    public void Apilar(T dato) {
        NodoPila<T> nuevoNodo = new NodoPila<>(dato);
        if (Vacia()) {
            ultimo = nuevoNodo;
        } else {
            nuevoNodo.setSiguiente(ultimo);
            ultimo = nuevoNodo;
        }
        n++;
    }

    public T LeerUltimo() {
        if (Vacia()) {
            return null;
        }
        return ultimo.getDato();
    }

    public T LeerPenultimo() {
        if (ultimo == null || ultimo.getSiguiente() == null) {
            return null;
        }
        return ultimo.getSiguiente().getDato();
    }

    public T Desapilar() {
        if (Vacia()) {
            return null;
        }
        T datoTemporal = ultimo.getDato();
        ultimo = ultimo.getSiguiente();
        n--;
        return datoTemporal;
    }

    public boolean Contiene(T dato) {
        NodoPila<T> actual = ultimo;
        while (actual != null) {
            if (dato == null ? actual.getDato() == null : dato.equals(actual.getDato())) {
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    public void Limpiar() {
        ultimo = null;
        n = 0;
    }

    /**
     * Devuelve los elementos en orden desde la base hasta la cima (orden de lectura de la palabra).
     */
    public Object[] AArregloOrdenado() {
        Object[] resultado = new Object[n];
        NodoPila<T> actual = ultimo;
        int indice = n - 1;
        while (actual != null && indice >= 0) {
            resultado[indice--] = actual.getDato();
            actual = actual.getSiguiente();
        }
        return resultado;
    }
}
