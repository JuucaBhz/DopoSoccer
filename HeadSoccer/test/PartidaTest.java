package test;

import static org.junit.jupiter.api.Assertions.*;

import domain.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PartidaTest {

    private static final double DT = 1.0 / 60;
    private static final double MARGEN = 1e-9;

    @TempDir
    File carpeta;

    private File archivoPartida;
    private String rutaUsuarios;

    @BeforeEach
    void prepararArchivos() {
        archivoPartida = new File(carpeta, "prueba.partida");
        rutaUsuarios = new File(carpeta, "usuarios.dat").getPath();
    }

    private HeadSoccer nuevoJuego() {
        return new HeadSoccer(new RegistroUsuarios(rutaUsuarios));
    }

    private void avanzar(HeadSoccer juego, int frames) {
        for (int i = 0; i < frames; i++) {
            juego.actualizar(DT);
        }
    }

    // Mete el balón en el arco derecho: gol del lado del jugador 1
    private void golDelJugadorUno(HeadSoccer juego) {
        Partido partido = juego.getPartido();
        Arco arco = partido.getCancha().getArcoDerecho();
        Balon balon = partido.getBalon();
        balon.descargarPoder();
        balon.setPosicion(arco.getX() + 20, partido.getCancha().getSuelo() - Balon.DIAMETRO);
        balon.setVelocidad(0, 0);
        juego.actualizar(DT);
    }

    @Test
    void guardarCambiarYAbrirVuelveAlEstadoGuardado() throws HeadSoccerException {
        HeadSoccer juego = nuevoJuego();
        juego.iniciarSesion(1, "Ana");
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);
        golDelJugadorUno(juego);
        juego.mover(1, 1);
        avanzar(juego, 30);
        juego.getJugador(2).recibirDano(30);
        juego.getJugador(2).aplicarEfecto(new Electrocutado(3));

        int golesUno = juego.getPartido().getGoles(juego.getJugador(1));
        double tiempo = juego.getPartido().getTiempoJugado();
        int vidaDos = juego.getJugador(2).getVida();
        double xUno = juego.getJugador(1).getX();
        double xDos = juego.getJugador(2).getX();
        double xBalon = juego.getPartido().getBalon().getX();
        juego.salve(archivoPartida);

        // Se cambia todo: más movimiento, otro gol, más daño
        juego.mover(2, -1);
        avanzar(juego, 60);
        golDelJugadorUno(juego);
        juego.getJugador(2).recibirDano(20);

        juego.cargue(archivoPartida);

        Partido abierto = juego.getPartido();
        assertTrue(abierto.estaPausado());
        assertEquals(golesUno, abierto.getGoles(juego.getJugador(1)));
        assertEquals(0, abierto.getGoles(juego.getJugador(2)));
        assertEquals(tiempo, abierto.getTiempoJugado(), MARGEN);
        assertEquals(vidaDos, juego.getJugador(2).getVida());
        assertEquals("Electrocutado", juego.getJugador(2).getEfectoActual().getNombre());
        assertEquals(xUno, juego.getJugador(1).getX(), MARGEN);
        assertEquals(xDos, juego.getJugador(2).getX(), MARGEN);
        assertEquals(xBalon, abierto.getBalon().getX(), MARGEN);
        assertEquals("Camerún", juego.getJugador(1).getPersonaje().getPais());
        assertSame(abierto.getJugadores().get(0), juego.getJugador(1));
        assertEquals("Ana", juego.getJugador(1).getUsuario().getNombre());

        // Queda en pausa hasta Continuar; después sigue jugándose normalmente
        avanzar(juego, 10);
        assertEquals(tiempo, abierto.getTiempoJugado(), MARGEN);
        juego.reanudar();
        avanzar(juego, 10);
        assertTrue(abierto.getTiempoJugado() > tiempo);
    }

    @Test
    void abrirUnArchivoInvalidoLanzaExcepcionYNoCambiaElPartidoActual() throws IOException {
        HeadSoccer juego = nuevoJuego();
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);
        golDelJugadorUno(juego);
        Partido actual = juego.getPartido();
        Jugador uno = juego.getJugador(1);
        File basura = new File(carpeta, "basura.partida");
        try (FileWriter escritor = new FileWriter(basura)) {
            escritor.write("esto no es una partida");
        }

        HeadSoccerException e = assertThrows(HeadSoccerException.class, () -> juego.cargue(basura));
        assertEquals(HeadSoccerException.ERROR_ABRIR_PARTIDA, e.getMessage());
        assertThrows(HeadSoccerException.class, () -> juego.cargue(new File(carpeta, "no_existe.partida")));

        assertSame(actual, juego.getPartido());
        assertSame(uno, juego.getJugador(1));
        assertEquals(1, actual.getGoles(uno));
        assertFalse(actual.estaPausado());
    }

    @Test
    void guardarSinPartidoEnCursoLanzaExcepcion() throws HeadSoccerException {
        HeadSoccer juego = nuevoJuego();
        HeadSoccerException e = assertThrows(HeadSoccerException.class, () -> juego.salve(archivoPartida));
        assertEquals(HeadSoccerException.SIN_PARTIDO_EN_CURSO, e.getMessage());

        // Un partido que ya terminó tampoco está en curso
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 1);
        golDelJugadorUno(juego);
        assertTrue(juego.estaTerminado());
        assertThrows(HeadSoccerException.class, () -> juego.salve(archivoPartida));
        assertFalse(archivoPartida.exists());
    }

    @Test
    void abrirUnaPartidaViejaNoLeQuitaPuntosNiNivelesAlUsuario() throws HeadSoccerException {
        HeadSoccer juego = nuevoJuego();
        juego.iniciarSesion(1, "Ana");
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);
        juego.salve(archivoPartida);

        // Después de guardar la partida, Ana gana puntos y compra una mejora (queda en el registro)
        juego.getUsuario(1).sumarPuntos(800);
        juego.mejorarEstadistica(1, Habilidad.SPEED);

        // Se abre la partida vieja en otra sesión del juego, leyendo el registro desde el archivo
        HeadSoccer otraSesion = nuevoJuego();
        otraSesion.cargue(archivoPartida);
        Usuario ana = otraSesion.getJugador(1).getUsuario();
        assertEquals("Ana", ana.getNombre());
        assertEquals(300, ana.getPuntos());
        assertEquals(2, ana.getEstadisticas().getNivel(Habilidad.SPEED));
        assertSame(ana, otraSesion.getUsuario(1));

        // Y en la misma sesión también se reconecta al usuario actual
        juego.cargue(archivoPartida);
        assertSame(juego.getUsuario(1), juego.getJugador(1).getUsuario());
        assertEquals(300, juego.getJugador(1).getPuntos());
    }

    @Test
    void unaPartidaContraLaMaquinaSeGuardaYSeAbreConSuPerfil() throws HeadSoccerException {
        HeadSoccer juego = nuevoJuego();
        juego.seleccionarModo(true, "Torpe");
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);
        avanzar(juego, 20);
        juego.salve(archivoPartida);

        HeadSoccer otraSesion = nuevoJuego();
        otraSesion.cargue(archivoPartida);

        Jugador maquina = otraSesion.getJugador(2);
        assertEquals(TipoJugador.MAQUINA, maquina.getTipo());
        assertEquals(JugadorMaquina.nombreMaquina, maquina.getUsuario().getNombre());
        assertEquals(TipoJugador.MAQUINA, otraSesion.getModo().getTipo(2));
        otraSesion.reanudar();
        assertDoesNotThrow(() -> avanzar(otraSesion, 120));
    }
}
