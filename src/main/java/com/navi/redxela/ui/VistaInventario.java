package com.navi.redxela.ui;

import com.navi.redxela.domain.Producto;
import com.navi.redxela.service.CatalogoProductos;
import com.navi.redxela.service.GestorRecepcion;
import com.navi.redxela.service.InventarioService;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

final class VistaInventario extends JPanel {
    private final InventarioService inventario;
    private final GestorRecepcion recepcion;
    private final ModeloInventario modeloInventario = new ModeloInventario();
    private final ModeloRecepciones modeloRecepciones = new ModeloRecepciones();
    private final JTable tablaStock = new JTable(modeloInventario);
    private final JLabel resumenCapacidad = UIStyles.subtitulo("");
    private final JLabel solicitudesPendientes = UIStyles.subtitulo("");
    private final JComboBox<Producto> producto = new JComboBox<>(CatalogoProductos.obtenerProductos().toArray(Producto[]::new));
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));

    VistaInventario(InventarioService inventario, GestorRecepcion recepcion) {
        this.inventario = inventario;
        this.recepcion = recepcion;
        construirVista();
        actualizar();
    }

    void actualizar() {
        modeloInventario.actualizar(inventario.obtenerResumen());
        for (int fila = 0; fila < tablaStock.getRowCount(); fila++) {
            String ubicaciones = String.valueOf(tablaStock.getValueAt(fila, 4));
            int lineas = ubicaciones.contains("<br>") ? ubicaciones.split("<br>").length : 1;
            tablaStock.setRowHeight(fila, Math.max(56, lineas * 20 + 12));
        }
        modeloRecepciones.actualizar(recepcion.getHistorial());
        resumenCapacidad.setText(inventario.getUnidadesAlmacenadas() + " / " + inventario.getCapacidad() + " unidades almacenadas");
        solicitudesPendientes.setText(recepcion.getPendientes() + " solicitudes esperando en la cola");
    }

    private void construirVista() {
        setLayout(new BorderLayout(0, 15));
        setBackground(UIStyles.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JPanel titulos = new JPanel(new BorderLayout(0, 4));
        titulos.setOpaque(false);
        titulos.add(UIStyles.titulo("Inventario y recepción"), BorderLayout.NORTH);
        titulos.add(UIStyles.subtitulo("Recepción envía solicitudes y el área de inventario las procesa en orden."), BorderLayout.CENTER);
        encabezado.add(titulos, BorderLayout.WEST);
        encabezado.add(resumenCapacidad, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 10));
        cuerpo.setOpaque(false);

        UIStyles.estilizarTabla(tablaStock);
        tablaStock.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablaStock.setRowHeight(56);
        tablaStock.getColumnModel().getColumn(0).setPreferredWidth(125);
        tablaStock.getColumnModel().getColumn(1).setPreferredWidth(115);
        tablaStock.getColumnModel().getColumn(2).setPreferredWidth(75);
        tablaStock.getColumnModel().getColumn(3).setPreferredWidth(75);
        tablaStock.getColumnModel().getColumn(4).setPreferredWidth(420);
        JPanel existencias = UIStyles.panelConTitulo("Existencias por producto", "Cada ubicación indica unidades de este producto y ocupación total de la ubicación (incluye productos reservados).",
                UIStyles.scrollTabla(tablaStock));

        JTable tablaRecepciones = new JTable(modeloRecepciones);
        UIStyles.estilizarTabla(tablaRecepciones);
        tablaRecepciones.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaRecepciones.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaRecepciones.getColumnModel().getColumn(1).setPreferredWidth(90);
        tablaRecepciones.getColumnModel().getColumn(2).setPreferredWidth(180);
        tablaRecepciones.getColumnModel().getColumn(3).setPreferredWidth(80);
        tablaRecepciones.getColumnModel().getColumn(4).setPreferredWidth(110);
        tablaRecepciones.getColumnModel().getColumn(5).setPreferredWidth(430);
        JPanel cola = UIStyles.panelConTitulo("Cola de recepción", "El resultado indica por qué una recepción fue aplicada o rechazada.", UIStyles.scrollTabla(tablaRecepciones));
        JPanel colaConTexto = new JPanel(new BorderLayout(0, 6));
        colaConTexto.setOpaque(false);
        colaConTexto.add(solicitudesPendientes, BorderLayout.NORTH);
        colaConTexto.add(cola, BorderLayout.CENTER);
        JSplitPane tablas = new JSplitPane(JSplitPane.VERTICAL_SPLIT, existencias, colaConTexto);
        tablas.setResizeWeight(0.52);
        tablas.setDividerSize(8);
        tablas.setBorder(null);
        cuerpo.add(crearFormularioRecepcion(), BorderLayout.NORTH);
        cuerpo.add(tablas, BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);
    }

    private JPanel crearFormularioRecepcion() {
        JPanel formulario = UIStyles.card();
        formulario.setLayout(new BorderLayout(14, 8));
        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 4));
        textos.setOpaque(false);
        JLabel titulo = new JLabel("Registrar llegada de mercadería");
        titulo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        titulo.setForeground(UIStyles.TINTA);
        textos.add(titulo);
        textos.add(UIStyles.subtitulo("El inventario procesa la solicitud en segundo plano y asigna ubicaciones automáticamente."));
        formulario.add(textos, BorderLayout.NORTH);

        JPanel campos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        campos.setOpaque(false);
        producto.setPreferredSize(new java.awt.Dimension(260, 34));
        cantidad.setPreferredSize(new java.awt.Dimension(90, 34));
        campos.add(new JLabel("Producto"));
        campos.add(producto);
        campos.add(new JLabel("Unidades"));
        campos.add(cantidad);
        var enviar = UIStyles.botonVerde("Enviar recepción");
        enviar.addActionListener(e -> enviarRecepcion());
        campos.add(enviar);
        formulario.add(campos, BorderLayout.CENTER);
        return formulario;
    }

    private void enviarRecepcion() {
        Producto seleccionado = (Producto) producto.getSelectedItem();
        if (seleccionado == null) {
            Dialogos.mostrarError(this, "Selecciona un producto del catálogo.");
            return;
        }
        boolean enviada = recepcion.recibir(seleccionado, (Integer) cantidad.getValue());
        if (!enviada) {
            Dialogos.mostrarError(this, "La cola está llena. Espera un momento y vuelve a intentarlo.");
        }
    }
}
