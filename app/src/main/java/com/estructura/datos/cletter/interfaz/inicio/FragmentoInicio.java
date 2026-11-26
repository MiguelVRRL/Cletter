package com.estructura.datos.cletter.interfaz.inicio;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.estructura.datos.cletter.R;
import com.estructura.datos.cletter.datos.AlmacenamientoJuego;
import com.estructura.datos.cletter.datos.CatalogoPisos;
import com.estructura.datos.cletter.datos.GestorPortadas;
import com.estructura.datos.cletter.interfaz.juego.ActividadJuego;
import com.estructura.datos.cletter.modelo.PerfilJugador;
import com.estructura.datos.cletter.modelo.PisoInfo;

/**
 * Pantalla de Inicio de Cletter: exhibe en un marco editorial la obra/hito del piso
 * actual, el progreso X / 10 dentro del piso y el botón principal para jugar el nivel.
 */
public class FragmentoInicio extends Fragment {

    private AlmacenamientoJuego almacenamiento;
    private TextView txtPisoCabecera;
    private TextView txtMonedas;
    private ImageView imgPortada;
    private TextView txtTituloPiso;
    private TextView txtSubtituloPiso;
    private ProgressBar barraProgresoPiso;
    private TextView txtProgresoPiso;
    private TextView txtAyudaPiso;
    private TextView btnJugarNivel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmento_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        almacenamiento = new AlmacenamientoJuego(requireContext());

        txtPisoCabecera = view.findViewById(R.id.txt_piso_cabecera);
        txtMonedas = view.findViewById(R.id.txt_monedas_inicio);
        imgPortada = view.findViewById(R.id.img_portada_inicio);
        txtTituloPiso = view.findViewById(R.id.txt_titulo_piso_inicio);
        txtSubtituloPiso = view.findViewById(R.id.txt_subtitulo_piso_inicio);
        barraProgresoPiso = view.findViewById(R.id.barra_progreso_piso);
        txtProgresoPiso = view.findViewById(R.id.txt_progreso_piso);
        txtAyudaPiso = view.findViewById(R.id.txt_ayuda_piso);
        btnJugarNivel = view.findViewById(R.id.btn_jugar_nivel);

        btnJugarNivel.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ActividadJuego.class);
            intent.putExtra(ActividadJuego.EXTRA_NIVEL, almacenamiento.obtenerNivelActual());
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        actualizarEstadoInicio();
    }

    private void actualizarEstadoInicio() {
        if (almacenamiento == null || !isAdded()) {
            return;
        }
        PerfilJugador perfil = almacenamiento.obtenerPerfil();
        int nivelesCompletados = almacenamiento.obtenerNivelesCompletados();
        int nivelActual = perfil.getNivelActual();
        boolean esIngles = almacenamiento.esIngles();

        PisoInfo pisoActual = CatalogoPisos.obtenerPorNivel(nivelActual);
        int inicioPiso = pisoActual.getNivelInicio();
        int completadosEnPiso = Math.max(0, Math.min(CatalogoPisos.NIVELES_POR_PISO, nivelesCompletados - (inicioPiso - 1)));

        String textoArea = obtenerTextoArea(pisoActual.getArea());
        txtPisoCabecera.setText(getString(R.string.etiqueta_piso_completo, pisoActual.getNumero(), textoArea));
        txtMonedas.setText(String.valueOf(perfil.getMonedas()));

        GestorPortadas.aplicarPortada(imgPortada, pisoActual, false, esIngles);
        txtTituloPiso.setText(pisoActual.getTitulo(esIngles));
        txtSubtituloPiso.setText(pisoActual.getSubtitulo(esIngles) + " · " + pisoActual.getAnio());

        barraProgresoPiso.setMax(CatalogoPisos.NIVELES_POR_PISO);
        barraProgresoPiso.setProgress(completadosEnPiso);
        txtProgresoPiso.setText(getString(R.string.progreso_piso, completadosEnPiso));

        if (nivelesCompletados >= CatalogoPisos.TOTAL_NIVELES) {
            txtAyudaPiso.setText(R.string.estado_todos_completados);
            btnJugarNivel.setText(getString(R.string.boton_nivel, CatalogoPisos.TOTAL_NIVELES));
        } else {
            txtAyudaPiso.setText(R.string.subtitulo_coleccion_actual);
            btnJugarNivel.setText(getString(R.string.boton_nivel, nivelActual));
        }
    }

    private String obtenerTextoArea(int area) {
        if (area == PisoInfo.AREA_CS) {
            return getString(R.string.area_cs);
        } else if (area == PisoInfo.AREA_VIDEOJUEGOS) {
            return getString(R.string.area_videojuegos);
        }
        return getString(R.string.area_mixto);
    }
}
