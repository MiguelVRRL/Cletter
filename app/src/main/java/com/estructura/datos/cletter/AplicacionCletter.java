package com.estructura.datos.cletter;

import android.app.Application;

import com.estructura.datos.cletter.datos.AlmacenamientoJuego;

/**
 * Punto de entrada del proceso de Cletter.
 * Aplica el tema guardado una única vez al iniciar el proceso para evitar
 * recreaciones redundantes de actividades en frío.
 */
public class AplicacionCletter extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AlmacenamientoJuego almacenamiento = new AlmacenamientoJuego(this);
        almacenamiento.aplicarTemaGuardado();
    }
}
