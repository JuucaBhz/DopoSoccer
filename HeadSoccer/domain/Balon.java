package domain;

public class Balon extends ObjetoFisico {
    private static final long serialVersionUID = 1L;
    public static final double DIAMETRO = 40;
    public static final double REBOTE = 0.75;
    public static final double velocidadMinimaRebote = 60;
    public static final double duracionDisparo = 1.5;
    public static final double fuerzaCabezazo = 500;

    private Poder poderCargado;
    private Jugador lanzador;
    private double tiempoDisparo;

    // Crea un balón en la posición dada
    public Balon(double x, double y) {
        super(x, y, DIAMETRO, DIAMETRO);
    }

    // Mueve el balón: con poder sigue la trayectoria del poder, si no cae por la gravedad
    @Override
    public void actualizar(double dt) {
        if (tienePoder()) {
            poderCargado.guiarTiro(this, dt);
            tiempoDisparo -= dt;
            if (tiempoDisparo <= 0) {
                descargarPoder();
            }
        } else {
            velY += getGravedad() * dt;
        }
        avanzar(dt);
    }

    // Carga el balón con un poder y recuerda quién lo lanzó
    public void cargarPoder(Poder poder, Jugador lanzador) {
        poderCargado = poder;
        this.lanzador = lanzador;
        tiempoDisparo = duracionDisparo;
    }

    // Le quita el poder al balón y lo deja normal
    public void descargarPoder() {
        poderCargado = null;
        lanzador = null;
        tiempoDisparo = 0;
    }

    // Dice si el balón lleva un poder cargado
    public boolean tienePoder() {
        return poderCargado != null;
    }

    // Dice si el balón quedó apoyado (tocó el suelo u otra superficie por arriba) en este instante
    public boolean tocoSuelo() {
        return estaApoyado();
    }

    public Poder getPoderCargado() { return poderCargado; }
    public Jugador getLanzador() { return lanzador; }

    // Le da al balón una velocidad nueva (por ejemplo al patearlo)
    public void impulsar(double velX, double velY) {
        setVelocidad(velX, velY);
    }

    // Se agrega a la lista porque es un balón que se debe dibujar
    @Override
    public void recolectarBalones(java.util.List<Balon> destino) {
        destino.add(this);
    }

    // El balón se dibuja como balón
    @Override
    public String getApariencia() {
        return "balon";
    }

    // Los jugadores no se bloquean con el balón ni los balones entre sí: el balón no es sólido
    @Override
    public boolean esSolido() {
        return false;
    }

    // El balón conserva el 75% de su velocidad al rebotar
    @Override
    public double getRebote() {
        return REBOTE;
    }

    // Al rodar o botar sobre algo pierde un poco de velocidad y deja de botar si va muy lento
    @Override
    protected void alApoyarse() {
        velX *= getRoce();
        if (Math.abs(velY) < velocidadMinimaRebote) {
            velY = 0;
        }
    }

    // Con poder cargado impacta solo a un rival de quien lo lanzó, y entonces se descarga
    @Override
    public void afectar(ObjetoFisico otro) {
        if (tienePoder() && otro.esRivalDe(lanzador)) {
            Poder poder = poderCargado;
            Jugador quienLanzo = lanzador;
            descargarPoder();
            otro.recibirImpacto(poder, quienLanzo, escenario);
        }
    }

    // Cuando un jugador lo toca: cabezazo y, si el jugador tenía un poder activo, lo lanza
    @Override
    public void recibirToque(Jugador jugador) {
        cabecear(jugador);
        usarPoderDe(jugador);
    }

    // Rebota alejándose del jugador (como un cabezazo), con al menos la fuerza del cabezazo
    protected void cabecear(Jugador jugador) {
        double dx = getCentroX() - jugador.getCentroX();
        double dy = getCentroY() - jugador.getCentroY();
        double distancia = Math.max(1, Math.hypot(dx, dy));
        double nx = dx / distancia;
        double ny = dy / distancia;
        double rapidezBalon = Math.hypot(velX, velY);
        double rapidez = Math.max(fuerzaCabezazo, rapidezBalon * 0.8);
        setVelocidad(nx * rapidez + jugador.getVelX() * 0.5, ny * rapidez + jugador.getVelY() * 0.5);
        for (int i = 0; i < 60 && colisionaCon(jugador); i++) {
            setPosicion(x + nx * 2, y + ny * 2);
        }
    }

    // Si el jugador tenía su poder activo, lo dispara con este balón
    protected void usarPoderDe(Jugador jugador) {
        Poder poder = jugador.consumirPoder();
        if (poder != null) {
            poder.ejecutar(jugador, this, escenario);
        }
    }
}
