package domain;

import java.io.Serializable;

public class Poder implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final double velocidadDisparo = 1100;

    protected String nombre;
    protected int danoBase;
    private Efecto efectoBase;

    // Crea un poder con su nombre, su daño base y el efecto que causa
    public Poder(String nombre, int danoBase, Efecto efecto) {
        this.nombre = nombre;
        this.danoBase = danoBase;
        this.efectoBase = efecto;
    }

    // Dispara el balón recto hacia el arco rival, cargado con este poder
    public void ejecutar(Jugador lanzador, Balon balon, Escenario escenario) {
        balon.impulsar(lanzador.getDireccionAtaque() * velocidadDisparo, 0);
        balon.cargarPoder(this, lanzador);
    }

    // Cambia la trayectoria del balón mientras vuela; el poder normal no hace nada
    public void guiarTiro(Balon balon, double dt) {
    }

    // Le hace daño al rival y, si sigue en pie, le aplica el efecto del poder
    public void impactar(Jugador lanzador, Jugador rival, Escenario escenario) {
        rival.recibirDano(getDano(lanzador));
        if (!rival.estaAturdido()) {
            Efecto efecto = crearEfecto();
            rival.aplicarEfecto(efecto);
            efecto.afectarEscenario(escenario);
        }
    }

    // Calcula el daño del poder según la habilidad KICK del que lo lanzó
    public int getDano(Jugador lanzador) {
        return (int) Math.round(danoBase * lanzador.getEstadisticas().multiplicador(Habilidad.KICK));
    }

    // Devuelve una copia nueva del efecto para no compartirlo entre jugadores
    public Efecto crearEfecto() {
        return efectoBase.copiar();
    }

    public String getNombre() { return nombre; }
    public int getDanoBase() { return danoBase; }
}
