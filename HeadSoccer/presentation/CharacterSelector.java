package presentation;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class CharacterSelector extends JPanel {

    private String[] countries;
    private int index = 0;
    private JLabel titleLabel;
    private Tarjeta tarjeta;
    private JButton countryButton;

    public CharacterSelector(String titulo, String[] countries, String marco){
        super(new BorderLayout(0, 10));
        this.countries = countries;
        setOpaque(false);

        titleLabel = new JLabel(titulo, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));

        tarjeta = new Tarjeta(marco);

        countryButton = new JButton();
        countryButton.setFont(new Font("Arial", Font.BOLD, 24));
        countryButton.addActionListener(e -> nextCountry());

        add(titleLabel, BorderLayout.NORTH);
        add(tarjeta, BorderLayout.CENTER);
        add(countryButton, BorderLayout.SOUTH);

        showCountry();
    }

    private void nextCountry(){
        index = (index + 1) % countries.length;
        showCountry();
    }

    private void showCountry(){
        String country = countries[index];
        countryButton.setText(country);
        tarjeta.mostrar(country, Imagenes.cabeza(country));
    }

    public void setTitle(String titulo){
        titleLabel.setText(titulo);
    }

    public String getSelectedCountry(){
        return countries[index];
    }

    // Dibuja el personaje dentro del hueco del marco de carta y el marco encima.
    // Si falta el marco, el personaje se dibuja solo; si falta el personaje, se escribe el país.
    private static class Tarjeta extends JComponent {
        private final BufferedImage marco;
        private final Rectangle hueco;
        private String pais = "";
        private BufferedImage personaje;

        Tarjeta(String nombreMarco) {
            marco = Imagenes.cargarSinMagenta(nombreMarco);
            hueco = marco == null ? null : buscarHueco(marco);
            setPreferredSize(new Dimension(200, 270));
            setFont(new Font("Arial", Font.BOLD, 30));
        }

        void mostrar(String pais, BufferedImage personaje) {
            this.pais = pais;
            this.personaje = personaje;
            repaint();
        }

        // El hueco es la zona transparente del centro del marco: se mide desde el centro hacia afuera.
        private static Rectangle buscarHueco(BufferedImage m) {
            int cx = m.getWidth() / 2;
            int cy = m.getHeight() / 2;
            if (transparente(m, cx, cy)) {
                int izq = cx;
                int der = cx;
                int arriba = cy;
                int abajo = cy;
                while (izq > 0 && transparente(m, izq - 1, cy)) izq--;
                while (der < m.getWidth() - 1 && transparente(m, der + 1, cy)) der++;
                while (arriba > 0 && transparente(m, cx, arriba - 1)) arriba--;
                while (abajo < m.getHeight() - 1 && transparente(m, cx, abajo + 1)) abajo++;
                return new Rectangle(izq, arriba, der - izq + 1, abajo - arriba + 1);
            }
            return null;
        }

        private static boolean transparente(BufferedImage m, int x, int y) {
            return (m.getRGB(x, y) >>> 24) < 40;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Rectangle zona = new Rectangle(0, 0, getWidth(), getHeight());
            if (marco != null && hueco != null) {
                double e = Math.min((double) getWidth() / marco.getWidth(), (double) getHeight() / marco.getHeight());
                int mw = (int) Math.round(marco.getWidth() * e);
                int mh = (int) Math.round(marco.getHeight() * e);
                int mx = (getWidth() - mw) / 2;
                int my = (getHeight() - mh) / 2;
                zona = new Rectangle(mx + (int) Math.round(hueco.x * e), my + (int) Math.round(hueco.y * e),
                    (int) Math.round(hueco.width * e), (int) Math.round(hueco.height * e));
                dibujarPersonaje(g2, zona);
                g2.drawImage(Imagenes.escalada(marco, mw, mh), mx, my, null);
            } else {
                dibujarPersonaje(g2, zona);
            }
        }

        // Cabe entero en la zona sin deformarse.
        private void dibujarPersonaje(Graphics2D g2, Rectangle zona) {
            if (personaje == null) {
                g2.setColor(Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(pais, zona.x + (zona.width - fm.stringWidth(pais)) / 2, zona.y + zona.height / 2);
                return;
            }
            double e = Math.min((double) zona.width / personaje.getWidth(), (double) zona.height / personaje.getHeight());
            int w = Math.max(1, (int) Math.round(personaje.getWidth() * e));
            int h = Math.max(1, (int) Math.round(personaje.getHeight() * e));
            g2.drawImage(Imagenes.escalada(personaje, w, h), zona.x + (zona.width - w) / 2, zona.y + (zona.height - h) / 2, null);
        }
    }
}
