package domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

public abstract class Jugador extends ObjetoFisico {
    private static final long serialVersionUID = 1L;
    public static final double ANCHO = 60;
    public static final double ALTO = 80;
    public static final double alcancePatada = 45;
    public static final double duracionPatada = 0.2;
    public static final double fuerzaPatadaX = 650;
    public static final double fuerzaPatadaY = -450;
    public static final double segundosAturdimiento = 3;
    public static final double frenadoSinControl = 0.05;
    // Por debajo de esta rapidez el impulso se da por terminado y el jugador vuelve a controlar su movimiento
    public static final double rapidezFinImpulso = 30;
    public static final String nombreInvitado = "Invitado";

    private Personaje personaje;
    // Dueño de los puntos y las estadísticas: el jugador solo existe durante el partido
    // transient: la partida guarda solo el nombre; al abrir, HeadSoccer lo reconecta con el registro
    private transient Usuario usuario;
    private BarraPoder barraPoder;
    private Poder poderActivo;
    private Efecto efectoActual;
    protected int vida;
    private Arco arco;
    private int direccionMovimiento;
    private double tiempoPatada;
    // Mientras dura un impulso, el movimiento del teclado no reemplaza la velocidad: se va frenando sola
    private boolean impulsado;

    // Crea un jugador con un usuario invitado interno (no se guarda en ningún registro)
    public Jugador(Personaje personaje) {
        this(personaje, crearUsuarioInterno(nombreInvitado));
    }

    // Crea un jugador con su personaje, el usuario dueño de su progreso y vida completa
    public Jugador(Personaje personaje, Usuario usuario) {
        super(0, 0, ANCHO, ALTO);
        this.personaje = personaje;
        this.usuario = usuario;
        barraPoder = new BarraPoder();
        vida = getVidaMax();
    }

    // Deja al jugador listo para empezar un partido: vida llena y barra vacía
    public void prepararPartido() {
        vida = getVidaMax();
        barraPoder.vaciar();
        poderActivo = null;
        efectoActual = null;
        direccionMovimiento = 0;
        tiempoPatada = 0;
        impulsado = false;
        setVelocidad(0, 0);
    }

    // Decide qué hacer en cada instante; un humano no hace nada porque lo maneja el teclado
    public void pensar(Escenario escenario, double dt) {
    }

    // Por defecto un jugador es humano y acepta teclas; JugadorMaquina lo cambia.
    // No es abstracto para no romper subclases existentes.
    public TipoJugador getTipo() {
        return TipoJugador.HUMANO;
    }

    // Dice si el jugador obedece al teclado; un humano sí
    public boolean aceptaControlManual() {
        return true;
    }

    // Gancho para reutilizar un jugador cambiando su perfil; un humano no lo usa.
    public void asignarPerfil(PerfilMaquina perfil) {
    }

    // Guarda hacia dónde quiere moverse el jugador (-1 izquierda, 0 quieto, 1 derecha)
    public void mover(int direccion) {
        direccionMovimiento = Integer.signum(direccion);
    }

    // Hace saltar al jugador si está en el suelo y ningún efecto se lo impide
    public void saltar() {
        if (estaApoyado() && puedeSaltar()) {
            velY = -getVelocidadSalto();
        }
    }

    // Patea en el escenario dado: le pide sus rivales y el balón
    public void patear(Escenario escenario) {
        patear(escenario.getRivales(this), escenario.getBalon());
    }

    // Da al jugador una velocidad (empujón, barrida...) que se va frenando sola en vez de borrarse en el siguiente instante
    public void impulsar(double velX, double velY) {
        setVelocidad(velX, velY);
        impulsado = true;
    }

    // Dice si el jugador sigue moviéndose por un impulso
    public boolean estaImpulsado() {
        return impulsado;
    }

    // Patea: si alcanza el balón lo lanza hacia el arco rival; si no, daña a los rivales que alcance
    public void patear(List<Jugador> rivales, Balon b) {
        if (!puedePatear() || tiempoPatada > 0) {
            return;
        }
        tiempoPatada = duracionPatada;
        if (tocaZonaDelPie(b)) {
            b.impulsar(getDireccionAtaque() * fuerzaPatadaX, fuerzaPatadaY);
        } else {
            for (Jugador rival : rivales) {
                if (tocaZonaDelPie(rival)) {
                    rival.recibirDano(getDanoPatada());
                }
            }
        }
    }

    // Al tocar algo, el jugador lo toca: un balón recibe el cabezazo; los demás objetos no reaccionan
    @Override
    public void afectar(ObjetoFisico otro) {
        otro.recibirToque(this);
    }

    // Es rival de quien lanzó el poder si defiende el otro arco (los compañeros no se golpean)
    @Override
    public boolean esRivalDe(Jugador lanzador) {
        return arco != lanzador.getArco();
    }

    // Al recibir el impacto de un poder, sufre su daño y su efecto
    @Override
    public void recibirImpacto(Poder poder, Jugador lanzador, Escenario escenario) {
        poder.impactar(lanzador, this, escenario);
    }

    // Activa el poder del personaje si la barra está llena
    public void activarPoder() {
        if (poderActivo == null && barraPoder.estaLlena() && personaje.getPoder() != null) {
            poderActivo = personaje.getPoder();
            barraPoder.vaciar();
        }
    }

    // Entrega el poder activo para usarlo y lo deja vacío
    public Poder consumirPoder() {
        Poder p = poderActivo;
        poderActivo = null;
        return p;
    }

    // Le quita vida al jugador; si llega a cero queda aturdido un rato
    @Override
    public void recibirDano(int dano) {
        if (vida == 0) {
            return;
        }
        vida = Math.max(0, vida - dano);
        if (vida == 0) {
            aplicarEfecto(new Inmovilizado(segundosAturdimiento));
        }
    }

    // Le devuelve vida al jugador sin pasar nunca de su vida máxima
    @Override
    public void curar(int cantidad) {
        vida = Math.min(getVidaMax(), vida + Math.max(0, cantidad));
    }

    // Le pone un efecto nuevo al jugador, quitando el que tuviera antes
    public void aplicarEfecto(Efecto e) {
        if (efectoActual != null) {
            efectoActual.quitar(this);
        }
        efectoActual = e;
        e.aplicar(this);
    }

    // Dice si el jugador está aturdido (sin vida)
    public boolean estaAturdido() {
        return vida == 0;
    }

    // Al recibir un gol, el jugador gana un cuarto de barra de poder
    public void recibirGol() {
        barraPoder.sumarCuarto();
    }

    // Suma puntos al usuario del jugador después de un partido
    public void sumarPuntos(int n) {
        usuario.sumarPuntos(n);
    }

    // Le pide a su usuario que compre una mejora de habilidad
    public void mejorarEstadistica(Habilidad habilidad) throws HeadSoccerException {
        usuario.mejorarEstadistica(habilidad);
    }

    // Cambia el usuario dueño del progreso; la máquina lo ignora porque usa siempre el suyo
    public void asignarUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    // Crea el usuario invitado del jugador número n (solo vive en memoria, no se guarda)
    public static Usuario crearInvitado(int numero) {
        return crearUsuarioInterno(nombreInvitado + " " + numero);
    }

    // Al leer una partida el usuario no viene en el archivo: queda uno interno hasta que HeadSoccer lo reconecte
    private void readObject(ObjectInputStream entrada) throws IOException, ClassNotFoundException {
        entrada.defaultReadObject();
        usuario = crearUsuarioInterno(nombreUsuarioInterno());
    }

    // Nombre del usuario interno que se crea al leer una partida; la máquina usa el suyo
    protected String nombreUsuarioInterno() {
        return nombreInvitado;
    }

    // Crea un usuario que solo vive en memoria; el nombre es fijo y válido, así que no puede fallar
    protected static Usuario crearUsuarioInterno(String nombre) {
        try {
            return new Usuario(nombre);
        } catch (HeadSoccerException e) {
            throw new IllegalStateException(e);
        }
    }

    // Mueve al jugador en cada instante: efectos, movimiento, gravedad, barra y patada
    @Override
    public void actualizar(double dt) {
        actualizarEfecto(dt);
        if (puedeMoverse() && !impulsado) {
            velX = direccionMovimiento * getVelocidad();
        } else {
            velX *= Math.pow(frenadoSinControl, dt);
            impulsado = impulsado && Math.abs(velX) >= rapidezFinImpulso;
        }
        velY += getGravedad() * dt;
        avanzar(dt);
        barraPoder.cargar(dt);
        tiempoPatada = Math.max(0, tiempoPatada - dt);
    }

    // Descuenta el tiempo del efecto y lo quita al terminar (si estaba aturdido recupera la vida)
    private void actualizarEfecto(double dt) {
        if (efectoActual == null) {
            return;
        }
        efectoActual.actualizar(dt);
        if (efectoActual.haTerminado()) {
            efectoActual.quitar(this);
            efectoActual = null;
            if (vida == 0) {
                vida = getVidaMax();
            }
        }
    }

    // Revisa si el objeto está al alcance del pie del jugador
    private boolean tocaZonaDelPie(ObjetoFisico o) {
        double pieX = getDireccionAtaque() > 0 ? x + ancho : x - alcancePatada;
        double pieY = y + alto / 2;
        return pieX < o.getX() + o.getAncho()
            && o.getX() < pieX + alcancePatada
            && pieY < o.getY() + o.getAlto()
            && o.getY() < pieY + alto / 2;
    }

    // Dice si el efecto actual deja moverse al jugador
    private boolean puedeMoverse() {
        return efectoActual == null || efectoActual.permiteMoverse();
    }

    // Dice si el efecto actual deja saltar al jugador
    private boolean puedeSaltar() {
        return efectoActual == null || efectoActual.permiteSaltar();
    }

    // Dice si el efecto actual deja patear al jugador
    private boolean puedePatear() {
        return efectoActual == null || efectoActual.permitePatear();
    }

    // Calcula la velocidad del jugador según su habilidad SPEED y su efecto
    public double getVelocidad() {
        double factor = efectoActual == null ? 1 : efectoActual.getFactorVelocidad();
        return getEstadisticas().valorActual(Habilidad.SPEED) * factor;
    }

    // Calcula con qué fuerza salta según su habilidad JUMP
    public double getVelocidadSalto() {
        double altura = getEstadisticas().valorActual(Habilidad.JUMP);
        return Math.sqrt(2 * getGravedad() * altura);
    }

    // Calcula el daño de la patada según su habilidad KICK
    public int getDanoPatada() {
        return (int) Math.round(getEstadisticas().valorActual(Habilidad.KICK));
    }

    // Devuelve la vida máxima según su habilidad SURVIVAL
    public int getVidaMax() {
        return (int) Math.round(getEstadisticas().valorActual(Habilidad.SURVIVAL));
    }

    public Personaje getPersonaje() { return personaje; }
    public void setPersonaje(Personaje personaje) { this.personaje = personaje; }
    public Estadisticas getEstadisticas() { return usuario.getEstadisticas(); }
    public Usuario getUsuario() { return usuario; }
    public BarraPoder getBarraPoder() { return barraPoder; }
    public Poder getPoderActivo() { return poderActivo; }
    public Efecto getEfectoActual() { return efectoActual; }
    public int getVida() { return vida; }
    public int getPuntos() { return usuario.getPuntos(); }
    // Hacia dónde ataca: lo decide el arco que defiende; sin arco (fuera de un partido) mira a la derecha
    public int getDireccionAtaque() {
        return arco == null ? 1 : arco.getDireccionAtaque();
    }

    public Arco getArco() { return arco; }

    // Pone al jugador en el lado del arco que defiende
    public void setArco(Arco arco) {
        this.arco = arco;
    }
    public boolean estaEnSuelo() { return estaApoyado(); }
    public boolean estaPateando() { return tiempoPatada > 0; }
}
