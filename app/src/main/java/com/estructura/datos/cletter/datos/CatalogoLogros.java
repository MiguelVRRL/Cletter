package com.estructura.datos.cletter.datos;

import com.estructura.datos.cletter.estructuras.lista.ListaSimple;
import com.estructura.datos.cletter.modelo.Logro;
import com.estructura.datos.cletter.modelo.PerfilJugador;

import java.util.Set;

/**
 * Catálogo de 15 logros locales que recompensan el dominio de palabras,
 * pisos conquistados y constancia.
 */
public final class CatalogoLogros {

    public static final int TOTAL_LOGROS = 15;

    private CatalogoLogros() {
    }

    private static class DefinicionLogro {
        final String id;
        final String tituloEs;
        final String tituloEn;
        final String descEs;
        final String descEn;

        DefinicionLogro(String id, String tituloEs, String tituloEn, String descEs, String descEn) {
            this.id = id;
            this.tituloEs = tituloEs;
            this.tituloEn = tituloEn;
            this.descEs = descEs;
            this.descEn = descEn;
        }
    }

    private static final DefinicionLogro[] DEFINICIONES = new DefinicionLogro[] {
            new DefinicionLogro("hola_mundo", "Hola Mundo", "Hello World",
                    "Supera tu primer nivel en Cletter.", "Clear your first level in Cletter."),
            new DefinicionLogro("piso_1", "Nota G de Ada", "Ada's Note G",
                    "Conquista el Piso 01 (Nivel 10).", "Conquer Floor 01 (Level 10)."),
            new DefinicionLogro("piso_5", "Ruta de Dijkstra", "Dijkstra's Path",
                    "Conquista 5 pisos (Nivel 50).", "Conquer 5 floors (Level 50)."),
            new DefinicionLogro("piso_10", "Maestro 8-Bits", "8-Bit Master",
                    "Conquista 10 pisos (Nivel 100).", "Conquer 10 floors (Level 100)."),
            new DefinicionLogro("piso_15", "Espíritu Open Source", "Open Source Spirit",
                    "Conquista 15 pisos (Nivel 150).", "Conquer 15 floors (Level 150)."),
            new DefinicionLogro("piso_20", "Arquitecto Cuántico", "Quantum Architect",
                    "Conquista los 20 pisos (Nivel 200).", "Conquer all 20 floors (Level 200)."),
            new DefinicionLogro("palabras_25", "Primer Compilado", "First Build",
                    "Forma 25 palabras válidas en total.", "Form 25 valid words in total."),
            new DefinicionLogro("palabras_150", "Léxico Binario", "Binary Lexicon",
                    "Forma 150 palabras válidas en total.", "Form 150 valid words in total."),
            new DefinicionLogro("palabras_500", "Enciclopedia Viva", "Living Encyclopedia",
                    "Forma 500 palabras válidas en total.", "Form 500 valid words in total."),
            new DefinicionLogro("palabra_6", "Cadena Larga", "Long String",
                    "Forma una palabra de al menos 6 letras.", "Form a word with at least 6 letters."),
            new DefinicionLogro("palabra_8", "Desbordamiento Léxico", "Lexical Overflow",
                    "Forma una palabra de al menos 8 letras.", "Form a word with at least 8 letters."),
            new DefinicionLogro("mejor_20", "Golpe Crítico", "Critical Hit",
                    "Consigue 20 o más puntos en una sola palabra.", "Score 20 or more points in a single word."),
            new DefinicionLogro("mejor_35", "Overclock", "Overclock",
                    "Consigue 35 o más puntos en una sola palabra.", "Score 35 or more points in a single word."),
            new DefinicionLogro("maraton_5", "Sesión Arcade", "Arcade Session",
                    "Completa 5 niveles en un mismo día.", "Complete 5 levels in a single day."),
            new DefinicionLogro("puntos_2500", "Alto Puntaje", "High Score",
                    "Acumula 2,500 puntos totales.", "Reach 2,500 total points.")
    };

    public static ListaSimple<Logro> construirLista(Set<String> desbloqueados) {
        ListaSimple<Logro> lista = new ListaSimple<>();
        for (DefinicionLogro def : DEFINICIONES) {
            lista.InsertarAlFinal(new Logro(
                    def.id,
                    def.tituloEs,
                    def.tituloEn,
                    def.descEs,
                    def.descEn,
                    desbloqueados.contains(def.id)
            ));
        }
        return lista;
    }

    public static Logro buscarPorId(String id, boolean desbloqueado) {
        for (DefinicionLogro def : DEFINICIONES) {
            if (def.id.equals(id)) {
                return new Logro(def.id, def.tituloEs, def.tituloEn, def.descEs, def.descEn, desbloqueado);
            }
        }
        return null;
    }

    public static ListaSimple<String> evaluarNuevosLogros(PerfilJugador perfil, Set<String> yaDesbloqueados) {
        ListaSimple<String> nuevos = new ListaSimple<>();
        verificar(nuevos, yaDesbloqueados, "hola_mundo", perfil.getNivelActual() >= 2);
        verificar(nuevos, yaDesbloqueados, "piso_1", perfil.getPisosConquistados() >= 1);
        verificar(nuevos, yaDesbloqueados, "piso_5", perfil.getPisosConquistados() >= 5);
        verificar(nuevos, yaDesbloqueados, "piso_10", perfil.getPisosConquistados() >= 10);
        verificar(nuevos, yaDesbloqueados, "piso_15", perfil.getPisosConquistados() >= 15);
        verificar(nuevos, yaDesbloqueados, "piso_20", perfil.getPisosConquistados() >= 20);
        verificar(nuevos, yaDesbloqueados, "palabras_25", perfil.getTotalPalabras() >= 25);
        verificar(nuevos, yaDesbloqueados, "palabras_150", perfil.getTotalPalabras() >= 150);
        verificar(nuevos, yaDesbloqueados, "palabras_500", perfil.getTotalPalabras() >= 500);
        verificar(nuevos, yaDesbloqueados, "palabra_6", perfil.getPalabraMasLarga() >= 6);
        verificar(nuevos, yaDesbloqueados, "palabra_8", perfil.getPalabraMasLarga() >= 8);
        verificar(nuevos, yaDesbloqueados, "mejor_20", perfil.getMejorPalabraPuntos() >= 20);
        verificar(nuevos, yaDesbloqueados, "mejor_35", perfil.getMejorPalabraPuntos() >= 35);
        verificar(nuevos, yaDesbloqueados, "maraton_5", perfil.getMasNivelesDia() >= 5);
        verificar(nuevos, yaDesbloqueados, "puntos_2500", perfil.getPuntosTotales() >= 2500);
        return nuevos;
    }

    private static void verificar(ListaSimple<String> nuevos, Set<String> actuales, String id, boolean condicion) {
        if (condicion && !actuales.contains(id)) {
            nuevos.InsertarAlFinal(id);
        }
    }
}
