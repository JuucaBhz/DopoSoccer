package test;

import static org.junit.jupiter.api.Assertions.*;

import domain.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UsuariosTest {

    private static final double DT = 1.0 / 60;

    @TempDir
    File carpeta;

    private String ruta;

    @BeforeEach
    void prepararArchivo() {
        ruta = new File(carpeta, "usuarios.dat").getPath();
    }

    // Lee de nuevo el archivo con un registro nuevo, como si se abriera el juego otra vez
    private RegistroUsuarios releer() throws HeadSoccerException {
        RegistroUsuarios otro = new RegistroUsuarios(ruta);
        otro.cargar();
        return otro;
    }

    // Mete el balón en el arco derecho: gol del jugador 1
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
    void crearUnUsuarioLoGuardaEnElArchivo() throws HeadSoccerException {
        RegistroUsuarios registro = new RegistroUsuarios(ruta);
        registro.cargar();

        registro.obtenerOCrear("Ana");

        assertTrue(new File(ruta).exists());
        assertNotNull(releer().buscar("Ana"));
    }

    @Test
    void unRegistroNuevoRecuperaNombrePuntosYNiveles() throws HeadSoccerException {
        RegistroUsuarios registro = new RegistroUsuarios(ruta);
        Usuario ana = registro.obtenerOCrear("Ana");
        ana.sumarPuntos(1700);
        ana.mejorarEstadistica(Habilidad.SPEED);
        registro.guardar();

        Usuario leida = releer().buscar("  ANA ");

        assertEquals("Ana", leida.getNombre());
        assertEquals(1200, leida.getPuntos());
        assertEquals(2, leida.getEstadisticas().getNivel(Habilidad.SPEED));
        assertEquals(1, leida.getEstadisticas().getNivel(Habilidad.JUMP));
        assertEquals(1000, leida.getEstadisticas().precioSiguienteMejora());
    }

    @Test
    void buscarIgnoraMayusculasYEspacios() throws HeadSoccerException {
        RegistroUsuarios registro = new RegistroUsuarios(ruta);
        Usuario ana = registro.obtenerOCrear("Ana");
        assertSame(ana, registro.obtenerOCrear(" aNA  "));
    }

    @Test
    void alTerminarUnPartidoSeRepartenLosPuntosUnaVezYQuedanGuardados() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer(new RegistroUsuarios(ruta));
        juego.iniciarSesion(1, "Ana");
        juego.iniciarSesion(2, "Beto");
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 1);

        golDelJugadorUno(juego);
        for (int i = 0; i < 30; i++) {
            juego.actualizar(DT);
        }

        assertTrue(juego.estaTerminado());
        assertEquals(Partido.puntosGanador, juego.getJugador(1).getPuntos());
        assertEquals(Partido.puntosPerdedor, juego.getJugador(2).getPuntos());
        RegistroUsuarios leido = releer();
        assertEquals(Partido.puntosGanador, leido.buscar("Ana").getPuntos());
        assertEquals(Partido.puntosPerdedor, leido.buscar("Beto").getPuntos());
    }

    @Test
    void mejorarUnaEstadisticaQuedaGuardado() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer(new RegistroUsuarios(ruta));
        juego.iniciarSesion(1, "Ana");
        juego.getUsuario(1).sumarPuntos(500);

        juego.mejorarEstadistica(1, Habilidad.KICK);

        Usuario leida = releer().buscar("Ana");
        assertEquals(2, leida.getEstadisticas().getNivel(Habilidad.KICK));
        assertEquals(0, leida.getPuntos());
    }

    @Test
    void unNombreInvalidoLanzaExcepcion() {
        RegistroUsuarios registro = new RegistroUsuarios(ruta);
        HeadSoccer juego = new HeadSoccer(registro);
        for (String nombre : new String[] {null, "", "   "}) {
            HeadSoccerException e = assertThrows(HeadSoccerException.class, () -> juego.iniciarSesion(1, nombre));
            assertEquals(HeadSoccerException.NOMBRE_INVALIDO, e.getMessage());
        }
        assertFalse(new File(ruta).exists());
    }

    @Test
    void enPvPLosDosJugadoresNoPuedenUsarElMismoUsuario() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer(new RegistroUsuarios(ruta));
        juego.iniciarSesion(1, "Ana");

        HeadSoccerException e = assertThrows(HeadSoccerException.class, () -> juego.iniciarSesion(2, " aNA "));
        assertEquals(HeadSoccerException.USUARIO_EN_USO, e.getMessage());
        assertNotSame(juego.getUsuario(1), juego.getUsuario(2));

        // El mismo jugador puede volver a entrar con su nombre, y otro nombre sí se acepta
        juego.iniciarSesion(1, "Ana");
        juego.iniciarSesion(2, "Beto");
        assertEquals("Beto", juego.getUsuario(2).getNombre());

        // Al cerrar la sesión del 2, el 1 puede tomar ese nombre (por ejemplo para cambiar de lado)
        juego.cerrarSesion(2);
        juego.iniciarSesion(1, "Beto");
        assertEquals("Beto", juego.getUsuario(1).getNombre());
    }

    @Test
    void alCambiarDePvPAPvMElHumanoConservaSuUsuarioYLaMaquinaNoSeGuarda() throws HeadSoccerException {
        RegistroUsuarios registro = new RegistroUsuarios(ruta);
        HeadSoccer juego = new HeadSoccer(registro);
        juego.iniciarSesion(1, "Ana");
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);
        Usuario ana = juego.getJugador(1).getUsuario();

        juego.seleccionarModo(true, "Torpe");
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);

        assertSame(ana, juego.getJugador(1).getUsuario());
        assertFalse(registro.contiene(juego.getJugador(2).getUsuario()));
        assertEquals(JugadorMaquina.nombreMaquina, juego.getJugador(2).getUsuario().getNombre());
    }

    @Test
    void unArchivoDanadoNoImpideJugar() throws IOException, HeadSoccerException {
        try (FileWriter basura = new FileWriter(ruta)) {
            basura.write("esto no es un archivo de usuarios");
        }
        assertThrows(HeadSoccerException.class, () -> new RegistroUsuarios(ruta).cargar());

        HeadSoccer juego = new HeadSoccer(new RegistroUsuarios(ruta));
        juego.iniciarSesion(1, "Ana");
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 5);
        assertEquals("Ana", juego.getJugador(1).getUsuario().getNombre());
    }
}
