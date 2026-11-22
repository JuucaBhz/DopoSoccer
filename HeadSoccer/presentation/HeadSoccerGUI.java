package presentation;

import domain.Estadisticas;
import domain.Habilidad;
import domain.HeadSoccer;
import domain.HeadSoccerException;
import domain.Usuario;
import java.awt.*;
import javax.swing.*;

public class HeadSoccerGUI extends JFrame{

    private JButton playButton;
    private CardLayout cards;
    private JPanel mainPanel;
    private JButton[] resolutionButtons;
    private JButton pvpButton;
    private JButton pvmButton;
    private JButton torpeButton;
    private JButton expertButton;
    private JButton offensiveButton;
    private JButton defensiveButton;
    private JButton backButton;
    private JButton skillsButton;
    private JButton skillsBackButton;
    private JButton[] upgradeButtons;
    private JLabel skillsTitle;
    private JLabel pointsLabel;
    private JLabel priceLabel;
    private JLabel[] levelLabels;
    private JButton startButton;
    private JButton matchTypeButton;
    private JButton valueButton;
    private int matchTypeIndex = 0;
    private int valueIndex = 0;
    private CharacterSelector player1Selector;
    private CharacterSelector player2Selector;
    private MatchPanel matchPanel;
    private HeadSoccer juego;

    
    private static final String[] matchTypes = {"TIME", "GOALS"};
    // Nombre que entiende la fábrica de partidos para cada opción de matchTypes
    private static final String[] matchTypeNames = {"Tiempo", "Goles"};
    private static final int[] MINUTES = {1, 2, 3};
    private static final int[] GOALS = {3, 5, 7};
    private static final String[] COUNTRIES = {"Corea del Sur", "Camerún", "Nigeria", "Estados Unidos"};
    private static final Dimension[] RESOLUTIONS = {
        new Dimension(800, 600),
        new Dimension(1280, 720),
        new Dimension(1600, 900)
    };



    public HeadSoccerGUI() {

        EstiloBoton.instalar();

        setTitle("HeadSoccer");
        setSize(RESOLUTIONS[0]);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        juego = new HeadSoccer();
        prepareElements();
        prepareActions();
    }

    private void prepareActions(){

        playButton.addActionListener(e -> cards.show(mainPanel, "RESOLUTION"));

        for (int i = 0; i < resolutionButtons.length; i++) {
            Dimension resolution = RESOLUTIONS[i];
            resolutionButtons[i].addActionListener(e -> selectResolution(resolution));
        }

        // En PvP se deja explícito que el jugador 2 es humano (por si antes se eligió una máquina).
        // Se cierra la sesión anterior del jugador 2 para que no bloquee el nombre del jugador 1.
        pvpButton.addActionListener(e -> {
            juego.cerrarSesion(2);
            if (selectModeInDomain(false, null) && login(1) && login(2)) {
                selectMode("JUGADOR 2", "CHAMP SELECT");
            }
        });
        pvmButton.addActionListener(e -> {
            juego.cerrarSesion(2);
            if (login(1)) {
                selectMode("MÁQUINA", "MACHINE PROFILE");
            }
        });

        torpeButton.addActionListener(e -> selectProfile("Torpe"));
        expertButton.addActionListener(e -> selectProfile("Experta"));
        offensiveButton.addActionListener(e -> selectProfile("Ofensiva"));
        defensiveButton.addActionListener(e -> selectProfile("Defensiva"));

        backButton.addActionListener(e -> cards.show(mainPanel, "MODE"));
        skillsButton.addActionListener(e -> showSkills());
        skillsBackButton.addActionListener(e -> cards.show(mainPanel, "CHAMP SELECT"));
        for (int i = 0; i < upgradeButtons.length; i++) {
            Habilidad habilidad = Habilidad.values()[i];
            upgradeButtons[i].addActionListener(e -> upgradeSkill(habilidad));
        }
        matchTypeButton.addActionListener(e -> changeMatchType());
        valueButton.addActionListener(e -> changeValue());
        startButton.addActionListener(e -> startMatch());

    }


    private void prepareElements(){

        cards = new CardLayout();
        mainPanel = new JPanel(cards);

        JPanel initPanel = new PanelConFondo("inicio.png", new GridBagLayout());
        playButton = new JButton("PLAY");
        playButton.setPreferredSize(new Dimension(450, 300));
        playButton.setFont(new Font("Roboto", Font.BOLD, 67));
        initPanel.add(playButton);


        mainPanel.add(initPanel, "INIT");
        mainPanel.add(prepareResolutionPanel(), "RESOLUTION");
        mainPanel.add(prepareModePanel(), "MODE");
        mainPanel.add(prepareProfilePanel(), "MACHINE PROFILE");
        mainPanel.add(prepareChampSelect(), "CHAMP SELECT");
        mainPanel.add(prepareSkillsPanel(), "SKILLS");
        matchPanel = new MatchPanel(() -> cards.show(mainPanel, "CHAMP SELECT"));
        mainPanel.add(matchPanel, "MATCH");
        add(mainPanel);

    }

    private JPanel prepareResolutionPanel(){

        JPanel buttons = transparente(new JPanel(new GridLayout(RESOLUTIONS.length, 1, 0, 20)));
        resolutionButtons = new JButton[RESOLUTIONS.length];

        for (int i = 0; i < RESOLUTIONS.length; i++) {
            Dimension resolution = RESOLUTIONS[i];
            resolutionButtons[i] = new JButton(resolution.width + " x " + resolution.height);
            resolutionButtons[i].setPreferredSize(new Dimension(450, 100));
            resolutionButtons[i].setFont(new Font("Roboto", Font.BOLD, 40));
            buttons.add(resolutionButtons[i]);
        }

        JPanel resolutionPanel = new PanelConFondo("menu.png", new GridBagLayout());
        resolutionPanel.add(buttons);
        return resolutionPanel;
    }

    private void selectResolution(Dimension resolution){
        setSize(resolution);
        setLocationRelativeTo(null);
        cards.show(mainPanel, "MODE");
    }

    private void startMatch(){
        String tipo = matchTypeNames[matchTypeIndex];
        int valor = currentValues()[valueIndex];
        juego.iniciarPartido(player1Selector.getSelectedCountry(), player2Selector.getSelectedCountry(), tipo, valor);
        cards.show(mainPanel, "MATCH");
        matchPanel.iniciar(juego);
    }

    // Solo avanza si el dominio aceptó el perfil; si no, el usuario se queda en MACHINE PROFILE.
    private void selectProfile(String profileName){
        if (selectModeInDomain(true, profileName)) {
            cards.show(mainPanel, "CHAMP SELECT");
        }
    }

    // La validación vive en el dominio; la GUI solo muestra el mensaje de la excepción.
    private boolean selectModeInDomain(boolean player2IsMachine, String profileName){
        try {
            juego.seleccionarModo(player2IsMachine, profileName);
            return true;
        } catch (HeadSoccerException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Perfil no disponible", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    private void selectMode(String player2Title, String nextCard){
        player2Selector.setTitle(player2Title);
        cards.show(mainPanel, nextCard);
    }

    private JPanel prepareChampSelect(){


        JPanel panel = new PanelConFondo("menu.png", new BorderLayout(10, 10));

        //Esta es la parte de arriba:
        JPanel top = transparente(new JPanel(new BorderLayout()));
        backButton = new JButton("BACK");
        JLabel title = new JLabel("PLAYER SELECT", SwingConstants.CENTER);
        title.setFont(new Font("Roboto", Font.BOLD, 60));
        top.add(backButton, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);

        //Centro
        player1Selector = new CharacterSelector("JUGADOR 1", COUNTRIES, "marco_jugador1.png");
        player2Selector = new CharacterSelector("JUGADOR 2", COUNTRIES, "marco_jugador2.png");

        JPanel center = transparente(new JPanel(new GridLayout(1, 3, 20, 0)));
        center.add(player1Selector);
        center.add(prepareMatchTypePanel());
        center.add(player2Selector);

        //Abajo
        JPanel bottom = transparente(new JPanel(new BorderLayout()));
        skillsButton = new JButton("SKILLS");
        startButton = new JButton("PLAY");
        bottom.add(skillsButton, BorderLayout.WEST);
        bottom.add(startButton, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }


    // Pantalla de mejoras del jugador 1: una fila por habilidad con su nivel y un botón MEJORAR.
    private JPanel prepareSkillsPanel(){

        Habilidad[] habilidades = Habilidad.values();
        JPanel rows = transparente(new JPanel(new GridLayout(habilidades.length, 3, 15, 10)));

        skillsTitle = new JLabel("SKILLS - JUGADOR 1", SwingConstants.CENTER);
        skillsTitle.setFont(new Font("Roboto", Font.BOLD, 40));
        pointsLabel = new JLabel("", SwingConstants.CENTER);
        pointsLabel.setFont(new Font("Roboto", Font.BOLD, 24));
        priceLabel = new JLabel("", SwingConstants.CENTER);
        priceLabel.setFont(new Font("Roboto", Font.BOLD, 24));

        upgradeButtons = new JButton[habilidades.length];
        levelLabels = new JLabel[habilidades.length];
        for (int i = 0; i < habilidades.length; i++) {
            JLabel name = new JLabel(habilidades[i].name(), SwingConstants.CENTER);
            name.setFont(new Font("Roboto", Font.BOLD, 24));
            levelLabels[i] = new JLabel("", SwingConstants.CENTER);
            levelLabels[i].setFont(new Font("Roboto", Font.BOLD, 24));
            upgradeButtons[i] = new JButton("MEJORAR");
            upgradeButtons[i].setFont(new Font("Roboto", Font.BOLD, 20));
            rows.add(name);
            rows.add(levelLabels[i]);
            rows.add(upgradeButtons[i]);
        }

        skillsBackButton = new JButton("BACK");

        JPanel top = transparente(new JPanel(new GridLayout(3, 1, 0, 5)));
        top.add(skillsTitle);
        top.add(pointsLabel);
        top.add(priceLabel);

        JPanel bottom = transparente(new JPanel(new BorderLayout()));
        bottom.add(skillsBackButton, BorderLayout.WEST);

        JPanel center = transparente(new JPanel(new GridBagLayout()));
        center.add(rows);

        JPanel panel = new PanelConFondo("menu.png", new BorderLayout(10, 10));
        panel.add(top, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // Se refresca al entrar porque puntos y niveles cambian después de cada partido.
    private void showSkills(){
        refreshSkills();
        cards.show(mainPanel, "SKILLS");
    }

    // Muestra el usuario con sesión del jugador 1 (existe aunque todavía no haya jugado un partido).
    private void refreshSkills(){
        Usuario usuario = juego.getUsuario(1);
        Estadisticas estadisticas = usuario.getEstadisticas();
        skillsTitle.setText("SKILLS - " + usuario.getNombre().toUpperCase());
        pointsLabel.setText("PUNTOS: " + usuario.getPuntos());
        priceLabel.setText("PRECIO SIGUIENTE MEJORA: " + estadisticas.precioSiguienteMejora());
        Habilidad[] habilidades = Habilidad.values();
        for (int i = 0; i < habilidades.length; i++) {
            levelLabels[i].setText("NIVEL " + estadisticas.getNivel(habilidades[i]) + " / " + Estadisticas.MAXIMO);
        }
    }

    // El dominio valida puntos y nivel máximo; la GUI solo muestra el mensaje de la excepción.
    private void upgradeSkill(Habilidad habilidad){
        try {
            juego.mejorarEstadistica(1, habilidad);
            refreshSkills();
        } catch (HeadSoccerException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se pudo mejorar", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Pide el nombre del jugador n hasta que sea válido; devuelve false si el usuario cancela
    private boolean login(int numero){
        while (true) {
            String nombre = JOptionPane.showInputDialog(this, "Nombre del jugador " + numero + ":", "Iniciar sesión", JOptionPane.QUESTION_MESSAGE);
            if (nombre == null) {
                return false;
            }
            try {
                juego.iniciarSesion(numero, nombre);
                return true;
            } catch (HeadSoccerException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Nombre inválido", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private JPanel prepareMatchTypePanel(){

        JPanel buttons = transparente(new JPanel(new GridLayout(2, 1, 0, 10)));

        matchTypeButton = new JButton(matchTypes[matchTypeIndex]);
        matchTypeButton.setFont(new Font("Roboto", Font.BOLD, 24));

        valueButton = new JButton();
        valueButton.setFont(new Font("Roboto", Font.BOLD, 24));

        buttons.add(matchTypeButton);
        buttons.add(valueButton);
        showValue();

        JPanel middle = transparente(new JPanel(new GridBagLayout()));
        middle.add(buttons);
        return middle;
    }

    private int[] currentValues(){
        return matchTypeIndex == 0 ? MINUTES : GOALS;
    }

    private void showValue(){
        String unidad = matchTypeIndex == 0 ? " MIN" : " GOALS";
        valueButton.setText(currentValues()[valueIndex] + unidad);
    }

    private void changeMatchType(){
        matchTypeIndex = (matchTypeIndex + 1) % matchTypes.length;
        matchTypeButton.setText(matchTypes[matchTypeIndex]);
        valueIndex = 0;
        showValue();
    }

    private void changeValue(){
        valueIndex = (valueIndex + 1) % currentValues().length;
        showValue();
    }

    private JPanel prepareProfilePanel(){

        JPanel buttons = transparente(new JPanel(new GridLayout(4, 1, 0, 20)));
        torpeButton = new JButton("TORPE BOT");
        torpeButton.setPreferredSize(new Dimension(500, 90));
        torpeButton.setFont(new Font("Roboto", Font.BOLD, 36));

        expertButton = new JButton("EXPERT BOT");
        expertButton.setPreferredSize(new Dimension(500, 90));
        expertButton.setFont(new Font("Roboto", Font.BOLD, 36));

        defensiveButton = new JButton("DEFENSIVE BOT");
        defensiveButton.setPreferredSize(new Dimension(500, 90));
        defensiveButton.setFont(new Font("Roboto", Font.BOLD, 36));

        offensiveButton = new JButton("OFFENSIVE BOT");
        offensiveButton.setPreferredSize(new Dimension(500, 90));
        offensiveButton.setFont(new Font("Roboto", Font.BOLD, 36));

        buttons.add(torpeButton);
        buttons.add(expertButton);
        buttons.add(defensiveButton);
        buttons.add(offensiveButton);

        JPanel profiPanel = new PanelConFondo("menu.png", new GridBagLayout());
        profiPanel.add(buttons);
        return profiPanel;

    }

    private JPanel prepareModePanel(){

        JPanel buttons = transparente(new JPanel(new GridLayout(2, 1, 0, 20)));
        pvpButton = new JButton("PLAYER VS PLAYER");
        pvpButton.setPreferredSize(new Dimension(600, 120));
        pvpButton.setFont(new Font("Roboto", Font.BOLD, 40));

        pvmButton = new JButton("PLAYER VS MACHINE");
        pvmButton.setPreferredSize(new Dimension(600, 120));
        pvmButton.setFont(new Font("Roboto", Font.BOLD, 40));

        buttons.add(pvpButton);
        buttons.add(pvmButton);

        JPanel modePanel = new PanelConFondo("menu.png", new GridBagLayout());
        modePanel.add(buttons);
        return modePanel;
    }
    // Los paneles de botones dejan ver la imagen de fondo de la pantalla.
    private static JPanel transparente(JPanel panel){
        panel.setOpaque(false);
        return panel;
    }

    public static void main(String[] args){
        HeadSoccerGUI ventana = new HeadSoccerGUI();
        ventana.setVisible(true);
    }
}
