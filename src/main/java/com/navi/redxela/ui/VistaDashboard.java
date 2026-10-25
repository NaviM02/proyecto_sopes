package com.navi.redxela.ui;

import com.navi.redxela.domain.Pedido;
import com.navi.redxela.service.MotorDistribucion;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

final class VistaDashboard extends JPanel {
    private final MotorDistribucion motor;
    private final JLabel completados = new JLabel("0");
    private final JLabel esperaPromedio = new JLabel("0 s");
    private final JLabel procesoPromedio = new JLabel("0 s");
    private final JLabel ocupacion = new JLabel("0 %");
    private final ModeloPedidosResumen modeloProceso = new ModeloPedidosResumen();
    private final ModeloPedidosResumen modeloEspera = new ModeloPedidosResumen();
    private final ModeloRecursos modeloRecursos = new ModeloRecursos();

    VistaDashboard(MotorDistribucion motor) {
        this.motor = motor;
        construirVista();
        actualizar();
    }

    void actualizar() {
        completados.setText(String.valueOf(motor.getPedidosCompletados()));
        esperaPromedio.setText(formatearDuracion(motor.getPromedioEsperaMs()));
        procesoPromedio.setText(formatearDuracion(motor.getPromedioProcesoMs()));
        ocupacion.setText(Math.round(motor.getOcupacionGeneral() * 100) + " %");
        List<Pedido> activos = motor.getPedidosEnProceso();
        modeloProceso.actualizar(activos);
        modeloEspera.actualizar(motor.getPedidosEnEspera().stream().limit(12).toList());
        modeloRecursos.actualizar(motor.getResumenRecursos());
    }

    private void construirVista() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyles.FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JPanel textos = new JPanel(new BorderLayout(0, 4));
        textos.setOpaque(false);
        textos.add(UIStyles.titulo("Centro de operaciones"), BorderLayout.NORTH);
        textos.add(UIStyles.subtitulo("Vista en tiempo real de pedidos, recursos y rendimiento."), BorderLayout.CENTER);
        encabezado.add(textos, BorderLayout.WEST);
        add(encabezado, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 13));
        contenido.setOpaque(false);
        JPanel metricas = new JPanel(new GridLayout(1, 4, 12, 0));
        metricas.setOpaque(false);
        metricas.add(tarjetaMetrica("Pedidos completados", completados, "Desde que inició la simulación", UIStyles.AZUL));
        metricas.add(tarjetaMetrica("Espera promedio", esperaPromedio, "Tiempo previo al procesamiento", UIStyles.VERDE));
        metricas.add(tarjetaMetrica("Proceso promedio", procesoPromedio, "Duración según unidades", UIStyles.NARANJA));
        metricas.add(tarjetaMetrica("Ocupación de recursos", ocupacion, "Uso actual del centro", new Color(125, 91, 173)));
        contenido.add(metricas, BorderLayout.NORTH);

        JPanel zonaCentral = new JPanel(new GridLayout(1, 2, 12, 0));
        zonaCentral.setOpaque(false);
        JTable tablaProceso = new JTable(modeloProceso);
        JTable tablaEspera = new JTable(modeloEspera);
        UIStyles.estilizarTabla(tablaProceso);
        UIStyles.estilizarTabla(tablaEspera);
        zonaCentral.add(UIStyles.panelConTitulo("En proceso", "Pedidos activos y avance actual.", UIStyles.scrollTabla(tablaProceso)));
        zonaCentral.add(UIStyles.panelConTitulo("Lista de espera", "Ordenada con la política seleccionada.", UIStyles.scrollTabla(tablaEspera)));
        contenido.add(zonaCentral, BorderLayout.CENTER);

        JTable tablaRecursos = new JTable(modeloRecursos);
        UIStyles.estilizarTabla(tablaRecursos);
        JPanel recursos = UIStyles.panelConTitulo("Recursos compartidos", "Capacidad disponible y pedidos que los están utilizando.",
                UIStyles.scrollTabla(tablaRecursos));
        recursos.setPreferredSize(new java.awt.Dimension(800, 330));
        contenido.add(recursos, BorderLayout.SOUTH);
        add(contenido, BorderLayout.CENTER);
    }

    private JPanel tarjetaMetrica(String titulo, JLabel valor, String detalle, Color acento) {
        JPanel tarjeta = UIStyles.card();
        tarjeta.setLayout(new BorderLayout(0, 6));
        JLabel nombre = new JLabel(titulo);
        nombre.setFont(new Font("SansSerif", Font.BOLD, 12));
        nombre.setForeground(UIStyles.SUAVE);
        valor.setFont(new Font("SansSerif", Font.BOLD, 25));
        valor.setForeground(acento);
        JLabel nota = UIStyles.subtitulo(detalle);
        tarjeta.add(nombre, BorderLayout.NORTH);
        tarjeta.add(valor, BorderLayout.CENTER);
        tarjeta.add(nota, BorderLayout.SOUTH);
        return tarjeta;
    }

    private String formatearDuracion(long millis) {
        if (millis < 60_000) {
            return (millis / 1_000) + " s";
        }
        return (millis / 60_000) + " min";
    }
}
