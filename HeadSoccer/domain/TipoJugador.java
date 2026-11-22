package domain;

// Cada tipo sabe construir su jugador: así HeadSoccer no necesita condicionales por tipo
// y un tipo nuevo solo exige agregar una constante aquí.
public enum TipoJugador {
    HUMANO {
        // Crea un jugador humano con el personaje elegido (el perfil no se usa)
        @Override
        public Jugador crear(Personaje personaje, PerfilMaquina perfil) {
            return new JugadorHumano(personaje);
        }

        // Un humano se maneja con el teclado y no necesita perfil
        @Override
        public boolean necesitaPerfil() {
            return false;
        }
    },
    MAQUINA {
        // Crea un jugador máquina que se maneja con el perfil indicado
        @Override
        public Jugador crear(Personaje personaje, PerfilMaquina perfil) {
            return new JugadorMaquina(personaje, perfil);
        }

        // Una máquina no puede jugar sin un perfil que le diga qué hacer
        @Override
        public boolean necesitaPerfil() {
            return true;
        }
    };

    // Cada tipo de jugador crea su propio objeto Jugador
    public abstract Jugador crear(Personaje personaje, PerfilMaquina perfil);

    // Dice si este tipo de jugador necesita un perfil de máquina para jugar
    public abstract boolean necesitaPerfil();
}
