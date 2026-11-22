package domain;

public class Empuje extends Efecto {
    private static final long serialVersionUID = 1L;

    private double fuerza;

    // Crea el efecto de empuje con su duración y su fuerza
    public Empuje(double duracion, double fuerza){
        super(duracion);
        this.fuerza = fuerza;
    }

    // Lanza al jugador hacia atrás y un poco hacia arriba
    @Override
    public void aplicar(Jugador j) {
        double direccionEmpuje = -j.getDireccionAtaque();
        j.impulsar(direccionEmpuje * fuerza, -300);
    }
    
    // Mientras dura el empuje el jugador no puede moverse
    @Override 
    public boolean  permiteMoverse(){
        return false;
    }

    @Override
    public String getNombre() {
        return "Empuje";
    }

    // Devuelve un empuje nuevo con la misma duración y fuerza
    @Override
    public Efecto copiar() {
        return new Empuje (duracion, fuerza);
    }
}
