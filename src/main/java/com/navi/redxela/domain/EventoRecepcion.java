package com.navi.redxela.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class EventoRecepcion {
    private final String id = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    private final Producto producto;
    private final int cantidad;
    private final Instant solicitadoEn = Instant.now();
    private String estado = "En cola";
    private String detalle = "Esperando al área de inventario.";

    public EventoRecepcion(Producto producto, int cantidad) {
        this.producto = Objects.requireNonNull(producto);
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        this.cantidad = cantidad;
    }

    public String getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public Instant getSolicitadoEn() {
        return solicitadoEn;
    }

    public synchronized String getEstado() {
        return estado;
    }

    public synchronized String getDetalle() {
        return detalle;
    }

    public synchronized void actualizar(String nuevoEstado, String nuevoDetalle) {
        estado = nuevoEstado;
        detalle = nuevoDetalle;
    }
}
