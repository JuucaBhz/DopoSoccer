
package test;
import static org.junit.jupiter.api.Assertions.*;
import domain.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class TorpeTest {

    private static final double DT = 1.0 / 60;
    private static final double REACCION = PerfilMaquina.tiempoReaccionPorDefecto;

    private static class JugadorEspia extends Jugador {
        List<String> acciones = new ArrayList<>();

        JugadorEspia() {
            super(Personaje.crear("Camerún"));
        }

        @Override
        public void mover(int direccion) {
            acciones.add("mover" + direccion);
            super.mover(direccion);
        }

        @Override
        public void saltar() {
            acciones.add("saltar");
            super.saltar();
        }

        @Override
        public void patear(List<Jugador> rivales, Balon b) {
            acciones.add("patear");
            super.patear(rivales, b);
        }

        @Override
        public void activarPoder() {
            acciones.add("poder");
            super.activarPoder();
        }

        long contar(String accion) {
            return acciones.stream().filter(a -> a.equals(accion)).count();
        }
    }

    private static class PerfilContador extends PerfilMaquina {
        int elecciones;

        @Override
        protected void elegirAccion(Jugador yo, Escenario escenario) {
            elecciones++;
        }
    }

    private static class AzarFijo extends Random {
        private double valor;

        AzarFijo(double valor) {
            this.valor = valor;
        }

        @Override
        public double nextDouble() {
            return valor;
        }
    }

    private Partido nuevoPartido(Jugador j1, Jugador j2) {
        Partido partido = new PartidoPorGoles(List.of(j1, j2), 5);
        partido.iniciar();
        return partido;
    }

    @Test
    void aturdidoNoHaceNada() {
        JugadorEspia yo = new JugadorEspia();
        JugadorEspia rival = new JugadorEspia();
        Partido partido = nuevoPartido(yo, rival);
        yo.recibirDano(1000);
        assertTrue(yo.estaAturdido());
        yo.acciones.clear();
        Torpe torpe = new Torpe(new AzarFijo(0.7));

        for (int i = 0; i < 10; i++) {
            torpe.decidir(yo, partido, 1);
        }

        assertTrue(yo.acciones.isEmpty());
    }

    @Test
    void noEligeAccionAntesDelTiempoDeReaccion() {
        JugadorEspia yo = new JugadorEspia();
        Partido partido = nuevoPartido(yo, new JugadorEspia());
        PerfilContador perfil = new PerfilContador();

        perfil.decidir(yo, partido, REACCION / 3);
        perfil.decidir(yo, partido, REACCION / 3);
        assertEquals(0, perfil.elecciones);

        perfil.decidir(yo, partido, REACCION / 3 + 0.01);
        assertEquals(1, perfil.elecciones);

        perfil.decidir(yo, partido, 0.01);
        assertEquals(1, perfil.elecciones);
    }

    @Test
    void conSemillaFijaLaSecuenciaEsReproducible() {
        JugadorEspia yoA = new JugadorEspia();
        JugadorEspia yoB = new JugadorEspia();
        Partido partidoA = nuevoPartido(yoA, new JugadorEspia());
        Partido partidoB = nuevoPartido(yoB, new JugadorEspia());
        Torpe torpeA = new Torpe(new Random(42));
        Torpe torpeB = new Torpe(new Random(42));

        for (int i = 0; i < 300; i++) {
            torpeA.decidir(yoA, partidoA, DT);
            torpeB.decidir(yoB, partidoB, DT);
        }

        assertFalse(yoA.acciones.isEmpty());
        assertEquals(yoA.acciones, yoB.acciones);
    }

    @Test
    void noActivaPoderConBarraVacia() {
        JugadorEspia yo = new JugadorEspia();
        Partido partido = nuevoPartido(yo, new JugadorEspia());
        Torpe torpe = new Torpe(new AzarFijo(0.95));

        torpe.decidir(yo, partido, REACCION);

        assertEquals(0, yo.contar("poder"));
    }

    @Test
    void activaPoderConBarraLlenaYNoLoRepiteSiYaEstaActivo() {
        JugadorEspia yo = new JugadorEspia();
        Partido partido = nuevoPartido(yo, new JugadorEspia());
        Torpe torpe = new Torpe(new AzarFijo(0.95));

        yo.getBarraPoder().cargar(BarraPoder.segundosCarga);
        torpe.decidir(yo, partido, REACCION);
        assertEquals(1, yo.contar("poder"));
        assertNotNull(yo.getPoderActivo());

        yo.getBarraPoder().cargar(BarraPoder.segundosCarga);
        torpe.decidir(yo, partido, REACCION);
        assertEquals(1, yo.contar("poder"));
    }

    @Test
    void cadaRangoDelSorteoEjecutaSuAccion() {
        JugadorEspia yo = new JugadorEspia();
        Partido partido = nuevoPartido(yo, new JugadorEspia());

        new Torpe(new AzarFijo(0.7)).decidir(yo, partido, REACCION);
        assertEquals(1, yo.contar("saltar"));

        new Torpe(new AzarFijo(0.8)).decidir(yo, partido, REACCION);
        assertEquals(1, yo.contar("patear"));
    }

    @Test
    void partidoConMaquinaTorpeCorreSinExcepciones() {
        JugadorMaquina maquina = new JugadorMaquina(Personaje.crear("Nigeria"), new Torpe(new Random(7)));
        JugadorHumano humano = new JugadorHumano(Personaje.crear("Corea del Sur"));
        Partido partido = nuevoPartido(humano, maquina);

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 3000; i++) {
                partido.actualizar(DT);
            }
        });
    }
}
