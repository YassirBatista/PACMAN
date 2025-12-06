import java.awt.*;

public class Fruta {
    private int x, y;         // Posición en píxeles
    private boolean visible;  // ¿Está activa en el juego?
    private Image imagen;
    private int puntaje;

    public Fruta(int xGrid, int yGrid, int tamanoBloque, Image imagen) {
        // Convertimos coordenadas de grilla (ej: 1, 13) a píxeles
        this.x = xGrid * tamanoBloque;
        this.y = yGrid * tamanoBloque;
        this.imagen = imagen;
        this.visible = true; // Nace visible
        this.puntaje = 500;
    }

    // Dibujarse a sí misma
    public void dibujar(Graphics2D g2d, Component observador) {
        if (visible) {
            // El +2 es para centrarla visualmente como tenías antes
            g2d.drawImage(imagen, x + 2, y + 2, observador);
        }
    }

    // Detectar si Pacman la tocó
    // Usamos rectángulos invisibles para ver si se chocan
    public boolean chequearColision(int pacmanX, int pacmanY, int tamanoBloque) {
        if (!visible) return false;

        Rectangle rectFruta = new Rectangle(x, y, tamanoBloque, tamanoBloque);
        Rectangle rectPacman = new Rectangle(pacmanX, pacmanY, tamanoBloque, tamanoBloque);

        if (rectFruta.intersects(rectPacman)) {
            visible = false; // ¡Se come y desaparece!
            return true;     // Avisamos que hubo colisión
        }
        return false;
    }
    
    // Getters y Setters si los necesitas luego
    public boolean esVisible() { return visible; }
    public int getPuntaje() { return puntaje; }
}