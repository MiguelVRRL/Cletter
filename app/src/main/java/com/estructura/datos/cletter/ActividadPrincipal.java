package com.estructura.datos.cletter;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.estructura.datos.cletter.datos.AlmacenamientoJuego;
import com.estructura.datos.cletter.datos.GestorDiccionario;
import com.estructura.datos.cletter.datos.GestorFeedback;
import com.estructura.datos.cletter.interfaz.archivo.FragmentoArchivo;
import com.estructura.datos.cletter.interfaz.configuracion.FragmentoConfiguracion;
import com.estructura.datos.cletter.interfaz.inicio.FragmentoInicio;
import com.estructura.datos.cletter.interfaz.perfil.FragmentoPerfil;

/**
 * Actividad contenedora principal de Cletter con barra inferior de 4 pestañas
 * e indicador hexagonal activo: Perfil, Inicio (por defecto), Archivo y Ajustes.
 */
public class ActividadPrincipal extends AppCompatActivity {

    public static final int TAB_PERFIL = 0;
    public static final int TAB_INICIO = 1;
    public static final int TAB_ARCHIVO = 2;
    public static final int TAB_CONFIGURACION = 3;

    private static final String ESTADO_TAB_ACTIVA = "estado_tab_activa";

    private AlmacenamientoJuego almacenamiento;
    private GestorFeedback feedback;
    private int tabActual = TAB_INICIO;

    private ImageView[] hexagonosTab;
    private ImageView[] iconosTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        almacenamiento = new AlmacenamientoJuego(this);
        feedback = GestorFeedback.obtenerInstancia(this);
        setContentView(R.layout.actividad_principal);

        hexagonosTab = new ImageView[]{
                findViewById(R.id.hex_tab_perfil),
                findViewById(R.id.hex_tab_inicio),
                findViewById(R.id.hex_tab_archivo),
                findViewById(R.id.hex_tab_configuracion)
        };

        iconosTab = new ImageView[]{
                findViewById(R.id.ico_tab_perfil),
                findViewById(R.id.ico_tab_inicio),
                findViewById(R.id.ico_tab_archivo),
                findViewById(R.id.ico_tab_configuracion)
        };

        findViewById(R.id.tab_perfil).setOnClickListener(v -> seleccionarTab(TAB_PERFIL, false));
        findViewById(R.id.tab_inicio).setOnClickListener(v -> seleccionarTab(TAB_INICIO, false));
        findViewById(R.id.tab_archivo).setOnClickListener(v -> seleccionarTab(TAB_ARCHIVO, false));
        findViewById(R.id.tab_configuracion).setOnClickListener(v -> seleccionarTab(TAB_CONFIGURACION, false));

        if (savedInstanceState != null) {
            tabActual = savedInstanceState.getInt(ESTADO_TAB_ACTIVA, TAB_INICIO);
            actualizarEstilosBarra(tabActual);
        } else {
            seleccionarTab(TAB_INICIO, true);
        }

        // Precargar diccionario del idioma activo después del primer frame para abrir niveles al instante
        getWindow().getDecorView().post(() ->
                GestorDiccionario.obtenerInstancia().precargarAsync(
                        getApplicationContext(),
                        almacenamiento.esIngles()
                )
        );
    }

    public void seleccionarTab(int indiceTab, boolean forzar) {
        if (!forzar && indiceTab == tabActual && getSupportFragmentManager().findFragmentById(R.id.contenedor_fragmento) != null) {
            return;
        }
        tabActual = indiceTab;
        actualizarEstilosBarra(indiceTab);

        Fragment destino;
        switch (indiceTab) {
            case TAB_PERFIL:
                destino = new FragmentoPerfil();
                break;
            case TAB_ARCHIVO:
                destino = new FragmentoArchivo();
                break;
            case TAB_CONFIGURACION:
                destino = new FragmentoConfiguracion();
                break;
            case TAB_INICIO:
            default:
                destino = new FragmentoInicio();
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedor_fragmento, destino)
                .commit();
    }

    private void actualizarEstilosBarra(int indiceActivo) {
        int colorActivo = ContextCompat.getColor(this, R.color.nav_icono_activo);
        int colorInactivo = ContextCompat.getColor(this, R.color.nav_icono_inactivo);

        for (int i = 0; i < hexagonosTab.length; i++) {
            boolean activo = (i == indiceActivo);
            ImageView hex = hexagonosTab[i];
            ImageView ico = iconosTab[i];

            if (activo) {
                hex.setVisibility(View.VISIBLE);
                hex.setScaleX(0.82f);
                hex.setScaleY(0.82f);
                hex.animate().scaleX(1f).scaleY(1f).setDuration(180).start();
                ico.setImageTintList(ColorStateList.valueOf(colorActivo));
            } else {
                hex.setVisibility(View.INVISIBLE);
                ico.setImageTintList(ColorStateList.valueOf(colorInactivo));
            }
        }
    }

    public void sincronizarMusicaAmbiente() {
        if (feedback == null || almacenamiento == null) {
            return;
        }
        if (almacenamiento.estaMusicaActiva()) {
            feedback.iniciarMusicaAmbiente();
        } else {
            feedback.detenerMusicaAmbiente();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ESTADO_TAB_ACTIVA, tabActual);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sincronizarMusicaAmbiente();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (feedback != null) {
            feedback.detenerMusicaAmbiente();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hexagonosTab != null) {
            for (ImageView hex : hexagonosTab) {
                if (hex != null) {
                    hex.animate().cancel();
                }
            }
        }
    }
}
