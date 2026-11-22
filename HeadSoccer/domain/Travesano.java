package domain;

// Barra de arriba de un arco: es sólida, el balón rebota en ella. La zona del arco no es sólida.
public class Travesano extends Limite {
    private static final long serialVersionUID = 1L;

    // Crea el travesaño en lo alto del arco dado
    public Travesano(Arco arco) {
        super(arco.getX(), arco.getYTravesano(), arco.getProfundidad(), Arco.grosorTravesano);
    }
}
