package domain;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Guarda quién controla a cada jugador (y con qué perfil si es máquina), en orden de jugador.
// Por defecto son dos humanos, que es el comportamiento original del juego.
public class ModoJuego implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<TipoJugador> tipos;
    private List<PerfilMaquina> perfiles;

    // Crea el modo por defecto: dos jugadores humanos
    public ModoJuego() {
        tipos = new ArrayList<>(Arrays.asList(TipoJugador.HUMANO, TipoJugador.HUMANO));
        perfiles = new ArrayList<>();
        perfiles.add(null);
        perfiles.add(null);
    }

    // Valida todo antes de asignar, para que un modo inválido no deje el objeto a medias.
    // Cada posición de las listas es un jugador; los humanos llevan perfil null.
    public void seleccionarModo(List<TipoJugador> tipos, List<PerfilMaquina> perfiles) {
        if (tipos.size() != perfiles.size()) {
            throw new DatoInvalidoException(DatoInvalidoException.MODO_INVALIDO);
        }
        for (int i = 0; i < tipos.size(); i++) {
            if (tipos.get(i).necesitaPerfil() && perfiles.get(i) == null) {
                throw new DatoInvalidoException(DatoInvalidoException.MODO_INVALIDO);
            }
        }
        this.tipos = new ArrayList<>(tipos);
        this.perfiles = new ArrayList<>(perfiles);
    }

    // Tipo del jugador número n (empieza en 1); si el modo no lo menciona, es humano
    public TipoJugador getTipo(int numero) {
        return numero <= tipos.size() ? tipos.get(numero - 1) : TipoJugador.HUMANO;
    }

    // Perfil de máquina del jugador número n (empieza en 1); null si es humano o no está en el modo
    public PerfilMaquina getPerfil(int numero) {
        return numero <= perfiles.size() ? perfiles.get(numero - 1) : null;
    }

    public List<TipoJugador> getTipos() { return Collections.unmodifiableList(tipos); }
    public List<PerfilMaquina> getPerfiles() { return Collections.unmodifiableList(perfiles); }
}
