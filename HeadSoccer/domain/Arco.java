package domain;

import java.io.Serializable;

public class Arco implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final double PROFUNDIDAD = 80;
    public static final double ALTO = 200;
    public static final double grosorTravesano = 8;

    private double x;
    private double suelo;
    private int direccionAtaque;

    // Crea un arco en la posición x, apoyado en el suelo; quienes lo defienden atacan hacia direccionAtaque (1 derecha, -1 izquierda)
    public Arco(double x, double suelo, int direccionAtaque) {
        this.x = x;
        this.suelo = suelo;
        this.direccionAtaque = Integer.signum(direccionAtaque);
    }

    // Revisa si el balón entró completo en el arco
    public boolean contiene(Balon b) {
        return b.getX() >= x
            && b.getX() + b.getAncho() <= x + PROFUNDIDAD
            && b.getY() >= getYTravesano()
            && b.getBorde() <= suelo;
    }

    // Devuelve la altura donde está el travesaño del arco
    public double getYTravesano() {
        return suelo - ALTO;
    }

    public double getX() { return x; }
    public int getDireccionAtaque() { return direccionAtaque; }
    public double getProfundidad() { return PROFUNDIDAD; }
    public double getAlto() { return ALTO; }
}
