package test;

import static org.junit.jupiter.api.Assertions.*;
import domain.*;
import org.junit.jupiter.api.Test;

public class HeadSoccerTest {

    private static final double DT = 1.0 / 60;
    private static final double MARGEN = 1e-6;

    private HeadSoccer nuevoJuego(String pais1, String pais2, boolean porTiempo, int valor) {
        HeadSoccer juego = new HeadSoccer();
        juego.iniciarPartido(pais1, pais2, porTiempo ? "Tiempo" : "Goles", valor);
        return juego;
    }

    private void avanzar(HeadSoccer juego, double segundos) {
        int frames = (int) Math.round(segundos / DT);
        for (int i = 0; i < frames; i++) {
            juego.actualizar(DT);
        }
    }

    private void anotarGol(HeadSoccer juego, int numeroJugador) {
        Partido partido = juego.getPartido();
        Arco arco = numeroJugador == 1 ? partido.getCancha().getArcoDerecho() : partido.getCancha().getArcoIzquierdo();
        Balon balon = partido.getBalon();
        balon.descargarPoder();
        balon.setPosicion(arco.getX() + 20, partido.getCancha().getSuelo() - Balon.DIAMETRO);
        balon.setVelocidad(0, 0);
        juego.actualizar(DT);
    }

    private void ponerRivalAlFrente(HeadSoccer juego, int numeroAtacante, double distancia) {
        Jugador atacante = juego.getJugador(numeroAtacante);
        Jugador rival = juego.getJugador(numeroAtacante == 1 ? 2 : 1);
        double x = atacante.getX() + atacante.getDireccionAtaque() * distancia;
        rival.setPosicion(x, rival.getY());
    }

    private void dispararPoder(HeadSoccer juego, int numero) {
        Jugador lanzador = juego.getJugador(numero);
        lanzador.getBarraPoder().cargar(BarraPoder.segundosCarga);
        juego.activarPoder(numero);
        Balon balon = juego.getPartido().getBalon();
        double x = lanzador.getDireccionAtaque() > 0 ? lanzador.getX() + lanzador.getAncho() - 5 : lanzador.getX() - Balon.DIAMETRO + 5;
        balon.setPosicion(x, lanzador.getY() + 20);
        balon.setVelocidad(0, 0);
        juego.actualizar(DT);
    }

    private void esperarImpacto(HeadSoccer juego, Jugador rival) {
        for (int i = 0; i < 120 && rival.getEfectoActual() == null; i++) {
            juego.actualizar(DT);
        }
    }

    private void mejorarVarias(HeadSoccer juego, int numero, Habilidad habilidad, int veces) throws HeadSoccerException {
        Jugador jugador = juego.getJugador(numero);
        for (int i = 0; i < veces; i++) {
            jugador.sumarPuntos(jugador.getEstadisticas().precioSiguienteMejora());
            juego.mejorarEstadistica(numero, habilidad);
        }
    }

    @Test
    public void deberiaIniciarElPartidoConMarcadorEnCero() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        assertEquals(0, juego.getPartido().getGoles(juego.getJugador(1)));
        assertEquals(0, juego.getPartido().getGoles(juego.getJugador(2)));
        assertFalse(juego.estaTerminado());
    }

    @Test
    public void deberiaIniciarConBarrasVaciasYVidaMaxima() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        for (int n = 1; n <= 2; n++) {
            Jugador jugador = juego.getJugador(n);
            assertEquals(0, jugador.getBarraPoder().getNivel(), MARGEN);
            assertEquals(jugador.getVidaMax(), jugador.getVida());
            assertNull(jugador.getPoderActivo());
        }
    }

    @Test
    public void deberiaUbicarCadaJugadorEnSuLadoDeLaCancha() {
        HeadSoccer juego = nuevoJuego("Nigeria", "Estados Unidos", false, 3);
        double mitad = juego.getPartido().getCancha().getAncho() / 2;
        assertTrue(juego.getJugador(1).getCentroX() < mitad);
        assertTrue(juego.getJugador(2).getCentroX() > mitad);
        assertEquals(1, juego.getJugador(1).getDireccionAtaque());
        assertEquals(-1, juego.getJugador(2).getDireccionAtaque());
    }

    @Test
    public void deberiaAsignarElPersonajeYPoderElegido() {
        HeadSoccer juego = nuevoJuego("Camerún", "Nigeria", true, 2);
        assertEquals("Camerún", juego.getJugador(1).getPersonaje().getPais());
        assertEquals("Rayo", juego.getJugador(1).getPersonaje().getPoder().getNombre());
        assertEquals("Nigeria", juego.getJugador(2).getPersonaje().getPais());
        assertEquals("Tornado", juego.getJugador(2).getPersonaje().getPoder().getNombre());
    }

    @Test
    public void noDeberiaIniciarConUnPaisInexistente() {
        HeadSoccer juego = new HeadSoccer();
        assertThrows(DatoInvalidoException.class, () -> juego.iniciarPartido("Canadá", "Nigeria", "Tiempo", 1));
    }

    @Test
    public void deberiaMoverAlJugadorSegunSuVelocidad() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        Jugador jugador = juego.getJugador(1);
        double xInicial = jugador.getX();
        juego.mover(1, 1);
        avanzar(juego, 0.5);
        assertEquals(xInicial + Habilidad.SPEED.getValorBase() * 0.5, jugador.getX(), 1);
    }

    @Test
    public void deberiaSaltarSoloDesdeElSuelo() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        Jugador jugador = juego.getJugador(1);
        juego.saltar(1);
        avanzar(juego, 0.1);
        assertFalse(jugador.estaEnSuelo());
        double velocidadEnElAire = jugador.getVelY();
        juego.saltar(1);
        assertEquals(velocidadEnElAire, jugador.getVelY(), MARGEN);
    }

    @Test
    public void deberiaVolverAlSueloDespuesDeSaltar() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        juego.saltar(1);
        avanzar(juego, 2);
        assertTrue(juego.getJugador(1).estaEnSuelo());
    }

    @Test
    public void deberiaImpulsarElBalonAlPatearlo() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        Jugador jugador = juego.getJugador(1);
        Balon balon = juego.getPartido().getBalon();
        balon.setPosicion(jugador.getX() + jugador.getAncho() + 5, jugador.getY() + jugador.getAlto() - Balon.DIAMETRO);
        balon.setVelocidad(0, 0);
        juego.patear(1);
        assertTrue(balon.getVelX() > 0);
        assertTrue(balon.getVelY() < 0);
    }

    @Test
    public void deberiaQuitarVidaAlRivalAlPatearlo() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        ponerRivalAlFrente(juego, 1, Jugador.ANCHO + 10);
        Jugador rival = juego.getJugador(2);
        juego.patear(1);
        assertEquals(rival.getVidaMax() - Habilidad.KICK.getValorBase(), rival.getVida());
    }

    @Test
    public void noDeberiaHacerDanoSiElRivalEstaLejos() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        ponerRivalAlFrente(juego, 1, 300);
        juego.patear(1);
        assertEquals(juego.getJugador(2).getVidaMax(), juego.getJugador(2).getVida());
    }

    @Test
    public void deberiaAturdirAlRivalCuandoSuVidaLlegaACero() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        Jugador rival = juego.getJugador(2);
        rival.recibirDano(rival.getVidaMax());
        assertTrue(rival.estaAturdido());
        double xInicial = rival.getX();
        juego.mover(2, -1);
        juego.saltar(2);
        avanzar(juego, 1);
        assertEquals(xInicial, rival.getX(), 1);
        assertTrue(rival.estaEnSuelo());
    }

    @Test
    public void deberiaRecuperarLaVidaDespuesDeTresSegundosAturdido() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        Jugador rival = juego.getJugador(2);
        rival.recibirDano(rival.getVidaMax());
        avanzar(juego, Jugador.segundosAturdimiento + 0.1);
        assertFalse(rival.estaAturdido());
        assertEquals(rival.getVidaMax(), rival.getVida());
    }

    @Test
    public void deberiaSumarGolAlAnotarEnElArcoRival() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", false, 3);
        anotarGol(juego, 1);
        assertEquals(1, juego.getPartido().getGoles(juego.getJugador(1)));
        assertEquals(0, juego.getPartido().getGoles(juego.getJugador(2)));
    }

    @Test
    public void deberiaDarElGolAlRivalSiEsEnSuPropioArco() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", false, 3);
        anotarGol(juego, 2);
        assertEquals(0, juego.getPartido().getGoles(juego.getJugador(1)));
        assertEquals(1, juego.getPartido().getGoles(juego.getJugador(2)));
    }

    @Test
    public void deberiaAumentarUnCuartoLaBarraDelQueRecibeElGol() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", false, 3);
        anotarGol(juego, 1);
        assertEquals(0.25, juego.getJugador(2).getBarraPoder().getNivel(), 0.01);
    }

    @Test
    public void deberiaReiniciarPosicionesDespuesDeUnGol() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", false, 3);
        Jugador jugador = juego.getJugador(1);
        double xInicial = jugador.getX();
        juego.mover(1, 1);
        avanzar(juego, 0.5);
        juego.mover(1, 0);
        anotarGol(juego, 1);
        assertEquals(xInicial, jugador.getX(), 1);
        Partido partido = juego.getPartido();
        assertEquals(partido.getCancha().getAncho() / 2, partido.getBalon().getCentroX(), 1);
    }

    @Test
    public void deberiaLlenarLaBarraEnDiezSegundos() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        avanzar(juego, 5);
        assertEquals(0.5, juego.getJugador(1).getBarraPoder().getNivel(), 0.01);
        avanzar(juego, 5.1);
        assertTrue(juego.getJugador(1).getBarraPoder().estaLlena());
    }

    @Test
    public void noDeberiaActivarElPoderSinLaBarraLlena() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        avanzar(juego, 2);
        juego.activarPoder(1);
        assertNull(juego.getJugador(1).getPoderActivo());
    }

    @Test
    public void deberiaActivarElAuraYVaciarLaBarra() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        Jugador jugador = juego.getJugador(1);
        jugador.getBarraPoder().cargar(BarraPoder.segundosCarga);
        juego.activarPoder(1);
        assertNotNull(jugador.getPoderActivo());
        assertEquals(0, jugador.getBarraPoder().getNivel(), MARGEN);
    }

    @Test
    public void noDeberiaActivarOtroPoderMientrasTengaElAura() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        Jugador jugador = juego.getJugador(1);
        jugador.getBarraPoder().cargar(BarraPoder.segundosCarga);
        juego.activarPoder(1);
        Poder primero = jugador.getPoderActivo();
        jugador.getBarraPoder().cargar(BarraPoder.segundosCarga);
        juego.activarPoder(1);
        assertSame(primero, jugador.getPoderActivo());
        assertTrue(jugador.getBarraPoder().estaLlena());
    }

    @Test
    public void deberiaDispararElTiroDePoderAlTocarElBalon() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        dispararPoder(juego, 1);
        Balon balon = juego.getPartido().getBalon();
        assertTrue(balon.tienePoder());
        assertNull(juego.getJugador(1).getPoderActivo());
        assertTrue(balon.getVelX() > 0);
    }

    @Test
    public void deberiaAplicarDanoYEfectoComoElEjemploDelEnunciado() throws HeadSoccerException {
        HeadSoccer juego = nuevoJuego("Camerún", "Nigeria", true, 1);
        mejorarVarias(juego, 1, Habilidad.KICK, 4);
        mejorarVarias(juego, 2, Habilidad.SURVIVAL, 2);
        juego.iniciarPartido("Camerún", "Nigeria", "Tiempo", 1);
        Jugador rival = juego.getJugador(2);
        assertEquals(120, rival.getVidaMax());
        assertEquals(14, juego.getJugador(1).getDanoPatada());
        ponerRivalAlFrente(juego, 1, 250);
        dispararPoder(juego, 1);
        esperarImpacto(juego, rival);
        assertEquals(36, rival.getVida());
        assertEquals("Electrocutado", rival.getEfectoActual().getNombre());
    }

    @Test
    public void deberiaImpedirSaltarYReducirVelocidadConElRayo() {
        HeadSoccer juego = nuevoJuego("Camerún", "Nigeria", true, 1);
        Jugador rival = juego.getJugador(2);
        ponerRivalAlFrente(juego, 1, 250);
        dispararPoder(juego, 1);
        esperarImpacto(juego, rival);
        assertEquals(Habilidad.SPEED.getValorBase() * 0.5, rival.getVelocidad(), MARGEN);
        juego.saltar(2);
        assertEquals(0, rival.getVelY(), MARGEN);
    }

    @Test
    public void deberiaEmpujarAlRivalHaciaSuArcoConAuraAzul() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        Jugador rival = juego.getJugador(2);
        ponerRivalAlFrente(juego, 1, 250);
        dispararPoder(juego, 1);
        esperarImpacto(juego, rival);
        assertEquals("Empuje", rival.getEfectoActual().getNombre());
        assertTrue(rival.getVelX() > 0);
    }

    @Test
    public void deberiaLanzarAlRivalAlAireConElTornadoARasDeSuelo() {
        HeadSoccer juego = nuevoJuego("Nigeria", "Camerún", true, 1);
        Jugador rival = juego.getJugador(2);
        Partido partido = juego.getPartido();
        ponerRivalAlFrente(juego, 1, 400);
        dispararPoder(juego, 1);
        avanzar(juego, 0.2);
        assertTrue(partido.getCancha().getSuelo() - partido.getBalon().getBorde() < Jugador.ALTO);
        esperarImpacto(juego, rival);
        assertEquals("Inmovilizado", rival.getEfectoActual().getNombre());
        assertTrue(rival.getVelY() < 0);
    }

    @Test
    public void deberiaDividirElBalonEnOchoConIlusion() {
        HeadSoccer juego = nuevoJuego("Estados Unidos", "Camerún", true, 1);
        juego.getJugador(2).setPosicion(900, juego.getJugador(2).getY());
        dispararPoder(juego, 1);
        assertEquals(7, juego.getPartido().getObjetosExtra().size());
        assertTrue(juego.getPartido().getBalon().tienePoder());
        juego.getPartido().getBalon().descargarPoder();
        avanzar(juego, 2.6);
        assertEquals(0, juego.getPartido().getObjetosExtra().size());
    }

    @Test
    public void noDeberiaAcumularEfectos() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        Jugador rival = juego.getJugador(2);
        rival.aplicarEfecto(new Electrocutado(3));
        rival.aplicarEfecto(new Empuje(1.5, 700));
        assertEquals("Empuje", rival.getEfectoActual().getNombre());
        assertEquals(Habilidad.SPEED.getValorBase(), rival.getVelocidad(), MARGEN);
    }

    @Test
    public void deberiaTerminarElPartidoPorGolesAlLlegarALaMeta() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", false, 3);
        for (int i = 0; i < 3; i++) {
            anotarGol(juego, 2);
        }
        assertTrue(juego.estaTerminado());
        assertSame(juego.getJugador(2).getArco(), juego.getPartido().getLadoGanador());
    }

    @Test
    public void deberiaTerminarElPartidoPorTiempoAlAcabarseLosMinutos() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        anotarGol(juego, 1);
        avanzar(juego, 59);
        assertFalse(juego.estaTerminado());
        avanzar(juego, 1.1);
        assertTrue(juego.estaTerminado());
        assertSame(juego.getJugador(1).getArco(), juego.getPartido().getLadoGanador());
    }

    @Test
    public void deberiaJugarMuerteSubitaSiHayEmpate() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        avanzar(juego, 61);
        Partido partido = juego.getPartido();
        assertFalse(juego.estaTerminado());
        assertTrue(partido.estaEnMuerteSubita());
        anotarGol(juego, 2);
        assertTrue(juego.estaTerminado());
        assertSame(juego.getJugador(2).getArco(), partido.getLadoGanador());
    }

    @Test
    public void deberiaDarCienPuntosAlGanadorYVeinticincoAlPerdedor() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", false, 3);
        for (int i = 0; i < 3; i++) {
            anotarGol(juego, 1);
        }
        assertEquals(Partido.puntosGanador, juego.getJugador(1).getPuntos());
        assertEquals(Partido.puntosPerdedor, juego.getJugador(2).getPuntos());
    }

    @Test
    public void deberiaCongelarElPartidoEnPausa() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        avanzar(juego, 1);
        juego.pausar();
        double tiempo = juego.getPartido().getTiempoJugado();
        double xBalon = juego.getPartido().getBalon().getX();
        double yBalon = juego.getPartido().getBalon().getY();
        avanzar(juego, 2);
        assertEquals(tiempo, juego.getPartido().getTiempoJugado(), MARGEN);
        assertEquals(xBalon, juego.getPartido().getBalon().getX(), MARGEN);
        assertEquals(yBalon, juego.getPartido().getBalon().getY(), MARGEN);
    }

    @Test
    public void deberiaContinuarAlReanudar() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        juego.pausar();
        juego.reanudar();
        avanzar(juego, 1);
        assertEquals(1, juego.getPartido().getTiempoJugado(), 0.01);
    }

    @Test
    public void deberiaTerminarSinPuntosAlAbandonar() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        anotarGol(juego, 1);
        juego.abandonar();
        assertTrue(juego.estaTerminado());
        assertNull(juego.getPartido().getLadoGanador());
        assertEquals(0, juego.getJugador(1).getPuntos());
    }

    @Test
    public void deberiaCobrarLasMejorasDuplicandoElPrecio() throws HeadSoccerException {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        Jugador jugador = juego.getJugador(1);
        jugador.sumarPuntos(3500);
        juego.mejorarEstadistica(1, Habilidad.SPEED);
        assertEquals(3000, jugador.getPuntos());
        juego.mejorarEstadistica(1, Habilidad.JUMP);
        assertEquals(2000, jugador.getPuntos());
        juego.mejorarEstadistica(1, Habilidad.KICK);
        assertEquals(0, jugador.getPuntos());
        assertEquals(2, jugador.getEstadisticas().getNivel(Habilidad.SPEED));
    }

    @Test
    public void noDeberiaMejorarSinPuntosSuficientes() {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        juego.getJugador(1).sumarPuntos(499);
        HeadSoccerException e = assertThrows(HeadSoccerException.class, () -> juego.mejorarEstadistica(1, Habilidad.SPEED));
        assertEquals(HeadSoccerException.PUNTOS_INSUFICIENTES, e.getMessage());
        assertEquals(499, juego.getJugador(1).getPuntos());
        assertEquals(1, juego.getJugador(1).getEstadisticas().getNivel(Habilidad.SPEED));
    }

    @Test
    public void noDeberiaMejorarPorEncimaDeDiez() throws HeadSoccerException {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        mejorarVarias(juego, 1, Habilidad.SURVIVAL, 9);
        Jugador jugador = juego.getJugador(1);
        assertEquals(Estadisticas.MAXIMO, jugador.getEstadisticas().getNivel(Habilidad.SURVIVAL));
        assertEquals(190, jugador.getVidaMax());
        jugador.sumarPuntos(jugador.getEstadisticas().precioSiguienteMejora());
        HeadSoccerException e = assertThrows(HeadSoccerException.class, () -> juego.mejorarEstadistica(1, Habilidad.SURVIVAL));
        assertEquals(HeadSoccerException.MEJORA_MAXIMA, e.getMessage());
    }

    @Test
    public void deberiaMantenerElProgresoEntrePartidos() throws HeadSoccerException {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", false, 1);
        anotarGol(juego, 1);
        mejorarVarias(juego, 1, Habilidad.SPEED, 1);
        int puntos = juego.getJugador(1).getPuntos();
        juego.iniciarPartido("Nigeria", "Estados Unidos", "Tiempo", 2);
        assertEquals(puntos, juego.getJugador(1).getPuntos());
        assertEquals(2, juego.getJugador(1).getEstadisticas().getNivel(Habilidad.SPEED));
        assertEquals("Nigeria", juego.getJugador(1).getPersonaje().getPais());
        assertEquals(0, juego.getPartido().getGoles(juego.getJugador(1)));
    }

    @Test
    public void deberiaMoverseMasRapidoConMasSpeed() throws HeadSoccerException {
        HeadSoccer juego = nuevoJuego("Corea del Sur", "Camerún", true, 1);
        mejorarVarias(juego, 1, Habilidad.SPEED, 3);
        assertEquals(Habilidad.SPEED.getValorBase() * 1.3, juego.getJugador(1).getVelocidad(), MARGEN);
    }
}
