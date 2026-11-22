package domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Guarda en un archivo los usuarios con nombre para que su progreso dure entre sesiones.
// Al buscar se ignoran mayúsculas y espacios de los extremos: "Ana" y " ana " son el mismo usuario
// (se conserva el nombre tal como se escribió la primera vez).
public class RegistroUsuarios {
    public static final String ARCHIVO = "usuarios.dat";

    private Map<String, Usuario> usuarios = new HashMap<>();
    private String ruta;

    // Crea un registro que usa el archivo por defecto (usuarios.dat)
    public RegistroUsuarios() {
        this(ARCHIVO);
    }

    // Crea un registro que usa otro archivo (por ejemplo uno temporal en las pruebas)
    public RegistroUsuarios(String ruta) {
        this.ruta = ruta;
    }

    // Lee los usuarios del archivo; si el archivo no existe arranca vacío
    public void cargar() throws HeadSoccerException {
        usuarios.clear();
        if (!new File(ruta).exists()) {
            return;
        }
        try (FileInputStream archivo = new FileInputStream(ruta);
             ObjectInputStream entrada = new ObjectInputStream(archivo)) {
            Map<?, ?> leidos = Map.class.cast(entrada.readObject());
            for (Object valor : leidos.values()) {
                Usuario u = Usuario.class.cast(valor);
                usuarios.put(clave(u.getNombre()), u);
            }
        } catch (Exception e) {
            usuarios.clear();
            throw new HeadSoccerException(HeadSoccerException.ERROR_ARCHIVO_USUARIOS, e);
        }
    }

    // Devuelve el usuario con ese nombre; si no existe lo crea, lo agrega y lo guarda de inmediato
    public Usuario obtenerOCrear(String nombre) throws HeadSoccerException {
        Usuario nuevo = new Usuario(nombre);
        Usuario existente = usuarios.get(clave(nombre));
        if (existente != null) {
            return existente;
        }
        usuarios.put(clave(nombre), nuevo);
        guardarSinInterrumpir();
        return nuevo;
    }

    // Escribe todos los usuarios en el archivo
    public void guardar() throws HeadSoccerException {
        try (FileOutputStream archivo = new FileOutputStream(ruta);
             ObjectOutputStream salida = new ObjectOutputStream(archivo)) {
            salida.writeObject(new HashMap<>(usuarios));
        } catch (Exception e) {
            throw new HeadSoccerException(HeadSoccerException.ERROR_ARCHIVO_USUARIOS, e);
        }
    }

    // Guarda y, si falla, sigue: el error ya quedó en el log y el juego no se detiene
    public void guardarSinInterrumpir() {
        try {
            guardar();
        } catch (HeadSoccerException e) {
        }
    }

    // Dice si este usuario está en el registro (los invitados y la máquina no lo están)
    public boolean contiene(Usuario usuario) {
        return usuario != null && usuarios.get(clave(usuario.getNombre())) == usuario;
    }

    // Devuelve el usuario con ese nombre, o null si no existe
    public Usuario buscar(String nombre) {
        return nombre == null ? null : usuarios.get(clave(nombre));
    }

    // Convierte el nombre en la llave de búsqueda: sin espacios en los extremos y en minúsculas
    private static String clave(String nombre) {
        return nombre.trim().toLowerCase(Locale.ROOT);
    }
}
