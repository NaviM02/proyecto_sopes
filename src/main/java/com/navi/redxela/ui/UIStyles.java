package com.navi.redxela.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class UIStyles {
    public static final Color TINTA = new Color(28, 43, 67);
    public static final Color AZUL = new Color(51, 105, 180);
    public static final Color VERDE = new Color(30, 145, 125);
    public static final Color FONDO = new Color(244, 247, 251);
    public static final Color TEXTO = new Color(56, 68, 86);
    public static final Color SUAVE = new Color(116, 129, 148);
    public static final Color BORDE = new Color(227, 232, 240);
    public static final Color NARANJA = new Color(224, 142, 49);
    public static final Color ROJO = new Color(190, 70, 75);

    private UIStyles() {
    }

    public static JPanel card() {
        JPanel panel = new TarjetaPanel();
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        return panel;
    }

    public static JLabel titulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.BOLD, 23));
        label.setForeground(TINTA);
        return label;
    }

    public static JLabel subtitulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(SUAVE);
        return label;
    }

    public static JButton botonPrincipal(String texto) {
        return boton(texto, AZUL, Color.WHITE);
    }

    public static JButton botonVerde(String texto) {
        return boton(texto, VERDE, Color.WHITE);
    }

    public static JButton botonSecundario(String texto) {
        JButton boton = boton(texto, Color.WHITE, TINTA);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), new EmptyBorder(8, 13, 8, 13)));
        return boton;
    }

    public static JButton botonAlerta(String texto) {
        return boton(texto, ROJO, Color.WHITE);
    }

    public static void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(34);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabla.setForeground(TEXTO);
        tabla.setBackground(Color.WHITE);
        tabla.setGridColor(new Color(239, 242, 247));
        tabla.setShowVerticalLines(false);
        tabla.setSelectionBackground(new Color(230, 239, 251));
        tabla.setSelectionForeground(TINTA);
        tabla.setFillsViewportHeight(true);
        tabla.setIntercellSpacing(new java.awt.Dimension(0, 1));
        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setFont(new Font("SansSerif", Font.BOLD, 11));
        encabezado.setForeground(SUAVE);
        encabezado.setBackground(new Color(248, 250, 253));
        encabezado.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE));
        encabezado.setReorderingAllowed(false);
        DefaultTableCellRenderer izquierda = new DefaultTableCellRenderer();
        izquierda.setBorder(new EmptyBorder(0, 9, 0, 8));
        tabla.setDefaultRenderer(Object.class, izquierda);
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(JLabel.CENTER);
        centrado.setBorder(new EmptyBorder(0, 5, 0, 5));
        tabla.setDefaultRenderer(Integer.class, centrado);
    }

    public static JScrollPane scrollTabla(JTable tabla) {
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    public static JPanel panelConTitulo(String titulo, String descripcion, Component contenido) {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 12));
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JLabel nombre = new JLabel(titulo);
        nombre.setFont(new Font("SansSerif", Font.BOLD, 15));
        nombre.setForeground(TINTA);
        encabezado.add(nombre, BorderLayout.NORTH);
        if (descripcion != null && !descripcion.isBlank()) {
            JLabel detalle = subtitulo(descripcion);
            detalle.setBorder(new EmptyBorder(4, 0, 0, 0));
            encabezado.add(detalle, BorderLayout.CENTER);
        }
        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(contenido, BorderLayout.CENTER);
        return panel;
    }

    private static JButton boton(String texto, Color fondo, Color tinta) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setForeground(tinta);
        boton.setBackground(fondo);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(new EmptyBorder(9, 14, 9, 14));
        return boton;
    }

    private static final class TarjetaPanel extends JPanel {
        private TarjetaPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g.setColor(BORDE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
