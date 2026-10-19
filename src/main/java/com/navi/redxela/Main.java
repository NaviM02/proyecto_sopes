package com.navi.redxela;

import com.navi.redxela.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Si el tema del sistema no está disponible, Swing usa su tema predeterminado.
            }
            new MainFrame().setVisible(true);
        });
    }
}
