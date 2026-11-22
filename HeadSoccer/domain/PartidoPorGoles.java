package domain;

import java.util.List;

public class PartidoPorGoles extends Partido {
    private static final long serialVersionUID = 1L;
    private int golesObjetivo;

    // Crea un partido que gana el primer lado en llegar a cierta cantidad de goles
    public PartidoPorGoles(List<Jugador> jugadores, int golesObjetivo) {
        this(jugadores, new Cancha(), golesObjetivo);
    }

    // Igual que el anterior, pero jugado en la cancha dada
    public PartidoPorGoles(List<Jugador> jugadores, Cancha cancha, int golesObjetivo) {
        super(jugadores, cancha);
        this.golesObjetivo = golesObjetivo;
    }

    // Revisa si algún lado ya llegó a los goles necesarios
    @Override
    public boolean hayGanador() {
        for (Arco lado : cancha.getArcos()) {
            if (getGoles(lado) >= golesObjetivo) {
                return true;
            }
        }
        return false;
    }

    // En este modo el reloj cuenta hacia arriba: muestra el tiempo jugado
    @Override
    public double getTiempoMarcador() {
        return tiempoJugado;
    }

    // La meta es llegar a cierta cantidad de goles
    @Override
    public String getDescripcionObjetivo() {
        return "A " + golesObjetivo + " GOLES";
    }

    public int getGolesObjetivo() { return golesObjetivo; }
}
