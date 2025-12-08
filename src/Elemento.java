import java.awt.*;

public class Elemento {
    
    // VARIABLES
    private int x, y;             //? Posicion exacta en pixeles
    private int tipo;             //? 0 = Punto Blanco, 1 = Cereza
    private boolean visible;      //* True = Aunc no se ha comido
    private Image imagen;         //? Solo se usa si es una cereza (tipo 1)

    // CONSTRUCTOR
    public Elemento(int xGrid, int yGrid, int tamanoBloque, int tipo, Image imagen) {
        //* Convertimos la posicion de la grilla 1,1 == 24, 24
        this.x = xGrid * tamanoBloque;
        this.y = yGrid * tamanoBloque;
        
        this.tipo = tipo;
        this.imagen = imagen;
        this.visible = true; //* Nace visible por defecto
    }

    public void dibujar(Graphics2D g2d, Component observador) { //! Si ya se comio (no es visible), no dibujamos nada y salimos
        if (!visible) return;

        if (tipo == 0) { //* Dibujamos las frutas normales
            g2d.setColor(Color.WHITE);
            // Dibujamos un cuadradito blanco centrado (+10 es para centrar en bloque de 24)
            g2d.fillRect(x + 10, y + 10, 4, 4); 
        } else { //* Dibujamos la cerezas
            // Dibujamos la imagen centrada (+2 para borde)
            g2d.drawImage(imagen, x + 2, y + 2, observador);
        }
    }

    public boolean chequearColision(int pacmanX, int pacmanY, int tamanoBloque) {  //! Si no es visible, no se puede chocar
        if (!visible) return false; 

        //* Creamos las hitboxs para detectar si choco
        Rectangle rectElemento = new Rectangle(x + 8, y + 8, 8, 8); // las hace,mos mas pequenas para que no sea dificl (es como el salto en el pixel justo)
        Rectangle rectPacman = new Rectangle(pacmanX + 4, pacmanY + 4, 16, 16);

        //* Si las cajas se tocan
        if (rectElemento.intersects(rectPacman)) {
            visible = false; //* se comio
            return true;     //! retornamos true para saber que hubo colision
        }
        return false;
    }

    // Getters
    public boolean esVisible() { return visible; }
    public int getTipo() { return tipo; }
}