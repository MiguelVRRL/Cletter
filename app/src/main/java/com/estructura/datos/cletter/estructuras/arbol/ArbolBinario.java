package com.estructura.datos.cletter.estructuras.arbol;

import com.estructura.datos.cletter.estructuras.lista.ListaSimple;

/**
 * Árbol Binario de Búsqueda (ABB) genérico basado en la clase universitaria.
 * Funciona con cualquier tipo Comparable (por ejemplo Integer o String).
 * En Cletter indexa de forma balanceada el diccionario bilingüe con búsqueda O(log N)
 * y verificación rápida de prefijos durante la generación de tableros.
 */
public class ArbolBinario<T extends Comparable<T>> {
    private NodoArbol<T> raiz;

    public ArbolBinario() {
        this.raiz = null;
    }

    public boolean Vacio() {
        return raiz == null;
    }

    public void Limpiar() {
        raiz = null;
    }

    /**
     * Inserta un elemento manteniendo la propiedad de Árbol Binario de Búsqueda.
     */
    public void Insertar(T dato) {
        if (dato == null) {
            return;
        }
        NodoArbol<T> nuevo = new NodoArbol<>(dato);
        if (raiz == null) {
            raiz = nuevo;
            return;
        }
        NodoArbol<T> anterior = null;
        NodoArbol<T> reco = raiz;
        while (reco != null) {
            anterior = reco;
            int cmp = dato.compareTo(reco.getDato());
            if (cmp == 0) {
                return; // Evita duplicados
            } else if (cmp < 0) {
                reco = reco.getIzquierdo();
            } else {
                reco = reco.getDerecho();
            }
        }
        if (dato.compareTo(anterior.getDato()) < 0) {
            anterior.setIzquierdo(nuevo);
        } else {
            anterior.setDerecho(nuevo);
        }
    }

    /**
     * Inserta un arreglo previamente ordenado de forma balanceada (punto medio),
     * garantizando altura O(log N) para búsquedas instantáneas.
     */
    public void InsertarBalanceado(T[] arregloOrdenado) {
        if (arregloOrdenado == null || arregloOrdenado.length == 0) {
            raiz = null;
            return;
        }
        raiz = construirBalanceado(arregloOrdenado, 0, arregloOrdenado.length - 1);
    }

    private NodoArbol<T> construirBalanceado(T[] arreglo, int inicio, int fin) {
        if (inicio > fin) {
            return null;
        }
        int medio = inicio + (fin - inicio) / 2;
        NodoArbol<T> nodo = new NodoArbol<>(arreglo[medio]);
        nodo.setIzquierdo(construirBalanceado(arreglo, inicio, medio - 1));
        nodo.setDerecho(construirBalanceado(arreglo, medio + 1, fin));
        return nodo;
    }

    public void ImprimirInOrden() {
        ImprimirInOrden(raiz);
    }

    private void ImprimirInOrden(NodoArbol<T> reco) {
        if (reco != null) {
            ImprimirInOrden(reco.getIzquierdo());
            System.out.print(reco.getDato() + " ");
            ImprimirInOrden(reco.getDerecho());
        }
    }

    public void ImprimirPreOrden() {
        ImprimirPreOrden(raiz);
    }

    private void ImprimirPreOrden(NodoArbol<T> reco) {
        if (reco != null) {
            System.out.print(reco.getDato() + " ");
            ImprimirPreOrden(reco.getIzquierdo());
            ImprimirPreOrden(reco.getDerecho());
        }
    }

    public void ImprimirPostOrden() {
        ImprimirPostOrden(raiz);
    }

    private void ImprimirPostOrden(NodoArbol<T> reco) {
        if (reco != null) {
            ImprimirPostOrden(reco.getIzquierdo());
            ImprimirPostOrden(reco.getDerecho());
            System.out.print(reco.getDato() + " ");
        }
    }

    public int ContarNodos() {
        return ContarNodos(raiz);
    }

    private int ContarNodos(NodoArbol<T> reco) {
        if (reco == null) {
            return 0;
        }
        return ContarNodos(reco.getIzquierdo()) + ContarNodos(reco.getDerecho()) + 1;
    }

    public T MenorValor() {
        if (raiz == null) {
            return null;
        }
        NodoArbol<T> reco = raiz;
        while (reco.getIzquierdo() != null) {
            reco = reco.getIzquierdo();
        }
        return reco.getDato();
    }

    public boolean existe(T valor) {
        return existeValor(valor);
    }

    /**
     * Búsqueda binaria iterativa O(log N) para cualquier tipo Comparable.
     */
    public boolean existeValor(T valor) {
        if (valor == null) {
            return false;
        }
        NodoArbol<T> reco = raiz;
        while (reco != null) {
            int cmp = valor.compareTo(reco.getDato());
            if (cmp == 0) {
                return true;
            } else if (cmp < 0) {
                reco = reco.getIzquierdo();
            } else {
                reco = reco.getDerecho();
            }
        }
        return false;
    }

    /**
     * Verifica en O(log N) si existe al menos un elemento cuyo texto comience con el prefijo.
     * Permite podar ramas imposibles durante el recorrido DFS sobre el Grafo hexagonal.
     */
    public boolean existePrefijo(String prefijo) {
        if (prefijo == null) {
            return false;
        }
        NodoArbol<T> reco = raiz;
        while (reco != null) {
            String actual = textoDe(reco.getDato());
            if (actual.startsWith(prefijo)) {
                return true;
            }
            int cmp = prefijo.compareTo(actual);
            if (cmp < 0) {
                reco = reco.getIzquierdo();
            } else {
                reco = reco.getDerecho();
            }
        }
        return false;
    }

    /**
     * Evalúa en un solo recorrido O(log N) si el texto es:
     * 0 = no es prefijo de ninguna palabra,
     * 1 = es prefijo válido (pero no palabra exacta),
     * 2 = es una palabra exacta del diccionario (y por tanto prefijo válido).
     */
    public int evaluarPrefijoYPalabra(String texto) {
        if (texto == null) {
            return 0;
        }
        NodoArbol<T> reco = raiz;
        boolean hayPrefijo = false;
        while (reco != null) {
            String actual = textoDe(reco.getDato());
            int cmp = texto.compareTo(actual);
            if (cmp == 0) {
                return 2;
            }
            if (!hayPrefijo && actual.startsWith(texto)) {
                hayPrefijo = true;
            }
            if (cmp < 0) {
                reco = reco.getIzquierdo();
            } else {
                reco = reco.getDerecho();
            }
        }
        return hayPrefijo ? 1 : 0;
    }

    private String textoDe(T dato) {
        return (dato instanceof String) ? (String) dato : String.valueOf(dato);
    }

    public ListaSimple<T> ListaOrdenada() {
        ListaSimple<T> lista = new ListaSimple<>();
        llenarInOrden(raiz, lista);
        return lista;
    }

    private void llenarInOrden(NodoArbol<T> reco, ListaSimple<T> lista) {
        if (reco != null) {
            llenarInOrden(reco.getIzquierdo(), lista);
            lista.InsertarAlFinal(reco.getDato());
            llenarInOrden(reco.getDerecho(), lista);
        }
    }
}
