package com.estructura.datos.cletter.estructuras.lista;

/**
 * Nodo genérico para la Lista Simplemente Enlazada.
 */
public class NodoListaSimple<T> {
    private T dato;
    private NodoListaSimple<T> siguiente;

    public NodoListaSimple(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    public T getDato() {
        return dato;
    }

    public NodoListaSimple<T> getSiguiente() {
        return siguiente;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public void setSiguiente(NodoListaSimple<T> siguiente) {
        this.siguiente = siguiente;
    }
}
