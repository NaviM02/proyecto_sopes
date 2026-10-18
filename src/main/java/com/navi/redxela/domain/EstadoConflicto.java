package com.navi.redxela.domain;

public record EstadoConflicto(
        String estado,
        String operacionA,
        String detalleA,
        String operacionB,
        String detalleB,
        String recursos,
        boolean activo
) {
}
