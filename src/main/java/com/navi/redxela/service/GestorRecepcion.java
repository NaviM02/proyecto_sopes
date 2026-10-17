package com.navi.redxela.service;

import com.navi.redxela.domain.EventoRecepcion;
import com.navi.redxela.domain.Producto;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public final class GestorRecepcion implements AutoCloseable {
    private final InventarioService inventario;
    private final Runnable alActualizarInventario;
    private final BlockingQueue<EventoRecepcion> cola = new ArrayBlockingQueue<>(100);
    private final CopyOnWriteArrayList<EventoRecepcion> historial = new CopyOnWriteArrayList<>();
    private final Thread trabajador;
    private volatile boolean cerrado;

    public GestorRecepcion(InventarioService inventario, Runnable alActualizarInventario) {
        this.inventario = inventario;
        this.alActualizarInventario = alActualizarInventario;
        trabajador = new Thread(this::procesarCola, "redxela-area-inventario");
        trabajador.setDaemon(true);
        trabajador.start();
    }

    public boolean recibir(Producto producto, int cantidad) {
        EventoRecepcion evento = new EventoRecepcion(producto, cantidad);
        if (!cola.offer(evento)) {
            return false;
        }
        historial.add(evento);
        return true;
    }

    public int getPendientes() {
        return cola.size();
    }

    public List<EventoRecepcion> getHistorial() {
        return historial.stream()
                .sorted(Comparator.comparing(EventoRecepcion::getSolicitadoEn).reversed())
                .limit(30)
                .toList();
    }

    private void procesarCola() {
        while (!cerrado) {
            EventoRecepcion evento = null;
            try {
                evento = cola.take();
                evento.actualizar("Procesando", "Inventario está ubicando la mercadería.");
                Thread.sleep(120);
                String resultado = inventario.recibir(evento.getProducto(), evento.getCantidad());
                if (resultado.startsWith("Recibidas")) {
                    evento.actualizar("Aplicada", resultado);
                    alActualizarInventario.run();
                } else {
                    evento.actualizar("Rechazada", resultado);
                }
            } catch (InterruptedException ex) {
                if (cerrado) {
                    Thread.currentThread().interrupt();
                    return;
                }
            } catch (RuntimeException ex) {
                if (evento != null) {
                    evento.actualizar("Error", "No se pudo procesar la recepción: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public void close() {
        cerrado = true;
        trabajador.interrupt();
    }
}
