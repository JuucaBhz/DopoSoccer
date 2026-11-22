package domain;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

public class Estadisticas implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAXIMO = 10;
    public static final double mejoraPorNivel = 0.10;
    private Map<Habilidad, Integer> niveles = new EnumMap<>(Habilidad.class);
    private int mejorasCompradas = 0;

    // Crea las estadísticas con todas las habilidades en nivel 1
    public Estadisticas() {
        for (Habilidad h : Habilidad.values()) {
            niveles.put(h, 1);
        }
    }

    // Cuánto se multiplica el valor base: +10% por cada nivel por encima del 1
    public double multiplicador(Habilidad h) {
        return 1 + mejoraPorNivel * (getNivel(h) - 1);
    }

    // Valor actual de la habilidad: su valor base aumentado según el nivel
    public double valorActual(Habilidad h) {
        return h.getValorBase() * multiplicador(h);
    }

    // Precio de la próxima mejora: empieza en 500 y se duplica con cada compra
    public int precioSiguienteMejora(){
        return (int) (500 * Math.pow(2, mejorasCompradas));
    }

    // Devuelve el nivel actual de la habilidad pedida
    public int getNivel(Habilidad h) {
        return niveles.get(h);
    }

    // Sube un nivel la habilidad; falla si ya está en el máximo
    public void mejorar(Habilidad h) throws HeadSoccerException{
        if (getNivel(h)>= MAXIMO){
            throw new HeadSoccerException(HeadSoccerException.MEJORA_MAXIMA);
        }
        niveles.put(h, getNivel(h) + 1);
        mejorasCompradas++;
    }

}
