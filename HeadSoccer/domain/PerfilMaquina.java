package domain;

import java.io.Serializable;

public abstract class PerfilMaquina implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final double tiempoReaccionPorDefecto = 0.3;

    protected double tiempoReaccion;
    protected double temporizador;
    protected int direccionActual;

    // Crea un perfil con el tiempo de reacción por defecto
    public PerfilMaquina() {
        this(tiempoReaccionPorDefecto);
    }

    // Crea un perfil que piensa cada cierto tiempo de reacción
    public PerfilMaquina(double tiempoReaccion) {
        this.tiempoReaccion = tiempoReaccion;
    }

    // Cada vez que pasa el tiempo de reacción elige una acción, y mueve a la máquina
    public final void decidir(Jugador yo, Escenario escenario, double dt) {
        if (yo.estaAturdido()) {
            return;
        }
        temporizador += dt;
        if (temporizador >= tiempoReaccion) {
            temporizador = 0;
            elegirAccion(yo, escenario);
        }
        yo.mover(direccionActual);
    }

    // Cada perfil decide a su manera qué hace la máquina
    protected abstract void elegirAccion(Jugador yo, Escenario escenario);

    // Fábrica con el mismo estilo de Personaje.crear. Al implementar un perfil nuevo
    // solo se cambia su línea aquí; mientras tanto avisa que no está disponible.
    public static PerfilMaquina crear(String nombre) throws HeadSoccerException {
        switch (nombre) {
            case "Torpe":
                return new Torpe();
            case "Experta":
                return new Experta();
            case "Ofensiva":
            case "Defensiva":
                throw new HeadSoccerException(HeadSoccerException.PERFIL_NO_DISPONIBLE);
            default:
                throw new DatoInvalidoException(DatoInvalidoException.PERFIL_INVALIDO, nombre);
        }
    }

    // El perfil se reutiliza entre partidos; sin esto arrastraría el temporizador
    // y la dirección del partido anterior.
    public void reiniciar() {
        temporizador = 0;
        direccionActual = 0;
    }

    public double getTiempoReaccion() { return tiempoReaccion; }
    public int getDireccionActual() { return direccionActual; }
}
