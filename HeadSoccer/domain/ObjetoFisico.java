package domain;

import java.io.Serializable;

public abstract class ObjetoFisico implements Serializable {
    private static final long serialVersionUID = 1L;
    protected double x;
    protected double y;
    protected double velX;
    protected double velY;
    protected double ancho;
    protected double alto;
    // Dónde estaba al empezar el instante: sirve para saber quién se metió en quién.
    private double xAnterior;
    private boolean apoyado;
    // Marca de que el objeto debe salir del partido (el escenario lo retira)
    private boolean expirado;
    // Escenario donde está el objeto; puede ser null fuera de un partido
    protected Escenario escenario;

    // Crea un objeto con su posición y su tamaño
    public ObjetoFisico(double x, double y, double ancho, double alto) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        xAnterior = x;
    }

    // Cada objeto sabe cómo moverse en cada instante del juego
    public abstract void actualizar(double dt);

    // Mueve el objeto según su velocidad y el tiempo que pasó; deja de estar apoyado hasta que vuelva a chocar
    protected void avanzar(double dt) {
        xAnterior = x;
        apoyado = false;
        x += velX * dt;
        y += velY * dt;
    }

    // Dice si el objeto ya debe salir del partido (si alguien llamó a desaparecer)
    public boolean haExpirado() {
        return expirado;
    }

    // Ganchos con comportamiento neutro: el escenario los llama sobre cualquier objeto extra
    // sin preguntar su tipo, y cada subclase sobreescribe solo lo que le corresponde.
    public void prolongar(double duracion) {
    }

    // Marca el objeto como expirado; el escenario lo retira al terminar el instante
    public void desaparecer() {
        expirado = true;
    }

    // Pide disipar las ilusiones (lo usa TiroIlusion); por defecto nada, solo un balón falso desaparece
    public void disiparIlusion() {
    }

    // Recuerda en qué escenario está el objeto
    public void setEscenario(Escenario escenario) {
        this.escenario = escenario;
    }

    // Gravedad que se aplica al objeto: la del escenario, o la normal si no tiene escenario
    protected double getGravedad() {
        return escenario == null ? Cancha.gravedadNormal : escenario.getGravedad();
    }

    // Cuánta velocidad conserva al rodar sobre algo: la del escenario, o la normal si no tiene escenario
    protected double getRoce() {
        return escenario == null ? Cancha.roceNormal : escenario.getRoce();
    }

    // Permite a la presentación dibujar los balones sin que el dominio tenga código de dibujo.
    public void recolectarBalones(java.util.List<Balon> destino) {
    }

    // Nombre de cómo se ve el objeto, para que la interfaz sepa dibujarlo sin preguntar su clase
    public String getApariencia() {
        return "objeto";
    }

    // Dice si el objeto sigue en la cancha después de un gol; por defecto no (se retira al sacar)
    public boolean persisteTrasGol() {
        return false;
    }

    // Revisa si este objeto se está tocando con otro
    public boolean colisionaCon(ObjetoFisico otro) {
        return x < otro.x + otro.ancho
            && otro.x < x + ancho
            && y < otro.y + otro.alto
            && otro.y < y + alto;
    }

    // Dice si los demás rebotan o se bloquean contra este objeto; por defecto sí
    public boolean esSolido() {
        return true;
    }

    // Dice si el objeto se puede mover al chocar; los límites de la cancha no
    public boolean esMovil() {
        return true;
    }

    // Cuánta velocidad conserva este objeto al rebotar contra algo sólido (0 = se frena del todo)
    public double getRebote() {
        return 0;
    }

    // Método general de choque: si el otro es sólido, este objeto se separa y rebota; después recibe el efecto del otro
    public void chocarCon(ObjetoFisico otro) {
        if (otro.esSolido() && esMovil()) {
            separarDe(otro);
        }
        otro.afectar(this);
    }

    // Efecto que este objeto produce sobre quien lo toca; por defecto ninguno
    public void afectar(ObjetoFisico otro) {
    }

    // Lo que pasa cuando un jugador toca este objeto; por defecto nada (el balón lo sobreescribe con el cabezazo)
    public void recibirToque(Jugador jugador) {
    }

    // Dice si este objeto es rival de quien lanzó un poder; por defecto no (solo un jugador del otro lado lo es)
    public boolean esRivalDe(Jugador lanzador) {
        return false;
    }

    // Le quita vida al objeto; por defecto nada (solo los que tienen vida, como el jugador)
    public void recibirDano(int dano) {
    }

    // Le devuelve vida al objeto; por defecto nada (solo los que tienen vida, como el jugador)
    public void curar(int cantidad) {
    }

    // Lo que pasa cuando un balón con poder impacta a este objeto; por defecto nada (el jugador recibe daño y efecto)
    public void recibirImpacto(Poder poder, Jugador lanzador, Escenario escenario) {
    }

    // Lo que hace el objeto al quedar apoyado encima de algo; por defecto nada
    protected void alApoyarse() {
    }

    // Pone el objeto quieto y apoyado justo encima de otro (por ejemplo, del piso al sacar)
    public void apoyarSobre(ObjetoFisico base) {
        y = base.y - alto;
        velY = 0;
        apoyado = true;
    }

    // Dice si el objeto quedó apoyado encima de algo en este instante
    public boolean estaApoyado() {
        return apoyado;
    }

    // Separa este objeto del otro por el eje en que menos se meten el uno en el otro
    private void separarDe(ObjetoFisico otro) {
        double haciaIzquierda = x + ancho - otro.x;
        double haciaDerecha = otro.x + otro.ancho - x;
        double haciaArriba = y + alto - otro.y;
        double haciaAbajo = otro.y + otro.alto - y;
        if (haciaIzquierda <= 0 || haciaDerecha <= 0 || haciaArriba <= 0 || haciaAbajo <= 0) {
            return;
        }
        if (Math.min(haciaIzquierda, haciaDerecha) < Math.min(haciaArriba, haciaAbajo)) {
            separarEnX(otro, haciaIzquierda, haciaDerecha);
        } else {
            separarEnY(otro, haciaArriba, haciaAbajo);
        }
    }

    // De lado: contra algo fijo se separa del todo; entre dos que se mueven, cada uno retrocede
    // lo que avanzó, así ninguno empuja al otro y los dos quedan bloqueados
    private void separarEnX(ObjetoFisico otro, double haciaIzquierda, double haciaDerecha) {
        double cuanto = Math.min(haciaIzquierda, haciaDerecha);
        if (otro.esMovil() && otro.desplazamientoX() > 0) {
            cuanto = Math.min(cuanto, desplazamientoX());
        }
        if (cuanto <= 0) {
            return;
        }
        int sentido = haciaIzquierda < haciaDerecha ? -1 : 1;
        x += sentido * cuanto;
        if (velX * sentido < 0) {
            velX = -velX * getRebote();
        }
    }

    // Arriba o abajo: entre dos que se mueven solo se separa el de arriba (el de abajo lo sostiene);
    // quedar encima de algo cuenta como estar en el suelo
    private void separarEnY(ObjetoFisico otro, double haciaArriba, double haciaAbajo) {
        boolean encima = haciaArriba < haciaAbajo;
        if (otro.esMovil() && !encima) {
            return;
        }
        if (encima) {
            y -= haciaArriba;
            if (velY > 0) {
                velY = -velY * getRebote();
            }
            apoyado = true;
            alApoyarse();
        } else {
            y += haciaAbajo;
            if (velY < 0) {
                velY = -velY * getRebote();
            }
        }
    }

    // Cuánto se movió de lado en este instante
    private double desplazamientoX() {
        return Math.abs(x - xAnterior);
    }

    // Cambia el tamaño del objeto manteniendo su centro y sus pies en el mismo sitio
    public void escalar(double factor) {
        double base = y + alto;
        double centro = getCentroX();
        ancho *= factor;
        alto *= factor;
        x = centro - ancho / 2;
        y = base - alto;
    }

    public void setPosicion(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setVelocidad(double velX, double velY) {
        this.velX = velX;
        this.velY = velY;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getVelX() { return velX; }
    public double getVelY() { return velY; }
    public double getAncho() { return ancho; }
    public double getAlto() { return alto; }
    public double getCentroX() { return x + ancho / 2; }
    public double getCentroY() { return y + alto / 2; }
    public double getBorde() { return y + alto; }
}
