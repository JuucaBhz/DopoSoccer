package domain;

public class BalonFalso extends Balon {
    private static final long serialVersionUID = 1L;
    private double tiempoVida;

    // Crea un balón falso en la posición dada que desaparece después de cierto tiempo
    public BalonFalso(double x, double y, double tiempoVida) {
        super(x, y);
        this.tiempoVida = tiempoVida;
        if (tiempoVida <= 0) {
            desaparecer();
        }
    }

    // Mueve el balón como uno normal y le va restando tiempo de vida; al acabarse, desaparece
    @Override
    public void actualizar(double dt) {
        super.actualizar(dt);
        tiempoVida -= dt;
        if (tiempoVida <= 0) {
            desaparecer();
        }
    }

    // Alarga la vida del balón falso si la nueva duración es mayor
    @Override
    public void prolongar(double duracion) {
        tiempoVida = Math.max(tiempoVida, duracion);
    }

    // Se dibuja con la apariencia propia del balón falso (hoy igual que un balón)
    @Override
    public String getApariencia() {
        return "balonFalso";
    }

    // Un balón falso es una ilusión: al disiparlas, desaparece
    @Override
    public void disiparIlusion() {
        desaparecer();
    }

    // Un balón falso es solo una ilusión: aunque lleve poder, no golpea a nadie
    @Override
    public void afectar(ObjetoFisico otro) {
    }

    // Un jugador puede cabecear un balón falso, pero no lanzar su poder con él
    @Override
    protected void usarPoderDe(Jugador jugador) {
    }
}
