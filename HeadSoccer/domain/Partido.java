package domain;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Partido implements Escenario, Serializable {
    private static final long serialVersionUID = 1L;
    public static final int puntosGanador = 100;
    public static final int puntosPerdedor = 25;
    public static final double margenInicial = 200;
    public static final double separacionCompaneros = 100;
    public static final double alturaSaqueBalon = 150;
    public static final String[] TIPOS = {"Goles", "Tiempo"};

    protected Cancha cancha;
    protected List<Jugador> jugadores;
    protected Balon balon;
    // Todos los objetos físicos: primero jugadores, balón y límites (fijos), después los objetos extra.
    protected List<ObjetoFisico> objetos;
    private int cantidadFijos;
    protected double tiempoJugado;
    // Goles de cada lado; la llave es el arco que defiende el lado que anotó.
    private Map<Arco, Integer> goles;
    private boolean pausado;
    private boolean terminado;
    private Arco ladoGanador;
    private Random azar;
    // Objetos que aparecen solos cada cierto tiempo; por defecto ninguno
    private List<Aparicion> apariciones;

    // Crea el partido del tipo indicado ("Goles" o "Tiempo") en la cancha dada; falla si el tipo no existe
    public static Partido crear(String tipo, List<Jugador> jugadores, Cancha cancha, int valor) {
        switch (tipo) {
            case "Goles":
                return new PartidoPorGoles(jugadores, cancha, valor);
            case "Tiempo":
                return new PartidoPorTiempo(jugadores, cancha, valor);
            default:
                throw new DatoInvalidoException(DatoInvalidoException.TIPO_PARTIDO_INVALIDO, tipo);
        }
    }

    // Crea un partido en la cancha normal
    public Partido(List<Jugador> jugadores) {
        this(jugadores, new Cancha());
    }

    // Crea un partido en la cancha dada con su balón; los jugadores se reparten por turnos entre
    // los lados (1.º izquierda, 2.º derecha, 3.º izquierda...), así 3 jugadores son dos contra uno
    public Partido(List<Jugador> jugadores, Cancha cancha) {
        this.jugadores = new ArrayList<>(jugadores);
        this.cancha = cancha;
        balon = new Balon(0, 0);
        objetos = new ArrayList<>(this.jugadores);
        objetos.add(balon);
        objetos.addAll(cancha.getLimites());
        cantidadFijos = objetos.size();
        for (ObjetoFisico o : objetos) {
            o.setEscenario(this);
        }
        apariciones = new ArrayList<>();
        azar = new Random();
        goles = new HashMap<>();
        List<Arco> arcos = cancha.getArcos();
        for (int i = 0; i < this.jugadores.size(); i++) {
            this.jugadores.get(i).setArco(arcos.get(i % arcos.size()));
        }
    }

    // Deja todo listo para empezar: jugadores preparados, marcador y reloj en cero y posiciones iniciales
    public void iniciar() {
        for (Jugador j : jugadores) {
            j.prepararPartido();
        }
        for (Arco arco : cancha.getArcos()) {
            goles.put(arco, 0);
        }
        tiempoJugado = 0;
        pausado = false;
        terminado = false;
        ladoGanador = null;
        for (Aparicion a : apariciones) {
            a.reiniciar();
        }
        reiniciarPosiciones();
    }

    // Avanza el partido un instante: los jugadores piensan, todo se mueve, se resuelven los choques y se revisan goles y ganador
    public void actualizar(double dt) {
        if (pausado || terminado) {
            return;
        }
        tiempoJugado += dt;
        for (Jugador j : jugadores) {
            j.pensar(this, dt);
        }
        for (ObjetoFisico o : new ArrayList<>(objetos)) {
            o.actualizar(dt);
        }
        resolverChoques();
        objetos.removeIf(ObjetoFisico::haExpirado);
        revisarGoles();
        for (Aparicion a : apariciones) {
            a.avanzar(dt, this);
        }
        if (hayGanador()) {
            terminar();
        }
    }

    // Pausa el partido
    public void pausar() {
        pausado = true;
    }

    // Quita la pausa del partido
    public void reanudar() {
        pausado = false;
    }

    // Termina el partido sin ganador
    public void abandonar() {
        terminado = true;
        ladoGanador = null;
    }

    // Agrega un objeto extra al partido (por ejemplo un balón falso) y le dice en qué escenario está
    public void agregarObjeto(ObjetoFisico o) {
        o.setEscenario(this);
        objetos.add(o);
    }

    // Registra algo que aparecerá solo cada cierto tiempo durante el partido
    public void agregarAparicion(Aparicion aparicion) {
        apariciones.add(aparicion);
    }

    // Gravedad de la cancha de este partido
    public double getGravedad() {
        return cancha.getGravedad();
    }

    // Roce de la cancha de este partido
    public double getRoce() {
        return cancha.getRoce();
    }

    // Saca un objeto extra del partido
    public void quitarObjeto(ObjetoFisico o) {
        objetos.remove(o);
    }

    // Por defecto un partido no tiene muerte súbita; PartidoPorTiempo lo sobreescribe.
    public boolean estaEnMuerteSubita() {
        return false;
    }

    // Copia con solo los balones extra, lista para dibujar sin conocer los demás objetos.
    public List<Balon> getBalonesExtra() {
        List<Balon> balones = new ArrayList<>();
        for (ObjetoFisico o : getObjetosExtra()) {
            o.recolectarBalones(balones);
        }
        return balones;
    }

    // Devuelve un número al azar entre 0 y 1
    public double getAzar() {
        return azar.nextDouble();
    }

    // Cada tipo de partido decide cuándo hay ganador
    public abstract boolean hayGanador();

    // Cada tipo de partido decide qué tiempo muestra el reloj
    public abstract double getTiempoMarcador();

    // Texto corto con la meta del partido para mostrarlo en pantalla (por ejemplo "A 3 GOLES" o "2 MIN")
    public abstract String getDescripcionObjetivo();

    // Goles que lleva el lado que defiende el arco dado
    public int getGoles(Arco lado) {
        return goles.getOrDefault(lado, 0);
    }

    // Goles que lleva el lado al que pertenece el jugador
    public int getGoles(Jugador j) {
        return getGoles(j.getArco());
    }

    // Devuelve el lado con más goles, o null si el máximo está empatado
    public Arco ladoConMasGoles() {
        Arco mejor = null;
        int maximo = -1;
        int conMaximo = 0;
        for (Arco arco : cancha.getArcos()) {
            int g = getGoles(arco);
            if (g > maximo) {
                maximo = g;
                mejor = arco;
                conMaximo = 1;
            } else if (g == maximo) {
                conMaximo++;
            }
        }
        return conMaximo == 1 ? mejor : null;
    }

    // Termina el partido, define el lado ganador y reparte los puntos a todos los jugadores
    protected void terminar() {
        terminado = true;
        ladoGanador = ladoConMasGoles();
        if (ladoGanador != null) {
            for (Jugador j : jugadores) {
                j.sumarPuntos(j.getArco() == ladoGanador ? puntosGanador : puntosPerdedor);
            }
        }
    }

    // Revisa cada par de objetos una sola vez: si se tocan, cada uno choca con el otro.
    // Se recorre una copia porque un choque puede agregar objetos (por ejemplo balones falsos).
    private void resolverChoques() {
        List<ObjetoFisico> actuales = new ArrayList<>(objetos);
        for (int i = 0; i < actuales.size(); i++) {
            for (int k = i + 1; k < actuales.size(); k++) {
                ObjetoFisico a = actuales.get(i);
                ObjetoFisico b = actuales.get(k);
                if (!a.haExpirado() && !b.haExpirado() && a.colisionaCon(b)) {
                    a.chocarCon(b);
                    b.chocarCon(a);
                }
            }
        }
    }

    // Cuánto vale un gol; un tipo de partido con otras reglas puede cambiarlo
    protected int valorDelGol() {
        return 1;
    }

    // Revisa si el balón entró en algún arco; el gol es para el lado contrario
    private void revisarGoles() {
        for (Arco arco : cancha.getArcos()) {
            if (arco.contiene(balon)) {
                anotarGol(cancha.getArcoContrario(arco));
                return;
            }
        }
    }

    // Suma el gol al lado que anotó, da barra de poder al otro lado y vuelve todo a su posición inicial
    private void anotarGol(Arco ladoQueAnota) {
        goles.put(ladoQueAnota, getGoles(ladoQueAnota) + valorDelGol());
        for (Jugador j : getRivalesDe(ladoQueAnota)) {
            j.recibirGol();
        }
        reiniciarPosiciones();
    }

    // Pone a los jugadores y al balón en su lugar de saque
    private void reiniciarPosiciones() {
        for (Arco lado : cancha.getArcos()) {
            colocarLado(lado);
        }
        balon.descargarPoder();
        balon.setPosicion(cancha.getAncho() / 2 - balon.getAncho() / 2, alturaSaqueBalon);
        balon.setVelocidad(0, 0);
        getObjetosExtra().removeIf(o -> !o.persisteTrasGol());
    }

    // Pone en el suelo a los jugadores de un lado, junto a su arco y separados entre sí hacia el centro
    private void colocarLado(Arco lado) {
        int direccion = lado.getDireccionAtaque();
        int k = 0;
        for (Jugador j : jugadores) {
            if (j.getArco() == lado) {
                double inicio = direccion > 0 ? margenInicial : cancha.getAncho() - margenInicial - j.getAncho();
                j.setPosicion(inicio + direccion * k * separacionCompaneros, 0);
                j.setVelocidad(0, 0);
                j.apoyarSobre(cancha.getPiso());
                k++;
            }
        }
    }

    // Devuelve los jugadores del otro lado del jugador dado
    public List<Jugador> getRivales(Jugador j) {
        return getRivalesDe(j.getArco());
    }

    // Devuelve los jugadores que no pertenecen al lado dado
    private List<Jugador> getRivalesDe(Arco lado) {
        List<Jugador> rivales = new ArrayList<>();
        for (Jugador j : jugadores) {
            if (j.getArco() != lado) {
                rivales.add(j);
            }
        }
        return rivales;
    }

    // Devuelve los jugadores del lado ganador (vacía si hubo empate o se abandonó)
    public List<Jugador> getGanadores() {
        List<Jugador> ganadores = new ArrayList<>();
        for (Jugador j : jugadores) {
            if (ladoGanador != null && j.getArco() == ladoGanador) {
                ganadores.add(j);
            }
        }
        return ganadores;
    }

    public Cancha getCancha() { return cancha; }
    public List<Aparicion> getApariciones() { return Collections.unmodifiableList(apariciones); }
    public List<Jugador> getJugadores() { return Collections.unmodifiableList(jugadores); }
    public Balon getBalon() { return balon; }
    public List<ObjetoFisico> getObjetos() { return Collections.unmodifiableList(objetos); }

    // Devuelve los objetos extra (los que se agregaron durante el partido, como los balones falsos)
    public List<ObjetoFisico> getObjetosExtra() {
        return objetos.subList(cantidadFijos, objetos.size());
    }
    public double getTiempoJugado() { return tiempoJugado; }
    public boolean estaPausado() { return pausado; }
    public boolean estaTerminado() { return terminado; }
    public Arco getLadoGanador() { return ladoGanador; }
}
