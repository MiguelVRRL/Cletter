package com.estructura.datos.cletter.estructuras.cola;

/**
 * Cola circular FIFO genérica basada en la clase universitaria.
 * En Cletter administra la rotación circular de pistas del tablero actual
 * y la cola de notificaciones de logros desbloqueados.
 */
public class ColaCircular<T> {
    int tamaño;
    NodoCola<T> primero;
    NodoCola<T> ultimo;

    public ColaCircular() {
        tamaño = 0;
        primero = null;
        ultimo = null;
    }

    public boolean Vacia() {
        return tamaño == 0;
    }

    public T frente() {
        if (Vacia()) {
            return null;
        }
        return primero.getDato();
    }

    public T ultimo() {
        if (Vacia()) {
            return null;
        }
        return ultimo.getDato();
    }

    public void encolar(T dato) {
        if (tamaño == 0) {
            primero = new NodoCola<>(dato);
            ultimo = primero;
            primero.setSiguiente(primero);
        } else {
            NodoCola<T> nodoAux = new NodoCola<>(dato);
            ultimo.setSiguiente(nodoAux);
            nodoAux.setSiguiente(primero);
            ultimo = nodoAux;
        }
        tamaño++;
    }

    public T desencolar() {
        if (tamaño == 0) {
            primero = null;
            ultimo = null;
            return null;
        }
        T datoAux = primero.getDato();
        if (tamaño == 1) {
            primero = null;
            ultimo = null;
        } else {
            primero = primero.getSiguiente();
            ultimo.setSiguiente(primero);
        }
        tamaño--;
        return datoAux;
    }

    public int getTamaño() {
        return this.tamaño;
    }

    public void limpiar() {
        primero = null;
        ultimo = null;
        tamaño = 0;
    }
}
