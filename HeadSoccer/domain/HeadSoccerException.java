package domain;

public class HeadSoccerException extends Exception {
    private static final long serialVersionUID = 1L;


    public static final String MEJORA_MAXIMA = "La habilidad esta mejorada al maximo.";
    public static final String PUNTOS_INSUFICIENTES = "No tiene puntos suficientes para esta mejora";
    public static final String PERFIL_NO_DISPONIBLE = "Este perfil aún no está disponible";
    public static final String SIN_PARTIDO_EN_CURSO = "No hay un partido en curso para guardar.";
    public static final String ERROR_GUARDAR_PARTIDA = "No se pudo guardar la partida en ese archivo.";
    public static final String ERROR_ABRIR_PARTIDA = "No se pudo abrir la partida: el archivo no existe, está dañado o es de otra versión.";
    public static final String NOMBRE_INVALIDO = "El nombre de usuario no puede estar vacío.";
    public static final String USUARIO_EN_USO = "Ese usuario ya lo está usando el otro jugador.";
    public static final String ERROR_ARCHIVO_USUARIOS = "No se pudo leer o guardar el archivo de usuarios.";

    // Toda excepción del juego se registra al crearse: la traza queda con el punto donde se lanza
    // y ningún sitio tiene que acordarse de llamar al log.
    public HeadSoccerException(String message){
        super(message);
        LogErrores.registrar(this);
    }

    // Conserva la causa original (p. ej. la IOException) para el log de programadores,
    // sin mostrársela al usuario.
    public HeadSoccerException(String message, Throwable causa){
        super(message, causa);
        LogErrores.registrar(this);
    }
    
}
