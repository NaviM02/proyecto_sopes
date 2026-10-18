package com.navi.redxela.service;

import com.navi.redxela.domain.EstadoConflicto;
import com.navi.redxela.domain.Recurso;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ControlConflicto implements AutoCloseable {
    private final GestorRecursos recursos;
    private final AtomicInteger consecutivo = new AtomicInteger();
    private volatile String estado = "Sin conflicto";
    private volatile String operacionA = "Operación A";
    private volatile String detalleA = "Lista para iniciar.";
    private volatile String operacionB = "Operación B";
    private volatile String detalleB = "Lista para iniciar.";
    private volatile String descripcionRecursos = "Escáner (1) y estaciones de calidad (4)";
    private volatile boolean activo;
    private volatile String ganador;
    private volatile Thread hiloA;
    private volatile Thread hiloB;
    private volatile Runnable listener = () -> {
    };

    public ControlConflicto(GestorRecursos recursos) {
        this.recursos = recursos;
    }

    public void setListener(Runnable listener) {
        this.listener = listener == null ? () -> {
        } : listener;
    }

    public synchronized boolean iniciar() {
        if (activo) {
            return false;
        }
        String sufijo = "%02d".formatted(consecutivo.incrementAndGet());
        String idA = "Conflicto-A-" + sufijo;
        String idB = "Conflicto-B-" + sufijo;
        Recurso primeroA = Recurso.ESCANER;
        Recurso segundoA = Recurso.ESTACION_CALIDAD;
        Recurso primeroB = Recurso.ESTACION_CALIDAD;
        Recurso segundoB = Recurso.ESCANER;
        try {
            recursos.prepararConflicto(primeroA, idA, primeroB, idB);
        } catch (IllegalStateException ex) {
            estado = "No se pudo iniciar";
            detalleA = ex.getMessage();
            detalleB = "Espera a que se liberen los recursos e inténtalo otra vez.";
            notificar();
            return false;
        }

        activo = true;
        ganador = null;
        operacionA = "Operación A · " + idA;
        detalleA = "Retiene el escáner y solicita calidad.";
        operacionB = "Operación B · " + idB;
        detalleB = "Retiene las 4 estaciones de calidad y solicita el escáner.";
        estado = "Bloqueo detectado";

        CountDownLatch preparadas = new CountDownLatch(2);
        hiloA = crearOperacion(idA, "A", primeroA, segundoA, preparadas);
        hiloB = crearOperacion(idB, "B", primeroB, segundoB, preparadas);
        hiloA.start();
        hiloB.start();
        Thread observador = new Thread(() -> {
            try {
                preparadas.await(1, TimeUnit.SECONDS);
                notificar();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }, "redxela-observador-conflicto");
        observador.setDaemon(true);
        observador.start();
        notificar();
        return true;
    }

    public synchronized boolean resolver(boolean continuarA) {
        if (!activo || ganador != null) {
            return false;
        }
        ganador = continuarA ? "A" : "B";
        estado = "Resolviendo conflicto";
        String ganadorId = continuarA ? "A" : "B";
        if (continuarA) {
            detalleA = "Continúa y termina su trabajo.";
            detalleB = "Se devuelve a la lista de espera.";
        } else {
            detalleB = "Continúa y termina su trabajo.";
            detalleA = "Se devuelve a la lista de espera.";
        }
        Thread hiloGanador = continuarA ? hiloA : hiloB;
        Thread hiloDescartado = continuarA ? hiloB : hiloA;
        Thread resolvedor = new Thread(() -> completarResolucion(hiloGanador, hiloDescartado, ganadorId),
                "redxela-resolvedor-conflicto");
        resolvedor.setDaemon(true);
        resolvedor.start();
        notificar();
        return true;
    }

    public EstadoConflicto obtenerEstado() {
        return new EstadoConflicto(estado, operacionA, detalleA, operacionB, detalleB, descripcionRecursos, activo);
    }

    private Thread crearOperacion(
            String id,
            String letra,
            Recurso recursoInicial,
            Recurso recursoSolicitado,
            CountDownLatch preparadas
    ) {
        return new Thread(() -> {
            boolean segundoAdquirido = false;
            preparadas.countDown();
            try {
                recursos.adquirirEsperando(recursoSolicitado, id);
                segundoAdquirido = true;
                actualizarDetalle(letra, "Obtuvo los dos recursos y está terminando el trabajo.");
                Thread.sleep(1_200);
                actualizarDetalle(letra, "Operación completada y recursos liberados.");
            } catch (InterruptedException ex) {
                actualizarDetalle(letra, "Devuelta a la lista de espera para reintentar después.");
            } finally {
                if (segundoAdquirido) {
                    recursos.liberarTodos(id, Set.of(recursoSolicitado));
                }
                recursos.liberarTodos(id, Set.of(recursoInicial));
                notificar();
            }
        }, "redxela-conflicto-" + letra.toLowerCase());
    }

    private void completarResolucion(Thread hiloGanador, Thread hiloDescartado, String ganadorId) {
        hiloDescartado.interrupt();
        try {
            hiloDescartado.join(2_000);
            hiloGanador.join(5_000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        synchronized (this) {
            estado = "Conflicto resuelto · continuó la operación " + ganadorId;
            activo = false;
        }
        notificar();
    }

    private void actualizarDetalle(String letra, String detalle) {
        if ("A".equals(letra)) {
            detalleA = detalle;
        } else {
            detalleB = detalle;
        }
        notificar();
    }

    private void notificar() {
        try {
            listener.run();
        } catch (RuntimeException ex) {
            // La ventana puede cerrarse mientras terminan los hilos del ejemplo.
        }
    }

    @Override
    public void close() {
        Thread localA = hiloA;
        Thread localB = hiloB;
        if (localA != null) {
            localA.interrupt();
        }
        if (localB != null) {
            localB.interrupt();
        }
    }
}
