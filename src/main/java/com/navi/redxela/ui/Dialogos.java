package com.navi.redxela.ui;

import com.navi.redxela.domain.ArticuloPedido;
import com.navi.redxela.domain.Cliente;
import com.navi.redxela.domain.NivelServicio;
import com.navi.redxela.domain.Pedido;
import com.navi.redxela.domain.Producto;
import com.navi.redxela.service.CatalogoProductos;
import com.navi.redxela.service.MotorDistribucion;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

final class Dialogos {
    private Dialogos() {
    }

    static void nuevoCliente(Component parent, MotorDistribucion motor) {
        JTextField nombre = new JTextField(22);
        JComboBox<NivelServicio> servicio = new JComboBox<>(NivelServicio.values());
        JPanel formulario = new JPanel(new GridLayout(0, 1, 0, 8));
        formulario.add(new JLabel("Nombre del cliente"));
        formulario.add(nombre);
        formulario.add(new JLabel("Nivel de servicio"));
        formulario.add(servicio);
        int resultado = JOptionPane.showConfirmDialog(parent, formulario, "Registrar cuenta",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Cliente nuevo = motor.registrarCliente(nombre.getText(), (NivelServicio) servicio.getSelectedItem());
            JOptionPane.showMessageDialog(parent, "Cuenta creada para " + nuevo.nombre() + ".",
                    "Cuenta registrada", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Revisa los datos", JOptionPane.WARNING_MESSAGE);
        }
    }

    static void nuevoPedido(Window owner, MotorDistribucion motor) {
        new DialogoPedido(owner, motor).setVisible(true);
    }

    static void mostrarError(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "No se pudo completar", JOptionPane.WARNING_MESSAGE);
    }

    private static final class DialogoPedido extends JDialog {
        private final MotorDistribucion motor;
        private final JComboBox<Cliente> clientes;
        private final DefaultListModel<String> filas = new DefaultListModel<>();
        private final List<ArticuloPedido> articulos = new ArrayList<>();

        private DialogoPedido(Window owner, MotorDistribucion motor) {
            super(owner, "Registrar pedido", ModalityType.APPLICATION_MODAL);
            this.motor = motor;
            clientes = new JComboBox<>(motor.getClientes().toArray(Cliente[]::new));
            construirVentana();
        }

        private void construirVentana() {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setMinimumSize(new Dimension(540, 440));
            setSize(600, 500);
            setLocationRelativeTo(getOwner());

            JPanel contenido = new JPanel(new BorderLayout(0, 16));
            contenido.setBackground(UIStyles.FONDO);
            contenido.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

            JPanel encabezado = new JPanel(new BorderLayout(0, 5));
            encabezado.setOpaque(false);
            encabezado.add(UIStyles.titulo("Nuevo pedido"), BorderLayout.NORTH);
            encabezado.add(UIStyles.subtitulo("Selecciona una cuenta y agrega los artículos solicitados."), BorderLayout.CENTER);
            contenido.add(encabezado, BorderLayout.NORTH);

            JPanel centro = UIStyles.card();
            centro.setLayout(new BorderLayout(0, 12));
            JPanel cuenta = new JPanel(new BorderLayout(0, 6));
            cuenta.setOpaque(false);
            cuenta.add(new JLabel("Cuenta del cliente"), BorderLayout.NORTH);
            cuenta.add(clientes, BorderLayout.CENTER);
            centro.add(cuenta, BorderLayout.NORTH);

            JList<String> lista = new JList<>(filas);
            lista.setVisibleRowCount(7);
            lista.setFixedCellHeight(30);
            lista.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            JScrollPane scroll = new JScrollPane(lista);
            scroll.setBorder(BorderFactory.createLineBorder(UIStyles.BORDE));
            centro.add(scroll, BorderLayout.CENTER);

            JPanel accionesArticulos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            accionesArticulos.setOpaque(false);
            var agregar = UIStyles.botonSecundario("Agregar artículo");
            var quitar = UIStyles.botonSecundario("Quitar seleccionado");
            agregar.addActionListener(e -> agregarArticulo());
            quitar.addActionListener(e -> {
                int indice = lista.getSelectedIndex();
                if (indice >= 0) {
                    articulos.remove(indice);
                    filas.remove(indice);
                }
            });
            accionesArticulos.add(agregar);
            accionesArticulos.add(quitar);
            JPanel pieCentro = new JPanel(new BorderLayout(0, 9));
            pieCentro.setOpaque(false);
            pieCentro.add(new JLabel("Artículos del pedido"), BorderLayout.NORTH);
            pieCentro.add(accionesArticulos, BorderLayout.CENTER);
            centro.add(pieCentro, BorderLayout.SOUTH);
            contenido.add(centro, BorderLayout.CENTER);

            JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pie.setOpaque(false);
            var cancelar = UIStyles.botonSecundario("Cancelar");
            var guardar = UIStyles.botonPrincipal("Crear pedido");
            cancelar.addActionListener(e -> dispose());
            guardar.addActionListener(e -> crearPedido());
            pie.add(cancelar);
            pie.add(guardar);
            contenido.add(pie, BorderLayout.SOUTH);
            setContentPane(contenido);
        }

        private void agregarArticulo() {
            JComboBox<Producto> producto = new JComboBox<>(CatalogoProductos.obtenerProductos().toArray(Producto[]::new));
            JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
            JPanel formulario = new JPanel(new GridLayout(0, 1, 0, 7));
            formulario.add(new JLabel("Producto y tipo de mercancía"));
            formulario.add(producto);
            formulario.add(new JLabel("Unidades"));
            formulario.add(cantidad);
            int resultado = JOptionPane.showConfirmDialog(this, formulario, "Agregar artículo",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (resultado != JOptionPane.OK_OPTION) {
                return;
            }
            Producto seleccionado = (Producto) producto.getSelectedItem();
            if (seleccionado == null) {
                return;
            }
            int unidades = (Integer) cantidad.getValue();
            articulos.add(new ArticuloPedido(seleccionado, unidades));
            filas.addElement(seleccionado.nombre() + " · " + seleccionado.tipoMercancia().getNombre()
                    + " · " + unidades + " unidades");
        }

        private void crearPedido() {
            if (articulos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Agrega al menos un artículo al pedido.",
                        "Pedido incompleto", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Pedido pedido = motor.registrarPedido((Cliente) clientes.getSelectedItem(), articulos);
                JOptionPane.showMessageDialog(this, "El pedido " + pedido.getId() + " quedó registrado.",
                        "Pedido creado", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Revisa los datos", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
