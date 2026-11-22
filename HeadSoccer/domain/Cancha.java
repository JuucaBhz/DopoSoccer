package domain;

import java.io.Serializable;

import java.util.Arrays;
import java.util.List;

public class Cancha implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final double ANCHO = 1000;
    public static final double ALTO = 600;
    // Grosor de paredes, piso y techo: muy gruesos para que nada los atraviese de un salto.
    public static final double grosorLimite = 1000;
    public static final double gravedadNormal = 1200;
    // Cuánta velocidad conserva el balón cada vez que rueda o bota sobre algo
    public static final double roceNormal = 0.98;
    public static final String[] NOMBRES = {"Normal"};

    private double ancho;
    private double alto;
    private double roce;
    private double gravedad;
    private Arco arcoIzquierdo;
    private Arco arcoDerecho;
    private Limite piso;
    private List<Limite> limites;

    // Crea la cancha con ese nombre; por ahora solo existe "Normal" y cualquier otro nombre falla
    public static Cancha crear(String nombre) {
        switch (nombre) {
            case "Normal":
                return new Cancha();
            default:
                throw new DatoInvalidoException(DatoInvalidoException.CANCHA_INVALIDA, nombre);
        }
    }

    // Crea la cancha con el tamaño estándar del juego
    public Cancha() {
        this(ANCHO, ALTO);
    }

    // Crea una cancha del tamaño dado con el roce y la gravedad normales
    public Cancha(double ancho, double alto) {
        this(ancho, alto, roceNormal, gravedadNormal);
    }

    // Crea una cancha del tamaño, roce y gravedad dados, con un arco en cada lado, paredes, piso, techo y travesaños
    public Cancha(double ancho, double alto, double roce, double gravedad) {
        this.ancho = ancho;
        this.alto = alto;
        this.roce = roce;
        this.gravedad = gravedad;
        arcoIzquierdo = new Arco(0, alto, 1);
        arcoDerecho = new Arco(ancho - Arco.PROFUNDIDAD, alto, -1);
        double g = grosorLimite;
        piso = new Limite(-g, alto, ancho + 2 * g, g);
        limites = Arrays.asList(
            new Limite(-g, -g, g, alto + 2 * g),
            new Limite(ancho, -g, g, alto + 2 * g),
            piso,
            new Limite(-g, -g, ancho + 2 * g, g),
            new Travesano(arcoIzquierdo),
            new Travesano(arcoDerecho));
    }

    public double getAncho() { return ancho; }
    public double getAlto() { return alto; }
    public double getSuelo() { return alto; }
    public Arco getArcoIzquierdo() { return arcoIzquierdo; }
    public Arco getArcoDerecho() { return arcoDerecho; }
    public Limite getPiso() { return piso; }
    public double getRoce() { return roce; }
    public double getGravedad() { return gravedad; }

    // Devuelve los límites sólidos de la cancha: paredes, piso, techo y los dos travesaños
    public List<Limite> getLimites() {
        return limites;
    }

    // Devuelve los arcos de la cancha: cada uno es un lado al que pertenecen los jugadores
    public List<Arco> getArcos() {
        return Arrays.asList(arcoIzquierdo, arcoDerecho);
    }

    // Devuelve el arco del otro lado de la cancha
    public Arco getArcoContrario(Arco arco) {
        return arco == arcoIzquierdo ? arcoDerecho : arcoIzquierdo;
    }
}
