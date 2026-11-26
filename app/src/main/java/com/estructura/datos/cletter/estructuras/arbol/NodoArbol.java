package com.estructura.datos.cletter.estructuras.arbol;

/**
 * Nodo genérico para el Árbol Binario de Búsqueda.
 */
public class NodoArbol<T> {
    private NodoArbol<T> izquierdo;
    private T dato;
    private NodoArbol<T> derecho;

    public NodoArbol() {
        this.izquierdo = null;
        this.dato = null;
        this.derecho = null;
    }

    public NodoArbol(T dato) {
        this.izquierdo = null;
        this.dato = dato;
        this.derecho = null;
    }

    public NodoArbol<T> getIzquierdo() {
        return izquierdo;
    }

    public T getDato() {
        return dato;
    }

    public NodoArbol<T> getDerecho() {
        return derecho;
    }

    public void setIzquierdo(NodoArbol<T> izquierdo) {
        this.izquierdo = izquierdo;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public void setDerecho(NodoArbol<T> derecho) {
        this.derecho = derecho;
    }
}
