package com.navi.redxela.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Pedido {
    private final String id;
    private final Cliente cliente;
    private final List<ArticuloPedido> articulos;
    private final Instant creadoEn;
    private EstadoPedido estado;
    private int progreso;
    private Instant iniciadoEn;
    private Instant completadoEn;

    public Pedido(Cliente cliente, List<ArticuloPedido> articulos) {
        this("PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), cliente, articulos);
    }

    public Pedido(String id, Cliente cliente, List<ArticuloPedido> articulos) {
        this.id = Objects.requireNonNull(id);
        this.cliente = Objects.requireNonNull(cliente);
        if (articulos == null || articulos.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe incluir al menos un artículo.");
        }
        this.articulos = List.copyOf(articulos);
        this.creadoEn = Instant.now();
        this.estado = EstadoPedido.EN_ESPERA;
    }

    public String getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<ArticuloPedido> getArticulos() {
        return articulos;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public synchronized EstadoPedido getEstado() {
        return estado;
    }

    public synchronized int getProgreso() {
        return progreso;
    }

    public int getCantidadTotal() {
        return articulos.stream().mapToInt(ArticuloPedido::cantidad).sum();
    }

    public String getResumenMercancia() {
        return articulos.stream()
                .map(a -> a.producto().tipoMercancia().getNombre())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("Sin mercancía");
    }

    public synchronized void actualizarEstado(EstadoPedido nuevoEstado) {
        estado = Objects.requireNonNull(nuevoEstado);
        if (nuevoEstado == EstadoPedido.EN_PROCESO && iniciadoEn == null) {
            iniciadoEn = Instant.now();
        }
    }

    public synchronized void actualizarProgreso(int nuevoProgreso) {
        progreso = Math.max(0, Math.min(100, nuevoProgreso));
    }

    public synchronized void completar() {
        progreso = 100;
        estado = EstadoPedido.COMPLETADO;
        completadoEn = Instant.now();
    }

    public synchronized long getTiempoEsperaMs() {
        Instant finEspera = iniciadoEn == null ? Instant.now() : iniciadoEn;
        return Math.max(0, Duration.between(creadoEn, finEspera).toMillis());
    }

    public synchronized long getTiempoProcesoMs() {
        if (iniciadoEn == null) {
            return 0;
        }
        Instant fin = completadoEn == null ? Instant.now() : completadoEn;
        return Math.max(0, Duration.between(iniciadoEn, fin).toMillis());
    }

    public synchronized long getTiempoRestanteEstimadoMs() {
        if (estado != EstadoPedido.EN_PROCESO) {
            return 0;
        }
        long duracionEstimada = Math.min(15_000, 450L + getCantidadTotal() * 320L);
        return Math.max(0, duracionEstimada * (100 - progreso) / 100);
    }
}
