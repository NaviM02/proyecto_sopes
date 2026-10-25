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
        this.creadoEn = TiempoSimulado.ahora();
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

    public String getResumenProductos() {
        return articulos.stream()
                .map(a -> a.producto().nombre() + " ×" + a.cantidad())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Sin productos");
    }

    public synchronized void actualizarEstado(EstadoPedido nuevoEstado) {
        estado = Objects.requireNonNull(nuevoEstado);
        if (nuevoEstado == EstadoPedido.EN_PROCESO && iniciadoEn == null) {
            iniciadoEn = TiempoSimulado.ahora();
        }
    }

    public synchronized void actualizarProgreso(int nuevoProgreso) {
        progreso = Math.max(0, Math.min(100, nuevoProgreso));
    }

    public synchronized void completar() {
        progreso = 100;
        estado = EstadoPedido.COMPLETADO;
        completadoEn = TiempoSimulado.ahora();
    }

    public synchronized long getTiempoEsperaMs() {
        Instant finEspera = iniciadoEn == null ? TiempoSimulado.ahora() : iniciadoEn;
        return Math.max(0, Duration.between(creadoEn, finEspera).toMillis());
    }

    public synchronized long getTiempoProcesoMs() {
        if (iniciadoEn == null) {
            return 0;
        }
        Instant fin = completadoEn == null ? TiempoSimulado.ahora() : completadoEn;
        return Math.max(0, Duration.between(iniciadoEn, fin).toMillis());
    }

    public synchronized long getTiempoRestanteEstimadoMs() {
        if (estado != EstadoPedido.EN_PROCESO) {
            return 0;
        }
        long duracionEstimada = Math.min(300_000, 15_000L + getCantidadTotal() * 20_000L);
        return Math.max(0, (long) (duracionEstimada * (100 - progreso)
                / 100.0 / TiempoSimulado.getAcelerador()));
    }
}
