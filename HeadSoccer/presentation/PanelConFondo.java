package presentation;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

// Panel con imagen de fondo en modo "cover" que se reescala sola al cambiar el tamaño.
// Si la imagen no existe se pinta un color liso (el problema ya queda en el log desde Imagenes).
public class PanelConFondo extends JPanel {
    private static final Color RESERVA = new Color(120, 190, 240);

    private final BufferedImage fondo;

    public PanelConFondo(String imagen, LayoutManager layout) {
        super(layout);
        fondo = Imagenes.cargar(imagen);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (fondo == null) {
            g.setColor(RESERVA);
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        Imagenes.dibujarCubriendo(g2, fondo, getWidth(), getHeight());
    }
}
