package com.estructura.datos.cletter.estructuras.lista;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Lista simplemente enlazada basada en la clase universitaria, optimizada con
 * puntero al último nodo para inserciones al final en O(1) y operaciones
 * utilitarias simples (Copiar, Mezclar y Ordenar).
 */
public class ListaSimple<T> {
    private NodoListaSimple<T> inicio;
    private NodoListaSimple<T> ultimo;
    private int cantidad;

    public ListaSimple() {
        this.cantidad = 0;
        this.inicio = null;
        this.ultimo = null;
    }

    public int Cantidad() {
        return this.cantidad;
    }

    public boolean EsVacia() {
        return inicio == null;
    }

    public void Anula() {
        this.inicio = null;
        this.ultimo = null;
        this.cantidad = 0;
    }

    /**
     * Inserta un dato en la posición dada (indexación basada en 1).
     */
    public void Insertar(T dato, int posicion) {
        if (posicion < 1 || posicion > (this.cantidad + 1)) {
            return;
        }
        NodoListaSimple<T> nuevoNodo = new NodoListaSimple<>(dato);
        if (posicion == 1) {
            nuevoNodo.setSiguiente(inicio);
            inicio = nuevoNodo;
            if (ultimo == null) {
                ultimo = nuevoNodo;
            }
        } else if (posicion == this.cantidad + 1) {
            ultimo.setSiguiente(nuevoNodo);
            ultimo = nuevoNodo;
        } else {
            NodoListaSimple<T> nodoAuxiliar = inicio;
            int contador = 1;
            while (contador < (posicion - 1)) {
                nodoAuxiliar = nodoAuxiliar.getSiguiente();
                contador++;
            }
            nuevoNodo.setSiguiente(nodoAuxiliar.getSiguiente());
            nodoAuxiliar.setSiguiente(nuevoNodo);
        }
        this.cantidad++;
    }

    /**
     * Inserta un dato al final de la lista en O(1).
     */
    public void InsertarAlFinal(T dato) {
        Insertar(dato, this.cantidad + 1);
    }

    /**
     * Obtiene el dato ubicado en la posición dada (indexación basada en 1).
     */
    public T Obtener(int posicion) {
        if (posicion < 1 || posicion > this.cantidad) {
            return null;
        }
        if (posicion == this.cantidad && ultimo != null) {
            return ultimo.getDato();
        }
        NodoListaSimple<T> actual = inicio;
        int contador = 1;
        while (contador < posicion) {
            actual = actual.getSiguiente();
            contador++;
        }
        return actual.getDato();
    }

    /**
     * Elimina y devuelve el dato ubicado en la posición dada (indexación basada en 1).
     */
    public T Eliminar(int posicion) {
        if (posicion < 1 || posicion > this.cantidad || inicio == null) {
            return null;
        }
        T datoEliminado;
        if (posicion == 1) {
            datoEliminado = inicio.getDato();
            inicio = inicio.getSiguiente();
            if (inicio == null) {
                ultimo = null;
            }
        } else {
            NodoListaSimple<T> anterior = inicio;
            int contador = 1;
            while (contador < posicion - 1) {
                anterior = anterior.getSiguiente();
                contador++;
            }
            NodoListaSimple<T> objetivo = anterior.getSiguiente();
            datoEliminado = objetivo.getDato();
            anterior.setSiguiente(objetivo.getSiguiente());
            if (objetivo == ultimo) {
                ultimo = anterior;
            }
        }
        this.cantidad--;
        return datoEliminado;
    }

    /**
     * Verifica si el elemento existe dentro de la lista.
     */
    public boolean Contiene(T dato) {
        NodoListaSimple<T> actual = inicio;
        while (actual != null) {
            if (dato == null ? actual.getDato() == null : dato.equals(actual.getDato())) {
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    /**
     * Devuelve una copia independiente con los mismos elementos en O(N).
     */
    public ListaSimple<T> Copiar() {
        ListaSimple<T> copia = new ListaSimple<>();
        NodoListaSimple<T> actual = inicio;
        while (actual != null) {
            copia.InsertarAlFinal(actual.getDato());
            actual = actual.getSiguiente();
        }
        return copia;
    }

    /**
     * Mezcla aleatoriamente los elementos en O(N) mediante Fisher-Yates.
     */
    public void Mezclar(Random azar) {
        if (cantidad <= 1 || azar == null) {
            return;
        }
        Object[] datos = extraerDatos();
        for (int i = datos.length - 1; i > 0; i--) {
            int j = azar.nextInt(i + 1);
            Object temporal = datos[i];
            datos[i] = datos[j];
            datos[j] = temporal;
        }
        escribirDatos(datos);
    }

    /**
     * Ordena los elementos en O(N log N) usando el comparador proporcionado.
     */
    @SuppressWarnings("unchecked")
    public void Ordenar(Comparator<T> comparador) {
        if (cantidad <= 1 || comparador == null) {
            return;
        }
        T[] datos = (T[]) extraerDatos();
        Arrays.sort(datos, comparador);
        escribirDatos(datos);
    }

    private Object[] extraerDatos() {
        Object[] datos = new Object[cantidad];
        NodoListaSimple<T> actual = inicio;
        int i = 0;
        while (actual != null) {
            datos[i++] = actual.getDato();
            actual = actual.getSiguiente();
        }
        return datos;
    }

    @SuppressWarnings("unchecked")
    private void escribirDatos(Object[] datos) {
        NodoListaSimple<T> actual = inicio;
        int i = 0;
        while (actual != null) {
            actual.setDato((T) datos[i++]);
            actual = actual.getSiguiente();
        }
    }
}
