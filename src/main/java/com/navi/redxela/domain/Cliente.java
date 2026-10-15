package com.navi.redxela.domain;

import java.util.Objects;

public record Cliente(String id, String nombre, NivelServicio nivelServicio) {
    public Cliente {
        Objects.requireNonNull(id);
        Objects.requireNonNull(nombre);
        Objects.requireNonNull(nivelServicio);
    }

    @Override
    public String toString() {
        return nombre + " · " + nivelServicio.getNombre();
    }
}
