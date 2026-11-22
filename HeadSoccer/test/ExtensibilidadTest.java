package test;

import static org.junit.jupiter.api.Assertions.*;

import domain.*;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ExtensibilidadTest {

    private static final double DT = 1.0 / 60;
    private static final int danoTrampa = 15;

    // Objeto de prueba: al tocar algo le quita vida solo con recibirDano y desaparece
    private static class Trampa extends ObjetoFisico {
        private static final long serialVersionUID = 1L;

        Trampa(double x, double y) {
            super(x, y, 20, 20);
        }

        @Override
        public void actualizar(double dt) {
        }

        @Override
        public boolean esSolido() {
            return false;
        }

        @Override
        public void afectar(ObjetoFisico otro) {
            otro.recibirDano(danoTrampa);
            desaparecer();
        }
    }

    // Objeto de prueba quieto y sin efecto, para contar apariciones
    private static class Marca extends ObjetoFisico {
        private static final long serialVersionUID = 1L;

        Marca() {
            super(30, 30, 5, 5);
        }

        @Override
        public void actualizar(double dt) {
        }

        @Override
        public boolean esSolido() {
            return false;
        }
    }

    private Jugador jugador(String pais) {
        return new JugadorHumano(Personaje.crear(pais));
    }

    private void avanzar(Partido partido, double segundos) {
        int frames = (int) Math.round(segundos / DT);
        for (int i = 0; i < frames; i++) {
            partido.actualizar(DT);
        }
    }

    @Test
    void unaTrampaQuitaVidaConRecibirDanoYDesapareceTrasTocarUnaVez() {
        Jugador uno = jugador("Camerún");
        Partido partido = new PartidoPorGoles(List.of(uno, jugador("Nigeria")), 5);
        partido.iniciar();
        Trampa trampa = new Trampa(uno.getCentroX() - 10, uno.getCentroY() - 10);
        partido.agregarObjeto(trampa);

        partido.actualizar(DT);
        assertEquals(uno.getVidaMax() - danoTrampa, uno.getVida());
        assertTrue(trampa.haExpirado());
        assertFalse(partido.getObjetosExtra().contains(trampa));

        avanzar(partido, 1);
        assertEquals(uno.getVidaMax() - danoTrampa, uno.getVida());
    }

    @Test
    void curarNoPasaDeLaVidaMaxima() {
        Jugador j = jugador("Camerún");
        j.recibirDano(30);
        j.curar(10);
        assertEquals(j.getVidaMax() - 20, j.getVida());

        j.curar(1000);
        assertEquals(j.getVidaMax(), j.getVida());

        // En un objeto sin vida curar y recibir daño no hacen nada
        Marca marca = new Marca();
        assertDoesNotThrow(() -> {
            marca.curar(10);
            marca.recibirDano(10);
        });
    }

    @Test
    void unaAparicionCadaDosSegundosCreaObjetos() {
        Partido partido = new PartidoPorGoles(List.of(jugador("Camerún"), jugador("Nigeria")), 5);
        partido.agregarAparicion(new Aparicion(2, escenario -> new Marca()));
        partido.iniciar();

        avanzar(partido, 1.9);
        assertEquals(0, partido.getObjetosExtra().size());
        avanzar(partido, 0.2);
        assertEquals(1, partido.getObjetosExtra().size());
        avanzar(partido, 4);
        assertEquals(3, partido.getObjetosExtra().size());
    }

    @Test
    void sinAparicionesRegistradasNoApareceNada() {
        Partido partido = new PartidoPorGoles(List.of(jugador("Camerún"), jugador("Nigeria")), 5);
        partido.iniciar();
        avanzar(partido, 5);
        assertTrue(partido.getApariciones().isEmpty());
        assertEquals(0, partido.getObjetosExtra().size());
    }

    @Test
    void headSoccerPoneLasAparicionesRegistradasEnElPartido() {
        HeadSoccer juego = new HeadSoccer();
        juego.agregarAparicion(new Aparicion(2, escenario -> new Marca()));
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);

        for (int i = 0; i < 130; i++) {
            juego.actualizar(DT);
        }

        assertEquals(1, juego.getPartido().getApariciones().size());
        assertEquals(1, juego.getPartido().getObjetosExtra().size());
    }

    // Pone el balón rodando sobre el piso y devuelve su velocidad horizontal después de un instante
    private double velocidadTrasRodar(Cancha cancha) {
        Partido partido = new PartidoPorGoles(List.of(jugador("Camerún"), jugador("Nigeria")), cancha, 5);
        partido.iniciar();
        Balon balon = partido.getBalon();
        balon.setPosicion(480, cancha.getSuelo() - Balon.DIAMETRO);
        balon.setVelocidad(300, 0);
        partido.actualizar(DT);
        return balon.getVelX();
    }

    @Test
    void unaCanchaConOtroRoceCambiaComoFrenaElBalon() {
        double normal = velocidadTrasRodar(new Cancha());
        double conMasRoce = velocidadTrasRodar(new Cancha(Cancha.ANCHO, Cancha.ALTO, 0.5, Cancha.gravedadNormal));

        assertEquals(300 * Cancha.roceNormal, normal, 1e-6);
        assertEquals(300 * 0.5, conMasRoce, 1e-6);
    }

    // Partido de prueba con otras reglas: cada gol vale dos
    private static class PartidoGolDoble extends PartidoPorGoles {
        private static final long serialVersionUID = 1L;

        PartidoGolDoble(List<Jugador> jugadores) {
            super(jugadores, 10);
        }

        @Override
        protected int valorDelGol() {
            return 2;
        }
    }

    @Test
    void unPartidoConValorDelGolDosSumaDeADos() {
        Jugador uno = jugador("Camerún");
        Partido partido = new PartidoGolDoble(List.of(uno, jugador("Nigeria")));
        partido.iniciar();
        Arco arcoRival = partido.getCancha().getArcoDerecho();
        Balon balon = partido.getBalon();
        balon.setPosicion(arcoRival.getX() + 20, partido.getCancha().getSuelo() - Balon.DIAMETRO);
        balon.setVelocidad(0, 0);

        partido.actualizar(DT);

        assertEquals(2, partido.getGoles(uno));
    }

    @Test
    void lasFabricasCreanLoConocidoYFallanConLoDesconocido() {
        List<Jugador> jugadores = List.of(jugador("Camerún"), jugador("Nigeria"));
        assertEquals(PartidoPorGoles.class, Partido.crear("Goles", jugadores, Cancha.crear("Normal"), 3).getClass());
        assertEquals(PartidoPorTiempo.class, Partido.crear("Tiempo", jugadores, Cancha.crear("Normal"), 1).getClass());

        DatoInvalidoException tipo = assertThrows(DatoInvalidoException.class,
            () -> Partido.crear("Penales", jugadores, new Cancha(), 3));
        assertTrue(tipo.getMessage().startsWith(DatoInvalidoException.TIPO_PARTIDO_INVALIDO));
        DatoInvalidoException cancha = assertThrows(DatoInvalidoException.class, () -> Cancha.crear("Marte"));
        assertTrue(cancha.getMessage().startsWith(DatoInvalidoException.CANCHA_INVALIDA));
    }

    @Test
    void headSoccerConTipoOCanchaDesconocidosLanzaYNoCambiaElPartido() {
        HeadSoccer juego = new HeadSoccer();
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);
        Partido actual = juego.getPartido();
        Jugador uno = juego.getJugador(1);

        assertThrows(DatoInvalidoException.class,
            () -> juego.iniciarPartido(List.of("Camerún", "Nigeria"), "Penales", 5));
        assertThrows(DatoInvalidoException.class,
            () -> juego.iniciarPartido(List.of("Camerún", "Nigeria"), "Goles", "Marte", 5));

        assertSame(actual, juego.getPartido());
        assertSame(uno, juego.getJugador(1));
        juego.iniciarPartido(List.of("Camerún", "Nigeria"), "Tiempo", "Normal", 2);
        assertEquals(PartidoPorTiempo.class, juego.getPartido().getClass());
    }

    @Test
    void unImpulsoMueveAlJugadorAunqueNoPresioneTeclasYSeVaFrenando() {
        Jugador uno = jugador("Camerún");
        Partido partido = new PartidoPorGoles(List.of(uno, jugador("Nigeria")), 5);
        partido.iniciar();
        double inicio = uno.getX();

        uno.impulsar(600, 0);
        partido.actualizar(DT);

        assertTrue(uno.getX() > inicio);
        assertTrue(uno.getVelX() > 0 && uno.getVelX() < 600);
        assertTrue(uno.estaImpulsado());
        double tras1Instante = uno.getX();

        avanzar(partido, 0.2);
        assertTrue(uno.getX() > tras1Instante);

        // Al frenarse del todo, vuelve a obedecer al teclado (que está quieto)
        avanzar(partido, 2);
        assertFalse(uno.estaImpulsado());
        assertEquals(0, uno.getVelX(), 1e-9);
    }

    @Test
    void dosContraDosConUnaMaquinaCompaneraDelHumano() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        juego.seleccionarModo(
            List.of(TipoJugador.HUMANO, TipoJugador.HUMANO, TipoJugador.MAQUINA, TipoJugador.HUMANO),
            java.util.Arrays.asList(null, null, "Torpe", null));
        juego.iniciarPartido(List.of("Camerún", "Nigeria", "Corea del Sur", "Estados Unidos"), "Goles", 5);

        Jugador humano = juego.getJugador(1);
        Jugador maquina = juego.getJugador(3);
        assertEquals(TipoJugador.MAQUINA, maquina.getTipo());
        assertEquals(TipoJugador.HUMANO, juego.getJugador(4).getTipo());
        assertSame(humano.getArco(), maquina.getArco());
        List<Jugador> rivales = juego.getPartido().getRivales(humano);
        assertEquals(List.of(juego.getJugador(2), juego.getJugador(4)), rivales);
        assertFalse(rivales.contains(maquina));

        // La máquina ignora las órdenes del teclado; el partido corre sin errores
        double xMaquina = maquina.getX();
        juego.mover(3, 1);
        assertEquals(xMaquina, maquina.getX());
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 600; i++) {
                juego.actualizar(DT);
            }
        });
    }

    // Objeto de prueba que se queda en la cancha aunque haya gol
    private static class Obstaculo extends Marca {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean persisteTrasGol() {
            return true;
        }
    }

    @Test
    void despuesDeUnGolSeQuedaElObjetoPermanenteYSeVaElNormal() {
        Jugador uno = jugador("Camerún");
        Partido partido = new PartidoPorGoles(List.of(uno, jugador("Nigeria")), 5);
        partido.iniciar();
        Obstaculo permanente = new Obstaculo();
        Marca normal = new Marca();
        partido.agregarObjeto(permanente);
        partido.agregarObjeto(normal);

        Arco arcoRival = partido.getCancha().getArcoDerecho();
        Balon balon = partido.getBalon();
        balon.setPosicion(arcoRival.getX() + 20, partido.getCancha().getSuelo() - Balon.DIAMETRO);
        balon.setVelocidad(0, 0);
        partido.actualizar(DT);

        assertEquals(1, partido.getGoles(uno));
        assertTrue(partido.getObjetosExtra().contains(permanente));
        assertFalse(partido.getObjetosExtra().contains(normal));
    }

    @Test
    void cadaObjetoDiceSuApariencia() {
        assertEquals("objeto", new Marca().getApariencia());
        assertEquals("balon", new Balon(0, 0).getApariencia());
        assertEquals("balonFalso", new BalonFalso(0, 0, 1).getApariencia());
    }

    @Test
    void sinEscenarioSeUsanLaGravedadYElRoceNormales() {
        Balon balon = new Balon(0, 0);
        balon.actualizar(DT);
        assertEquals(Cancha.gravedadNormal * DT, balon.getVelY(), 1e-9);
    }
}
