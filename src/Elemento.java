import java.awt.*;

public class Elemento {
    private int x, y;
    private int tipo; // 0 = Punto Normal, 1 = Fruta/Cereza
    private boolean visible;
    private Image imagen;

    // Constructor
    public Elemento(int xGrid, int yGrid, int tamanoBloque, int tipo, Image imagen) {
        this.x = xGrid * tamanoBloque;
        this.y = yGrid * tamanoBloque;
        this.tipo = tipo;
        this.imagen = imagen;
        this.visible = true;
    }

    public void dibujar(Graphics2D g2d, Component observador) {
        if (!visible) return;

        if (tipo == 0) {
            // DIBUJAR PUNTO BLANCO (Si es tipo 0)
            g2d.setColor(Color.WHITE);
            g2d.fillRect(x + 10, y + 10, 4, 4); 
        } else {
            // DIBUJAR IMAGEN (Si es Cereza)
            g2d.drawImage(imagen, x + 2, y + 2, observador);
        }
    }

    public boolean chequearColision(int pacmanX, int pacmanY, int tamanoBloque) {
        if (!visible) return false;

        // Creamos un rectángulo pequeño para el objeto y otro para Pacman
        Rectangle rectElemento = new Rectangle(x + 8, y + 8, 8, 8); // +8 para hacerlo más pequeño y centrado
        Rectangle rectPacman = new Rectangle(pacmanX + 4, pacmanY + 4, 16, 16);

        if (rectElemento.intersects(rectPacman)) {
            visible = false;
            return true;
        }
        return false;
    }

    public boolean esVisible() { return visible; }
    public int getTipo() { return tipo; }
}