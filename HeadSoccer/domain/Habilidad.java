package domain;

// Cada habilidad guarda el valor con el que empieza el jugador en nivel 1:
// velocidad (SPEED), altura de salto (JUMP), daño de patada (KICK) y vida máxima (SURVIVAL).
public enum Habilidad {
    SPEED(320), JUMP(150), KICK(10), SURVIVAL(100);

    private final int valorBase;

    // Guarda el valor base de la habilidad
    Habilidad(int valorBase) {
        this.valorBase = valorBase;
    }

    // Devuelve el valor con el que empieza la habilidad en nivel 1
    public int getValorBase() {
        return valorBase;
    }
}
