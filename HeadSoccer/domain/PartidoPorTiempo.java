package domain;

import java.util.List;

public class PartidoPorTiempo extends Partido {
    private static final long serialVersionUID = 1L;
    private double duracion;

    // Crea un partido que dura la cantidad de minutos indicada
    public PartidoPorTiempo(List<Jugador> jugadores, int minutos) {
        this(jugadores, new Cancha(), minutos);
    }

    // Igual que el anterior, pero jugado en la cancha dada
    public PartidoPorTiempo(List<Jugador> jugadores, Cancha cancha, int minutos) {
        super(jugadores, cancha);
        duracion = minutos * 60;
    }

    // Hay ganador cuando se acaba el tiempo y el marcador no está empatado
    @Override
    public boolean hayGanador() {
        return tiempoJugado >= duracion && ladoConMasGoles() != null;
    }

    // El reloj cuenta hacia atrás: muestra el tiempo que falta, sin bajar de cero
    @Override
    public double getTiempoMarcador() {
        return Math.max(0, duracion - tiempoJugado);
    }

    // Dice si se acabó el tiempo con empate y el siguiente gol decide
    @Override
    public boolean estaEnMuerteSubita() {
        return tiempoJugado >= duracion && ladoConMasGoles() == null;
    }

    // La meta es jugar cierta cantidad de minutos
    @Override
    public String getDescripcionObjetivo() {
        return Math.round(duracion / 60) + " MIN";
    }

    public double getDuracion() { return duracion; }
}
