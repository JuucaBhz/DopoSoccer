package presentation;

import domain.LogErrores;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class Imagenes {
    private static final String[] EXTENSIONES = {"png", "jpeg", "jpg"};
    // Margen para el magenta #FF00FF: el JPEG y el suavizado de bordes lo ensucian un poco.
    private static final int magentaSeguro = 110;
    private static final int magentaBorde = 60;

    private static final Map<String, BufferedImage> cache = new HashMap<>();
    private static final Map<BufferedImage, BufferedImage> escaladas = new HashMap<>();

    public static BufferedImage cargar(String nombre) {
        return obtener(nombre, false);
    }

    // Para ilustraciones y marcos con fondo magenta liso: ese color se vuelve transparente.
    public static BufferedImage cargarSinMagenta(String nombre) {
        return obtener(nombre, true);
    }

    public static BufferedImage cabeza(String pais) {
        return cargarSinMagenta("personajes/" + nombreArchivo(pais) + ".png");
    }

    public static String nombreArchivo(String pais) {
        String sinTildes = Normalizer.normalize(pais, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        StringBuilder nombre = new StringBuilder();
        for (String palabra : sinTildes.split(" ")) {
            if (!palabra.isEmpty()) {
                nombre.append(Character.toUpperCase(palabra.charAt(0))).append(palabra.substring(1).toLowerCase());
            }
        }
        return nombre.toString();
    }

    // Devuelve la imagen a ese tamaño exacto. Se recuerda la última versión de cada imagen,
    // así repintar a 60 fps no vuelve a escalar mientras el tamaño no cambie.
    public static BufferedImage escalada(BufferedImage origen, int ancho, int alto) {
        ancho = Math.max(1, ancho);
        alto = Math.max(1, alto);
        BufferedImage previa = escaladas.get(origen);
        if (previa != null && previa.getWidth() == ancho && previa.getHeight() == alto) {
            return previa;
        }
        BufferedImage actual = origen;
        // Reducir a la mitad por pasos conserva la nitidez mejor que un solo salto.
        while (actual.getWidth() / 2 >= ancho && actual.getHeight() / 2 >= alto) {
            actual = redibujar(actual, actual.getWidth() / 2, actual.getHeight() / 2);
        }
        BufferedImage resultado = redibujar(actual, ancho, alto);
        escaladas.put(origen, resultado);
        return resultado;
    }

    private static BufferedImage redibujar(BufferedImage origen, int ancho, int alto) {
        BufferedImage destino = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = destino.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(origen, 0, 0, ancho, alto, null);
        g.dispose();
        return destino;
    }

    // Modo "cover": la imagen cubre todo el espacio, centrada y sin deformarse; lo que sobra se recorta.
    public static void dibujarCubriendo(Graphics2D g, BufferedImage imagen, int ancho, int alto) {
        double e = Math.max((double) ancho / imagen.getWidth(), (double) alto / imagen.getHeight());
        int w = (int) Math.ceil(imagen.getWidth() * e);
        int h = (int) Math.ceil(imagen.getHeight() * e);
        g.drawImage(escalada(imagen, w, h), (ancho - w) / 2, (alto - h) / 2, null);
    }

    private static BufferedImage obtener(String nombre, boolean sinMagenta) {
        String clave = (sinMagenta ? "magenta:" : "") + nombre;
        if (cache.containsKey(clave)) {
            return cache.get(clave);
        }
        BufferedImage imagen = leer(nombre);
        if (imagen != null && sinMagenta) {
            imagen = quitarMagenta(imagen);
        }
        cache.put(clave, imagen);
        return imagen;
    }

    // Acepta el nombre con cualquier extensión: prueba .png, .jpeg y .jpg. Si falta o no se
    // puede leer, devuelve null (cada pantalla tiene su aspecto de reserva) y lo anota en el log.
    private static BufferedImage leer(String nombre) {
        String base = nombre.contains(".") ? nombre.substring(0, nombre.lastIndexOf('.')) : nombre;
        try {
            for (String extension : EXTENSIONES) {
                String ruta = base + "." + extension;
                URL url = Imagenes.class.getResource("/images/" + ruta);
                BufferedImage imagen = null;
                if (url != null) {
                    imagen = ImageIO.read(url);
                } else {
                    File archivo = new File("images/" + ruta);
                    if (archivo.exists()) {
                        imagen = ImageIO.read(archivo);
                    }
                }
                if (imagen != null) {
                    return imagen;
                }
            }
            LogErrores.registrar(new IOException("No se encontró la imagen: images/" + base + ".(png|jpeg|jpg)"));
        } catch (IOException e) {
            LogErrores.registrar(new IOException("No se pudo leer la imagen: images/" + base, e));
        }
        return null;
    }

    // Transparencia total para el magenta y gradual en el borde suavizado, con el tono rosado quitado.
    private static BufferedImage quitarMagenta(BufferedImage origen) {
        int ancho = origen.getWidth();
        int alto = origen.getHeight();
        BufferedImage destino = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                destino.setRGB(x, y, sinMagenta(origen.getRGB(x, y)));
            }
        }
        return destino;
    }

    private static int sinMagenta(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int magenta = Math.min(r, b) - g;
        if (magenta >= magentaSeguro) {
            return 0;
        }
        if (magenta >= magentaBorde) {
            a = a * (magentaSeguro - magenta) / (magentaSeguro - magentaBorde);
            r = Math.min(r, g + 40);
            b = Math.min(b, g + 40);
        }
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
