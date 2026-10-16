package com.navi.redxela.service;

import com.navi.redxela.domain.Recurso;
import com.navi.redxela.domain.ResumenRecurso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public final class GestorRecursos {
    private final Map<Recurso, Semaphore> semaforos = new EnumMap<>(Recurso.class);
    private final Map<Recurso, Map<String, Integer>> ocupantes = new EnumMap<>(Recurso.class);

    public GestorRecursos() {
        for (Recurso recurso : Recurso.values()) {
            semaforos.put(recurso, new Semaphore(recurso.getCapacidad(), true));
            ocupantes.put(recurso, new HashMap<>());
        }
    }

    // El intento es inmediato y todo o nada; así dos pedidos no se quedan con media reserva.
    public synchronized boolean intentarAdquirir(String propietario, Set<Recurso> recursos) {
        List<Recurso> ordenados = recursos.stream().sorted(Comparator.naturalOrder()).toList();
        List<Recurso> adquiridos = new ArrayList<>();
        for (Recurso recurso : ordenados) {
            if (!semaforos.get(recurso).tryAcquire()) {
                for (Recurso adquirido : adquiridos) {
                    liberarUnoInterno(adquirido, propietario);
                }
                return false;
            }
            registrar(recurso, propietario);
            adquiridos.add(recurso);
        }
        return true;
    }

    public synchronized void liberarTodos(String propietario, Set<Recurso> recursos) {
        for (Recurso recurso : recursos) {
            int unidades = ocupantes.get(recurso).getOrDefault(propietario, 0);
            for (int i = 0; i < unidades; i++) {
                liberarUnoInterno(recurso, propietario);
            }
        }
    }

    // El laboratorio de conflicto sí espera por un recurso para poder mostrar el interbloqueo.
    public void adquirirEsperando(Recurso recurso, String propietario) throws InterruptedException {
        semaforos.get(recurso).acquire();
        synchronized (this) {
            registrar(recurso, propietario);
        }
    }

    public synchronized void prepararConflicto(
            Recurso recursoA,
            String propietarioA,
            Recurso recursoB,
            String propietarioB
    ) {
        if (recursoA == recursoB) {
            throw new IllegalArgumentException("El conflicto necesita dos recursos distintos.");
        }
        Semaphore semaforoA = semaforos.get(recursoA);
        Semaphore semaforoB = semaforos.get(recursoB);
        if (!semaforoA.tryAcquire()) {
            throw new IllegalStateException("" + recursoA.getNombre() + " está ocupado.");
        }
        int adquiridosB = 0;
        while (adquiridosB < recursoB.getCapacidad() && semaforoB.tryAcquire()) {
            adquiridosB++;
        }
        if (adquiridosB < recursoB.getCapacidad()) {
            semaforoB.release(adquiridosB);
            semaforoA.release();
            throw new IllegalStateException("" + recursoB.getNombre() + " está ocupado.");
        }
        registrar(recursoA, propietarioA);
        ocupantes.get(recursoB).put(propietarioB, adquiridosB);
    }

    public synchronized List<ResumenRecurso> obtenerResumen() {
        return List.of(Recurso.values()).stream()
                .map(recurso -> {
                    Map<String, Integer> porPropietario = ocupantes.get(recurso);
                    int enUso = porPropietario.values().stream().mapToInt(Integer::intValue).sum();
                    String nombres = porPropietario.entrySet().stream()
                            .map(e -> e.getKey() + " (" + e.getValue() + ")")
                            .collect(Collectors.joining(", "));
                    return new ResumenRecurso(recurso, enUso, recurso.getCapacidad(),
                            nombres.isEmpty() ? "Disponible" : nombres);
                })
                .toList();
    }

    private void registrar(Recurso recurso, String propietario) {
        ocupantes.get(recurso).merge(propietario, 1, Integer::sum);
    }

    private void liberarUnoInterno(Recurso recurso, String propietario) {
        Map<String, Integer> porPropietario = ocupantes.get(recurso);
        Integer cantidad = porPropietario.get(propietario);
        if (cantidad == null) {
            return;
        }
        if (cantidad == 1) {
            porPropietario.remove(propietario);
        } else {
            porPropietario.put(propietario, cantidad - 1);
        }
        semaforos.get(recurso).release();
    }
}
