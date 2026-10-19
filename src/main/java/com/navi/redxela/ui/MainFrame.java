package com.navi.redxela.ui;

import com.navi.redxela.domain.EstadoConflicto;
import com.navi.redxela.service.ControlConflicto;
import com.navi.redxela.service.GestorRecepcion;
import com.navi.redxela.service.InventarioService;
import com.navi.redxela.service.MotorDistribucion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MainFrame extends JFrame {
    private static final String RESUMEN = "resumen";
    private static final String PEDIDOS = "pedidos";
    private static final String INVENTARIO = "inventario";
    private static final String CONFLICTO = "conflicto";
    private static final DateTimeFormatter RELOJ = DateTimeFormatter.ofPattern("EEE, d MMM · HH:mm:ss", new Locale("es", "GT"));

    private final InventarioService inventario = new InventarioService();
    private final MotorDistribucion motor = new MotorDistribucion(inventario);
    private final GestorRecepcion recepcion = new GestorRecepcion(inventario, motor::avisarInventarioActualizado);
    private final ControlConflicto conflicto = new ControlConflicto(motor.getGestorRecursos());
    private final CardLayout vistas = new CardLayout();
    private final JPanel contenedorVistas = new JPanel(vistas);
    private final Map<String, JButton> botonesNavegacion = new LinkedHashMap<>();
    private final JLabel tituloPagina = new JLabel("Resumen");
    private final JLabel reloj = new JLabel();
    private final JLabel estadoSistema = new JLabel("●  Sistema activo");
    private final VistaDashboard vistaDashboard = new VistaDashboard(motor);
    private final VistaPedidos vistaPedidos = new VistaPedidos(motor);
    private final VistaInventario vistaInventario = new VistaInventario(inventario, recepcion);
    private final VistaConflicto vistaConflicto = new VistaConflicto(conflicto);
    private final Timer refresco;

    public MainFrame() {
        super("REDXela · Centro de distribución");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setSize(1440, 900);
        setLocationRelativeTo(null);
        setContentPane(crearContenido());
        conflicto.setListener(() -> SwingUtilities.invokeLater(this::actualizarVistas));
        motor.agregarListener(() -> SwingUtilities.invokeLater(this::actualizarVistas));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarAplicacion();
            }
        });
        refresco = new Timer(500, e -> actualizarVistas());
        refresco.start();
    }

    private JPanel crearContenido() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(UIStyles.FONDO);
        raiz.add(crearBarraLateral(), BorderLayout.WEST);

        JPanel zonaTrabajo = new JPanel(new BorderLayout());
        zonaTrabajo.setBackground(UIStyles.FONDO);
        zonaTrabajo.add(crearBarraSuperior(), BorderLayout.NORTH);
        contenedorVistas.setBackground(UIStyles.FONDO);
        contenedorVistas.add(vistaDashboard, RESUMEN);
        contenedorVistas.add(vistaPedidos, PEDIDOS);
        contenedorVistas.add(vistaInventario, INVENTARIO);
        contenedorVistas.add(vistaConflicto, CONFLICTO);
        zonaTrabajo.add(contenedorVistas, BorderLayout.CENTER);
        raiz.add(zonaTrabajo, BorderLayout.CENTER);
        mostrarVista(RESUMEN, "Resumen");
        return raiz;
    }

    private JPanel crearBarraLateral() {
        JPanel lateral = new JPanel(new BorderLayout(0, 20));
        lateral.setBackground(UIStyles.TINTA);
        lateral.setPreferredSize(new Dimension(224, 0));
        lateral.setBorder(BorderFactory.createEmptyBorder(25, 16, 20, 16));

        JPanel marca = new JPanel(new BorderLayout(11, 0));
        marca.setOpaque(false);
        JLabel icono = new JLabel("R", SwingConstants.CENTER);
        icono.setOpaque(true);
        icono.setBackground(new Color(62, 126, 201));
        icono.setForeground(Color.WHITE);
        icono.setFont(new Font("SansSerif", Font.BOLD, 21));
        icono.setPreferredSize(new Dimension(42, 42));
        JLabel nombre = new JLabel("REDXela");
        nombre.setForeground(Color.WHITE);
        nombre.setFont(new Font("SansSerif", Font.BOLD, 19));
        JLabel lema = new JLabel("Centro de distribución");
        lema.setForeground(new Color(178, 190, 209));
        lema.setFont(new Font("SansSerif", Font.PLAIN, 10));
        JPanel etiquetas = new JPanel(new GridLayout(0, 1, 0, 2));
        etiquetas.setOpaque(false);
        etiquetas.add(nombre);
        etiquetas.add(lema);
        marca.add(icono, BorderLayout.WEST);
        marca.add(etiquetas, BorderLayout.CENTER);
        lateral.add(marca, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 8));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        agregarBotonNavegacion(menu, RESUMEN, "▦   Resumen");
        agregarBotonNavegacion(menu, PEDIDOS, "▤   Pedidos");
        agregarBotonNavegacion(menu, INVENTARIO, "▧   Inventario");
        agregarBotonNavegacion(menu, CONFLICTO, "⇄   Conflicto");
        lateral.add(menu, BorderLayout.CENTER);

        JPanel pie = new JPanel(new GridLayout(0, 1, 0, 6));
        pie.setOpaque(false);
        JLabel version = new JLabel("SIMULADOR ACADÉMICO");
        version.setForeground(new Color(178, 190, 209));
        version.setFont(new Font("SansSerif", Font.BOLD, 9));
        JLabel recursos = new JLabel("6 tipos de mercancía · 6 recursos");
        recursos.setForeground(new Color(220, 226, 235));
        recursos.setFont(new Font("SansSerif", Font.PLAIN, 10));
        pie.add(version);
        pie.add(recursos);
        lateral.add(pie, BorderLayout.SOUTH);
        return lateral;
    }

    private void agregarBotonNavegacion(JPanel menu, String clave, String texto) {
        JButton boton = new JButton(texto);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setForeground(new Color(224, 231, 241));
        boton.setBackground(UIStyles.TINTA);
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 8));
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        boton.addActionListener(e -> mostrarVista(clave, tituloDe(clave)));
        botonesNavegacion.put(clave, boton);
        menu.add(boton);
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyles.BORDE),
                BorderFactory.createEmptyBorder(15, 24, 15, 26)));
        tituloPagina.setFont(new Font("SansSerif", Font.BOLD, 16));
        tituloPagina.setForeground(UIStyles.TINTA);
        barra.add(tituloPagina, BorderLayout.WEST);

        JPanel estado = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        estado.setOpaque(false);
        this.estadoSistema.setForeground(UIStyles.VERDE);
        this.estadoSistema.setFont(new Font("SansSerif", Font.BOLD, 11));
        reloj.setForeground(UIStyles.SUAVE);
        reloj.setFont(new Font("SansSerif", Font.PLAIN, 11));
        estado.add(this.estadoSistema);
        estado.add(reloj);
        barra.add(estado, BorderLayout.EAST);
        return barra;
    }

    private void mostrarVista(String clave, String titulo) {
        vistas.show(contenedorVistas, clave);
        tituloPagina.setText(titulo);
        botonesNavegacion.forEach((id, boton) -> {
            boolean seleccionado = id.equals(clave);
            boton.setBackground(seleccionado ? new Color(51, 73, 105) : UIStyles.TINTA);
            boton.setForeground(seleccionado ? Color.WHITE : new Color(224, 231, 241));
        });
    }

    private String tituloDe(String clave) {
        return switch (clave) {
            case PEDIDOS -> "Pedidos";
            case INVENTARIO -> "Inventario y recepción";
            case CONFLICTO -> "Conflicto de recursos";
            default -> "Resumen";
        };
    }

    private void actualizarVistas() {
        if (!isDisplayable()) {
            return;
        }
        vistaDashboard.actualizar();
        vistaPedidos.actualizar();
        vistaInventario.actualizar();
        vistaConflicto.actualizar();
        reloj.setText(RELOJ.format(LocalDateTime.now()));
        EstadoConflicto estadoActual = conflicto.obtenerEstado();
        if (estadoActual.activo()) {
            estadoSistema.setText("●  Conflicto en curso");
            estadoSistema.setForeground(UIStyles.NARANJA);
        } else if (motor.isGeneradorActivo()) {
            estadoSistema.setText("●  Simulación en vivo");
            estadoSistema.setForeground(UIStyles.VERDE);
        } else {
            estadoSistema.setText("●  Sistema activo");
            estadoSistema.setForeground(UIStyles.VERDE);
        }
    }

    private void cerrarAplicacion() {
        refresco.stop();
        conflicto.close();
        recepcion.close();
        motor.close();
        dispose();
    }
}
