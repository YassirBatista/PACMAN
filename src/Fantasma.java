import java.awt.*;
import javax.swing.ImageIcon;

public class Fantasma {

    private int x, y;
    private int dx, dy;
    private final int VELOCIDAD = 4;
    
    private int tamanoBloque;
    private int numBloques;

    private Image imagenNormal;
    private Image imagenAsustado;

    public Fantasma(int x, int y, int tamanoBloque, int numBloques, Image normal, Image asustado) {
        this.x = x;
        this.y = y;
        this.tamanoBloque = tamanoBloque;
        this.numBloques = numBloques;
        this.imagenNormal = normal;
        this.imagenAsustado = asustado;
        
        this.dx = 1; 
        this.dy = 0;
    }

    public void dibujar(Graphics2D g2d, Component observador, boolean modoCaza) {
        if (modoCaza && imagenAsustado != null) {
            g2d.drawImage(imagenAsustado, x + 2, y + 2, observador);
        } else {
            g2d.drawImage(imagenNormal, x + 2, y + 2, observador);
        }
    }

    // --- AQUÍ ESTÁ EL CAMBIO PRINCIPAL ---
    // Agregamos 'boolean modoCaza' para saber si debe huir
    public void mover(int pacmanX, int pacmanY, short[] datosPantalla, boolean modoCaza) {
        
        // Solo decidimos cuando estamos centrados en un bloque
        if (x % tamanoBloque == 0 && y % tamanoBloque == 0) {
            
            int posX = x / tamanoBloque;
            int posY = y / tamanoBloque;
            int pacmanPosX = pacmanX / tamanoBloque;
            int pacmanPosY = pacmanY / tamanoBloque;

            // Variables para decidir la nueva dirección
            int mejorDX = 0;
            int mejorDY = 0;
            boolean decisionTomada = false;

            // 1. LÓGICA DE PERSECUCIÓN / HUDA (Inteligente)
            // Calculamos distancias
            int distX = Math.abs(pacmanPosX - posX);
            int distY = Math.abs(pacmanPosY - posY);

            // Decidimos hacia dónde nos gustaría ir idealmente
            int idealDX = 0; 
            int idealDY = 0;

            if (pacmanPosX > posX) idealDX = 1; else idealDX = -1;
            if (pacmanPosY > posY) idealDY = 1; else idealDY = -1;

            // TRUCO SIMPLE: Si estamos en modo caza, invertimos la dirección ideal
            if (modoCaza) {
                idealDX = -idealDX; // Si era derecha, ahora es izquierda
                idealDY = -idealDY; // Si era abajo, ahora es arriba
            }

            // Intentamos movernos en el eje donde la distancia es mayor (Prioridad)
            if (distX > distY) {
                // Prioridad Horizontal
                if (!esMuro(posX + idealDX, posY, datosPantalla) && !(idealDX == -dx)) {
                    dx = idealDX; dy = 0; decisionTomada = true;
                } else if (!esMuro(posX, posY + idealDY, datosPantalla) && !(idealDY == -dy)) {
                    dx = 0; dy = idealDY; decisionTomada = true;
                }
            } else {
                // Prioridad Vertical
                if (!esMuro(posX, posY + idealDY, datosPantalla) && !(idealDY == -dy)) {
                    dx = 0; dy = idealDY; decisionTomada = true;
                } else if (!esMuro(posX + idealDX, posY, datosPantalla) && !(idealDX == -dx)) {
                    dx = idealDX; dy = 0; decisionTomada = true;
                }
            }

            // 2. MOVIMIENTO ALEATORIO (Plan B)
            // Si la lógica inteligente falló (había muro) O si chocamos de frente O por azar
            boolean choqueFrente = esMuro(posX + dx, posY + dy, datosPantalla);
            
            if (!decisionTomada && (choqueFrente || Math.random() < 0.2)) {
                int intentos = 0;
                while (intentos < 10) {
                    int rand = (int) (Math.random() * 4);
                    int tempDX = 0, tempDY = 0;

                    if (rand == 0) { tempDX = -1; tempDY = 0; }
                    else if (rand == 1) { tempDX = 1; tempDY = 0; }
                    else if (rand == 2) { tempDX = 0; tempDY = -1; }
                    else if (rand == 3) { tempDX = 0; tempDY = 1; }

                    // Si es válido y no es dar media vuelta (salvo que estemos acorralados)
                    if (!esMuro(posX + tempDX, posY + tempDY, datosPantalla)) {
                        if (choqueFrente || !(tempDX == -dx && tempDY == -dy)) {
                            dx = tempDX;
                            dy = tempDY;
                            break; // Encontramos salida, rompemos el bucle
                        }
                    }
                    intentos++;
                }
            }
            
            // 3. SEGURIDAD FINAL (Anti-Traspaso)
            // Si después de todo seguimos apuntando a un muro, nos detenemos o invertimos
            if (esMuro(posX + dx, posY + dy, datosPantalla)) {
                if (!esMuro(posX - dx, posY - dy, datosPantalla)) {
                    dx = -dx; dy = -dy; // Media vuelta de emergencia
                } else {
                    dx = 0; dy = 0; // Parar en seco
                }
            }
        }

        // Aplicar movimiento
        x += dx * VELOCIDAD;
        y += dy * VELOCIDAD;
    }

    private boolean esMuro(int x, int y, short[] datosPantalla) {
        if (x < 0 || x >= numBloques || y < 0 || y >= numBloques) return true;
        return (datosPantalla[y * numBloques + x] == 1);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    
    public void setPosicion(int x, int y) {
        this.x = x;
        this.y = y;
        this.dx = 1; 
        this.dy = 0;
    }
}