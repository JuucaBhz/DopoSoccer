package domain;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;

// Log para programadores: el usuario ve un mensaje claro y aquí queda el detalle técnico.
public class LogErrores {
    public static final String ARCHIVO = "errores.log";

    private static String rutaActual = ARCHIVO;

    // Permite redirigir el log (por ejemplo a un archivo temporal en las pruebas); null restaura ARCHIVO.
    public static void usarArchivo(String ruta) {
        rutaActual = ruta == null ? ARCHIVO : ruta;
    }

    // Registrar un error nunca debe causar otro: cualquier fallo del propio log se descarta.
    public static void registrar(Throwable error) {
        try (PrintWriter salida = new PrintWriter(new FileWriter(rutaActual, true))) {
            salida.println(LocalDateTime.now() + " - " + error);
            if (error != null) {
                error.printStackTrace(salida);
            }
            salida.println();
        } catch (Exception e) {
        }
    }
}
