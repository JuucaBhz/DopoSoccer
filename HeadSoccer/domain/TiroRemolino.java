package domain;

public class TiroRemolino extends Poder {
    private static final long serialVersionUID = 1L;
    public static final double velocidadRemolino = 900;
    public static final double gravedadRemolino = 3600;
    public static final double impulsoRebote = 450;

    // Crea el poder del remolino con su nombre, daño y efecto
    public TiroRemolino(String nombre, int danoBase, Efecto efecto) {
        super(nombre, danoBase, efecto);
    }

    // Dispara el balón rápido y recto hacia el arco rival, cargado con el poder
    @Override
    public void ejecutar(Jugador lanzador, Balon balon, Escenario escenario) {
        balon.impulsar(lanzador.getDireccionAtaque() * velocidadRemolino, 0);
        balon.cargarPoder(this, lanzador);
    }

    // Hace que el balón caiga con fuerza y rebote hacia arriba cada vez que toca el suelo
    @Override
    public void guiarTiro(Balon balon, double dt) {
        if (balon.tocoSuelo()) {
            balon.setVelocidad(balon.getVelX(), -impulsoRebote);
        } else {
            balon.setVelocidad(balon.getVelX(), balon.getVelY() + gravedadRemolino * dt);
        }
    }
}
