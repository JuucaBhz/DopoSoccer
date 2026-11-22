package presentation;

import domain.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MatchPanel extends JPanel {
    private static final double profundidadSuelo = 0.35;
    private static final int milisegundosFrame = 16;
    private static final double dtMaximo = 0.05;
    private static final Color AZUL = new Color(40, 120, 255);
    private static final Color NARANJA = new Color(255, 130, 30);
    private static final Color VIDA = new Color(220, 40, 40);
    private static final String extensionPartida = "partida";
    private static final int anchoBarraHud = 200;
    private static final int altoBloqueHud = 106;
    private static final Color TABLERO = new Color(15, 20, 35, 230);
    private static final Color BORDE = new Color(200, 210, 230);
    private static final Color colorObjetoPorDefecto = new Color(120, 70, 30);

    // Cómo dibujar algo a partir de su posición y tamaño en pantalla (una imagen o una figura)
    private interface Dibujo {
        void dibujar(Graphics2D g, int x, int y, int ancho, int alto);
    }

    private HeadSoccer juego;
    private Runnable alSalir;
    private Timer timer;
    private long ultimoFrame;
    private Set<Integer> teclas;
    private BufferedImage fondo;
    private Image pasto;
    private JButton botonPausa;
    // Apariencia -> cómo dibujarla; las apariencias que no están aquí se dibujan como un rectángulo
    private Map<String, Dibujo> dibujos;

    private double escala;
    private int origenX;
    private int suelo;

    public MatchPanel(Runnable alSalir) {
        this.alSalir = alSalir;
        teclas = new HashSet<>();
        setBackground(new Color(120, 190, 240));
        setFocusable(true);
        fondo = Imagenes.cargar("cancha.png");
        pasto = Imagenes.cargar("pasto.png");
        dibujos = prepararDibujos();
        timer = new Timer(milisegundosFrame, e -> siguienteFrame());
        prepareElements();
        prepareActions();
    }

    // Layout nulo para anclar el botón bajo el reloj (doLayout lo centra); así no tapa la info de los jugadores.
    // No toma el foco, así las teclas del partido siguen funcionando tras pulsarlo.
    private void prepareElements() {
        setLayout(null);
        botonPausa = new JButton("PAUSA");
        botonPausa.setFocusable(false);
        add(botonPausa);
    }

    // Centra el botón de pausa debajo del reloj cada vez que cambia el tamaño del panel
    @Override
    public void doLayout() {
        botonPausa.setBounds((getWidth() - 90) / 2, 112, 90, 28);
    }

    public void iniciar(HeadSoccer juego) {
        this.juego = juego;
        teclas.clear();
        ultimoFrame = System.nanoTime();
        timer.start();
        requestFocusInWindow();
    }

    private void prepareActions() {
        botonPausa.addActionListener(e -> {
            if (juego != null && !juego.estaTerminado()) {
                mostrarPausa();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                teclaPresionada(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                teclas.remove(codigo(e));
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                teclas.clear();
            }
        });
    }

    private int codigo(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL && e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
            return -KeyEvent.VK_CONTROL;
        }
        return e.getKeyCode();
    }

    private void teclaPresionada(KeyEvent e) {
        int tecla = codigo(e);
        boolean nueva = teclas.add(tecla);
        if (juego == null || juego.estaTerminado()) {
            return;
        }
        if (tecla == KeyEvent.VK_ESCAPE || tecla == KeyEvent.VK_P) {
            mostrarPausa();
            return;
        }
        if (juego.getPartido().estaPausado()) {
            return;
        }
        if (tecla == KeyEvent.VK_W) juego.saltar(1);
        if (tecla == KeyEvent.VK_S) juego.patear(1);
        if (tecla == KeyEvent.VK_Q && nueva) juego.activarPoder(1);
        if (tecla == KeyEvent.VK_UP) juego.saltar(2);
        if (tecla == KeyEvent.VK_DOWN) juego.patear(2);
        if (tecla == -KeyEvent.VK_CONTROL && nueva) juego.activarPoder(2);
    }

    private void siguienteFrame() {
        long ahora = System.nanoTime();
        double dt = Math.min(dtMaximo, (ahora - ultimoFrame) / 1e9);
        ultimoFrame = ahora;
        juego.mover(1, direccion(KeyEvent.VK_A, KeyEvent.VK_D));
        juego.mover(2, direccion(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT));
        juego.actualizar(dt);
        repaint();
        if (juego.estaTerminado()) {
            timer.stop();
            SwingUtilities.invokeLater(this::mostrarGanador);
        }
    }

    private int direccion(int izquierda, int derecha) {
        int dir = 0;
        if (teclas.contains(izquierda)) dir--;
        if (teclas.contains(derecha)) dir++;
        return dir;
    }

    private void mostrarPausa() {
        juego.pausar();
        teclas.clear();
        String[] opciones = {"Continuar", "Guardar partida", "Abrir partida", "Salir del partido"};
        int eleccion;
        // Guardar y abrir vuelven al menú: el partido sigue en pausa hasta "Continuar".
        do {
            eleccion = JOptionPane.showOptionDialog(this, "Partido en pausa", "Pausa",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
            if (eleccion == 1) {
                guardarPartida();
            } else if (eleccion == 2) {
                abrirPartida();
            }
        } while (eleccion == 1 || eleccion == 2);
        if (eleccion == 3) {
            juego.abandonar();
            timer.stop();
            alSalir.run();
            return;
        }
        juego.reanudar();
        ultimoFrame = System.nanoTime();
        requestFocusInWindow();
    }

    // Pide dónde guardar (agrega ".partida" si falta) y guarda; si falla muestra el mensaje y sigue en pausa
    private void guardarPartida() {
        JFileChooser selector = selectorDePartidas();
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith("." + extensionPartida)) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + "." + extensionPartida);
        }
        try {
            juego.salve(archivo);
            JOptionPane.showMessageDialog(this, "Partida guardada.", "Guardar partida", JOptionPane.INFORMATION_MESSAGE);
        } catch (HeadSoccerException e) {
            mostrarError(e);
        }
    }

    // Pide el archivo y abre la partida; el panel ya dibuja la partida cargada, que queda en pausa
    private void abrirPartida() {
        JFileChooser selector = selectorDePartidas();
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            juego.cargue(selector.getSelectedFile());
            teclas.clear();
            repaint();
        } catch (HeadSoccerException e) {
            mostrarError(e);
        }
    }

    // Selector de archivos que muestra solo las partidas de HeadSoccer
    private JFileChooser selectorDePartidas() {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Partidas de HeadSoccer (*." + extensionPartida + ")", extensionPartida));
        return selector;
    }

    private void mostrarError(HeadSoccerException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarGanador() {
        Partido partido = juego.getPartido();
        Arco izquierdo = partido.getCancha().getArcoIzquierdo();
        Arco derecho = partido.getCancha().getArcoDerecho();
        if (partido.getLadoGanador() != null) {
            Jugador ganador = partido.getGanadores().get(0);
            Jugador perdedor = partido.getRivales(ganador).get(0);
            String numero = ganador == juego.getJugador(1) ? "1" : "2";
            String mensaje = "¡Gana el Jugador " + numero + " (" + ganador.getPersonaje().getPais() + ")!\n"
                + "Marcador: " + partido.getGoles(izquierdo) + " - " + partido.getGoles(derecho) + "\n\n"
                + "Ganador: +" + Partido.puntosGanador + " puntos (total " + ganador.getPuntos() + ")\n"
                + "Perdedor: +" + Partido.puntosPerdedor + " puntos (total " + perdedor.getPuntos() + ")";
            JOptionPane.showMessageDialog(this, mensaje, "Fin del partido", JOptionPane.INFORMATION_MESSAGE);
        }
        alSalir.run();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int ancho = getWidth();
        int alto = getHeight();
        int alturaPasto = alturaDelPasto(ancho);
        int inicioPasto = alto - alturaPasto;
        suelo = inicioPasto + (int) (alturaPasto * profundidadSuelo);
        escala = Math.min(ancho / Cancha.ANCHO, suelo / Cancha.ALTO);
        origenX = (int) ((ancho - Cancha.ANCHO * escala) / 2);

        dibujarFondo(g2, ancho, alto);
        dibujarPasto(g2, ancho, inicioPasto, alturaPasto);
        if (juego == null || juego.getPartido() == null) {
            return;
        }
        Partido partido = juego.getPartido();
        dibujarArco(g2, partido.getCancha().getArcoIzquierdo());
        dibujarArco(g2, partido.getCancha().getArcoDerecho());
        for (Jugador j : partido.getJugadores()) {
            dibujarJugador(g2, j, colorDelLado(partido, j));
        }
        for (Balon extra : partido.getBalonesExtra()) {
            dibujarHaloPoder(g2, extra);
        }
        for (ObjetoFisico extra : partido.getObjetosExtra()) {
            dibujarObjeto(g2, extra);
        }
        dibujarHaloPoder(g2, partido.getBalon());
        dibujarObjeto(g2, partido.getBalon());
        dibujarHud(g2, ancho, partido);
    }

    private int px(double x) {
        return origenX + (int) Math.round(x * escala);
    }

    private int py(double y) {
        return suelo - (int) Math.round((Cancha.ALTO - y) * escala);
    }

    private int medida(double d) {
        return (int) Math.round(d * escala);
    }

    private int alturaDelPasto(int ancho) {
        if (pasto == null) {
            return 80;
        }
        return ancho * pasto.getHeight(null) / pasto.getWidth(null);
    }

    // La cancha cubre toda la ventana (cover); el pasto se dibuja encima como antes.
    private void dibujarFondo(Graphics2D g, int ancho, int alto) {
        if (fondo == null) {
            return;
        }
        Imagenes.dibujarCubriendo(g, fondo, ancho, alto);
    }

    private void dibujarPasto(Graphics2D g, int ancho, int inicioPasto, int alturaPasto) {
        if (pasto != null) {
            g.drawImage(pasto, 0, inicioPasto, ancho, alturaPasto, null);
        } else {
            g.setColor(new Color(40, 140, 40));
            g.fillRect(0, inicioPasto, ancho, alturaPasto);
        }
    }

    private void dibujarArco(Graphics2D g, Arco arco) {
        int x = px(arco.getX());
        int y = py(arco.getYTravesano());
        int w = medida(arco.getProfundidad());
        int h = medida(arco.getAlto());
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(x, y, w, h);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(Math.max(2, medida(Arco.grosorTravesano))));
        g.drawLine(x, y, x + w, y);
        int poste = arco.getX() == 0 ? x + w : x;
        g.drawLine(poste, y, poste, suelo);
        g.setStroke(new BasicStroke(1));
    }

    private void dibujarJugador(Graphics2D g, Jugador j, Color color) {
        int x = px(j.getX());
        int y = py(j.getY());
        int w = medida(j.getAncho());
        int h = medida(j.getAlto());
        int cabeza = w;

        if (j.getPoderActivo() != null) {
            g.setColor(new Color(255, 230, 0, 110));
            int halo = medida(14);
            g.fillOval(x - halo, y - halo, w + 2 * halo, h + 2 * halo);
        }

        Color cuerpo = j.estaAturdido() ? Color.GRAY : color;
        g.setColor(cuerpo);
        g.fillRoundRect(x + w / 6, y + cabeza - medida(6), w * 2 / 3, h - cabeza + medida(6), medida(10), medida(10));

        Image imagen = Imagenes.cabeza(j.getPersonaje().getPais());
        if (imagen != null) {
            dibujarCabeza(g, imagen, x, y, cabeza, j.getDireccionAtaque());
        } else {
            g.fillOval(x, y, cabeza, cabeza);
            int ojoX = j.getDireccionAtaque() > 0 ? x + w * 2 / 3 : x + w / 3;
            g.setColor(Color.WHITE);
            g.fillOval(ojoX - medida(7), y + cabeza / 3, medida(14), medida(14));
            g.setColor(Color.BLACK);
            g.fillOval(ojoX - medida(3) + j.getDireccionAtaque() * medida(2), y + cabeza / 3 + medida(4), medida(6), medida(6));
        }

        int largoPie = j.estaPateando() ? medida(Jugador.alcancePatada) : medida(16);
        int pieX = j.getDireccionAtaque() > 0 ? x + w - medida(8) : x + medida(8) - largoPie;
        g.setColor(Color.DARK_GRAY);
        g.fillRoundRect(pieX, y + h - medida(12), largoPie, medida(12), medida(8), medida(8));

        dibujarEstado(g, j, x, y, w);
    }

    private void dibujarCabeza(Graphics2D g, Image imagen, int x, int y, int tamano, int direccion) {
        int ancho = imagen.getWidth(null);
        int alto = imagen.getHeight(null);
        int h = tamano * alto / Math.max(1, ancho);
        int arriba = y + tamano - h;
        if (direccion > 0) {
            g.drawImage(imagen, x, arriba, tamano, h, null);
        } else {
            g.drawImage(imagen, x + tamano, arriba, -tamano, h, null);
        }
    }

    private void dibujarEstado(Graphics2D g, Jugador j, int x, int y, int w) {
        Efecto efecto = j.getEfectoActual();
        if (efecto == null) {
            return;
        }
        String texto = j.estaAturdido() ? "ATURDIDO" : efecto.getNombre().toUpperCase();
        g.setFont(new Font("Arial", Font.BOLD, Math.max(10, medida(16))));
        g.setColor(Color.BLACK);
        dibujarCentrado(g, texto, x + w / 2 + 1, y - medida(14) + 1);
        g.setColor(Color.YELLOW);
        dibujarCentrado(g, texto, x + w / 2, y - medida(14));
    }

    // Único lugar que dice cómo se ve cada apariencia (para una imagen nueva: registrarImagen aquí)
    private Map<String, Dibujo> prepararDibujos() {
        Map<String, Dibujo> mapa = new HashMap<>();
        mapa.put("balon", this::dibujarFiguraBalon);
        mapa.put("balonFalso", this::dibujarFiguraBalon);
        return mapa;
    }

    // Asocia una apariencia con una imagen de la carpeta images; si la imagen no existe, se queda el rectángulo
    private void registrarImagen(Map<String, Dibujo> mapa, String apariencia, String archivo) {
        BufferedImage imagen = Imagenes.cargarSinMagenta(archivo);
        if (imagen != null) {
            mapa.put(apariencia, (g, x, y, ancho, alto) -> g.drawImage(Imagenes.escalada(imagen, ancho, alto), x, y, null));
        }
    }

    // Dibuja cualquier objeto según su apariencia, o como rectángulo si no hay dibujo para ella
    private void dibujarObjeto(Graphics2D g, ObjetoFisico o) {
        Dibujo dibujo = dibujos.getOrDefault(o.getApariencia(), this::dibujarRectangulo);
        dibujo.dibujar(g, px(o.getX()), py(o.getY()), medida(o.getAncho()), medida(o.getAlto()));
    }

    // Dibujo por defecto: un rectángulo del tamaño del objeto
    private void dibujarRectangulo(Graphics2D g, int x, int y, int ancho, int alto) {
        g.setColor(colorObjetoPorDefecto);
        g.fillRect(x, y, ancho, alto);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, ancho, alto);
    }

    // Si el balón lleva un poder, dibuja el halo de color del poder alrededor
    private void dibujarHaloPoder(Graphics2D g, Balon b) {
        if (b.tienePoder()) {
            int x = px(b.getX());
            int y = py(b.getY());
            int d = medida(b.getAncho());
            g.setColor(colorPoder(b.getPoderCargado()));
            g.fillOval(x - d / 3, y - d / 3, d + 2 * d / 3, d + 2 * d / 3);
        }
    }

    // Figura del balón (círculo blanco con borde y centro negros), igual que antes
    private void dibujarFiguraBalon(Graphics2D g, int x, int y, int d, int alto) {
        g.setColor(Color.WHITE);
        g.fillOval(x, y, d, d);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawOval(x, y, d, d);
        g.fillOval(x + d / 2 - d / 6, y + d / 2 - d / 6, d / 3, d / 3);
        g.setStroke(new BasicStroke(1));
    }

    private Color colorPoder(Poder poder) {
        switch (poder.getNombre()) {
            case "AuraAzul":
                return new Color(60, 140, 255, 150);
            case "Rayo":
                return new Color(255, 240, 0, 170);
            case "Tornado":
                return new Color(200, 200, 200, 160);
            default:
                return new Color(190, 90, 255, 140);
        }
    }

    // Azul para el lado izquierdo y naranja para el derecho, como antes con el jugador 1 y el 2
    private Color colorDelLado(Partido partido, Jugador j) {
        return j.getArco() == partido.getCancha().getArcoIzquierdo() ? AZUL : NARANJA;
    }

    // Dibuja el marcador y un bloque de información por cada jugador, apilados en la esquina de su lado
    private void dibujarHud(Graphics2D g, int ancho, Partido partido) {
        dibujarMarcador(g, ancho, partido);
        Map<Arco, Integer> filasPorLado = new HashMap<>();
        for (Jugador j : juego.getJugadores()) {
            int fila = filasPorLado.getOrDefault(j.getArco(), 0);
            filasPorLado.put(j.getArco(), fila + 1);
            boolean izquierda = j.getArco() == partido.getCancha().getArcoIzquierdo();
            int x = izquierda ? 20 : ancho - 20 - anchoBarraHud;
            dibujarInfoJugador(g, x, 12 + fila * altoBloqueHud, j, colorDelLado(partido, j));
        }
    }

    // Nombre del usuario y país, barra de vida con su número, barra de poder, estado del poder y efecto actual
    private void dibujarInfoJugador(Graphics2D g, int x, int y, Jugador j, Color color) {
        g.setFont(new Font("Arial", Font.BOLD, 14));
        dibujarTexto(g, j.getUsuario().getNombre() + " (" + j.getPersonaje().getPais() + ")", x, y + 14, color);

        double proporcionVida = (double) j.getVida() / j.getVidaMax();
        dibujarBarra(g, x, y + 20, proporcionVida, VIDA);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        dibujarTexto(g, "VIDA " + j.getVida() + " / " + j.getVidaMax(), x + 6, y + 35, Color.WHITE);

        dibujarBarra(g, x, y + 44, j.getBarraPoder().getNivel(), Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        dibujarTexto(g, "PODER", x + 6, y + 59, Color.BLACK);

        g.setFont(new Font("Arial", Font.BOLD, 14));
        String estado = j.getPoderActivo() != null ? "PODER: " + j.getPoderActivo().getNombre()
            : j.getBarraPoder().estaLlena() ? "PODER LISTO" : "";
        dibujarTexto(g, estado, x, y + 80, Color.YELLOW);
        Efecto efecto = j.getEfectoActual();
        if (efecto != null) {
            dibujarTexto(g, "EFECTO: " + efecto.getNombre(), x, y + 98, Color.WHITE);
        }
    }

    // Escribe un texto con una sombra negra para que se lea sobre cualquier fondo
    private void dibujarTexto(Graphics2D g, String texto, int x, int y, Color color) {
        g.setColor(Color.BLACK);
        g.drawString(texto, x + 1, y + 1);
        g.setColor(color);
        g.drawString(texto, x, y);
    }

    private void dibujarMarcador(Graphics2D g, int ancho, Partido partido) {
        int anchoTablero = 240;
        int altoTablero = 64;
        int x = (ancho - anchoTablero) / 2;
        int y = 12;

        g.setColor(TABLERO);
        g.fillRoundRect(x, y, anchoTablero, altoTablero, 18, 18);
        g.setColor(BORDE);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, anchoTablero, altoTablero, 18, 18);

        int anchoCasilla = 80;
        int altoCasilla = altoTablero - 16;
        dibujarCasilla(g, x + 10, y + 8, anchoCasilla, altoCasilla, AZUL, partido.getGoles(partido.getCancha().getArcoIzquierdo()));
        dibujarCasilla(g, x + anchoTablero - 10 - anchoCasilla, y + 8, anchoCasilla, altoCasilla, NARANJA, partido.getGoles(partido.getCancha().getArcoDerecho()));

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        dibujarCentrado(g, "-", x + anchoTablero / 2, y + altoTablero / 2);

        String tiempo = textoTiempo(partido) + "  ·  " + partido.getDescripcionObjetivo();
        int anchoTiempo = 60 + 11 * tiempo.length();
        int altoTiempo = 30;
        int xt = (ancho - anchoTiempo) / 2;
        int yt = y + altoTablero - 2;
        g.setColor(TABLERO);
        g.fillRoundRect(xt, yt, anchoTiempo, altoTiempo, 12, 12);
        g.setColor(BORDE);
        g.drawRoundRect(xt, yt, anchoTiempo, altoTiempo, 12, 12);
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        dibujarCentrado(g, tiempo, ancho / 2, yt + altoTiempo / 2);
    }

    private String textoTiempo(Partido partido) {
        if (partido.estaEnMuerteSubita()) {
            return "GOL DE ORO";
        }
        int segundos = (int) Math.ceil(partido.getTiempoMarcador());
        return String.format("%d:%02d", segundos / 60, segundos % 60);
    }

    private void dibujarCasilla(Graphics2D g, int x, int y, int w, int h, Color color, int goles) {
        g.setColor(color);
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 34));
        dibujarCentrado(g, String.valueOf(goles), x + w / 2, y + h / 2);
    }

    private void dibujarCentrado(Graphics2D g, String texto, int cx, int cy) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, cx - fm.stringWidth(texto) / 2, cy + (fm.getAscent() - fm.getDescent()) / 2);
    }

    private void dibujarBarra(Graphics2D g, int x, int y, double nivel, Color relleno) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, anchoBarraHud, 20);
        g.setColor(relleno);
        g.fillRect(x, y, (int) (anchoBarraHud * Math.max(0, Math.min(1, nivel))), 20);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, anchoBarraHud, 20);
    }
}
