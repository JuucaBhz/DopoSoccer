package domain;

public class JugadorHumano extends Jugador {
    private static final long serialVersionUID = 1L;

    // Crea un jugador controlado por el teclado con el personaje dado
    public JugadorHumano(Personaje personaje) {
        super(personaje);
    }

    // Crea un jugador humano cuyo progreso pertenece al usuario dado
    public JugadorHumano(Personaje personaje, Usuario usuario) {
        super(personaje, usuario);
    }
}
