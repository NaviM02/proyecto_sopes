package com.navi.redxela.service;

import com.navi.redxela.domain.ArticuloPedido;
import com.navi.redxela.domain.Producto;
import com.navi.redxela.domain.ResumenProducto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class InventarioService {
    private static final int CANTIDAD_UBICACIONES = 20;

    private final int capacidad;
    private final List<Ubicacion> ubicaciones = new ArrayList<>();
    private final Map<String, Integer> disponibles = new HashMap<>();
    private final Map<String, LinkedHashMap<String, Integer>> ubicacionesPorProducto = new HashMap<>();
    private int unidadesAlmacenadas;
    private int cursorUbicacion;

    public InventarioService() {
        capacidad = leerCapacidad();
        int capacidadBase = capacidad / CANTIDAD_UBICACIONES;
        int sobrante = capacidad % CANTIDAD_UBICACIONES;
        for (int i = 0; i < CANTIDAD_UBICACIONES; i++) {
            int extra = i < sobrante ? 1 : 0;
            ubicaciones.add(new Ubicacion("PAS-%02d-NIV-%02d".formatted(i / 5 + 1, i % 5 + 1), capacidadBase + extra));
        }
    }

    public int getCapacidad() {
        return capacidad;
    }

    public synchronized int getUnidadesAlmacenadas() {
        return unidadesAlmacenadas;
    }

    public synchronized int getUnidadesLibres() {
        return capacidad - unidadesAlmacenadas;
    }

    public synchronized String recibir(Producto producto, int cantidad) {
        if (cantidad <= 0) {
            return "La cantidad debe ser mayor que cero.";
        }
        if (cantidad > capacidad - unidadesAlmacenadas) {
            return "No hay espacio suficiente: quedan " + (capacidad - unidadesAlmacenadas) + " unidades libres.";
        }

        int pendientes = cantidad;
        LinkedHashMap<String, Integer> ubicacionesProducto = ubicacionesPorProducto.computeIfAbsent(
                producto.codigo(), ignorado -> new LinkedHashMap<>());
        for (int paso = 0; paso < ubicaciones.size() && pendientes > 0; paso++) {
            int indice = (cursorUbicacion + paso * 7) % ubicaciones.size();
            Ubicacion ubicacion = ubicaciones.get(indice);
            int asignadas = Math.min(pendientes, ubicacion.capacidad - ubicacion.ocupacion);
            if (asignadas > 0) {
                ubicacion.productos.merge(producto.codigo(), asignadas, Integer::sum);
                ubicacion.ocupacion += asignadas;
                ubicacionesProducto.merge(ubicacion.codigo, asignadas, Integer::sum);
                pendientes -= asignadas;
                cursorUbicacion = (indice + 1) % ubicaciones.size();
            }
        }

        if (pendientes > 0) {
            return "No se pudo asignar la mercancía a una ubicación.";
        }
        disponibles.merge(producto.codigo(), cantidad, Integer::sum);
        unidadesAlmacenadas += cantidad;
        return "Recibidas " + cantidad + " unidades de " + producto.nombre() + ".";
    }

    public synchronized boolean puedeAtender(List<ArticuloPedido> articulos) {
        Map<String, Integer> requeridas = sumarCantidades(articulos);
        return requeridas.entrySet().stream()
                .allMatch(e -> disponibles.getOrDefault(e.getKey(), 0) >= e.getValue());
    }

    public synchronized Optional<Reserva> reservar(List<ArticuloPedido> articulos) {
        Map<String, Integer> requeridas = sumarCantidades(articulos);
        boolean hayExistencias = requeridas.entrySet().stream()
                .allMatch(e -> disponibles.getOrDefault(e.getKey(), 0) >= e.getValue());
        if (!hayExistencias) {
            return Optional.empty();
        }
        requeridas.forEach((codigo, cantidad) -> disponibles.compute(codigo, (ignorado, actual) -> actual - cantidad));
        return Optional.of(new Reserva(requeridas));
    }

    public synchronized List<ResumenProducto> obtenerResumen() {
        return CatalogoProductos.obtenerProductos().stream()
                .map(producto -> {
                    LinkedHashMap<String, Integer> porUbicacion = ubicacionesPorProducto.getOrDefault(
                            producto.codigo(), new LinkedHashMap<>());
                    int fisicas = porUbicacion.values().stream().mapToInt(Integer::intValue).sum();
                    String textoUbicaciones = porUbicacion.entrySet().stream()
                            .map(e -> e.getKey() + " (" + e.getValue() + ")")
                            .collect(Collectors.joining(", "));
                    if (textoUbicaciones.isEmpty()) {
                        textoUbicaciones = "Sin ubicación";
                    }
                    int disponiblesProducto = disponibles.getOrDefault(producto.codigo(), 0);
                    return new ResumenProducto(producto, disponiblesProducto,
                            Math.max(0, fisicas - disponiblesProducto), textoUbicaciones);
                })
                .toList();
    }

    private Map<String, Integer> sumarCantidades(List<ArticuloPedido> articulos) {
        Map<String, Integer> requeridas = new HashMap<>();
        for (ArticuloPedido articulo : articulos) {
            requeridas.merge(articulo.producto().codigo(), articulo.cantidad(), Integer::sum);
        }
        return requeridas;
    }

    private void retirarUbicaciones(String codigoProducto, int cantidad) {
        LinkedHashMap<String, Integer> porUbicacion = ubicacionesPorProducto.get(codigoProducto);
        int pendientes = cantidad;
        for (var entrada : new ArrayList<>(porUbicacion.entrySet())) {
            if (pendientes == 0) {
                break;
            }
            int retiradas = Math.min(pendientes, entrada.getValue());
            porUbicacion.compute(entrada.getKey(), (ignorado, actual) -> actual == retiradas ? null : actual - retiradas);
            Ubicacion ubicacion = ubicaciones.stream()
                    .filter(u -> u.codigo.equals(entrada.getKey()))
                    .findFirst()
                    .orElseThrow();
            int restantesEnUbicacion = ubicacion.productos.get(codigoProducto) - retiradas;
            if (restantesEnUbicacion == 0) {
                ubicacion.productos.remove(codigoProducto);
            } else {
                ubicacion.productos.put(codigoProducto, restantesEnUbicacion);
            }
            ubicacion.ocupacion -= retiradas;
            pendientes -= retiradas;
        }
        if (porUbicacion.isEmpty()) {
            ubicacionesPorProducto.remove(codigoProducto);
        }
    }

    private static int leerCapacidad() {
        try {
            return Math.max(CANTIDAD_UBICACIONES,
                    Integer.parseInt(System.getProperty("redxela.capacidad", "500")));
        } catch (NumberFormatException ex) {
            return 500;
        }
    }

    private static final class Ubicacion {
        private final String codigo;
        private final int capacidad;
        private final Map<String, Integer> productos = new HashMap<>();
        private int ocupacion;

        private Ubicacion(String codigo, int capacidad) {
            this.codigo = codigo;
            this.capacidad = capacidad;
        }
    }

    public final class Reserva {
        private final Map<String, Integer> cantidades;
        private boolean finalizada;

        private Reserva(Map<String, Integer> cantidades) {
            this.cantidades = Map.copyOf(cantidades);
        }

        public synchronized void confirmarDespacho() {
            if (finalizada) {
                return;
            }
            synchronized (InventarioService.this) {
                cantidades.forEach((codigo, cantidad) -> retirarUbicaciones(codigo, cantidad));
                unidadesAlmacenadas -= cantidades.values().stream().mapToInt(Integer::intValue).sum();
            }
            finalizada = true;
        }

        public synchronized void liberar() {
            if (finalizada) {
                return;
            }
            synchronized (InventarioService.this) {
                cantidades.forEach((codigo, cantidad) -> disponibles.merge(codigo, cantidad, Integer::sum));
            }
            finalizada = true;
        }
    }
}
