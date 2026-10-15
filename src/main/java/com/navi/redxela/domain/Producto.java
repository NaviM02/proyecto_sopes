package com.navi.redxela.domain;

import java.util.Objects;

public record Producto(String codigo, String nombre, TipoMercancia tipoMercancia) {
    public Producto {
        Objects.requireNonNull(codigo);
        Objects.requireNonNull(nombre);
        Objects.requireNonNull(tipoMercancia);
    }

    @Override
    public String toString() {
        return nombre + " · " + tipoMercancia.getNombre();
    }
}
