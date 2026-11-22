package test;

import static org.junit.jupiter.api.Assertions.*;

import domain.*;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ColisionesTest {

    private static final double DT = 1.0 / 60;

    private Jugador jugador(String pais) {
        return new JugadorHumano(Personaje.crear(pais));
    }

    private Partido partido(Jugador a, Jugador b) {
        Partido partido = new PartidoPorGoles(List.of(a, b), 5);
        partido.iniciar();
        return partido;
    }

    private void avanzar(Partido partido, double segundos) {
        int frames = (int) Math.round(segundos / DT);
        for (int i = 0; i < frames; i++) {
            partido.actualizar(DT);
        }
    }

    @Test
    void dosJugadoresQueCaminanUnoHaciaElOtroNoSeAtraviesan() {
        Jugador a = jugador("Camerún");
        Jugador b = jugador("Nigeria");
        Partido partido = partido(a, b);
        a.setPosicion(250, a.getY());
        b.setPosicion(400, b.getY());

        a.mover(1);
        b.mover(-1);
        avanzar(partido, 2);

        assertTrue(a.getX() + a.getAncho() <= b.getX() + 1e-6);
        // Se encuentran cerca del punto medio entre ambos (355) y ninguno empuja al otro
        assertEquals(355, (a.getX() + a.getAncho() + b.getX()) / 2, Jugador.ANCHO / 10);
    }

    @Test
    void unJugadorQueCaminaHaciaOtroQuietoNoLoEmpuja() {
        Jugador a = jugador("Camerún");
        Jugador b = jugador("Nigeria");
        Partido partido = partido(a, b);
        a.setPosicion(250, a.getY());
        b.setPosicion(400, b.getY());

        a.mover(1);
        avanzar(partido, 2);

        assertEquals(400, b.getX(), 1e-6);
        assertEquals(400, a.getX() + a.getAncho(), 1e-6);
    }

    @Test
    void unJugadorQueCaeEncimaDeOtroQuedaApoyadoYPuedeSaltar() {
        Jugador abajo = jugador("Camerún");
        Jugador arriba = jugador("Nigeria");
        Partido partido = partido(abajo, arriba);
        abajo.setPosicion(300, abajo.getY());
        arriba.setPosicion(300, abajo.getY() - arriba.getAlto() - 100);

        avanzar(partido, 1);

        assertEquals(abajo.getY(), arriba.getBorde(), 1);
        assertTrue(arriba.estaEnSuelo());
        assertEquals(partido.getCancha().getSuelo(), abajo.getBorde(), 1);
        arriba.saltar();
        assertTrue(arriba.getVelY() < 0);
    }

    @Test
    void elBalonConPoderAfectaAlRivalYNoAlLanzador() {
        Jugador lanzador = jugador("Camerún");
        Jugador rival = jugador("Nigeria");
        Partido partido = partido(lanzador, rival);
        Balon balon = partido.getBalon();
        balon.cargarPoder(lanzador.getPersonaje().getPoder(), lanzador);

        balon.afectar(lanzador);
        assertEquals(lanzador.getVidaMax(), lanzador.getVida());
        assertNull(lanzador.getEfectoActual());
        assertTrue(balon.tienePoder());

        balon.afectar(rival);
        assertTrue(rival.getVida() < rival.getVidaMax());
        assertEquals("Electrocutado", rival.getEfectoActual().getNombre());
        assertFalse(balon.tienePoder());
    }

    @Test
    void enUnDosContraUnoElPoderNoGolpeaAlCompaneroDelLanzador() {
        Jugador lanzador = jugador("Camerún");
        Jugador rival = jugador("Nigeria");
        Jugador companero = jugador("Corea del Sur");
        Partido partido = new PartidoPorGoles(List.of(lanzador, rival, companero), 5);
        partido.iniciar();
        Balon balon = partido.getBalon();
        balon.cargarPoder(lanzador.getPersonaje().getPoder(), lanzador);

        // A través del choque general: el balón con poder pasa por encima del compañero
        balon.setPosicion(companero.getX(), companero.getY());
        balon.setVelocidad(0, 0);
        partido.actualizar(DT);
        assertEquals(companero.getVidaMax(), companero.getVida());
        assertNull(companero.getEfectoActual());
        assertTrue(balon.tienePoder());

        balon.afectar(rival);
        assertTrue(rival.getVida() < rival.getVidaMax());
        assertEquals("Electrocutado", rival.getEfectoActual().getNombre());
        assertFalse(balon.tienePoder());
    }

    @Test
    void elBalonConPoderNoSeDescargaAlChocarConLaCancha() {
        Jugador lanzador = jugador("Nigeria");
        Partido partido = partido(lanzador, jugador("Camerún"));
        Balon balon = partido.getBalon();
        balon.cargarPoder(lanzador.getPersonaje().getPoder(), lanzador);

        for (Limite limite : partido.getCancha().getLimites()) {
            balon.afectar(limite);
        }

        assertTrue(balon.tienePoder());
    }

    @Test
    void elBalonRebotaEnElPisoPerdiendoFuerzaYNoLoAtraviesa() {
        Partido partido = partido(jugador("Camerún"), jugador("Nigeria"));
        Balon balon = partido.getBalon();
        double suelo = partido.getCancha().getSuelo();
        balon.setPosicion(500, suelo - Balon.DIAMETRO - 1);
        balon.setVelocidad(0, 400);

        partido.actualizar(DT);

        assertTrue(balon.getBorde() <= suelo + 1e-6);
        assertTrue(balon.getVelY() < 0);
        assertTrue(Math.abs(balon.getVelY()) <= 0.75 * 420);
    }

    @Test
    void elBalonRebotaEnLaParedYSigueDentroDeLaCancha() {
        Partido partido = partido(jugador("Camerún"), jugador("Nigeria"));
        Balon balon = partido.getBalon();
        balon.setPosicion(partido.getCancha().getAncho() - Balon.DIAMETRO - 1, 100);
        balon.setVelocidad(800, 0);

        partido.actualizar(DT);

        assertTrue(balon.getX() + balon.getAncho() <= partido.getCancha().getAncho() + 1e-6);
        assertEquals(-800 * Balon.REBOTE, balon.getVelX(), 1e-6);
    }

    @Test
    void elBalonRebotaEnElTravesano() {
        Partido partido = partido(jugador("Camerún"), jugador("Nigeria"));
        Balon balon = partido.getBalon();
        Arco arco = partido.getCancha().getArcoIzquierdo();
        balon.setPosicion(arco.getX() + 20, arco.getYTravesano() - Balon.DIAMETRO - 1);
        balon.setVelocidad(0, 300);

        partido.actualizar(DT);

        assertTrue(balon.getBorde() <= arco.getYTravesano() + 1e-6);
        assertTrue(balon.getVelY() < 0);
    }

    @Test
    void losJugadoresNoSeSalenDeLaCancha() {
        Jugador a = jugador("Camerún");
        Jugador b = jugador("Nigeria");
        Partido partido = partido(a, b);

        a.mover(-1);
        b.mover(1);
        avanzar(partido, 3);

        assertEquals(0, a.getX(), 1e-6);
        assertEquals(partido.getCancha().getAncho(), b.getX() + b.getAncho(), 1e-6);
        assertEquals(partido.getCancha().getSuelo(), a.getBorde(), 1);
    }
}
