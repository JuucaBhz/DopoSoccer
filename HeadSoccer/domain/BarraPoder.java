package domain;

import java.io.Serializable;

public class BarraPoder implements Serializable {
    private static final long serialVersionUID = 1L;
    private double nivel = 0;
    public static final int segundosCarga = 10;

    // Llena la barra poco a poco con el tiempo, sin pasarse del máximo
    public void cargar(double dt) {
        nivel += dt / segundosCarga;
        nivel = Math.min(nivel, 1);
    }

    // Suma un cuarto de barra de golpe (pasa al recibir un gol)
    public void sumarCuarto(){
        nivel += 0.25;
        nivel = Math.min(nivel, 1);
    }

    // Dice si la barra está completa y el poder se puede usar
    public boolean estaLlena(){
        return nivel >= 1;
    }

    // Deja la barra en cero
    public void vaciar(){
        nivel = 0;
    }

    public double getNivel(){
        return nivel;
    }
}