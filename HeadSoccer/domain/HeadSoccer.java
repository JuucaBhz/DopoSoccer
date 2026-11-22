package domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeadSoccer {
    public static final String encabezadoPartida = "HeadSoccer-partida";
    // Versión 2: los partidos guardan sus apariciones (las partidas de la versión 1 ya no se abren)
    public static final int versionPartida = 2;

    private List<Jugador> jugadores = new ArrayList<>();
    private Partido partido;
    private ModoJuego modo = new ModoJuego();
    private RegistroUsuarios registro;
    // Usuario de cada número de jugador; se conserva aunque el jugador cambie de tipo (PvP <-> PvM)
    private Map<Integer, Usuario> usuarios = new HashMap<>();
    // Apariciones que se ponen en cada partido que empiece; por defecto ninguna
    private List<Aparicion> apariciones = new ArrayList<>();

    // Abre el juego con el archivo de usuarios por defecto
    public HeadSoccer() {
        this(new RegistroUsuarios());
    }

    // Abre el juego con el registro dado y carga sus usuarios; si el archivo falla arranca vacío
    public HeadSoccer(RegistroUsuarios registro) {
        this.registro = registro;
        try {
            registro.cargar();
        } catch (HeadSoccerException e) {
        }
    }

    // Login del jugador número n: busca o crea (y guarda) el usuario con ese nombre; falla si otro jugador ya lo usa
    public void iniciarSesion(int numero, String nombre) throws HeadSoccerException {
        Usuario existente = registro.buscar(nombre);
        for (Map.Entry<Integer, Usuario> sesion : usuarios.entrySet()) {
            if (existente != null && sesion.getKey() != numero && sesion.getValue() == existente) {
                throw new HeadSoccerException(HeadSoccerException.USUARIO_EN_USO);
            }
        }
        usuarios.put(numero, registro.obtenerOCrear(nombre));
    }

    // Cierra la sesión del jugador número n; vuelve a ser invitado hasta que inicie sesión otra vez
    public void cerrarSesion(int numero) {
        usuarios.remove(numero);
    }

    // Devuelve el usuario del jugador número n; si nadie inició sesión es un invitado que no se guarda
    public Usuario getUsuario(int numero) {
        return usuarios.computeIfAbsent(numero, n -> Jugador.crearInvitado(n));
    }

    // Versión para la GUI de dos jugadores: el 1 siempre es humano y solo el 2 puede ser máquina
    public void seleccionarModo(boolean segundoEsMaquina, String nombrePerfil) throws HeadSoccerException {
        TipoJugador segundo = segundoEsMaquina ? TipoJugador.MAQUINA : TipoJugador.HUMANO;
        String perfilSegundo = segundoEsMaquina ? nombrePerfil : null;
        seleccionarModo(Arrays.asList(TipoJugador.HUMANO, segundo), Arrays.asList(null, perfilSegundo));
    }

    // Modo para n jugadores: un tipo y un nombre de perfil por jugador, en orden (null si no tiene perfil).
    // Los perfiles se crean (y validan) antes de tocar el modo, así si algo falla el modo anterior queda intacto.
    public void seleccionarModo(List<TipoJugador> tipos, List<String> perfiles) throws HeadSoccerException {
        List<PerfilMaquina> creados = new ArrayList<>();
        for (String nombre : perfiles) {
            creados.add(nombre == null ? null : PerfilMaquina.crear(nombre));
        }
        modo.seleccionarModo(tipos, creados);
    }

    public ModoJuego getModo() {
        return modo;
    }

    // Guarda una foto del partido en curso y del modo; de los usuarios solo guarda el nombre de los registrados
    public void salve(File archivo) throws HeadSoccerException {
        if (partido == null || partido.estaTerminado()) {
            throw new HeadSoccerException(HeadSoccerException.SIN_PARTIDO_EN_CURSO);
        }
        if (archivo == null) {
            throw new HeadSoccerException(HeadSoccerException.ERROR_GUARDAR_PARTIDA);
        }
        ArrayList<String> nombres = new ArrayList<>();
        for (Jugador j : jugadores) {
            nombres.add(registro.contiene(j.getUsuario()) ? j.getUsuario().getNombre() : null);
        }
        try (FileOutputStream salidaArchivo = new FileOutputStream(archivo);
             ObjectOutputStream salida = new ObjectOutputStream(salidaArchivo)) {
            salida.writeUTF(encabezadoPartida);
            salida.writeInt(versionPartida);
            salida.writeObject(partido);
            salida.writeObject(modo);
            salida.writeObject(nombres);
        } catch (Exception e) {
            throw new HeadSoccerException(HeadSoccerException.ERROR_GUARDAR_PARTIDA, e);
        }
    }

    // Abre una partida guardada: lee todo aparte y solo si nada falla reemplaza partido, jugadores y modo.
    // Cada humano se reconecta al usuario ACTUAL del registro, así abrir algo viejo no quita puntos ni niveles.
    public void cargue(File archivo) throws HeadSoccerException {
        if (archivo == null) {
            throw new HeadSoccerException(HeadSoccerException.ERROR_ABRIR_PARTIDA);
        }
        Partido partidoLeido;
        ModoJuego modoLeido;
        List<String> nombres = new ArrayList<>();
        try (FileInputStream entradaArchivo = new FileInputStream(archivo);
             ObjectInputStream entrada = new ObjectInputStream(entradaArchivo)) {
            if (!encabezadoPartida.equals(entrada.readUTF()) || entrada.readInt() != versionPartida) {
                throw new StreamCorruptedException("No es una partida de esta versión");
            }
            partidoLeido = Partido.class.cast(entrada.readObject());
            modoLeido = ModoJuego.class.cast(entrada.readObject());
            List<?> leidos = List.class.cast(entrada.readObject());
            for (Object nombre : leidos) {
                nombres.add(String.class.cast(nombre));
            }
        } catch (Exception e) {
            throw new HeadSoccerException(HeadSoccerException.ERROR_ABRIR_PARTIDA, e);
        }
        List<Jugador> jugadoresLeidos = new ArrayList<>(partidoLeido.getJugadores());
        if (nombres.size() != jugadoresLeidos.size()) {
            throw new HeadSoccerException(HeadSoccerException.ERROR_ABRIR_PARTIDA);
        }
        Map<Integer, Usuario> usuariosLeidos = new HashMap<>(usuarios);
        for (int i = 0; i < jugadoresLeidos.size(); i++) {
            String nombre = nombres.get(i);
            Usuario usuario = nombre == null ? Jugador.crearInvitado(i + 1) : registro.obtenerOCrear(nombre);
            usuariosLeidos.put(i + 1, usuario);
            jugadoresLeidos.get(i).asignarUsuario(usuario);
        }
        partidoLeido.pausar();
        partido = partidoLeido;
        jugadores = jugadoresLeidos;
        modo = modoLeido;
        usuarios = usuariosLeidos;
    }

    // Prepara a los dos jugadores y empieza un partido del tipo dado ("Goles" o "Tiempo") en la cancha normal
    public void iniciarPartido(String pais1, String pais2, String tipo, int valor) {
        iniciarPartido(Arrays.asList(pais1, pais2), tipo, valor);
    }

    // Igual que el siguiente, pero en la cancha "Normal" (así la GUI no tiene que elegir cancha)
    public void iniciarPartido(List<String> paises, String tipo, int valor) {
        iniciarPartido(paises, tipo, "Normal", valor);
    }

    // Prepara un jugador por cada país (en orden: 1, 2, 3...) y empieza un partido del tipo y la cancha indicados.
    // La cancha y el partido se crean antes de cambiar el estado: si el tipo o la cancha no existen, nada cambia.
    public void iniciarPartido(List<String> paises, String tipo, String nombreCancha, int valor) {
        Cancha cancha = Cancha.crear(nombreCancha);
        List<Jugador> nuevos = new ArrayList<>();
        for (int i = 0; i < paises.size(); i++) {
            nuevos.add(prepararJugador(getJugador(i + 1), paises.get(i), i + 1));
        }
        Partido nuevo = Partido.crear(tipo, nuevos, cancha, valor);
        jugadores = nuevos;
        partido = nuevo;
        for (Aparicion a : apariciones) {
            partido.agregarAparicion(a);
        }
        partido.iniciar();
    }

    // Registra una aparición para los partidos que empiecen (y para el actual, si hay uno)
    public void agregarAparicion(Aparicion aparicion) {
        apariciones.add(aparicion);
        if (partido != null) {
            partido.agregarAparicion(aparicion);
        }
    }

    // Avanza el partido un instante; en el instante en que termina (ya repartidos 100/25) guarda una sola vez
    public void actualizar(double dt) {
        if (partido == null) {
            return;
        }
        boolean yaTerminado = partido.estaTerminado();
        partido.actualizar(dt);
        if (!yaTerminado && partido.estaTerminado()) {
            guardarUsuarios(jugadores);
        }
    }

    // Las órdenes manuales se ignoran si el jugador es máquina: así MatchPanel
    // sigue mandando las teclas del jugador 2 sin que choquen con el perfil.
    public void mover(int numero, int direccion) {
        Jugador j = getJugador(numero);
        if (j.aceptaControlManual()) {
            j.mover(direccion);
        }
    }

    // Hace saltar al jugador indicado si lo maneja el teclado
    public void saltar(int numero) {
        Jugador j = getJugador(numero);
        if (j.aceptaControlManual()) {
            j.saltar();
        }
    }

    // Hace patear al jugador indicado si lo maneja el teclado
    public void patear(int numero) {
        Jugador j = getJugador(numero);
        if (j.aceptaControlManual()) {
            j.patear(partido);
        }
    }

    // Activa el poder del jugador indicado si lo maneja el teclado
    public void activarPoder(int numero) {
        Jugador j = getJugador(numero);
        if (j.aceptaControlManual()) {
            j.activarPoder();
        }
    }

    // Pausa el partido actual
    public void pausar() {
        partido.pausar();
    }

    // Reanuda el partido actual
    public void reanudar() {
        partido.reanudar();
    }

    // Abandona el partido actual sin ganador
    public void abandonar() {
        partido.abandonar();
    }

    // Dice si no hay partido o si ya terminó
    public boolean estaTerminado() {
        return partido == null || partido.estaTerminado();
    }

    // Compra una mejora de habilidad para el usuario del jugador indicado y la guarda
    public void mejorarEstadistica(int numero, Habilidad habilidad) throws HeadSoccerException {
        Usuario usuario = getUsuario(numero);
        usuario.mejorarEstadistica(habilidad);
        guardarSi(usuario);
    }

    // Guarda el registro si alguno de los jugadores tiene un usuario registrado (invitados y máquina no)
    private void guardarUsuarios(List<Jugador> participantes) {
        for (Jugador j : participantes) {
            if (registro.contiene(j.getUsuario())) {
                registro.guardarSinInterrumpir();
                return;
            }
        }
    }

    // Guarda el registro si el usuario dado está registrado
    private void guardarSi(Usuario usuario) {
        if (registro.contiene(usuario)) {
            registro.guardarSinInterrumpir();
        }
    }

    // Devuelve el jugador número n, empezando en 1 (null antes del primer partido)
    public Jugador getJugador(int numero) {
        return numero >= 1 && numero <= jugadores.size() ? jugadores.get(numero - 1) : null;
    }

    // Devuelve todos los jugadores en orden (vacía antes del primer partido)
    public List<Jugador> getJugadores() {
        return Collections.unmodifiableList(jugadores);
    }

    public Partido getPartido() {
        return partido;
    }

    // Se reutiliza el jugador si ya es del tipo pedido por el modo; si el tipo cambió (p. ej. PvP -> PvM)
    // se crea uno nuevo con la fábrica del enum. En ambos casos recibe el usuario de su número,
    // así el progreso del humano no se pierde (la máquina ignora el usuario y usa el suyo).
    private Jugador prepararJugador(Jugador jugador, String pais, int numero) {
        Personaje personaje = Personaje.crear(pais);
        TipoJugador tipo = modo.getTipo(numero);
        PerfilMaquina perfil = modo.getPerfil(numero);
        if (jugador == null || jugador.getTipo() != tipo) {
            jugador = tipo.crear(personaje, perfil);
        } else {
            jugador.setPersonaje(personaje);
            jugador.asignarPerfil(perfil);
        }
        jugador.asignarUsuario(getUsuario(numero));
        return jugador;
    }
}
