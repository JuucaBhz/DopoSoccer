package test;

import static org.junit.jupiter.api.Assertions.*;

import domain.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PolimorfismoTest {

    private static final double DT = 1.0 / 60;

    // Poder de ilusión de Estados Unidos, que es quien crea los balones falsos
    private static TiroIlusion ilusion() {
        return new TiroIlusion("Ilusión", 30, new Confusion(2.5), 2.5);
    }

    private static class ObjetoInerte extends ObjetoFisico {
        ObjetoInerte() {
            super(0, 0, 10, 10);
        }

        @Override
        public void actualizar(double dt) {
        }
    }

    private Jugador jugador(String pais) {
        return new JugadorHumano(Personaje.crear(pais));
    }

    // Mete el balón en el arco dado y avanza un instante para que el partido cuente el gol
    private void golEn(Partido partido, Arco arco) {
        Balon balon = partido.getBalon();
        balon.descargarPoder();
        balon.setPosicion(arco.getX() + 20, partido.getCancha().getSuelo() - Balon.DIAMETRO);
        balon.setVelocidad(0, 0);
        partido.actualizar(0.05);
    }

    private Partido partidoPorTiempo(int minutos) {
        Partido partido = new PartidoPorTiempo(List.of(jugador("Camerún"), jugador("Nigeria")), minutos);
        partido.iniciar();
        return partido;
    }

    @Test
    void prolongarAlargaLaVidaDeUnBalonFalsoPeroNoHaceNadaEnUnObjetoInerte() {
        BalonFalso falso = new BalonFalso(0, 0, 1);
        ObjetoInerte inerte = new ObjetoInerte();

        falso.prolongar(5);
        inerte.prolongar(5);
        falso.actualizar(2);

        assertFalse(falso.haExpirado());
        assertFalse(inerte.haExpirado());
    }

    @Test
    void disiparIlusionExpiraUnBalonFalsoPeroNoUnObjetoInerte() {
        BalonFalso falso = new BalonFalso(0, 0, 10);
        ObjetoInerte inerte = new ObjetoInerte();
        assertFalse(falso.haExpirado());

        falso.disiparIlusion();
        inerte.disiparIlusion();

        assertTrue(falso.haExpirado());
        assertFalse(inerte.haExpirado());

        // desaparecer, en cambio, vive en la base y expira a cualquier objeto
        inerte.desaparecer();
        assertTrue(inerte.haExpirado());
    }

    @Test
    void elToqueDeUnJugadorMueveUnBalonPeroNoHaceNadaEnUnObjetoInerte() {
        Partido partido = partidoPorTiempo(1);
        Jugador uno = partido.getJugadores().get(0);
        Balon balon = new Balon(uno.getX(), uno.getY());
        ObjetoInerte inerte = new ObjetoInerte();
        inerte.setPosicion(uno.getX(), uno.getY());

        uno.afectar(balon);
        uno.afectar(inerte);

        assertTrue(balon.getVelX() != 0 || balon.getVelY() != 0);
        assertEquals(0, inerte.getVelX(), 1e-9);
        assertEquals(0, inerte.getVelY(), 1e-9);
        assertEquals(uno.getX(), inerte.getX(), 1e-9);
    }

    @Test
    void recolectarBalonesSoloAportaLosBalones() {
        List<Balon> destino = new ArrayList<>();
        Balon balon = new Balon(0, 0);

        balon.recolectarBalones(destino);
        new ObjetoInerte().recolectarBalones(destino);

        assertEquals(1, destino.size());
        assertSame(balon, destino.get(0));
    }

    @Test
    void getBalonesExtraIgnoraLosObjetosQueNoSonBalones() {
        Partido partido = partidoPorTiempo(1);
        ilusion().crearBalonesFalsos(partido, 7, 2.5, Math.toRadians(25));
        partido.agregarObjeto(new ObjetoInerte());

        assertEquals(8, partido.getObjetosExtra().size());
        assertEquals(7, partido.getBalonesExtra().size());
    }

    @Test
    void crearBalonesFalsosReemplazaLosAnterioresYConservaOtrosObjetos() {
        Partido partido = partidoPorTiempo(1);
        ilusion().crearBalonesFalsos(partido, 3, 2.5, Math.toRadians(25));
        ObjetoInerte inerte = new ObjetoInerte();
        partido.agregarObjeto(inerte);

        ilusion().crearBalonesFalsos(partido, 5, 2.5, Math.toRadians(25));

        assertEquals(5, partido.getBalonesExtra().size());
        assertTrue(partido.getObjetosExtra().contains(inerte));
    }

    @Test
    void prolongarBalonesFalsosMantieneVivosLosBalonesYNoAfectaAOtrosObjetos() {
        Partido partido = partidoPorTiempo(1);
        ilusion().crearBalonesFalsos(partido, 2, 0.1, Math.toRadians(25));
        partido.agregarObjeto(new ObjetoInerte());

        new Confusion(5).afectarEscenario(partido);
        for (int i = 0; i < 120; i++) {
            partido.actualizar(DT);
        }

        assertEquals(2, partido.getBalonesExtra().size());
        assertEquals(3, partido.getObjetosExtra().size());
    }

    @Test
    void sinProlongarLosBalonesFalsosExpiran() {
        Partido partido = partidoPorTiempo(1);
        ilusion().crearBalonesFalsos(partido, 2, 0.1, Math.toRadians(25));

        for (int i = 0; i < 60; i++) {
            partido.actualizar(DT);
        }

        assertEquals(0, partido.getBalonesExtra().size());
    }

    @Test
    void muerteSubitaSoloOcurreAlAcabarElTiempoConEmpate() {
        PartidoPorTiempo partido = new PartidoPorTiempo(List.of(jugador("Camerún"), jugador("Nigeria")), 1);
        partido.iniciar();
        assertFalse(partido.estaEnMuerteSubita());

        for (int i = 0; i < 1300; i++) {
            partido.actualizar(0.05);
        }

        assertEquals(0, partido.getGoles(partido.getCancha().getArcoIzquierdo()));
        assertEquals(0, partido.getGoles(partido.getCancha().getArcoDerecho()));
        assertTrue(partido.estaEnMuerteSubita());
        assertFalse(partido.hayGanador());

        golEn(partido, partido.getCancha().getArcoDerecho());
        assertFalse(partido.estaEnMuerteSubita());
    }

    @Test
    void sinEmpateAlAcabarElTiempoNoHayMuerteSubita() {
        PartidoPorTiempo partido = new PartidoPorTiempo(List.of(jugador("Camerún"), jugador("Nigeria")), 1);
        partido.iniciar();
        golEn(partido, partido.getCancha().getArcoIzquierdo());

        for (int i = 0; i < 1300; i++) {
            partido.actualizar(0.05);
        }

        assertFalse(partido.estaEnMuerteSubita());
        assertTrue(partido.hayGanador());
    }

    @Test
    void unPartidoPorGolesNuncaTieneMuerteSubita() {
        Partido partido = new PartidoPorGoles(List.of(jugador("Camerún"), jugador("Nigeria")), 3);
        partido.iniciar();

        for (int i = 0; i < 600; i++) {
            partido.actualizar(DT);
        }

        assertFalse(partido.estaEnMuerteSubita());
    }

    @Test
    void cadaEfectoTieneSuNombre() {
        assertEquals("Empuje", new Empuje(1, 100).getNombre());
        assertEquals("Electrocutado", new Electrocutado(1).getNombre());
        assertEquals("Inmovilizado", new Inmovilizado(1).getNombre());
        assertEquals("Confusion", new Confusion(1).getNombre());
    }
}
