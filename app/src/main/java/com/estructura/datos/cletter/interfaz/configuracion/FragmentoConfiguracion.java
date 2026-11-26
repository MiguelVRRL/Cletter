package com.estructura.datos.cletter.interfaz.configuracion;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.estructura.datos.cletter.ActividadPrincipal;
import com.estructura.datos.cletter.R;
import com.estructura.datos.cletter.datos.AlmacenamientoJuego;
import com.google.android.material.card.MaterialCardView;

/**
 * Pestaña Configuración minimalista: controles rápidos de Música/Sonido/Vibración,
 * selector de Idioma (ES/EN), Apariencia (Claro/Oscuro/Automático), Tabla de puntos,
 * Acerca de y Reiniciar progreso.
 */
public class FragmentoConfiguracion extends Fragment {

    private AlmacenamientoJuego almacenamiento;

    private MaterialCardView cardMusica;
    private MaterialCardView cardSonido;
    private MaterialCardView cardVibracion;
    private ImageView icoMusica;
    private ImageView icoSonido;
    private ImageView icoVibracion;
    private TextView txtValorIdioma;
    private TextView txtValorTema;
    private TextView txtDetalleTabla;
    private ImageView icoChevronPuntos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmento_configuracion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        almacenamiento = new AlmacenamientoJuego(requireContext());

        cardMusica = view.findViewById(R.id.card_musica);
        cardSonido = view.findViewById(R.id.card_sonido);
        cardVibracion = view.findViewById(R.id.card_vibracion);
        icoMusica = view.findViewById(R.id.ico_musica_cfg);
        icoSonido = view.findViewById(R.id.ico_sonido_cfg);
        icoVibracion = view.findViewById(R.id.ico_vibracion_cfg);
        txtValorIdioma = view.findViewById(R.id.txt_valor_idioma);
        txtValorTema = view.findViewById(R.id.txt_valor_tema);
        txtDetalleTabla = view.findViewById(R.id.txt_detalle_tabla_puntos);
        icoChevronPuntos = view.findViewById(R.id.ico_chevron_puntos);

        view.findViewById(R.id.btn_toggle_musica).setOnClickListener(v -> {
            almacenamiento.setMusicaActiva(!almacenamiento.estaMusicaActiva());
            actualizarToggles();
            if (getActivity() instanceof ActividadPrincipal) {
                ((ActividadPrincipal) getActivity()).sincronizarMusicaAmbiente();
            }
        });

        view.findViewById(R.id.btn_toggle_sonido).setOnClickListener(v -> {
            almacenamiento.setSonidoActivo(!almacenamiento.estaSonidoActivo());
            actualizarToggles();
        });

        view.findViewById(R.id.btn_toggle_vibracion).setOnClickListener(v -> {
            almacenamiento.setVibracionActiva(!almacenamiento.estaVibracionActiva());
            actualizarToggles();
        });

        view.findViewById(R.id.fila_idioma).setOnClickListener(v -> mostrarSelectorIdioma());
        view.findViewById(R.id.fila_tema).setOnClickListener(v -> mostrarSelectorTema());
        view.findViewById(R.id.fila_tabla_puntos).setOnClickListener(v -> alternarTablaPuntos());
        view.findViewById(R.id.fila_acerca).setOnClickListener(v -> mostrarAcercaDe());
        view.findViewById(R.id.fila_reiniciar).setOnClickListener(v -> mostrarConfirmacionReinicio());

        actualizarUI();
    }

    private void actualizarUI() {
        actualizarToggles();
        txtValorIdioma.setText(almacenamiento.esIngles() ? R.string.idioma_ingles : R.string.idioma_espanol);

        int tema = almacenamiento.obtenerTema();
        if (tema == AlmacenamientoJuego.TEMA_CLARO) {
            txtValorTema.setText(R.string.tema_claro);
        } else if (tema == AlmacenamientoJuego.TEMA_OSCURO) {
            txtValorTema.setText(R.string.tema_oscuro);
        } else {
            txtValorTema.setText(R.string.tema_sistema);
        }
    }

    private void actualizarToggles() {
        aplicarEstiloToggle(cardMusica, icoMusica, almacenamiento.estaMusicaActiva());
        aplicarEstiloToggle(cardSonido, icoSonido, almacenamiento.estaSonidoActivo());
        aplicarEstiloToggle(cardVibracion, icoVibracion, almacenamiento.estaVibracionActiva());
    }

    private void aplicarEstiloToggle(MaterialCardView tarjeta, ImageView icono, boolean activo) {
        int fondo = ContextCompat.getColor(requireContext(), activo ? R.color.marca_acento_suave : R.color.fondo_tarjeta);
        int borde = ContextCompat.getColor(requireContext(), activo ? R.color.marca_primario : R.color.borde_sutil);
        int tinteIcono = ContextCompat.getColor(requireContext(), activo ? R.color.marca_primario : R.color.texto_tenue);

        tarjeta.setCardBackgroundColor(fondo);
        tarjeta.setStrokeColor(borde);
        icono.setImageTintList(ColorStateList.valueOf(tinteIcono));
        icono.setAlpha(activo ? 1.0f : 0.45f);
    }

    private AlertDialog dialogoActivo;

    private void mostrarDialogoUnico(AlertDialog dialogo) {
        if (dialogoActivo != null && dialogoActivo.isShowing()) {
            dialogoActivo.dismiss();
        }
        dialogoActivo = dialogo;
        dialogoActivo.setOnDismissListener(d -> {
            if (dialogoActivo == d) {
                dialogoActivo = null;
            }
        });
        dialogoActivo.show();
    }

    private void mostrarSelectorIdioma() {
        String[] opciones = new String[]{
                getString(R.string.idioma_espanol),
                getString(R.string.idioma_ingles)
        };
        int actual = almacenamiento.esIngles() ? 1 : 0;
        mostrarDialogoUnico(new AlertDialog.Builder(requireContext())
                .setTitle(R.string.opcion_idioma)
                .setSingleChoiceItems(opciones, actual, (dialog, which) -> {
                    dialog.dismiss();
                    String nuevoCodigo = (which == 1) ? "en" : "es";
                    if (!nuevoCodigo.equals(almacenamiento.obtenerIdioma())) {
                        almacenamiento.guardarYAplicarIdioma(nuevoCodigo);
                    }
                })
                .setNegativeButton(R.string.cancelar, null)
                .create());
    }

    private void mostrarSelectorTema() {
        String[] opciones = new String[]{
                getString(R.string.tema_claro),
                getString(R.string.tema_oscuro),
                getString(R.string.tema_sistema)
        };
        int temaGuardado = almacenamiento.obtenerTema();
        int indiceActual;
        if (temaGuardado == AlmacenamientoJuego.TEMA_CLARO) {
            indiceActual = 0;
        } else if (temaGuardado == AlmacenamientoJuego.TEMA_OSCURO) {
            indiceActual = 1;
        } else {
            indiceActual = 2;
        }

        mostrarDialogoUnico(new AlertDialog.Builder(requireContext())
                .setTitle(R.string.opcion_tema)
                .setSingleChoiceItems(opciones, indiceActual, (dialog, which) -> {
                    dialog.dismiss();
                    int nuevoTema;
                    if (which == 0) {
                        nuevoTema = AlmacenamientoJuego.TEMA_CLARO;
                    } else if (which == 1) {
                        nuevoTema = AlmacenamientoJuego.TEMA_OSCURO;
                    } else {
                        nuevoTema = AlmacenamientoJuego.TEMA_SISTEMA;
                    }
                    if (nuevoTema != almacenamiento.obtenerTema()) {
                        almacenamiento.guardarYAplicarTema(nuevoTema);
                        actualizarUI();
                    }
                })
                .setNegativeButton(R.string.cancelar, null)
                .create());
    }

    private void alternarTablaPuntos() {
        boolean mostrar = txtDetalleTabla.getVisibility() != View.VISIBLE;
        txtDetalleTabla.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        icoChevronPuntos.animate().rotation(mostrar ? 180f : 0f).setDuration(180).start();
    }

    private void mostrarAcercaDe() {
        mostrarDialogoUnico(new AlertDialog.Builder(requireContext())
                .setTitle(R.string.opcion_acerca)
                .setMessage(R.string.acerca_texto)
                .setPositiveButton(R.string.cerrar, null)
                .create());
    }

    private void mostrarConfirmacionReinicio() {
        mostrarDialogoUnico(new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirmar_reinicio_titulo)
                .setMessage(R.string.confirmar_reinicio_mensaje)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.confirmar_reinicio_si, (dialog, which) -> {
                    almacenamiento.reiniciarProgreso();
                    Toast.makeText(requireContext(), R.string.progreso_reiniciado, Toast.LENGTH_SHORT).show();
                })
                .create());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (icoChevronPuntos != null) {
            icoChevronPuntos.animate().cancel();
        }
        if (dialogoActivo != null) {
            try {
                if (dialogoActivo.isShowing()) {
                    dialogoActivo.dismiss();
                }
            } catch (Throwable ignored) {
            }
            dialogoActivo = null;
        }
    }
}
