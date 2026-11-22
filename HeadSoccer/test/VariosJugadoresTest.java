package test;

import static org.junit.jupiter.api.Assertions.*;

import domain.*;
import java.util.List;
import org.junit.jupiter.api.Test;

public class VariosJugadoresTest {

    private Jugador jugador(String pais) {
        return new JugadorHumano(Personaje.crear(pais));
    }

    // Mete el balón en el arco dado y avanza un instante para que el partido cuente el gol
    private void golEn(Partido partido, Arco arco) {
        Balon balon = partido.getBalon();
        balon.descargarPoder();
        balon.setPosicion(arco.getX() + 20, partido.getCancha().getSuelo() - Balon.DIAMETRO);
        balon.setVelocidad(0, 0);
        partido.actualizar(1.0 / 60);
    }

    @Test
    void dosContraUnoCuentaLosGolesPorLado() {
        Jugador a = jugador("Camerún");
        Jugador b = jugador("Nigeria");
        Jugador c = jugador("Corea del Sur");
        Partido partido = new PartidoPorGoles(List.of(a, b, c), 5);
        partido.iniciar();
        Arco izquierdo = partido.getCancha().getArcoIzquierdo();
        Arco derecho = partido.getCancha().getArcoDerecho();

        // 1.º y 3.º defienden el arco izquierdo y atacan a la derecha; el 2.º está solo al otro lado
        assertSame(izquierdo, a.getArco());
        assertSame(derecho, b.getArco());
        assertSame(izquierdo, c.getArco());
        assertEquals(1, a.getDireccionAtaque());
        assertEquals(-1, b.getDireccionAtaque());
        assertEquals(1, c.getDireccionAtaque());
        assertFalse(a.colisionaCon(c));
        assertEquals(List.of(a, c), partido.getRivales(b));
        assertEquals(List.of(b), partido.getRivales(a));

        golEn(partido, izquierdo);
        assertEquals(1, partido.getGoles(derecho));
        assertEquals(0, partido.getGoles(izquierdo));
        assertEquals(1, partido.getGoles(b));

        golEn(partido, derecho);
        assertEquals(1, partido.getGoles(izquierdo));
        assertEquals(1, partido.getGoles(derecho));
        assertEquals(1, partido.getGoles(a));
        assertEquals(1, partido.getGoles(c));
        assertNull(partido.ladoConMasGoles());
        assertFalse(partido.estaTerminado());
    }

    @Test
    void elLadoGanadorDeUnDosContraUnoRecibeLosPuntosDeGanador() {
        Jugador a = jugador("Camerún");
        Jugador b = jugador("Nigeria");
        Jugador c = jugador("Corea del Sur");
        Partido partido = new PartidoPorGoles(List.of(a, b, c), 1);
        partido.iniciar();

        golEn(partido, partido.getCancha().getArcoDerecho());

        assertTrue(partido.estaTerminado());
        assertSame(partido.getCancha().getArcoIzquierdo(), partido.getLadoGanador());
        assertEquals(List.of(a, c), partido.getGanadores());
        assertEquals(Partido.puntosGanador, a.getPuntos());
        assertEquals(Partido.puntosGanador, c.getPuntos());
        assertEquals(Partido.puntosPerdedor, b.getPuntos());
    }

    @Test
    void headSoccerArmaUnPartidoConTantosJugadoresComoPaises() {
        HeadSoccer juego = new HeadSoccer();
        juego.iniciarPartido(List.of("Camerún", "Nigeria", "Corea del Sur"), "Goles", 3);

        assertEquals(3, juego.getJugadores().size());
        assertEquals(3, juego.getPartido().getJugadores().size());
        assertSame(juego.getPartido().getCancha().getArcoIzquierdo(), juego.getJugador(3).getArco());
        assertNull(juego.getJugador(4));
    }
}
