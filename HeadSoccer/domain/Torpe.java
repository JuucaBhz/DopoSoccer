package domain;

import java.util.Random;

public class Torpe extends PerfilMaquina {
    private static final long serialVersionUID = 1L;
    public static final double probabilidadMover = 0.35;
    public static final double probabilidadQuieta = 0.60;
    public static final double probabilidadSaltar = 0.75;
    public static final double probabilidadPatear = 0.90;

    private Random azar;

    // Crea el perfil torpe con un generador de azar normal
    public Torpe() {
        this(new Random());
    }

    // Crea el perfil torpe con un azar dado (sirve para las pruebas)
    public Torpe(Random azar) {
        super();
        this.azar = azar;
    }

    // Elige una acción al azar: moverse, quedarse quieto, saltar, patear o usar el poder
    @Override
    protected void elegirAccion(Jugador yo, Escenario escenario) {
        double sorteo = azar.nextDouble();
        if (sorteo < probabilidadMover) {
            direccionActual = azar.nextBoolean() ? 1 : -1;
        } else if (sorteo < probabilidadQuieta) {
            direccionActual = 0;
        } else if (sorteo < probabilidadSaltar) {
            yo.saltar();
        } else if (sorteo < probabilidadPatear) {
            yo.patear(escenario);
        } else if (yo.getBarraPoder().estaLlena() && yo.getPoderActivo() == null) {
            yo.activarPoder();
        }
    }
}
