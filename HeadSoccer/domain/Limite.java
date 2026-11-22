package domain;

// Borde quieto y sólido de la cancha (pared, piso o techo): los demás objetos rebotan o se apoyan en él.
public class Limite extends ObjetoFisico {
    private static final long serialVersionUID = 1L;

    // Crea un límite en la posición y con el tamaño dados
    public Limite(double x, double y, double ancho, double alto) {
        super(x, y, ancho, alto);
    }

    // Un límite no se mueve con el tiempo
    @Override
    public void actualizar(double dt) {
    }

    // Un límite no se mueve cuando algo choca con él
    @Override
    public boolean esMovil() {
        return false;
    }
}
