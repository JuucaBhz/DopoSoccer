package domain;

public class JugadorMaquina extends Jugador {
    private static final long serialVersionUID = 1L;
    public static final String nombreMaquina = "Máquina";
    private PerfilMaquina perfilMaquina;

    // Crea un jugador que maneja la computadora con el perfil indicado
    public JugadorMaquina(Personaje personaje, PerfilMaquina perfilMaquina) {
        super(personaje, crearUsuarioInterno(nombreMaquina));
        this.perfilMaquina = perfilMaquina;
    }

    // Le pide al perfil que decida qué hace la máquina en este momento
    @Override
    public void pensar(Escenario escenario, double dt) {
        perfilMaquina.decidir(this, escenario, dt);
    }

    // Deja al jugador listo para un partido nuevo y reinicia su perfil
    @Override
    public void prepararPartido() {
        super.prepararPartido();
        perfilMaquina.reiniciar();
    }

    @Override
    public TipoJugador getTipo() {
        return TipoJugador.MAQUINA;
    }

    // La máquina solo obedece a su perfil, no al teclado.
    @Override
    public boolean aceptaControlManual() {
        return false;
    }

    // Cambia el perfil con el que juega la máquina
    @Override
    public void asignarPerfil(PerfilMaquina perfil) {
        perfilMaquina = perfil;
    }

    // La máquina no hace login: conserva su usuario interno, que no se guarda
    @Override
    public void asignarUsuario(Usuario usuario) {
    }

    // Al leer una partida, la máquina vuelve a tener su usuario interno "Máquina"
    @Override
    protected String nombreUsuarioInterno() {
        return nombreMaquina;
    }

    public PerfilMaquina getPerfilMaquina() { return perfilMaquina; }
}
