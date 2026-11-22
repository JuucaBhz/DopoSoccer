package domain;

import java.util.List;

// Lo poco que poderes, efectos y perfiles necesitan del juego en curso, sin conocer todo el juego:
// así un poder nuevo usa estos métodos genéricos en vez de pedir métodos nuevos a la clase del partido.
public interface Escenario {
    // Agrega un objeto extra a la cancha (por ejemplo un balón falso)
    void agregarObjeto(ObjetoFisico o);

    // Saca un objeto extra de la cancha
    void quitarObjeto(ObjetoFisico o);

    // Devuelve los objetos extra que hay ahora en la cancha
    List<ObjetoFisico> getObjetosExtra();

    // Devuelve el balón principal
    Balon getBalon();

    // Devuelve los jugadores del otro lado del jugador dado
    List<Jugador> getRivales(Jugador j);

    // Devuelve un número al azar entre 0 y 1
    double getAzar();

    // Gravedad de la cancha donde se juega
    double getGravedad();

    // Roce de la cancha donde se juega (cuánta velocidad conserva lo que rueda)
    double getRoce();
}
