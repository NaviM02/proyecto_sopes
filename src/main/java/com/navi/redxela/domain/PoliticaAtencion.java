package com.navi.redxela.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;

public enum PoliticaAtencion {
    PRIORIDAD("Prioridad estricta", "Atiende primero el servicio contratado y luego el pedido más antiguo."),
    ANTIGUEDAD("Prioridad con antigüedad", "El tiempo de espera reduce gradualmente la prioridad numérica."),
    MENOS_UNIDADES("Menos unidades primero", "Empieza por los pedidos cortos y usa el nivel como desempate.");

    private final String nombre;
    private final String descripcion;

    PoliticaAtencion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Comparator<Pedido> comparador() {
        Instant ahora = TiempoSimulado.ahora();
        return switch (this) {
            case PRIORIDAD -> Comparator.comparingInt((Pedido p) -> p.getCliente().nivelServicio().getOrden())
                    .thenComparing(Pedido::getCreadoEn);
            case ANTIGUEDAD -> Comparator.comparingDouble((Pedido p) -> puntajeAntiguedad(p, ahora))
                    .thenComparing(Pedido::getCreadoEn);
            case MENOS_UNIDADES -> Comparator.comparingInt(Pedido::getCantidadTotal)
                    .thenComparingInt(p -> p.getCliente().nivelServicio().getOrden())
                    .thenComparing(Pedido::getCreadoEn);
        };
    }

    private static double puntajeAntiguedad(Pedido pedido, Instant ahora) {
        double minutosEnEspera = Duration.between(pedido.getCreadoEn(), ahora).toMillis() / 60_000.0;
        return pedido.getCliente().nivelServicio().getOrden() * 5.0 - minutosEnEspera;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
