package domain;

public class Experta extends PerfilMaquina {
    private static final long serialVersionUID = 1L;
    public static final double tiempoReaccionExperta = 0.08;
    public static final double distanciaDetras = 35;
    public static final double toleranciaPosicion = 12;
    public static final double alcanceGolpe = 70;
    public static final double alcanceSalto = 90;

    public Experta() {
        super(tiempoReaccionExperta);
    }

    @Override
    protected void elegirAccion(Jugador yo, Escenario escenario) {
        Balon balon = escenario.getBalon();
        int ataque = yo.getDireccionAtaque();
        double dx = balon.getCentroX() - yo.getCentroX();
        double dy = balon.getCentroY() - yo.getCentroY();
        double objetivo = balon.getCentroX() - ataque * distanciaDetras;
        double falta = objetivo - yo.getCentroX();

        if (Math.abs(falta) <= toleranciaPosicion) {
            direccionActual = 0;
        } else {
            direccionActual = falta > 0 ? 1 : -1;
        }

        boolean alFrente = dx * ataque >= 0;
        if (alFrente && Math.abs(dx) < alcanceGolpe && Math.abs(dy) < alcanceGolpe) {
            yo.patear(escenario);
        }
        if (Math.abs(dx) < alcanceSalto && dy < -Jugador.ALTO / 2) {
            yo.saltar();
        }
        if (yo.getBarraPoder().estaLlena() && yo.getPoderActivo() == null) {
            yo.activarPoder();
        }
    }
}
