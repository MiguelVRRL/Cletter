package com.estructura.datos.cletter.datos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.LruCache;
import android.widget.ImageView;

import com.estructura.datos.cletter.modelo.PisoInfo;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Carga la portada de cada piso buscando automáticamente el archivo
 * app/src/main/res/drawable-nodpi/floor_XX.webp (o .png).
 * Mientras no exista la imagen definitiva, renderiza una portada editorial de respaldo
 * cacheada en memoria (LruCache + RGB_565) para evitar recrear bitmaps al navegar o hacer scroll.
 */
public final class GestorPortadas {

    // Caché en memoria (hasta 6 MB en KB) de portadas generadas
    private static final LruCache<String, Bitmap> CACHE_PORTADAS = new LruCache<String, Bitmap>(6 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return Math.max(1, value.getByteCount() / 1024);
        }
    };

    // Caché de IDs de recursos para evitar llamadas repetitivas a Resources.getIdentifier()
    private static final Map<String, Integer> CACHE_RECURSOS = new HashMap<>(32);

    private GestorPortadas() {
    }

    public static void aplicarPortada(ImageView vistaImagen, PisoInfo piso, boolean bloqueado, boolean esIngles) {
        aplicarPortada(vistaImagen, piso, bloqueado, esIngles, false);
    }

    public static void aplicarPortadaMiniatura(ImageView vistaImagen, PisoInfo piso, boolean bloqueado, boolean esIngles) {
        aplicarPortada(vistaImagen, piso, bloqueado, esIngles, true);
    }

    private static void aplicarPortada(
            ImageView vistaImagen,
            PisoInfo piso,
            boolean bloqueado,
            boolean esIngles,
            boolean miniatura
    ) {
        Context contexto = vistaImagen.getContext();
        String nombreRecurso = piso.getNombreRecursoPortada();

        int idRecurso = obtenerIdRecursoCacheado(contexto, nombreRecurso);
        if (idRecurso != 0) {
            vistaImagen.setImageResource(idRecurso);
            vistaImagen.setAlpha(bloqueado ? 0.35f : 1.0f);
            return;
        }

        int ancho = miniatura ? 240 : 320;
        int alto = miniatura ? 300 : 400;
        String clave = piso.getNumero() + "_" + (bloqueado ? "B" : "D") + "_" + (esIngles ? "EN" : "ES") + "_" + ancho;

        Bitmap respaldo = CACHE_PORTADAS.get(clave);
        if (respaldo == null || respaldo.isRecycled()) {
            respaldo = crearPortadaEditorial(piso, bloqueado, esIngles, ancho, alto);
            CACHE_PORTADAS.put(clave, respaldo);
        }
        vistaImagen.setImageBitmap(respaldo);
        vistaImagen.setAlpha(1.0f);
    }

    private static int obtenerIdRecursoCacheado(Context contexto, String nombreRecurso) {
        Integer cacheado = CACHE_RECURSOS.get(nombreRecurso);
        if (cacheado != null) {
            return cacheado;
        }
        int id = contexto.getResources().getIdentifier(nombreRecurso, "drawable", contexto.getPackageName());
        CACHE_RECURSOS.put(nombreRecurso, id);
        return id;
    }

    private static Bitmap crearPortadaEditorial(PisoInfo piso, boolean bloqueado, boolean esIngles, int ancho, int alto) {
        // RGB_565 consume la mitad de RAM que ARGB_8888 al ser una portada opaca
        Bitmap bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.RGB_565);
        Canvas lienzo = new Canvas(bitmap);
        float escala = ancho / 480f;

        int colorInicio = bloqueado ? 0xFF3E3538 : piso.getColorPrimarioFondo();
        int colorFin = bloqueado ? 0xFF63575A : piso.getColorSecundarioFondo();

        Paint pincelFondo = new Paint(Paint.ANTI_ALIAS_FLAG);
        pincelFondo.setShader(new LinearGradient(
                0, 0, ancho, alto,
                colorInicio, colorFin,
                Shader.TileMode.CLAMP
        ));
        lienzo.drawRect(0, 0, ancho, alto, pincelFondo);

        Paint pincelLinea = new Paint(Paint.ANTI_ALIAS_FLAG);
        pincelLinea.setStyle(Paint.Style.STROKE);
        pincelLinea.setStrokeWidth(2.5f * escala);
        pincelLinea.setColor(0x38FFFDFB);

        float margen = 28f * escala;
        RectF marcoInterior = new RectF(margen, margen, ancho - margen, alto - margen);
        lienzo.drawRoundRect(marcoInterior, 24f * escala, 24f * escala, pincelLinea);

        float cx = ancho / 2f;
        float cy = alto * 0.46f;
        dibujarHexagono(lienzo, cx, cy, 128f * escala, pincelLinea);
        dibujarHexagono(lienzo, cx, cy, 94f * escala, pincelLinea);

        Paint pincelCirculo = new Paint(Paint.ANTI_ALIAS_FLAG);
        pincelCirculo.setColor(0x22FFFDFB);
        lienzo.drawCircle(cx, cy, 62f * escala, pincelCirculo);

        Paint pincelNumero = new Paint(Paint.ANTI_ALIAS_FLAG);
        pincelNumero.setColor(0xFFFFFDFB);
        pincelNumero.setTextAlign(Paint.Align.CENTER);
        pincelNumero.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        pincelNumero.setTextSize(66f * escala);
        String numTexto = String.format(Locale.US, "%02d", piso.getNumero());
        Paint.FontMetrics fm = pincelNumero.getFontMetrics();
        float yTexto = cy - (fm.ascent + fm.descent) / 2f;
        lienzo.drawText(numTexto, cx, yTexto, pincelNumero);

        Paint pincelEtiqueta = new Paint(Paint.ANTI_ALIAS_FLAG);
        pincelEtiqueta.setColor(0xDDFFFDFB);
        pincelEtiqueta.setTextAlign(Paint.Align.CENTER);
        pincelEtiqueta.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        pincelEtiqueta.setTextSize(22f * escala);
        String prefijo = esIngles ? "FLOOR " : "PISO ";
        lienzo.drawText(prefijo + numTexto + " · " + piso.getAnio(), cx, 88f * escala, pincelEtiqueta);

        Paint pincelArea = new Paint(Paint.ANTI_ALIAS_FLAG);
        pincelArea.setColor(0xEEFFFDFB);
        pincelArea.setTextAlign(Paint.Align.CENTER);
        pincelArea.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        pincelArea.setTextSize(24f * escala);
        String areaTexto;
        if (piso.getArea() == PisoInfo.AREA_CS) {
            areaTexto = esIngles ? "COMPUTER SCIENCE" : "COMPUTACIÓN";
        } else if (piso.getArea() == PisoInfo.AREA_VIDEOJUEGOS) {
            areaTexto = esIngles ? "VIDEO GAMES" : "VIDEOJUEGOS";
        } else {
            areaTexto = esIngles ? "CS & GAMES" : "CS Y JUEGOS";
        }
        lienzo.drawText(areaTexto, cx, alto - (70f * escala), pincelArea);

        return bitmap;
    }

    private static void dibujarHexagono(Canvas lienzo, float cx, float cy, float radio, Paint pincel) {
        Path ruta = new Path();
        for (int i = 0; i < 6; i++) {
            double angulo = Math.toRadians(60 * i - 30);
            float x = (float) (cx + radio * Math.cos(angulo));
            float y = (float) (cy + radio * Math.sin(angulo));
            if (i == 0) {
                ruta.moveTo(x, y);
            } else {
                ruta.lineTo(x, y);
            }
        }
        ruta.close();
        lienzo.drawPath(ruta, pincel);
    }
}
