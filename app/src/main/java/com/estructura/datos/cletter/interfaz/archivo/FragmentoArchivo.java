package com.estructura.datos.cletter.interfaz.archivo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.estructura.datos.cletter.R;
import com.estructura.datos.cletter.datos.AlmacenamientoJuego;
import com.estructura.datos.cletter.datos.CatalogoPisos;
import com.estructura.datos.cletter.modelo.PisoInfo;

/**
 * Pestaña Archivo (Museo): muestra los 20 pisos temáticos de Videojuegos y
 * Ciencias de la Computación en una cuadrícula editorial de 2 columnas.
 */
public class FragmentoArchivo extends Fragment {

    private AlmacenamientoJuego almacenamiento;
    private TextView txtSubtitulo;
    private AdaptadorPisos adaptador;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmento_archivo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        almacenamiento = new AlmacenamientoJuego(requireContext());

        txtSubtitulo = view.findViewById(R.id.txt_subtitulo_archivo);
        RecyclerView listaPisos = view.findViewById(R.id.lista_pisos);
        listaPisos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        listaPisos.setHasFixedSize(true);
        listaPisos.setItemViewCacheSize(20);

        adaptador = new AdaptadorPisos(
                CatalogoPisos.obtenerTodos(),
                almacenamiento.obtenerNivelesCompletados(),
                almacenamiento.esIngles(),
                this::manejarClickPiso
        );
        listaPisos.setAdapter(adaptador);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (almacenamiento == null || !isAdded()) {
            return;
        }
        int nivelesCompletados = almacenamiento.obtenerNivelesCompletados();
        int pisosConquistados = Math.min(CatalogoPisos.TOTAL_PISOS,
                nivelesCompletados / CatalogoPisos.NIVELES_POR_PISO);
        txtSubtitulo.setText(getString(R.string.subtitulo_archivo, pisosConquistados));
        if (adaptador != null) {
            adaptador.actualizarEstado(nivelesCompletados, almacenamiento.esIngles());
        }
    }

    private void manejarClickPiso(PisoInfo piso, boolean conquistado) {
        if (conquistado) {
            Intent intent = new Intent(requireContext(), ActividadDetallePiso.class);
            intent.putExtra(ActividadDetallePiso.EXTRA_NUMERO_PISO, piso.getNumero());
            startActivity(intent);
        } else {
            Toast.makeText(
                    requireContext(),
                    getString(R.string.aviso_piso_bloqueado, piso.getNumero(), piso.getNivelFin()),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
