package com.navi.redxela.domain;

public record ResumenProducto(
        Producto producto,
        int unidadesDisponibles,
        int unidadesReservadas,
        String ubicaciones
) {
}
