package com.navi.redxela.domain;

public record ResumenRecurso(Recurso recurso, int enUso, int capacidad, String ocupantes) {
    public double getOcupacion() {
        return capacidad == 0 ? 0 : (double) enUso / capacidad;
    }
}
