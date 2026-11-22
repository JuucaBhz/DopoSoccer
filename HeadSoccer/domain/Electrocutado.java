package domain;

public class Electrocutado extends Efecto {
    private static final long serialVersionUID = 1L;
    // Crea el efecto de electrocutado que dura los segundos indicados
    public Electrocutado(double duracion){
        super(duracion);
    }

    // Un jugador electrocutado no puede saltar
    @Override
    public boolean permiteSaltar(){
        return false;
    }

    // Un jugador electrocutado se mueve a la mitad de su velocidad
    @Override
    public double getFactorVelocidad(){
        return 0.5;
    }

    @Override
    public String getNombre() {
        return "Electrocutado";
    }

    // Devuelve un electrocutado nuevo con la misma duración
    @Override
    public Efecto copiar(){
        return new Electrocutado(duracion);
    }
}
