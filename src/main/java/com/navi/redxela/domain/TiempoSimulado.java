package com.navi.redxela.domain;

import java.time.Instant;

/** Reloj lógico cuya velocidad puede cambiarse mientras la simulación está activa. */
public final class TiempoSimulado {
    private static final long INICIO_REAL_NANOS = System.nanoTime();
    private static final Instant INICIO_SIMULADO = Instant.now();
    private static long acumuladoNanos;
    private static long ultimaActualizacionNanos = INICIO_REAL_NANOS;
    private static double acelerador = leerAceleradorInicial();

    private TiempoSimulado() {
    }

    public static synchronized Instant ahora() {
        actualizar();
        return INICIO_SIMULADO.plusNanos(acumuladoNanos);
    }

    public static synchronized double getAcelerador() {
        return acelerador;
    }

    public static synchronized void setAcelerador(double nuevoAcelerador) {
        if (!Double.isFinite(nuevoAcelerador) || nuevoAcelerador < 1.0 || nuevoAcelerador > 100.0) {
            throw new IllegalArgumentException("El acelerador debe estar entre ×1 y ×100.");
        }
        actualizar();
        acelerador = nuevoAcelerador;
    }

    private static void actualizar() {
        long ahora = System.nanoTime();
        acumuladoNanos += (long) ((ahora - ultimaActualizacionNanos) * acelerador);
        ultimaActualizacionNanos = ahora;
    }

    private static double leerAceleradorInicial() {
        try {
            return Math.max(1.0, Math.min(100.0,
                    Double.parseDouble(System.getProperty("redxela.acelerador", "1"))));
        } catch (NumberFormatException ex) {
            return 1.0;
        }
    }
}
