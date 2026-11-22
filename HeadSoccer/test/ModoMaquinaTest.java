package test;

import static org.junit.jupiter.api.Assertions.*;

import domain.*;
import org.junit.jupiter.api.Test;

public class ModoMaquinaTest {

    private static final double DT = 1.0 / 60;

    private HeadSoccer juegoContraMaquina() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        juego.seleccionarModo(true, "Torpe");
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);
        return juego;
    }

    @Test
    void crearTorpeDevuelveUnTorpe() throws HeadSoccerException {
        assertEquals(Torpe.class, PerfilMaquina.crear("Torpe").getClass());
    }

    @Test
    void perfilesNoImplementadosLanzanExcepcionConMensaje() {
        for (String nombre : new String[] {"Ofensiva", "Defensiva"}) {
            HeadSoccerException e = assertThrows(HeadSoccerException.class, () -> PerfilMaquina.crear(nombre));
            assertEquals(HeadSoccerException.PERFIL_NO_DISPONIBLE, e.getMessage());
        }
    }

    @Test
    void nombreDesconocidoLanzaDatoInvalido() {
        assertThrows(DatoInvalidoException.class, () -> PerfilMaquina.crear("Inventada"));
    }

    @Test
    void pvpDejaAlJugadorDosHumano() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        juego.seleccionarModo(false, null);
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);
        assertEquals(TipoJugador.HUMANO, juego.getJugador(2).getTipo());
    }

    @Test
    void porDefectoLosDosJugadoresSonHumanos() {
        HeadSoccer juego = new HeadSoccer();
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);
        assertEquals(TipoJugador.HUMANO, juego.getJugador(1).getTipo());
        assertEquals(TipoJugador.HUMANO, juego.getJugador(2).getTipo());
    }

    @Test
    void maquinaConTorpeCreaJugadorMaquinaConTorpe() throws HeadSoccerException {
        HeadSoccer juego = juegoContraMaquina();
        assertEquals(TipoJugador.MAQUINA, juego.getJugador(2).getTipo());
        assertEquals(Torpe.class, juego.getModo().getPerfil(2).getClass());
        assertEquals(TipoJugador.HUMANO, juego.getJugador(1).getTipo());
    }

    @Test
    void perfilNoDisponibleLanzaExcepcionYNoCambiaElModo() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        juego.seleccionarModo(true, "Torpe");
        PerfilMaquina anterior = juego.getModo().getPerfil(2);

        HeadSoccerException e = assertThrows(HeadSoccerException.class, () -> juego.seleccionarModo(true, "Ofensiva"));

        assertEquals(HeadSoccerException.PERFIL_NO_DISPONIBLE, e.getMessage());
        assertEquals(TipoJugador.MAQUINA, juego.getModo().getTipo(2));
        assertSame(anterior, juego.getModo().getPerfil(2));
    }

    @Test
    void perfilNoDisponibleNoCambiaUnModoHumanoPrevio() {
        HeadSoccer juego = new HeadSoccer();
        assertThrows(HeadSoccerException.class, () -> juego.seleccionarModo(true, "Ofensiva"));
        assertEquals(TipoJugador.HUMANO, juego.getModo().getTipo(2));
        assertNull(juego.getModo().getPerfil(2));
    }

    @Test
    void ordenesManualesSobreLaMaquinaSeIgnoran() throws HeadSoccerException {
        HeadSoccer juego = juegoContraMaquina();
        Jugador maquina = juego.getJugador(2);
        maquina.getBarraPoder().cargar(BarraPoder.segundosCarga);

        juego.mover(2, 1);
        juego.saltar(2);
        juego.patear(2);
        juego.activarPoder(2);
        maquina.actualizar(DT);

        assertEquals(0, maquina.getVelX(), 1e-9);
        assertTrue(maquina.getVelY() >= 0);
        assertFalse(maquina.estaPateando());
        assertNull(maquina.getPoderActivo());
    }

    @Test
    void ordenesManualesSobreElHumanoSiFuncionan() throws HeadSoccerException {
        HeadSoccer juego = juegoContraMaquina();
        Jugador humano = juego.getJugador(1);
        humano.getBarraPoder().cargar(BarraPoder.segundosCarga);

        juego.mover(1, 1);
        juego.saltar(1);
        juego.patear(1);
        juego.activarPoder(1);
        humano.actualizar(DT);

        assertTrue(humano.getVelX() > 0);
        assertTrue(humano.getVelY() < 0);
        assertTrue(humano.estaPateando());
        assertNotNull(humano.getPoderActivo());
    }

    @Test
    void partidoContraMaquinaCorreSinExcepciones() throws HeadSoccerException {
        HeadSoccer juego = juegoContraMaquina();
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 600; i++) {
                juego.mover(2, 1);
                juego.actualizar(DT);
            }
        });
    }

    @Test
    void alCambiarDeTipoSeCreaJugadorNuevoYElUnoConservaSuProgreso() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);
        Jugador uno = juego.getJugador(1);
        Jugador dosHumano = juego.getJugador(2);

        juego.seleccionarModo(true, "Torpe");
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);

        assertSame(uno, juego.getJugador(1));
        assertNotSame(dosHumano, juego.getJugador(2));
        assertEquals(TipoJugador.MAQUINA, juego.getJugador(2).getTipo());
    }

    @Test
    void mismoTipoSeReutilizaParaConservarProgreso() throws HeadSoccerException {
        HeadSoccer juego = juegoContraMaquina();
        Jugador maquina = juego.getJugador(2);

        juego.seleccionarModo(true, "Torpe");
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);

        assertSame(maquina, juego.getJugador(2));
    }

    @Test
    void volverAPvPCreaUnHumanoNuevo() throws HeadSoccerException {
        HeadSoccer juego = juegoContraMaquina();
        juego.seleccionarModo(false, null);
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);
        assertEquals(TipoJugador.HUMANO, juego.getJugador(2).getTipo());
    }
}
