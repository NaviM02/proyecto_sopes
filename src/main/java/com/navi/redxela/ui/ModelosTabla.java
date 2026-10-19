package com.navi.redxela.ui;

import com.navi.redxela.domain.EventoRecepcion;
import com.navi.redxela.domain.Pedido;
import com.navi.redxela.domain.ResumenProducto;
import com.navi.redxela.domain.ResumenRecurso;

import javax.swing.table.AbstractTableModel;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

final class ModeloPedidos extends AbstractTableModel {
    private final String[] columnas = {"Pedido", "Cliente", "Servicio", "Unidades", "Mercancía", "Estado", "Progreso (%)", "Restante", "Espera"};
    private List<Pedido> pedidos = List.of();

    public void actualizar(List<Pedido> pedidos) {
        this.pedidos = List.copyOf(pedidos);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return pedidos.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnas[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 3, 6 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Pedido pedido = pedidos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> pedido.getId();
            case 1 -> pedido.getCliente().nombre();
            case 2 -> pedido.getCliente().nivelServicio().getNombre();
            case 3 -> pedido.getCantidadTotal();
            case 4 -> pedido.getResumenMercancia();
            case 5 -> pedido.getEstado().getNombre();
            case 6 -> pedido.getProgreso();
            case 7 -> duracion(pedido.getTiempoRestanteEstimadoMs());
            case 8 -> duracion(pedido.getTiempoEsperaMs());
            default -> "";
        };
    }

    private String duracion(long millis) {
        if (millis < 60_000) {
            return ((millis + 999) / 1_000) + " s";
        }
        return (millis / 60_000) + " min";
    }
}

final class ModeloPedidosResumen extends AbstractTableModel {
    private final String[] columnas = {"Pedido", "Cliente", "Servicio", "Unidades", "Estado", "Progreso (%)", "Restante"};
    private List<Pedido> pedidos = List.of();

    public void actualizar(List<Pedido> pedidos) {
        this.pedidos = List.copyOf(pedidos);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return pedidos.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnas[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 3 || columnIndex == 5 ? Integer.class : String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Pedido pedido = pedidos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> pedido.getId();
            case 1 -> pedido.getCliente().nombre();
            case 2 -> pedido.getCliente().nivelServicio().getNombre();
            case 3 -> pedido.getCantidadTotal();
            case 4 -> pedido.getEstado().getNombre();
            case 5 -> pedido.getProgreso();
            case 6 -> duracion(pedido.getTiempoRestanteEstimadoMs());
            default -> "";
        };
    }

    private String duracion(long millis) {
        return Math.max(0, (millis + 999) / 1_000) + " s";
    }
}

final class ModeloRecursos extends AbstractTableModel {
    private final String[] columnas = {"Recurso", "Disponibilidad", "Uso", "En uso por"};
    private List<ResumenRecurso> recursos = List.of();

    public void actualizar(List<ResumenRecurso> recursos) {
        this.recursos = List.copyOf(recursos);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return recursos.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnas[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ResumenRecurso resumen = recursos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> resumen.recurso().getNombre();
            case 1 -> (resumen.capacidad() - resumen.enUso()) + " / " + resumen.capacidad() + " libres";
            case 2 -> resumen.enUso() + " / " + resumen.capacidad();
            case 3 -> resumen.ocupantes();
            default -> "";
        };
    }
}

final class ModeloInventario extends AbstractTableModel {
    private final String[] columnas = {"Producto", "Mercancía", "Disponibles", "Reservadas", "Ubicaciones físicas"};
    private List<ResumenProducto> productos = List.of();

    public void actualizar(List<ResumenProducto> productos) {
        this.productos = List.copyOf(productos);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return productos.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnas[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 2 || columnIndex == 3 ? Integer.class : String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ResumenProducto resumen = productos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> resumen.producto().nombre();
            case 1 -> resumen.producto().tipoMercancia().getNombre();
            case 2 -> resumen.unidadesDisponibles();
            case 3 -> resumen.unidadesReservadas();
            case 4 -> resumen.ubicaciones();
            default -> "";
        };
    }
}

final class ModeloRecepciones extends AbstractTableModel {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());
    private final String[] columnas = {"Solicitud", "Hora", "Producto", "Unidades", "Estado", "Resultado"};
    private List<EventoRecepcion> eventos = List.of();

    public void actualizar(List<EventoRecepcion> eventos) {
        this.eventos = List.copyOf(eventos);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return eventos.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnas[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        EventoRecepcion evento = eventos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> evento.getId();
            case 1 -> HORA.format(evento.getSolicitadoEn());
            case 2 -> evento.getProducto().nombre();
            case 3 -> evento.getCantidad();
            case 4 -> evento.getEstado();
            case 5 -> evento.getDetalle();
            default -> "";
        };
    }
}
