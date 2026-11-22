package presentation;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

// Pintado propio de los botones (naranja claro con estado de mouse y de presión). Se instala una
// sola vez en UIManager, así lo usan todos los JButton, incluidos los de las ventanas emergentes,
// sin importar el Look and Feel del sistema (el de Windows ignora setBackground).
public class EstiloBoton extends BasicButtonUI {
    private static final Color BASE = new Color(0xFFB870);
    private static final Color ENCIMA = new Color(0xFFCB96);
    private static final Color PRESIONADO = new Color(0xE9994A);
    private static final Color DESACTIVADO = new Color(0xE3D3BF);
    private static final Color BORDE = new Color(0xC47A2C);
    private static final Color TEXTO = new Color(0x3A2200);
    private static final Color FONDO_AVISO = new Color(0xFFF3E4);
    private static final int redondeo = 20;

    public static void instalar() {
        UIManager.put("ButtonUI", EstiloBoton.class.getName());
        UIManager.put("Panel.background", FONDO_AVISO);
        UIManager.put("OptionPane.background", FONDO_AVISO);
        UIManager.put("OptionPane.messageForeground", TEXTO);
    }

    public static ComponentUI createUI(JComponent c) {
        return new EstiloBoton();
    }

    @Override
    protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        b.setOpaque(false);
        b.setRolloverEnabled(true);
        b.setFocusPainted(false);
        b.setForeground(TEXTO);
        b.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton boton = (AbstractButton) c;
        ButtonModel modelo = boton.getModel();
        Color relleno = BASE;
        if (!boton.isEnabled()) {
            relleno = DESACTIVADO;
        } else if (modelo.isArmed() && modelo.isPressed()) {
            relleno = PRESIONADO;
        } else if (modelo.isRollover()) {
            relleno = ENCIMA;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(relleno);
        g2.fillRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, redondeo, redondeo);
        g2.setColor(BORDE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, redondeo, redondeo);
        g2.dispose();
        super.paint(g, c);
    }
}
