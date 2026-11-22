package domain;

import java.io.Serializable;

// Hace aparecer un objeto en el partido cada cierto tiempo (por ejemplo un obstáculo o un botiquín).
public class Aparicion implements Serializable {
    private static final long serialVersionUID = 1L;

    private double intervalo;
    private FabricaDeObjetos fabrica;
    private double acumulado;

    // Crea una aparición que cada 'intervalo' segundos agrega el objeto que construye la fábrica
    public Aparicion(double intervalo, FabricaDeObjetos fabrica) {
        if (intervalo <= 0 || fabrica == null) {
            throw new IllegalArgumentException("La aparición necesita un intervalo positivo y una fábrica");
        }
        this.intervalo = intervalo;
        this.fabrica = fabrica;
    }

    // Avanza el reloj y agrega al escenario un objeto por cada intervalo completo que pasó
    public void avanzar(double dt, Escenario escenario) {
        acumulado += dt;
        while (acumulado >= intervalo) {
            acumulado -= intervalo;
            ObjetoFisico nuevo = fabrica.crear(escenario);
            if (nuevo != null) {
                escenario.agregarObjeto(nuevo);
            }
        }
    }

    // Vuelve el reloj a cero (al empezar un partido)
    public void reiniciar() {
        acumulado = 0;
    }

    public double getIntervalo() { return intervalo; }
}
