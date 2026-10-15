package com.navi.redxela.domain;

import java.util.Objects;

public record ArticuloPedido(Producto producto, int cantidad) {
    public ArticuloPedido {
        Objects.requireNonNull(producto);
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
    }
}
