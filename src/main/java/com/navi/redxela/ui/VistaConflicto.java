package com.navi.redxela.ui;

import com.navi.redxela.domain.EstadoConflicto;
import com.navi.redxela.service.ControlConflicto;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

final class VistaConflicto extends JPanel {
    private final ControlConflicto control;
    private final JLabel estado = new JLabel();
    private final JLabel recursos = UIStyles.subtitulo("");
    private final JLabel detalleA = UIStyles.subtitulo("");
    private final JLabel detalleB = UIStyles.subtitulo("");
    private final javax.swing.JButton iniciar = UIStyles.botonAlerta("Provocar conflicto");
    private final javax.swing.JButton resolverA = UIStyles.botonPrincipal("Continuar con A");
    private final javax.swing.JButton resolverB = UIStyles.botonPrincipal("Continuar con B");

    VistaConflicto(ControlConflicto control) {
        this.control = control;
        construirVista();
        actualizar();
    }

    void actualizar() {
        EstadoConflicto actual = control.obtenerEstado();
        estado.setText(actual.estado());
        recursos.setText("Recursos disputados: " + actual.recursos());
        detalleA.setText("<html>" + actual.operacionA() + "<br><br>" + actual.detalleA() + "</html>");
        detalleB.setText("<html>" + actual.operacionB() + "<br><br>" + actual.detalleB() + "</html>");
        boolean puedeResolver = actual.activo() && actual.estado().equals("Bloqueo detectado");
        resolverA.setEnabled(puedeResolver);
        resolverB.setEnabled(puedeResolver);
        iniciar.setEnabled(!actual.activo());
    }

    private void construirVista() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyles.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.setOpaque(false);
        encabezado.add(UIStyles.titulo("Conflicto de recursos"), BorderLayout.NORTH);
        encabezado.add(UIStyles.subtitulo("Provoca un interbloqueo real entre dos hilos y elige cuál operación continúa."), BorderLayout.CENTER);
        add(encabezado, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setOpaque(false);
        JPanel explicacion = UIStyles.card();
        explicacion.setLayout(new BorderLayout(0, 12));
        JPanel texto = new JPanel(new GridLayout(0, 1, 0, 7));
        texto.setOpaque(false);
        JLabel nombre = new JLabel("Escenario guiado de interbloqueo");
        nombre.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        nombre.setForeground(UIStyles.TINTA);
        texto.add(nombre);
        texto.add(UIStyles.subtitulo("A retiene el escáner y solicita calidad; B retiene las estaciones de calidad y solicita el escáner."));
        texto.add(UIStyles.subtitulo("Cada hilo espera un recurso que conserva el otro. Selecciona A o B para liberar al perdedor y devolverlo a espera."));
        texto.add(recursos);
        explicacion.add(texto, BorderLayout.CENTER);
        JPanel accion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        accion.setOpaque(false);
        iniciar.addActionListener(e -> {
            if (!control.iniciar()) {
                actualizar();
            }
        });
        accion.add(iniciar);
        explicacion.add(accion, BorderLayout.SOUTH);
        contenido.add(explicacion, BorderLayout.NORTH);

        JPanel operaciones = new JPanel(new GridLayout(1, 2, 14, 0));
        operaciones.setOpaque(false);
        operaciones.add(crearOperacion("Operación A", detalleA, resolverA, true));
        operaciones.add(crearOperacion("Operación B", detalleB, resolverB, false));
        contenido.add(operaciones, BorderLayout.CENTER);

        JPanel estadoPanel = UIStyles.card();
        estadoPanel.setLayout(new BorderLayout(0, 6));
        JLabel etiqueta = new JLabel("Estado del escenario");
        etiqueta.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        etiqueta.setForeground(UIStyles.SUAVE);
        estado.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        estado.setForeground(UIStyles.TINTA);
        estadoPanel.add(etiqueta, BorderLayout.NORTH);
        estadoPanel.add(estado, BorderLayout.CENTER);
        contenido.add(estadoPanel, BorderLayout.SOUTH);
        add(contenido, BorderLayout.CENTER);
    }

    private JPanel crearOperacion(String titulo, JLabel detalle, javax.swing.JButton boton, boolean esA) {
        JPanel tarjeta = UIStyles.card();
        tarjeta.setLayout(new BorderLayout(0, 14));
        JLabel nombre = new JLabel(titulo);
        nombre.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
        nombre.setForeground(esA ? UIStyles.AZUL : UIStyles.VERDE);
        tarjeta.add(nombre, BorderLayout.NORTH);
        detalle.setForeground(UIStyles.TEXTO);
        tarjeta.add(detalle, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        acciones.setOpaque(false);
        boton.addActionListener(e -> {
            control.resolver(esA);
            actualizar();
        });
        acciones.add(boton);
        tarjeta.add(acciones, BorderLayout.SOUTH);
        return tarjeta;
    }
}
