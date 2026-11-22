package test;

import static org.junit.jupiter.api.Assertions.*;

import domain.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ExcepcionesTest {

    @TempDir
    Path carpeta;

    private Path log;

    // Todo el log de estas pruebas va a un archivo temporal para no ensuciar la raíz del proyecto.
    @BeforeEach
    void redirigirLog() {
        log = carpeta.resolve("prueba.log");
        LogErrores.usarArchivo(log.toString());
    }

    @AfterEach
    void restaurarLog() {
        LogErrores.usarArchivo(null);
    }

    private String contenidoDelLog() throws IOException {
        return Files.exists(log) ? Files.readString(log) : "";
    }

    @Test
    void paisInvalidoLanzaDatoInvalidoConSuMensaje() {
        DatoInvalidoException e = assertThrows(DatoInvalidoException.class, () -> Personaje.crear("Canadá"));
        assertTrue(e.getMessage().startsWith(DatoInvalidoException.PAIS_INVALIDO));
        assertTrue(e.getMessage().contains("Canadá"));
    }

    @Test
    void paisInvalidoAlIniciarPartidoNoRequiereThrowsYLanzaLaExcepcion() {
        HeadSoccer juego = new HeadSoccer();
        DatoInvalidoException e = assertThrows(DatoInvalidoException.class,
            () -> juego.iniciarPartido("Canadá", "Nigeria", "Tiempo", 1));
        assertTrue(e.getMessage().startsWith(DatoInvalidoException.PAIS_INVALIDO));
    }

    @Test
    void perfilInvalidoLanzaDatoInvalidoConSuMensaje() {
        DatoInvalidoException e = assertThrows(DatoInvalidoException.class, () -> PerfilMaquina.crear("Inventada"));
        assertTrue(e.getMessage().startsWith(DatoInvalidoException.PERFIL_INVALIDO));
        assertTrue(e.getMessage().contains("Inventada"));
    }

    @Test
    void modoInvalidoLanzaDatoInvalidoYNoCambiaElModo() {
        ModoJuego modo = new ModoJuego();

        DatoInvalidoException e = assertThrows(DatoInvalidoException.class,
            () -> modo.seleccionarModo(java.util.Arrays.asList(TipoJugador.HUMANO, TipoJugador.MAQUINA), java.util.Arrays.asList(null, null)));

        assertEquals(DatoInvalidoException.MODO_INVALIDO, e.getMessage());
        assertEquals(TipoJugador.HUMANO, modo.getTipo(2));
    }

    @Test
    void datoInvalidoConservaLaCausa() {
        Throwable causa = new IllegalStateException("origen");
        DatoInvalidoException e = new DatoInvalidoException("mensaje", causa);
        assertSame(causa, e.getCause());
    }

    @Test
    void datoInvalidoYHeadSoccerExceptionSonDeTiposDistintos() {
        assertTrue(RuntimeException.class.isAssignableFrom(DatoInvalidoException.class));
        assertFalse(RuntimeException.class.isAssignableFrom(HeadSoccerException.class));
    }

    @Test
    void datoInvalidoQuedaRegistradoEnElLog() throws IOException {
        assertThrows(DatoInvalidoException.class, () -> Personaje.crear("Canadá"));

        String texto = contenidoDelLog();
        assertTrue(texto.contains("DatoInvalidoException"));
        assertTrue(texto.contains(DatoInvalidoException.PAIS_INVALIDO));
        assertTrue(texto.contains("Canadá"));
    }

    @Test
    void headSoccerExceptionQuedaRegistradaEnElLogAunSiNoPasaPorHeadSoccer() throws IOException {
        Jugador jugador = new JugadorHumano(Personaje.crear("Camerún"));

        assertThrows(HeadSoccerException.class, () -> jugador.mejorarEstadistica(Habilidad.SPEED));

        assertTrue(contenidoDelLog().contains(HeadSoccerException.PUNTOS_INSUFICIENTES));
    }

    @Test
    void perfilNoDisponibleQuedaRegistrado() throws IOException {
        assertThrows(HeadSoccerException.class, () -> PerfilMaquina.crear("Ofensiva"));
        assertTrue(contenidoDelLog().contains(HeadSoccerException.PERFIL_NO_DISPONIBLE));
    }

    @Test
    void salveSinPartidoEnCursoLanzaYQuedaRegistrado() throws IOException {
        HeadSoccer juego = new HeadSoccer();

        HeadSoccerException e = assertThrows(HeadSoccerException.class,
            () -> juego.salve(carpeta.resolve("x.partida").toFile()));

        assertEquals(HeadSoccerException.SIN_PARTIDO_EN_CURSO, e.getMessage());
        assertTrue(contenidoDelLog().contains(HeadSoccerException.SIN_PARTIDO_EN_CURSO));
        assertFalse(Files.exists(carpeta.resolve("x.partida")));
    }

    @Test
    void cargueDeUnArchivoInexistenteLanzaYQuedaRegistrado() throws IOException {
        HeadSoccer juego = new HeadSoccer();

        HeadSoccerException e = assertThrows(HeadSoccerException.class,
            () -> juego.cargue(carpeta.resolve("x.partida").toFile()));

        assertEquals(HeadSoccerException.ERROR_ABRIR_PARTIDA, e.getMessage());
        assertTrue(contenidoDelLog().contains(HeadSoccerException.ERROR_ABRIR_PARTIDA));
    }

    @Test
    void salveYCargueNoCambianElEstadoDelPartido() throws HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 3);
        juego.pausar();
        Partido partido = juego.getPartido();
        int goles1 = juego.getPartido().getGoles(juego.getJugador(1));
        int goles2 = juego.getPartido().getGoles(juego.getJugador(2));
        double x1 = juego.getJugador(1).getX();
        double x2 = juego.getJugador(2).getX();
        File archivo = carpeta.resolve("no_existe.partida").toFile();

        assertThrows(HeadSoccerException.class, () -> juego.salve(null));
        assertThrows(HeadSoccerException.class, () -> juego.cargue(archivo));
        assertThrows(HeadSoccerException.class, () -> juego.cargue(null));

        assertSame(partido, juego.getPartido());
        assertTrue(partido.estaPausado());
        assertEquals(goles1, juego.getPartido().getGoles(juego.getJugador(1)));
        assertEquals(goles2, juego.getPartido().getGoles(juego.getJugador(2)));
        assertEquals(x1, juego.getJugador(1).getX());
        assertEquals(x2, juego.getJugador(2).getX());
    }

    @Test
    void cargueDeUnArchivoQueNoEsPartidaFallaYNoLoModifica() throws IOException, HeadSoccerException {
        HeadSoccer juego = new HeadSoccer();
        juego.iniciarPartido("Camerún", "Nigeria", "Goles", 3);
        Path existente = carpeta.resolve("existente.partida");
        Files.writeString(existente, "contenido original");

        assertThrows(HeadSoccerException.class, () -> juego.cargue(existente.toFile()));

        assertEquals("contenido original", Files.readString(existente));
    }

    @Test
    void siElLogNoSePuedeEscribirLaExcepcionOriginalSeLanzaIgual() {
        LogErrores.usarArchivo(carpeta.resolve("carpeta_inexistente").resolve("x.log").toString());

        assertDoesNotThrow(() -> LogErrores.registrar(new RuntimeException("prueba")));
        assertDoesNotThrow(() -> LogErrores.registrar(null));
        assertThrows(DatoInvalidoException.class, () -> Personaje.crear("Canadá"));
        assertThrows(HeadSoccerException.class, () -> new HeadSoccer().cargue(null));
    }

    @Test
    void usarArchivoNuloRestauraElArchivoPorDefecto() throws IOException {
        LogErrores.usarArchivo(null);
        File porDefecto = new File(LogErrores.ARCHIVO);
        boolean existiaAntes = porDefecto.exists();
        long tamanoAntes = existiaAntes ? porDefecto.length() : 0;
        LogErrores.usarArchivo(log.toString());

        LogErrores.registrar(new RuntimeException("solo al temporal"));

        assertEquals(existiaAntes ? tamanoAntes : 0, porDefecto.exists() ? porDefecto.length() : 0);
        assertTrue(contenidoDelLog().contains("solo al temporal"));
    }
}
