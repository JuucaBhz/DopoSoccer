package domain;

import java.io.Serializable;

public class Personaje implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String[] PAISES = {"Corea del Sur", "Camerún", "Nigeria", "Estados Unidos"};

    private String pais;
    private Poder poder;

    // Crea un personaje con su país y su poder especial
    public Personaje(String pais, Poder poder) {
        this.pais = pais;
        this.poder = poder;
    }

    // Crea el personaje de un país con el poder que le corresponde; falla si el país no existe
    public static Personaje crear(String pais) {
        switch (pais) {
            case "Corea del Sur":
                return new Personaje(pais, new Poder("AuraAzul", 40, new Empuje(1.5, 700)));
            case "Camerún":
                return new Personaje(pais, new Poder("Rayo", 60, new Electrocutado(3)));
            case "Nigeria":
                return new Personaje(pais, new TiroRemolino("Tornado", 40, new Inmovilizado(3, 700)));
            case "Estados Unidos":
                return new Personaje(pais, new TiroIlusion("Ilusión", 30, new Confusion(2.5), 2.5));
            default:
                throw new DatoInvalidoException(DatoInvalidoException.PAIS_INVALIDO, pais);
        }
    }

    public String getPais() { return pais; }
    public Poder getPoder() { return poder; }
}
