package domain;

public class Inmovilizado extends Efecto {
    private static final long serialVersionUID = 1L;
    private double impulsoVertical;

    // Crea el efecto de inmovilizado sin impulso hacia arriba
    public Inmovilizado(double duracion) {
        this(duracion, 0);
    }

    // Crea el efecto de inmovilizado con un impulso hacia arriba
    public Inmovilizado(double duracion, double impulsoVertical) {
        super(duracion);
        this.impulsoVertical = impulsoVertical;
    }

    // Frena al jugador y, si tiene impulso, lo lanza hacia arriba
    @Override
    public void aplicar(Jugador j) {
        j.setVelocidad(0, -impulsoVertical);
    }

    // Un jugador inmovilizado no puede moverse
    @Override
    public boolean permiteMoverse() {
        return false;
    }

    // Un jugador inmovilizado no puede saltar
    @Override
    public boolean permiteSaltar() {
        return false;
    }

    // Un jugador inmovilizado no puede patear
    @Override
    public boolean permitePatear() {
        return false;
    }

    @Override
    public String getNombre() {
        return "Inmovilizado";
    }

    // Devuelve un inmovilizado nuevo con la misma duración e impulso
    @Override
    public Efecto copiar() {
        return new Inmovilizado(duracion, impulsoVertical);
    }
}
