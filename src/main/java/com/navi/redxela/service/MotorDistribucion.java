package com.navi.redxela.service;

import com.navi.redxela.domain.ArticuloPedido;
import com.navi.redxela.domain.Cliente;
import com.navi.redxela.domain.EstadoPedido;
import com.navi.redxela.domain.NivelServicio;
import com.navi.redxela.domain.Pedido;
import com.navi.redxela.domain.PoliticaAtencion;
import com.navi.redxela.domain.Producto;
import com.navi.redxela.domain.Recurso;
import com.navi.redxela.domain.ResumenRecurso;
import com.navi.redxela.domain.TipoMercancia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class MotorDistribucion implements AutoCloseable {
    private final InventarioService inventario;
    private final GestorRecursos gestorRecursos = new GestorRecursos();
    private final CopyOnWriteArrayList<Cliente> clientes = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Pedido> pedidos = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService planificador = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread hilo = new Thread(r, "redxela-planificador");
        hilo.setDaemon(true);
        return hilo;
    });
    private final ScheduledExecutorService trabajadores = Executors.newScheduledThreadPool(6, r -> {
        Thread hilo = new Thread(r, "redxela-pedido");
        hilo.setDaemon(true);
        return hilo;
    });
    private final ScheduledExecutorService generador = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread hilo = new Thread(r, "redxela-generador");
        hilo.setDaemon(true);
        return hilo;
    });
    private final AtomicInteger consecutivoCliente = new AtomicInteger(5);
    private final AtomicLong completados = new AtomicLong();
    private volatile PoliticaAtencion politica = PoliticaAtencion.PRIORIDAD;
    private volatile ScheduledFuture<?> tareaGenerador;
    private volatile boolean cerrado;

    public MotorDistribucion(InventarioService inventario) {
        this.inventario = inventario;
        cargarClientesIniciales();
        cargarInventarioInicial();
        planificador.scheduleWithFixedDelay(this::intentarDespachar, 0, 200, TimeUnit.MILLISECONDS);
    }

    public Cliente registrarCliente(String nombre, NivelServicio nivelServicio) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("Escribe el nombre del cliente.");
        }
        Cliente cliente = new Cliente("CLI-%03d".formatted(consecutivoCliente.incrementAndGet()), limpio, nivelServicio);
        clientes.add(cliente);
        notificarCambio();
        return cliente;
    }

    public Pedido registrarPedido(Cliente cliente, List<ArticuloPedido> articulos) {
        if (cliente == null) {
            throw new IllegalArgumentException("Selecciona una cuenta de cliente.");
        }
        Pedido pedido = new Pedido(cliente, articulos);
        pedidos.add(pedido);
        notificarCambio();
        return pedido;
    }

    public List<Cliente> getClientes() {
        return List.copyOf(clientes);
    }

    public List<Pedido> getPedidos() {
        return pedidos.stream()
                .sorted(Comparator.comparing(Pedido::getCreadoEn).reversed())
                .toList();
    }

    public List<Pedido> getPedidosEnEspera() {
        return pedidos.stream()
                .filter(this::estaEnCola)
                .sorted(politica.comparador())
                .toList();
    }

    public List<Pedido> getPedidosEnProceso() {
        return pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.EN_PROCESO)
                .toList();
    }

    public List<ResumenRecurso> getResumenRecursos() {
        return gestorRecursos.obtenerResumen();
    }

    public InventarioService getInventario() {
        return inventario;
    }

    public GestorRecursos getGestorRecursos() {
        return gestorRecursos;
    }

    public PoliticaAtencion getPolitica() {
        return politica;
    }

    public void setPolitica(PoliticaAtencion politica) {
        this.politica = politica;
        notificarCambio();
    }

    public boolean isGeneradorActivo() {
        return tareaGenerador != null && !tareaGenerador.isCancelled();
    }

    public synchronized void iniciarGenerador() {
        if (isGeneradorActivo()) {
            return;
        }
        tareaGenerador = generador.scheduleWithFixedDelay(this::generarPedidoAleatorio,
                1, 4, TimeUnit.SECONDS);
    }

    public synchronized void detenerGenerador() {
        if (tareaGenerador != null) {
            tareaGenerador.cancel(false);
            tareaGenerador = null;
        }
    }

    public long getPedidosCompletados() {
        return completados.get();
    }

    public long getPromedioEsperaMs() {
        List<Pedido> terminados = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.COMPLETADO)
                .toList();
        if (terminados.isEmpty()) {
            return 0;
        }
        return (long) terminados.stream().mapToLong(Pedido::getTiempoEsperaMs).average().orElse(0);
    }

    public long getPromedioProcesoMs() {
        List<Pedido> terminados = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.COMPLETADO)
                .toList();
        if (terminados.isEmpty()) {
            return 0;
        }
        return (long) terminados.stream().mapToLong(Pedido::getTiempoProcesoMs).average().orElse(0);
    }

    public double getOcupacionGeneral() {
        List<ResumenRecurso> resumen = gestorRecursos.obtenerResumen();
        int enUso = resumen.stream().mapToInt(ResumenRecurso::enUso).sum();
        int capacidad = resumen.stream().mapToInt(ResumenRecurso::capacidad).sum();
        return capacidad == 0 ? 0 : (double) enUso / capacidad;
    }

    public void agregarListener(Runnable listener) {
        listeners.add(listener);
    }

    public void avisarInventarioActualizado() {
        notificarCambio();
    }

    private void intentarDespachar() {
        if (cerrado) {
            return;
        }
        try {
            for (Pedido pedido : getPedidosEnEspera()) {
                if (!inventario.puedeAtender(pedido.getArticulos())) {
                    pedido.actualizarEstado(EstadoPedido.ESPERANDO_INVENTARIO);
                    continue;
                }

                Optional<InventarioService.Reserva> posibleReserva = inventario.reservar(pedido.getArticulos());
                if (posibleReserva.isEmpty()) {
                    pedido.actualizarEstado(EstadoPedido.ESPERANDO_INVENTARIO);
                    continue;
                }

                Set<Recurso> necesarios = recursosNecesarios(pedido);
                if (!gestorRecursos.intentarAdquirir(pedido.getId(), necesarios)) {
                    posibleReserva.get().liberar();
                    pedido.actualizarEstado(EstadoPedido.ESPERANDO_RECURSOS);
                    continue;
                }

                pedido.actualizarEstado(EstadoPedido.EN_PROCESO);
                pedido.actualizarProgreso(0);
                InventarioService.Reserva reserva = posibleReserva.get();
                trabajadores.execute(() -> procesarPedido(pedido, necesarios, reserva));
                notificarCambio();
            }
        } catch (RuntimeException ex) {
            // Un pedido con datos inválidos no debe apagar el planificador.
        }
    }

    private void procesarPedido(Pedido pedido, Set<Recurso> necesarios, InventarioService.Reserva reserva) {
        boolean terminado = false;
        try {
            long duracionMs = Math.min(15_000, 450L + pedido.getCantidadTotal() * 320L);
            long inicio = System.nanoTime();
            while (true) {
                long transcurrido = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);
                int progreso = (int) Math.min(99, transcurrido * 100 / duracionMs);
                pedido.actualizarProgreso(progreso);
                if (transcurrido >= duracionMs) {
                    break;
                }
                Thread.sleep(Math.min(150, Math.max(10, duracionMs - transcurrido)));
            }
            reserva.confirmarDespacho();
            pedido.completar();
            completados.incrementAndGet();
            terminado = true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException ex) {
            // Si algo falla, se devuelve el stock para que el pedido pueda intentarse otra vez.
        } finally {
            if (!terminado) {
                reserva.liberar();
                pedido.actualizarProgreso(0);
                pedido.actualizarEstado(EstadoPedido.EN_ESPERA);
            }
            gestorRecursos.liberarTodos(pedido.getId(), necesarios);
            notificarCambio();
        }
    }

    private void generarPedidoAleatorio() {
        try {
            List<Cliente> cuentas = clientes;
            List<Producto> productos = CatalogoProductos.obtenerProductos();
            Cliente cliente = cuentas.get(ThreadLocalRandom.current().nextInt(cuentas.size()));
            Producto producto = productos.get(ThreadLocalRandom.current().nextInt(productos.size()));
            int cantidad = ThreadLocalRandom.current().nextInt(1, 5);
            registrarPedido(cliente, List.of(new ArticuloPedido(producto, cantidad)));
        } catch (RuntimeException ex) {
            // Si una llegada sale mal, el siguiente ciclo del generador sigue funcionando.
        }
    }

    private Set<Recurso> recursosNecesarios(Pedido pedido) {
        Set<Recurso> necesarios = EnumSet.noneOf(Recurso.class);
        pedido.getArticulos().forEach(articulo -> necesarios.addAll(articulo.producto().tipoMercancia().getRecursos()));
        return necesarios;
    }

    private boolean estaEnCola(Pedido pedido) {
        EstadoPedido estado = pedido.getEstado();
        return estado == EstadoPedido.EN_ESPERA
                || estado == EstadoPedido.ESPERANDO_INVENTARIO
                || estado == EstadoPedido.ESPERANDO_RECURSOS;
    }

    private void cargarClientesIniciales() {
        clientes.add(new Cliente("CLI-001", "Ana López", NivelServicio.EXPRESO));
        clientes.add(new Cliente("CLI-002", "Luis García", NivelServicio.PRIORITARIO));
        clientes.add(new Cliente("CLI-003", "Marta Pérez", NivelServicio.ESTANDAR));
        clientes.add(new Cliente("CLI-004", "Carlos Ruiz", NivelServicio.PROGRAMADO));
        clientes.add(new Cliente("CLI-005", "Sofía Morales", NivelServicio.ECONOMICO));
    }

    private void cargarInventarioInicial() {
        for (Producto producto : CatalogoProductos.obtenerProductos()) {
            inventario.recibir(producto, 25);
        }
    }

    private void notificarCambio() {
        listeners.forEach(listener -> {
            try {
                listener.run();
            } catch (RuntimeException ex) {
                // Una pantalla cerrada no debe afectar los hilos del simulador.
            }
        });
    }

    @Override
    public void close() {
        cerrado = true;
        detenerGenerador();
        planificador.shutdownNow();
        trabajadores.shutdownNow();
        generador.shutdownNow();
    }
}
