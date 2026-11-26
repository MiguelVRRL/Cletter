package com.estructura.datos.cletter.interfaz.archivo;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.estructura.datos.cletter.R;
import com.estructura.datos.cletter.datos.AlmacenamientoJuego;
import com.estructura.datos.cletter.datos.CatalogoPisos;
import com.estructura.datos.cletter.datos.GestorPortadas;
import com.estructura.datos.cletter.modelo.PisoInfo;

/**
 * Ficha editorial de un piso conquistado: muestra la portada enmarcada 4:5,
 * el año con separador ornamental, la historia del hito y el vocabulario clave.
 */
public class ActividadDetallePiso extends AppCompatActivity {

    public static final String EXTRA_NUMERO_PISO = "extra_numero_piso";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_detalle_piso);

        AlmacenamientoJuego almacenamiento = new AlmacenamientoJuego(this);
        boolean esIngles = almacenamiento.esIngles();

        int numeroPiso = getIntent().getIntExtra(EXTRA_NUMERO_PISO, 1);
        PisoInfo piso = CatalogoPisos.obtenerPorNumero(numeroPiso);

        findViewById(R.id.btn_volver_detalle).setOnClickListener(v -> finish());

        TextView txtBadge = findViewById(R.id.txt_badge_piso_detalle);
        TextView txtTitulo = findViewById(R.id.txt_titulo_detalle);
        TextView txtSubtitulo = findViewById(R.id.txt_subtitulo_detalle);
        ImageView imgPortada = findViewById(R.id.img_portada_detalle);
        TextView txtAnio = findViewById(R.id.txt_anio_detalle);
        TextView txtHistoria = findViewById(R.id.txt_historia_detalle);
        TextView txtNiveles = findViewById(R.id.txt_niveles_detalle);
        TextView txtVocabulario = findViewById(R.id.txt_vocabulario_detalle);

        String areaTexto;
        if (piso.getArea() == PisoInfo.AREA_CS) {
            areaTexto = getString(R.string.area_cs);
        } else if (piso.getArea() == PisoInfo.AREA_VIDEOJUEGOS) {
            areaTexto = getString(R.string.area_videojuegos);
        } else {
            areaTexto = getString(R.string.area_mixto);
        }

        txtBadge.setText(getString(R.string.etiqueta_piso_completo, piso.getNumero(), areaTexto));
        txtTitulo.setText(piso.getTitulo(esIngles));
        txtSubtitulo.setText(piso.getSubtitulo(esIngles));
        GestorPortadas.aplicarPortada(imgPortada, piso, false, esIngles);
        txtAnio.setText(String.format("✦ %s ✦", piso.getAnio()));
        txtHistoria.setText(piso.getHistoria(esIngles));
        txtNiveles.setText(getString(R.string.niveles_del_piso, piso.getNivelInicio(), piso.getNivelFin()));

        String[] claves = piso.getPalabrasClave(esIngles);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < claves.length; i++) {
            if (i > 0) {
                sb.append(" · ");
            }
            sb.append(claves[i]);
        }
        txtVocabulario.setText(sb.toString());
    }
}
