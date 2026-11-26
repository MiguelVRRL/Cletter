package com.estructura.datos.cletter.interfaz.archivo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.estructura.datos.cletter.R;
import com.estructura.datos.cletter.datos.CatalogoPisos;
import com.estructura.datos.cletter.datos.GestorPortadas;
import com.estructura.datos.cletter.modelo.PisoInfo;

/**
 * Adaptador de la cuadrícula de 20 pisos en el Archivo de Cletter.
 */
public class AdaptadorPisos extends RecyclerView.Adapter<AdaptadorPisos.PisoViewHolder> {

    public interface EscuchaPiso {
        void alTocarPiso(PisoInfo piso, boolean conquistado);
    }

    private final PisoInfo[] pisos;
    private int nivelesCompletados;
    private boolean esIngles;
    private final EscuchaPiso escucha;

    public AdaptadorPisos(PisoInfo[] pisos, int nivelesCompletados, boolean esIngles, EscuchaPiso escucha) {
        this.pisos = pisos;
        this.nivelesCompletados = nivelesCompletados;
        this.esIngles = esIngles;
        this.escucha = escucha;
    }

    public void actualizarEstado(int nuevosNivelesCompletados, boolean nuevoEsIngles) {
        if (this.nivelesCompletados == nuevosNivelesCompletados && this.esIngles == nuevoEsIngles) {
            return;
        }
        this.nivelesCompletados = nuevosNivelesCompletados;
        this.esIngles = nuevoEsIngles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PisoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_piso, parent, false);
        return new PisoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull PisoViewHolder holder, int position) {
        PisoInfo piso = pisos[position];
        Context contexto = holder.itemView.getContext();

        boolean conquistado = nivelesCompletados >= piso.getNivelFin();
        boolean enProgreso = !conquistado && nivelesCompletados >= (piso.getNivelInicio() - 1);
        boolean bloqueado = !conquistado && !enProgreso;

        GestorPortadas.aplicarPortadaMiniatura(holder.imgPortada, piso, bloqueado, esIngles);
        holder.capaBloqueado.setVisibility(bloqueado ? View.VISIBLE : View.GONE);
        holder.txtCodigo.setText(contexto.getString(R.string.etiqueta_piso, piso.getNumero()));
        holder.txtTitulo.setText(piso.getTitulo(esIngles));

        if (conquistado) {
            holder.txtEstado.setText(R.string.piso_conquistado_badge);
            holder.txtEstado.setTextColor(ContextCompat.getColor(contexto, R.color.estado_exito));
        } else if (enProgreso) {
            int avance = Math.max(0, Math.min(CatalogoPisos.NIVELES_POR_PISO,
                    nivelesCompletados - (piso.getNivelInicio() - 1)));
            holder.txtEstado.setText(contexto.getString(R.string.piso_en_progreso, avance));
            holder.txtEstado.setTextColor(ContextCompat.getColor(contexto, R.color.marca_primario));
        } else {
            holder.txtEstado.setText(R.string.piso_bloqueado);
            holder.txtEstado.setTextColor(ContextCompat.getColor(contexto, R.color.texto_tenue));
        }

        holder.itemView.setOnClickListener(v -> {
            if (escucha != null) {
                escucha.alTocarPiso(piso, conquistado);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pisos.length;
    }

    static class PisoViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgPortada;
        final View capaBloqueado;
        final TextView txtCodigo;
        final TextView txtTitulo;
        final TextView txtEstado;

        PisoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPortada = itemView.findViewById(R.id.img_portada_item);
            capaBloqueado = itemView.findViewById(R.id.capa_bloqueado);
            txtCodigo = itemView.findViewById(R.id.txt_codigo_piso_item);
            txtTitulo = itemView.findViewById(R.id.txt_titulo_piso_item);
            txtEstado = itemView.findViewById(R.id.txt_estado_piso_item);
        }
    }
}
