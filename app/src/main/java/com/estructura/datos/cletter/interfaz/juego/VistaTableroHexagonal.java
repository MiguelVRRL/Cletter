package com.estructura.datos.cletter.interfaz.juego;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.estructura.datos.cletter.R;
import com.estructura.datos.cletter.datos.GeneradorTablero;
import com.estructura.datos.cletter.estructuras.grafo.Grafo;
import com.estructura.datos.cletter.estructuras.pila.Pila;
import com.estructura.datos.cletter.modelo.CeldaHexagonal;
import com.estructura.datos.cletter.modelo.TableroNivel;

import java.util.Arrays;

/**
 * Vista personalizada del panal hexagonal fijo de 18 celdas (4-3-4-3-4).
 * Optimizada para renderizado acelerado por GPU con cero asignaciones por frame en onDraw().
 * Utiliza:
 * - Grafo (del TableroNivel) para validar adyacencias reales entre celdas vecinas.
 * - Pila<Integer> para gestionar el trazo activo de letras y el retroceso (backtracking).
 */
public class VistaTableroHexagonal extends View {

    public interface EscuchaTablero {
        void alCambiarTrazo(String palabraParcial, int longitud);
        void alCompletarTrazo(String palabraFormada);
    }

    private static final float RAIZ_3 = (float) Math.sqrt(3.0);

    private TableroNivel tablero;
    private EscuchaTablero escucha;
    private final Pila<Integer> pilaTrazo = new Pila<>();

    // Buffers reutilizables sin asignaciones durante onDraw()
    private final boolean[] activasMask = new boolean[GeneradorTablero.TOTAL_CELDAS];
    private final boolean[] pistaMask = new boolean[GeneradorTablero.TOTAL_CELDAS];
    private final int[] ordenTrazo = new int[GeneradorTablero.TOTAL_CELDAS];
    private int largoTrazo = 0;
    private int[] caminoPistaOrdenado = null;
    private final char[] bufferLetra = new char[1];
    private boolean bloqueado = false;
    private boolean estadoRepetida = false;

    private float radioToque = 40f;
    private float offsetTextoY = 0f;

    // Rutas hexagonales redondeadas precalculadas en (0,0) (evita CornerPathEffect en cada frame)
    private final Path rutaHexNormal = new Path();
    private final Path rutaHexActivo = new Path();

    private final Paint pincelRellenoInactivo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelRellenoActivo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelRellenoRepetida = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelRellenoPista = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelBorde = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelBordePista = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelLineaTrazo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelLineaRepetida = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelLineaPista = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelTextoInactivo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelTextoActivo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelTextoRepetida = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelTextoPista = new Paint(Paint.ANTI_ALIAS_FLAG);

    public VistaTableroHexagonal(Context context) {
        super(context);
        inicializarPinceles(context);
    }

    public VistaTableroHexagonal(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inicializarPinceles(context);
    }

    public VistaTableroHexagonal(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inicializarPinceles(context);
    }

    private void inicializarPinceles(Context contexto) {
        float densidad = contexto.getResources().getDisplayMetrics().density;

        pincelRellenoInactivo.setStyle(Paint.Style.FILL);
        pincelRellenoInactivo.setColor(ContextCompat.getColor(contexto, R.color.hex_inactivo));

        pincelRellenoActivo.setStyle(Paint.Style.FILL);
        pincelRellenoActivo.setColor(ContextCompat.getColor(contexto, R.color.hex_activo));

        pincelRellenoRepetida.setStyle(Paint.Style.FILL);
        pincelRellenoRepetida.setColor(ContextCompat.getColor(contexto, R.color.hex_repetida));

        pincelRellenoPista.setStyle(Paint.Style.FILL);
        pincelRellenoPista.setColor(ContextCompat.getColor(contexto, R.color.hex_pista));

        pincelBorde.setStyle(Paint.Style.STROKE);
        pincelBorde.setStrokeWidth(1.5f * densidad);
        pincelBorde.setColor(ContextCompat.getColor(contexto, R.color.hex_borde));

        pincelBordePista.setStyle(Paint.Style.STROKE);
        pincelBordePista.setStrokeWidth(2.2f * densidad);
        pincelBordePista.setColor(ContextCompat.getColor(contexto, R.color.marca_primario));

        pincelLineaTrazo.setStyle(Paint.Style.STROKE);
        pincelLineaTrazo.setStrokeCap(Paint.Cap.ROUND);
        pincelLineaTrazo.setStrokeJoin(Paint.Join.ROUND);
        pincelLineaTrazo.setColor(ContextCompat.getColor(contexto, R.color.hex_activo));

        pincelLineaRepetida.setStyle(Paint.Style.STROKE);
        pincelLineaRepetida.setStrokeCap(Paint.Cap.ROUND);
        pincelLineaRepetida.setStrokeJoin(Paint.Join.ROUND);
        pincelLineaRepetida.setColor(ContextCompat.getColor(contexto, R.color.hex_repetida));

        pincelLineaPista.setStyle(Paint.Style.STROKE);
        pincelLineaPista.setStrokeCap(Paint.Cap.ROUND);
        pincelLineaPista.setStrokeJoin(Paint.Join.ROUND);
        pincelLineaPista.setColor(ContextCompat.getColor(contexto, R.color.hex_linea));
        pincelLineaPista.setAlpha(155);

        Typeface fuenteLetra = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);

        pincelTextoInactivo.setColor(ContextCompat.getColor(contexto, R.color.hex_texto_inactivo));
        pincelTextoInactivo.setTextAlign(Paint.Align.CENTER);
        pincelTextoInactivo.setTypeface(fuenteLetra);

        pincelTextoActivo.setColor(ContextCompat.getColor(contexto, R.color.hex_texto_activo));
        pincelTextoActivo.setTextAlign(Paint.Align.CENTER);
        pincelTextoActivo.setTypeface(fuenteLetra);

        pincelTextoRepetida.setColor(ContextCompat.getColor(contexto, R.color.hex_texto_repetida));
        pincelTextoRepetida.setTextAlign(Paint.Align.CENTER);
        pincelTextoRepetida.setTypeface(fuenteLetra);

        pincelTextoPista.setColor(ContextCompat.getColor(contexto, R.color.marca_sobre_acento));
        pincelTextoPista.setTextAlign(Paint.Align.CENTER);
        pincelTextoPista.setTypeface(fuenteLetra);
    }

    public void setTablero(TableroNivel nuevoTablero) {
        this.tablero = nuevoTablero;
        limpiarTrazoInterno();
        Arrays.fill(pistaMask, false);
        this.caminoPistaOrdenado = null;
        calcularGeometria();
        invalidate();
    }

    public void setEscuchaTablero(EscuchaTablero escucha) {
        this.escucha = escucha;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
        if (bloqueado && !pilaTrazo.Vacia()) {
            limpiarTrazoInterno();
            invalidate();
        }
    }

    public void mostrarPista(int[] caminoIndices) {
        Arrays.fill(pistaMask, false);
        caminoPistaOrdenado = caminoIndices;
        if (caminoIndices != null) {
            for (int idx : caminoIndices) {
                if (idx >= 0 && idx < pistaMask.length) {
                    pistaMask[idx] = true;
                }
            }
        }
        invalidate();
    }

    public void limpiarPista() {
        if (caminoPistaOrdenado != null) {
            Arrays.fill(pistaMask, false);
            caminoPistaOrdenado = null;
            invalidate();
        }
    }

    public void setEstadoRepetida(boolean repetida) {
        if (this.estadoRepetida != repetida) {
            this.estadoRepetida = repetida;
            invalidate();
        }
    }

    private void limpiarTrazoInterno() {
        pilaTrazo.Limpiar();
        Arrays.fill(activasMask, false);
        largoTrazo = 0;
        estadoRepetida = false;
    }

    private void sincronizarCacheDesdePila() {
        Arrays.fill(activasMask, false);
        Object[] indices = pilaTrazo.AArregloOrdenado();
        largoTrazo = Math.min(indices.length, ordenTrazo.length);
        for (int i = 0; i < largoTrazo; i++) {
            int idx = (Integer) indices[i];
            ordenTrazo[i] = idx;
            if (idx >= 0 && idx < activasMask.length) {
                activasMask[idx] = true;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calcularGeometria();
    }

    private void calcularGeometria() {
        if (tablero == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        float anchoDisponible = getWidth() - getPaddingLeft() - getPaddingRight() - 16f;
        float altoDisponible = getHeight() - getPaddingTop() - getPaddingBottom() - 16f;
        if (anchoDisponible <= 0 || altoDisponible <= 0) {
            return;
        }

        float radioPorAncho = anchoDisponible / 8.0f;
        float radioPorAlto = altoDisponible / (4.0f * RAIZ_3);
        float radioHex = Math.min(radioPorAncho, radioPorAlto);
        float radioDibujo = radioHex * 0.93f;
        radioToque = radioHex * 0.84f;

        construirHexagonoRedondeado(rutaHexNormal, radioDibujo, 0.16f);
        construirHexagonoRedondeado(rutaHexActivo, radioDibujo * 1.03f, 0.16f);

        pincelLineaTrazo.setStrokeWidth(radioHex * 0.56f);
        pincelLineaRepetida.setStrokeWidth(radioHex * 0.56f);
        pincelLineaPista.setStrokeWidth(radioHex * 0.34f);

        float tamanoTexto = radioHex * 0.72f;
        pincelTextoInactivo.setTextSize(tamanoTexto);
        pincelTextoActivo.setTextSize(tamanoTexto);
        pincelTextoRepetida.setTextSize(tamanoTexto);
        pincelTextoPista.setTextSize(tamanoTexto);

        Paint.FontMetrics fm = pincelTextoInactivo.getFontMetrics();
        offsetTextoY = -(fm.ascent + fm.descent) / 2f;

        float anchoPanal = 8.0f * radioHex;
        float altoPanal = 4.0f * RAIZ_3 * radioHex;
        float origenX = getPaddingLeft() + (getWidth() - getPaddingLeft() - getPaddingRight() - anchoPanal) / 2f + radioHex;
        float origenY = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom() - altoPanal) / 2f + (RAIZ_3 * radioHex / 2f);

        float pasoX = 1.5f * radioHex;
        float pasoY = RAIZ_3 * radioHex;

        for (CeldaHexagonal celda : tablero.getCeldas()) {
            int col = celda.getColumna();
            int fila = celda.getFila();
            float desplazamientoY = (col % 2 == 1) ? (pasoY * 0.5f) : 0f;
            float cx = origenX + col * pasoX;
            float cy = origenY + fila * pasoY + desplazamientoY;
            celda.setCentroX(cx);
            celda.setCentroY(cy);
        }
    }

    /**
     * Construye un hexágono flat-top centrado en (0, 0) con vértices suaves vía quadTo
     * para renderizado GPU directo sin CornerPathEffect.
     */
    private static void construirHexagonoRedondeado(Path ruta, float radio, float fraccionEsquina) {
        ruta.reset();
        float[] vx = new float[6];
        float[] vy = new float[6];
        for (int i = 0; i < 6; i++) {
            double angulo = Math.toRadians(60.0 * i);
            vx[i] = (float) (radio * Math.cos(angulo));
            vy[i] = (float) (radio * Math.sin(angulo));
        }

        for (int i = 0; i < 6; i++) {
            int prev = (i + 5) % 6;
            int next = (i + 1) % 6;

            float inicioX = vx[i] + (vx[prev] - vx[i]) * fraccionEsquina;
            float inicioY = vy[i] + (vy[prev] - vy[i]) * fraccionEsquina;
            float finX = vx[i] + (vx[next] - vx[i]) * fraccionEsquina;
            float finY = vy[i] + (vy[next] - vy[i]) * fraccionEsquina;

            if (i == 0) {
                ruta.moveTo(inicioX, inicioY);
            } else {
                ruta.lineTo(inicioX, inicioY);
            }
            ruta.quadTo(vx[i], vy[i], finX, finY);
        }
        ruta.close();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (tablero == null) {
            return;
        }
        CeldaHexagonal[] celdas = tablero.getCeldas();
        if (celdas == null || celdas.length == 0) {
            return;
        }

        // 1. Guía de pista si existe
        if (caminoPistaOrdenado != null && caminoPistaOrdenado.length >= 2) {
            for (int i = 0; i < caminoPistaOrdenado.length - 1; i++) {
                CeldaHexagonal a = celdas[caminoPistaOrdenado[i]];
                CeldaHexagonal b = celdas[caminoPistaOrdenado[i + 1]];
                canvas.drawLine(a.getCentroX(), a.getCentroY(), b.getCentroX(), b.getCentroY(), pincelLineaPista);
            }
        }

        // 2. Hexágonos inactivos o en pista
        for (CeldaHexagonal celda : celdas) {
            int idx = celda.getIndice();
            if (activasMask[idx]) {
                continue;
            }
            float cx = celda.getCentroX();
            float cy = celda.getCentroY();
            canvas.save();
            canvas.translate(cx, cy);
            if (pistaMask[idx]) {
                canvas.drawPath(rutaHexNormal, pincelRellenoPista);
                canvas.drawPath(rutaHexNormal, pincelBordePista);
                dibujarLetraSinAsignacion(canvas, celda.getLetra(), pincelTextoPista);
            } else {
                canvas.drawPath(rutaHexNormal, pincelRellenoInactivo);
                canvas.drawPath(rutaHexNormal, pincelBorde);
                dibujarLetraSinAsignacion(canvas, celda.getLetra(), pincelTextoInactivo);
            }
            canvas.restore();
        }

        // 3. Línea continua + hexágonos activos (naranja si la palabra ya fue encontrada)
        if (largoTrazo > 0) {
            Paint pincelLinea = estadoRepetida ? pincelLineaRepetida : pincelLineaTrazo;
            Paint pincelRelleno = estadoRepetida ? pincelRellenoRepetida : pincelRellenoActivo;
            Paint pincelTexto = estadoRepetida ? pincelTextoRepetida : pincelTextoActivo;

            for (int i = 0; i < largoTrazo - 1; i++) {
                CeldaHexagonal a = celdas[ordenTrazo[i]];
                CeldaHexagonal b = celdas[ordenTrazo[i + 1]];
                canvas.drawLine(a.getCentroX(), a.getCentroY(), b.getCentroX(), b.getCentroY(), pincelLinea);
            }

            for (int i = 0; i < largoTrazo; i++) {
                CeldaHexagonal celda = celdas[ordenTrazo[i]];
                canvas.save();
                canvas.translate(celda.getCentroX(), celda.getCentroY());
                canvas.drawPath(rutaHexActivo, pincelRelleno);
                dibujarLetraSinAsignacion(canvas, celda.getLetra(), pincelTexto);
                canvas.restore();
            }
        }
    }

    private void dibujarLetraSinAsignacion(Canvas canvas, char letra, Paint pincel) {
        bufferLetra[0] = letra;
        canvas.drawText(bufferLetra, 0, 1, 0f, offsetTextoY, pincel);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (bloqueado || tablero == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                int indice = detectarCeldaEn(x, y);
                if (indice != -1) {
                    pilaTrazo.Limpiar();
                    pilaTrazo.Apilar(indice);
                    sincronizarCacheDesdePila();
                    notificarCambioTrazo();
                    invalidate();
                    return true;
                }
                return false;
            }
            case MotionEvent.ACTION_MOVE: {
                if (pilaTrazo.Vacia()) {
                    return false;
                }
                int indice = detectarCeldaEn(x, y);
                if (indice == -1) {
                    return true;
                }
                Integer ultimo = pilaTrazo.LeerUltimo();
                if (ultimo != null && ultimo == indice) {
                    return true;
                }

                // Retroceso (backtracking) si el dedo vuelve a la celda penúltima en la Pila
                Integer penultimo = pilaTrazo.LeerPenultimo();
                if (penultimo != null && penultimo == indice) {
                    pilaTrazo.Desapilar();
                    sincronizarCacheDesdePila();
                    notificarCambioTrazo();
                    invalidate();
                    return true;
                }

                // Avanzar solo si no fue visitada y es vecina en el Grafo hexagonal
                if (!pilaTrazo.Contiene(indice) && ultimo != null) {
                    Grafo grafo = tablero.getGrafo();
                    CeldaHexagonal anterior = tablero.getCeldas()[ultimo];
                    CeldaHexagonal candidata = tablero.getCeldas()[indice];
                    if (grafo.Adyacente(anterior.getIdVertice(), candidata.getIdVertice())) {
                        pilaTrazo.Apilar(indice);
                        sincronizarCacheDesdePila();
                        notificarCambioTrazo();
                        invalidate();
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if (!pilaTrazo.Vacia()) {
                    String palabra = construirPalabraActual();
                    limpiarTrazoInterno();
                    invalidate();
                    if (escucha != null) {
                        escucha.alCompletarTrazo(palabra);
                    }
                    performClick();
                }
                return true;
            }
            case MotionEvent.ACTION_CANCEL: {
                if (!pilaTrazo.Vacia()) {
                    limpiarTrazoInterno();
                    invalidate();
                    if (escucha != null) {
                        escucha.alCambiarTrazo("", 0);
                    }
                }
                return true;
            }
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private int detectarCeldaEn(float x, float y) {
        if (tablero == null) {
            return -1;
        }
        CeldaHexagonal[] celdas = tablero.getCeldas();
        float mejorDistCuad = radioToque * radioToque;
        int mejorIndice = -1;
        for (int i = 0; i < GeneradorTablero.TOTAL_CELDAS && i < celdas.length; i++) {
            float dx = x - celdas[i].getCentroX();
            float dy = y - celdas[i].getCentroY();
            float distCuad = dx * dx + dy * dy;
            if (distCuad <= mejorDistCuad) {
                mejorDistCuad = distCuad;
                mejorIndice = i;
            }
        }
        return mejorIndice;
    }

    private String construirPalabraActual() {
        if (tablero == null || largoTrazo == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(largoTrazo);
        CeldaHexagonal[] celdas = tablero.getCeldas();
        for (int i = 0; i < largoTrazo; i++) {
            sb.append(celdas[ordenTrazo[i]].getLetra());
        }
        return sb.toString();
    }

    private void notificarCambioTrazo() {
        if (escucha != null) {
            escucha.alCambiarTrazo(construirPalabraActual(), pilaTrazo.Tamaño());
        }
    }
}
