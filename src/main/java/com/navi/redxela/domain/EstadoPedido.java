package com.navi.redxela.domain;

public enum EstadoPedido {
    EN_ESPERA("En espera"),
    ESPERANDO_INVENTARIO("Esperando inventario"),
    ESPERANDO_RECURSOS("Esperando recursos"),
    EN_PROCESO("En proceso"),
    COMPLETADO("Completado");

    private final String nombre;

    EstadoPedido(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
