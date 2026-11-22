package domain;

// Para argumentos inválidos (país, perfil o modo que no existen): son errores de quien llama,
// no fallos esperables como un archivo dañado, por eso es unchecked y no obliga a poner throws
// (HeadSoccerException, checked, queda para los fallos que sí se esperan y se muestran al usuario).
public class DatoInvalidoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public static final String PAIS_INVALIDO = "El país indicado no existe";
    public static final String PERFIL_INVALIDO = "El perfil de máquina indicado no existe";
    public static final String TIPO_PARTIDO_INVALIDO = "El tipo de partido indicado no existe";
    public static final String CANCHA_INVALIDA = "La cancha indicada no existe";
    public static final String MODO_INVALIDO = "El modo de juego no es válido: un jugador máquina necesita un perfil";

    // Crea el error con su mensaje y lo anota en el log
    public DatoInvalidoException(String mensaje) {
        super(mensaje);
        LogErrores.registrar(this);
    }

    // Agrega el valor recibido para que el mensaje diga exactamente qué estuvo mal.
    public DatoInvalidoException(String mensaje, String valor) {
        super(mensaje + ": " + valor);
        LogErrores.registrar(this);
    }

    // Crea el error guardando la causa original y lo anota en el log
    public DatoInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        LogErrores.registrar(this);
    }
}
