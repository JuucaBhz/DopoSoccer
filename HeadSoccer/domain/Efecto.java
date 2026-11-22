package domain;

import java.io.Serializable;

public abstract class Efecto implements Serializable {
    private static final long serialVersionUID = 1L;
    protected double duracion;
    protected double tiempoRestante;

    // Crea un efecto que dura los segundos indicados
    public Efecto(double duracion) {
        this.duracion = duracion;
        this.tiempoRestante = duracion;
    }

    // Lo que pasa al jugador cuando recibe el efecto; por defecto nada
    public void aplicar(Jugador j) {
    }

    // Lo que pasa al jugador cuando se le quita el efecto; por defecto nada
    public void quitar(Jugador j) {
    }

    // Cambios que el efecto hace en el escenario; por defecto ninguno
    public void afectarEscenario(Escenario escenario) {
    }

    // Descuenta el tiempo que le queda al efecto
    public void actualizar(double dt) {
        tiempoRestante = Math.max(0, tiempoRestante - dt);
    }

    // Dice si el efecto ya se acabó
    public boolean haTerminado() {
        return tiempoRestante <= 0;
    }

    // Dice si el jugador puede moverse con este efecto; por defecto sí
    public boolean permiteMoverse() {
        return true;
    }

    // Dice si el jugador puede saltar con este efecto; por defecto sí
    public boolean permiteSaltar() {
        return true;
    }

    // Dice si el jugador puede patear con este efecto; por defecto sí
    public boolean permitePatear() {
        return true;
    }

    // Cuánto cambia la velocidad del jugador; por defecto no la cambia
    public double getFactorVelocidad() {
        return 1;
    }

    public double getTiempoRestante() {
        return tiempoRestante;
    }

    // Cada efecto sabe crear una copia nueva de sí mismo
    public abstract Efecto copiar();

    // Nombre para mostrar en la interfaz; cada efecto declara el suyo en vez de usar su clase.
    public abstract String getNombre();
}
