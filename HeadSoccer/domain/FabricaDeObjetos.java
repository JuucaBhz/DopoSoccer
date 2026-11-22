package domain;

import java.io.Serializable;

// Sabe construir un objeto nuevo para el escenario; es Serializable para que las partidas guardadas lo conserven.
@FunctionalInterface
public interface FabricaDeObjetos extends Serializable {
    // Crea el objeto que va a aparecer en el escenario dado
    ObjetoFisico crear(Escenario escenario);
}
