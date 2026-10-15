package com.navi.redxela.domain;

public enum Recurso {
    ESTACION_EMPAQUE("Estaciones de empaque", 6),
    AREA_CARGA("Áreas de carga", 2),
    ENCARGADO_BODEGA("Encargados de bodega", 10),
    MONTACARGAS("Montacargas", 3),
    ESTACION_CALIDAD("Estaciones de calidad", 4),
    ESCANER("Sistema de escaneo", 1);

    private final String nombre;
    private final int capacidad;

    Recurso(String nombre, int capacidad) {
        this.nombre = nombre;
        this.capacidad = capacidad;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCapacidad() {
        return capacidad;
    }
}
