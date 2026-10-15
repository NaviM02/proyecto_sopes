package com.navi.redxela.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum TipoMercancia {
    FRAGIL("Frágil", "Requiere manipulación cuidadosa y revisión antes del empaque.",
            EnumSet.of(Recurso.ESTACION_CALIDAD, Recurso.ESTACION_EMPAQUE)),
    PESADA("Pesada", "Necesita montacargas y espacio en el área de carga.",
            EnumSet.of(Recurso.MONTACARGAS, Recurso.AREA_CARGA)),
    REFRIGERADA("Refrigerada", "Se mueve con apoyo de bodega y montacargas.",
            EnumSet.of(Recurso.ENCARGADO_BODEGA, Recurso.MONTACARGAS)),
    ELECTRONICA("Electrónica", "Se escanea y pasa por control de calidad.",
            EnumSet.of(Recurso.ESCANER, Recurso.ESTACION_CALIDAD)),
    PELIGROSA("Peligrosa", "Requiere identificación, revisión y manejo especializado.",
            EnumSet.of(Recurso.ESCANER, Recurso.ESTACION_CALIDAD, Recurso.ENCARGADO_BODEGA));

    private final String nombre;
    private final String descripcion;
    private final Set<Recurso> recursos;

    TipoMercancia(String nombre, String descripcion, Set<Recurso> recursos) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.recursos = Collections.unmodifiableSet(EnumSet.copyOf(recursos));
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Set<Recurso> getRecursos() {
        return recursos;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
