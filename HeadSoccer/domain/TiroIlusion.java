package domain;

import java.util.ArrayList;
import java.util.List;

public class TiroIlusion extends Poder {
    private static final long serialVersionUID = 1L;
    public static final int balonesFalsos = 7;
    public static final double anguloApertura = Math.toRadians(25);

    private double duracionIlusion;

    // Crea el poder de ilusión con su daño, su efecto y cuánto duran los balones falsos
    public TiroIlusion(String nombre, int danoBase, Efecto efecto, double duracionIlusion) {
        super(nombre, danoBase, efecto);
        this.duracionIlusion = duracionIlusion;
    }

    // Dispara el balón con un ángulo al azar y suelta varios balones falsos para confundir
    @Override
    public void ejecutar(Jugador lanzador, Balon balon, Escenario escenario) {
        double a = (escenario.getAzar() * 2 - 1) * anguloApertura;
        double direccion = lanzador.getDireccionAtaque();
        balon.impulsar(direccion * velocidadDisparo * Math.cos(a), -velocidadDisparo * Math.sin(a));
        balon.cargarPoder(this, lanzador);
        crearBalonesFalsos(escenario, balonesFalsos, duracionIlusion, anguloApertura);
    }

    // Quita los balones falsos anteriores y crea nuevos que salen junto al balón real
    public void crearBalonesFalsos(Escenario escenario, int cantidad, double duracion, double angulo) {
        Balon balon = escenario.getBalon();
        double rapidez = Math.hypot(balon.getVelX(), balon.getVelY());
        double direccion = Math.signum(balon.getVelX());
        // Se disipan las ilusiones anteriores (solo los balones falsos responden) y se retiran los expirados.
        List<ObjetoFisico> anteriores = new ArrayList<>(escenario.getObjetosExtra());
        for (ObjetoFisico o : anteriores) {
            o.disiparIlusion();
        }
        for (ObjetoFisico o : anteriores) {
            if (o.haExpirado()) {
                escenario.quitarObjeto(o);
            }
        }
        for (int i = 0; i < cantidad; i++) {
            BalonFalso falso = new BalonFalso(balon.getX(), balon.getY(), duracion);
            double a = (escenario.getAzar() * 2 - 1) * angulo;
            falso.impulsar(direccion * rapidez * Math.cos(a), -rapidez * Math.sin(a));
            if (balon.tienePoder()) {
                falso.cargarPoder(balon.getPoderCargado(), balon.getLanzador());
            }
            escenario.agregarObjeto(falso);
        }
    }
}
