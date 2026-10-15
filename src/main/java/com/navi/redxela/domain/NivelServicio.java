package com.navi.redxela.domain;

public enum NivelServicio {
    EXPRESO(1, "Exprés", 0),
    PRIORITARIO(2, "Prioritario", 10),
    ESTANDAR(3, "Estándar", 30),
    PROGRAMADO(4, "Programado", 60),
    ECONOMICO(5, "Económico", 120);

    private final int orden;
    private final String nombre;
    private final int esperaObjetivoMinutos;

    NivelServicio(int orden, String nombre, int esperaObjetivoMinutos) {
        this.orden = orden;
        this.nombre = nombre;
        this.esperaObjetivoMinutos = esperaObjetivoMinutos;
    }

    public int getOrden() {
        return orden;
    }

    public String getNombre() {
        return nombre;
    }

    public int getEsperaObjetivoMinutos() {
        return esperaObjetivoMinutos;
    }

    @Override
    public String toString() {
        String objetivo = esperaObjetivoMinutos == 0
                ? "casi instantáneo"
                : "espera máx. " + esperaObjetivoMinutos + " min";
        return nombre + " · " + objetivo;
    }
}
