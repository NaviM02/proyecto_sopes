package com.navi.redxela.ui;

import com.navi.redxela.domain.PoliticaAtencion;
import com.navi.redxela.service.MotorDistribucion;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

final class VistaPedidos extends JPanel {
    private final MotorDistribucion motor;
    private final ModeloPedidos modelo = new ModeloPedidos();
    private final JComboBox<PoliticaAtencion> politica = new JComboBox<>(PoliticaAtencion.values());
    private final JLabel descripcionPolitica = UIStyles.subtitulo("");
    private final JLabel estadoGenerador = UIStyles.subtitulo("Llegadas automáticas detenidas.");
    private final javax.swing.JButton botonGenerador = UIStyles.botonVerde("Iniciar generador");

    VistaPedidos(MotorDistribucion motor) {
        this.motor = motor;
        construirVista();
        politica.setSelectedItem(motor.getPolitica());
        descripcionPolitica.setText(motor.getPolitica().getDescripcion());
        actualizar();
    }

    void actualizar() {
        modelo.actualizar(motor.getPedidos());
        boolean activo = motor.isGeneradorActivo();
        botonGenerador.setText(activo ? "Detener generador" : "Iniciar generador");
        botonGenerador.setBackground(activo ? UIStyles.ROJO : UIStyles.VERDE);
        estadoGenerador.setText(activo
                ? "Llegan pedidos de todos los niveles cada pocos segundos."
                : "Llegadas automáticas detenidas.");
    }

    private void construirVista() {
        setLayout(new BorderLayout(0, 15));
        setBackground(UIStyles.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel encabezado = new JPanel(new BorderLayout(15, 0));
        encabezado.setOpaque(false);
        JPanel titulos = new JPanel(new BorderLayout(0, 4));
        titulos.setOpaque(false);
        titulos.add(UIStyles.titulo("Pedidos"), BorderLayout.NORTH);
        titulos.add(UIStyles.subtitulo("Crea cuentas, registra pedidos y compara políticas de atención."), BorderLayout.CENTER);
        encabezado.add(titulos, BorderLayout.WEST);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        var nuevaCuenta = UIStyles.botonSecundario("Nueva cuenta");
        var nuevoPedido = UIStyles.botonPrincipal("+ Nuevo pedido");
        nuevaCuenta.addActionListener(e -> Dialogos.nuevoCliente(this, motor));
        nuevoPedido.addActionListener(e -> {
            Window ventana = SwingUtilities.getWindowAncestor(this);
            Dialogos.nuevoPedido(ventana, motor);
        });
        botones.add(nuevaCuenta);
        botones.add(nuevoPedido);
        encabezado.add(botones, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 12));
        cuerpo.setOpaque(false);
        JPanel controles = UIStyles.card();
        controles.setLayout(new BorderLayout(14, 8));
        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        selector.setOpaque(false);
        JLabel etiqueta = new JLabel("Política de atención");
        etiqueta.setForeground(UIStyles.TINTA);
        selector.add(etiqueta);
        politica.setPreferredSize(new java.awt.Dimension(210, 34));
        politica.addActionListener(e -> {
            PoliticaAtencion seleccionada = (PoliticaAtencion) politica.getSelectedItem();
            if (seleccionada != null) {
                motor.setPolitica(seleccionada);
                descripcionPolitica.setText(seleccionada.getDescripcion());
            }
        });
        selector.add(politica);
        controles.add(selector, BorderLayout.WEST);
        JPanel descripcion = new JPanel(new BorderLayout(0, 5));
        descripcion.setOpaque(false);
        descripcion.add(descripcionPolitica, BorderLayout.NORTH);
        JPanel generador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        generador.setOpaque(false);
        botonGenerador.addActionListener(e -> {
            if (motor.isGeneradorActivo()) {
                motor.detenerGenerador();
            } else {
                motor.iniciarGenerador();
            }
            actualizar();
        });
        generador.add(estadoGenerador);
        generador.add(botonGenerador);
        descripcion.add(generador, BorderLayout.SOUTH);
        controles.add(descripcion, BorderLayout.CENTER);
        cuerpo.add(controles, BorderLayout.NORTH);

        JTable tabla = new JTable(modelo);
        UIStyles.estilizarTabla(tabla);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(125);
        cuerpo.add(UIStyles.panelConTitulo("Registro de pedidos", "El estado y el tiempo de espera se actualizan durante la simulación.",
                UIStyles.scrollTabla(tabla)), BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);
    }
}
