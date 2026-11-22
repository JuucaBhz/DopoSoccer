package domain;

public class Confusion extends Efecto {
    private static final long serialVersionUID = 1L;
    // Crea el efecto de confusión que dura los segundos indicados
    public Confusion(double duracion) {
        super(duracion);
    }

    // Hace que los objetos extra de la cancha (los balones falsos) duren lo mismo que la confusión
    @Override
    public void afectarEscenario(Escenario escenario) {
        for (ObjetoFisico o : escenario.getObjetosExtra()) {
            o.prolongar(duracion);
        }
    }

    @Override
    public String getNombre() {
        return "Confusion";
    }

    // Devuelve una confusión nueva con la misma duración
    @Override
    public Efecto copiar() {
        return new Confusion(duracion);
    }
}
