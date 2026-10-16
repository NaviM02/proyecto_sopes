package com.navi.redxela.service;

import com.navi.redxela.domain.Producto;
import com.navi.redxela.domain.TipoMercancia;

import java.util.List;

public final class CatalogoProductos {
    private static final List<Producto> PRODUCTOS = List.of(
            new Producto("PRD-001", "Vajilla de cerámica", TipoMercancia.FRAGIL),
            new Producto("PRD-002", "Refrigeradora compacta", TipoMercancia.PESADA),
            new Producto("PRD-003", "Caja de vacunas", TipoMercancia.REFRIGERADA),
            new Producto("PRD-004", "Tableta electrónica", TipoMercancia.ELECTRONICA),
            new Producto("PRD-005", "Limpiador industrial", TipoMercancia.PELIGROSA),
            new Producto("PRD-006", "Monitor de oficina", TipoMercancia.ELECTRONICA),
            new Producto("PRD-007", "Lámpara de vidrio", TipoMercancia.FRAGIL),
            new Producto("PRD-008", "Batería para equipo", TipoMercancia.PESADA)
    );

    private CatalogoProductos() {
    }

    public static List<Producto> obtenerProductos() {
        return PRODUCTOS;
    }
}
