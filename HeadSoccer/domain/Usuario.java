package domain;

import java.io.Serializable;

// Lo que dura entre sesiones: el nombre, los puntos ganados y las mejoras compradas.
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private int puntos;
    private Estadisticas estadisticas;

    // Crea un usuario nuevo sin puntos y con todas las habilidades en nivel 1; el nombre no puede estar vacío
    public Usuario(String nombre) throws HeadSoccerException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new HeadSoccerException(HeadSoccerException.NOMBRE_INVALIDO);
        }
        this.nombre = nombre.trim();
        estadisticas = new Estadisticas();
    }

    // Suma los puntos ganados en un partido
    public void sumarPuntos(int n) {
        puntos += n;
    }

    // Compra una mejora de habilidad si le alcanzan los puntos y los descuenta
    public void mejorarEstadistica(Habilidad habilidad) throws HeadSoccerException {
        int precio = estadisticas.precioSiguienteMejora();
        if (puntos < precio) {
            throw new HeadSoccerException(HeadSoccerException.PUNTOS_INSUFICIENTES);
        }
        estadisticas.mejorar(habilidad);
        puntos -= precio;
    }

    public String getNombre() { return nombre; }
    public int getPuntos() { return puntos; }
    public Estadisticas getEstadisticas() { return estadisticas; }
}
