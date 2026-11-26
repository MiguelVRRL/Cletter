package com.estructura.datos.cletter.interfaz.perfil;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.estructura.datos.cletter.R;
import com.estructura.datos.cletter.datos.AlmacenamientoJuego;
import com.estructura.datos.cletter.datos.CatalogoLogros;
import com.estructura.datos.cletter.datos.CatalogoPisos;
import com.estructura.datos.cletter.estructuras.lista.ListaSimple;
import com.estructura.datos.cletter.modelo.Logro;
import com.estructura.datos.cletter.modelo.PerfilJugador;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/**
 * Pestaña Perfil de Cletter: banner superior, avatar editable con emblemas CS/Gaming,
 * fecha de registro, rejilla 3x3 de estadísticas y visor de logros locales.
 */
public class FragmentoPerfil extends Fragment {

    private static final String[] EMBLEMAS_AVATAR = new String[]{
            "{ }", "01", "λ", "⌘", "<>", "★", "⚡", "♦"
    };

    private AlmacenamientoJuego almacenamiento;
    private TextView txtNombre;
    private TextView txtFecha;
    private TextView txtEmblemaAvatar;
    private TextView txtStatMasNivelesDia;
    private TextView txtStatTotalDias;
    private TextView txtStatPisos;
    private TextView txtStatPalabras;
    private TextView txtStatPalabraLarga;
    private TextView txtStatMejorPalabra;
    private TextView txtStatNivel;
    private TextView txtStatPuntos;
    private TextView txtStatLogros;
    private TextView txtResumenLogros;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmento_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        almacenamiento = new AlmacenamientoJuego(requireContext());

        txtNombre = view.findViewById(R.id.txt_nombre_perfil);
        txtFecha = view.findViewById(R.id.txt_fecha_perfil);
        txtEmblemaAvatar = view.findViewById(R.id.txt_emblema_avatar);
        txtStatMasNivelesDia = view.findViewById(R.id.txt_stat_mas_niveles_dia);
        txtStatTotalDias = view.findViewById(R.id.txt_stat_total_dias);
        txtStatPisos = view.findViewById(R.id.txt_stat_pisos);
        txtStatPalabras = view.findViewById(R.id.txt_stat_palabras);
        txtStatPalabraLarga = view.findViewById(R.id.txt_stat_palabra_larga);
        txtStatMejorPalabra = view.findViewById(R.id.txt_stat_mejor_palabra);
        txtStatNivel = view.findViewById(R.id.txt_stat_nivel);
        txtStatPuntos = view.findViewById(R.id.txt_stat_puntos);
        txtStatLogros = view.findViewById(R.id.txt_stat_logros);
        txtResumenLogros = view.findViewById(R.id.txt_resumen_logros);

        view.findViewById(R.id.btn_editar_perfil).setOnClickListener(v -> mostrarDialogoEditarPerfil());
        view.findViewById(R.id.tarjeta_logros_perfil).setOnClickListener(v -> mostrarDialogoLogros());
    }

    @Override
    public void onResume() {
        super.onResume();
        actualizarPerfil();
    }

    private void actualizarPerfil() {
        if (almacenamiento == null || !isAdded()) {
            return;
        }
        PerfilJugador perfil = almacenamiento.obtenerPerfil();
        int idxAvatar = Math.max(0, Math.min(EMBLEMAS_AVATAR.length - 1, perfil.getIndiceAvatar()));

        txtNombre.setText(perfil.getNombre());
        txtFecha.setText(perfil.getFechaRegistro());
        txtEmblemaAvatar.setText(EMBLEMAS_AVATAR[idxAvatar]);

        txtStatMasNivelesDia.setText(String.valueOf(perfil.getMasNivelesDia()));
        txtStatTotalDias.setText(String.valueOf(perfil.getTotalDias()));
        txtStatPisos.setText(String.format(Locale.US, "%d/%d", perfil.getPisosConquistados(), CatalogoPisos.TOTAL_PISOS));
        txtStatPalabras.setText(String.valueOf(perfil.getTotalPalabras()));
        txtStatPalabraLarga.setText(String.valueOf(perfil.getPalabraMasLarga()));
        txtStatMejorPalabra.setText(String.valueOf(perfil.getMejorPalabraPuntos()));
        txtStatNivel.setText(String.valueOf(perfil.getNivelActual()));
        txtStatPuntos.setText(String.valueOf(perfil.getPuntosTotales()));
        txtStatLogros.setText(String.format(Locale.US, "%d/%d", perfil.getCantidadLogros(), CatalogoLogros.TOTAL_LOGROS));
        txtResumenLogros.setText(getString(R.string.subtitulo_logros, perfil.getCantidadLogros(), CatalogoLogros.TOTAL_LOGROS));
    }

    private void mostrarDialogoEditarPerfil() {
        PerfilJugador perfilActual = almacenamiento.obtenerPerfil();
        View vista = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_editar_perfil, null);

        EditText campoNombre = vista.findViewById(R.id.campo_nombre_jugador);
        GridLayout rejillaAvatares = vista.findViewById(R.id.rejilla_avatares);
        TextView btnCancelar = vista.findViewById(R.id.btn_cancelar_perfil);
        TextView btnGuardar = vista.findViewById(R.id.btn_guardar_perfil);

        campoNombre.setText(perfilActual.getNombre());
        campoNombre.setSelection(campoNombre.getText().length());

        final int[] avatarSeleccionado = new int[]{
                Math.max(0, Math.min(EMBLEMAS_AVATAR.length - 1, perfilActual.getIndiceAvatar()))
        };
        final MaterialCardView[] tarjetasAvatar = new MaterialCardView[EMBLEMAS_AVATAR.length];
        final TextView[] textosAvatar = new TextView[EMBLEMAS_AVATAR.length];

        int densidad = Math.round(getResources().getDisplayMetrics().density);
        for (int i = 0; i < EMBLEMAS_AVATAR.length; i++) {
            final int indice = i;
            MaterialCardView tarjeta = new MaterialCardView(requireContext());
            tarjeta.setRadius(16 * densidad);
            tarjeta.setCardElevation(0f);
            tarjeta.setStrokeWidth(Math.round(1.5f * densidad));
            tarjeta.setClickable(true);
            tarjeta.setFocusable(true);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 54 * densidad;
            params.columnSpec = GridLayout.spec(i % 4, 1f);
            params.rowSpec = GridLayout.spec(i / 4);
            params.setMargins(5 * densidad, 5 * densidad, 5 * densidad, 5 * densidad);
            tarjeta.setLayoutParams(params);

            TextView tv = new TextView(requireContext());
            tv.setText(EMBLEMAS_AVATAR[i]);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18f);
            tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            tarjeta.addView(tv, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            tarjetasAvatar[i] = tarjeta;
            textosAvatar[i] = tv;

            tarjeta.setOnClickListener(v -> {
                avatarSeleccionado[0] = indice;
                refrescarSeleccionAvatares(tarjetasAvatar, textosAvatar, indice);
            });
            rejillaAvatares.addView(tarjeta);
        }

        refrescarSeleccionAvatares(tarjetasAvatar, textosAvatar, avatarSeleccionado[0]);

        AlertDialog dialogo = crearDialogoTransparente(vista);
        btnCancelar.setOnClickListener(v -> dialogo.dismiss());
        btnGuardar.setOnClickListener(v -> {
            almacenamiento.actualizarPerfil(campoNombre.getText().toString(), avatarSeleccionado[0]);
            actualizarPerfil();
            dialogo.dismiss();
        });
        dialogo.show();
    }

    private void refrescarSeleccionAvatares(MaterialCardView[] tarjetas, TextView[] textos, int seleccionado) {
        int fondoActivo = ContextCompat.getColor(requireContext(), R.color.marca_primario);
        int textoActivo = ContextCompat.getColor(requireContext(), R.color.marca_sobre_primario);
        int bordeActivo = ContextCompat.getColor(requireContext(), R.color.marca_acento);

        int fondoInactivo = ContextCompat.getColor(requireContext(), R.color.fondo_suave);
        int textoInactivo = ContextCompat.getColor(requireContext(), R.color.texto_primario);
        int bordeInactivo = ContextCompat.getColor(requireContext(), R.color.borde_sutil);

        for (int i = 0; i < tarjetas.length; i++) {
            boolean activo = (i == seleccionado);
            tarjetas[i].setCardBackgroundColor(activo ? fondoActivo : fondoInactivo);
            tarjetas[i].setStrokeColor(activo ? bordeActivo : bordeInactivo);
            textos[i].setTextColor(activo ? textoActivo : textoInactivo);
        }
    }

    private void mostrarDialogoLogros() {
        View vista = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_logros, null);
        LinearLayout contenedor = vista.findViewById(R.id.contenedor_logros);
        TextView btnCerrar = vista.findViewById(R.id.btn_cerrar_logros);

        boolean esIngles = almacenamiento.esIngles();
        ListaSimple<Logro> logros = almacenamiento.obtenerTodosLosLogros();
        int densidad = Math.round(getResources().getDisplayMetrics().density);

        for (int i = 1; i <= logros.Cantidad(); i++) {
            Logro logro = logros.Obtener(i);
            if (logro == null) {
                continue;
            }
            LinearLayout fila = new LinearLayout(requireContext());
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.CENTER_VERTICAL);
            fila.setBackgroundResource(R.drawable.fondo_pildora_suave);
            fila.setPadding(14 * densidad, 12 * densidad, 14 * densidad, 12 * densidad);
            fila.setAlpha(logro.isDesbloqueado() ? 1.0f : 0.55f);

            LinearLayout.LayoutParams paramsFila = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            paramsFila.bottomMargin = 8 * densidad;
            fila.setLayoutParams(paramsFila);

            TextView badge = new TextView(requireContext());
            badge.setText(logro.isDesbloqueado() ? "★" : "○");
            badge.setTextSize(18f);
            badge.setTextColor(ContextCompat.getColor(
                    requireContext(),
                    logro.isDesbloqueado() ? R.color.marca_primario : R.color.texto_tenue
            ));
            badge.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams paramsBadge = new LinearLayout.LayoutParams(32 * densidad, 32 * densidad);
            paramsBadge.setMarginEnd(10 * densidad);
            badge.setLayoutParams(paramsBadge);

            LinearLayout columnaTexto = new LinearLayout(requireContext());
            columnaTexto.setOrientation(LinearLayout.VERTICAL);
            columnaTexto.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView titulo = new TextView(requireContext());
            titulo.setText(logro.getTitulo(esIngles));
            titulo.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_primario));
            titulo.setTextSize(14f);
            titulo.setTypeface(null, Typeface.BOLD);

            TextView desc = new TextView(requireContext());
            desc.setText(logro.getDescripcion(esIngles));
            desc.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_secundario));
            desc.setTextSize(12f);

            columnaTexto.addView(titulo);
            columnaTexto.addView(desc);

            fila.addView(badge);
            fila.addView(columnaTexto);
            contenedor.addView(fila);
        }

        AlertDialog dialogo = crearDialogoTransparente(vista);
        btnCerrar.setOnClickListener(v -> dialogo.dismiss());
        dialogo.show();
    }

    private AlertDialog dialogoActivo;

    private AlertDialog crearDialogoTransparente(View vista) {
        if (dialogoActivo != null && dialogoActivo.isShowing()) {
            dialogoActivo.dismiss();
        }
        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setView(vista)
                .setCancelable(true)
                .create();
        if (dialogo.getWindow() != null) {
            dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialogoActivo = dialogo;
        return dialogo;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
