import java.awt.*;
import javax.swing.ImageIcon;

public class Fantasma {

    private int x, y;
    private int dx, dy;
    private final int VELOCIDAD = 4;
    
    private int tamanoBloque;
    private int numBloques;
    private int tipo; 
    private int espera; //? NUEVO: Contador para salir de la casa

    private Image imagenNormal;
    private Image imagenAsustado;

    // Actualizamos el constructor para recibir la "espera"
    public Fantasma(int x, int y, int tamanoBloque, int numBloques, int tipo, int espera, Image normal, Image asustado) {
        this.x = x;
        this.y = y;
        this.tamanoBloque = tamanoBloque;
        this.numBloques = numBloques;
        this.tipo = tipo; 
        this.espera = espera; //? Guardamos el tiempo de espera
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

    public void mover(int pacmanX, int pacmanY, short[] datosPantalla, boolean modoCaza) {
        
        //? LÓGICA DE ESPERA (Salida escalonada)
        if (espera > 0) {
            espera--; // Restamos 1 al contador
            return;   // NO HACEMOS NADA MÁS, se queda quieto
        }

        // Solo decidimos cuando estamos centrados en un bloque
        if (x % tamanoBloque == 0 && y % tamanoBloque == 0) {
            
            int posX = x / tamanoBloque;
            int posY = y / tamanoBloque;

            // --- 1. LÓGICA DE SALIDA (ANTI-ATRAPE) ---
            if (posY >= 6 && posY <= 7 && posX > 5 && posX < 9) {
                dx = 0;
                dy = -1; // Fuerza ir ARRIBA
                x += dx * VELOCIDAD;
                y += dy * VELOCIDAD;
                return; 
            }

            // ... (EL RESTO DEL CÓDIGO SIGUE IGUAL QUE ANTES) ...
            
            int pacmanPosX = pacmanX / tamanoBloque;
            int pacmanPosY = pacmanY / tamanoBloque;

            boolean decisionTomada = false;

            // --- 2. PERSONALIDAD ---
            double probabilidadInteligencia = 0.0;
            
            if (tipo == 0) probabilidadInteligencia = 0.9; 
            else if (tipo == 1) probabilidadInteligencia = 0.6; 
            else probabilidadInteligencia = 0.1; 

            if (modoCaza) probabilidadInteligencia = 0.0; 

            int distX = Math.abs(pacmanPosX - posX);
            int distY = Math.abs(pacmanPosY - posY);

            int idealDX = 0; 
            int idealDY = 0;

            if (pacmanPosX > posX) idealDX = 1; else idealDX = -1;
            if (pacmanPosY > posY) idealDY = 1; else idealDY = -1;

            if (modoCaza) {
                idealDX = -idealDX; 
                idealDY = -idealDY;
            }

            // --- 3. INTENTAR MOVERSE INTELIGENTEMENTE ---
            if (Math.random() < probabilidadInteligencia) {
                if (distX > distY) {
                    if (!esMuro(posX + idealDX, posY, datosPantalla) && !(idealDX == -dx)) {
                        dx = idealDX; dy = 0; decisionTomada = true;
                    } else if (!esMuro(posX, posY + idealDY, datosPantalla) && !(idealDY == -dy)) {
                        dx = 0; dy = idealDY; decisionTomada = true;
                    }
                } else {
                    if (!esMuro(posX, posY + idealDY, datosPantalla) && !(idealDY == -dy)) {
                        dx = 0; dy = idealDY; decisionTomada = true;
                    } else if (!esMuro(posX + idealDX, posY, datosPantalla) && !(idealDX == -dx)) {
                        dx = idealDX; dy = 0; decisionTomada = true;
                    }
                }
            }

            // --- 4. MOVIMIENTO ALEATORIO ---
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

                    if (!esMuro(posX + tempDX, posY + tempDY, datosPantalla)) {
                        if (choqueFrente || !(tempDX == -dx && tempDY == -dy)) {
                            dx = tempDX;
                            dy = tempDY;
                            break; 
                        }
                    }
                    intentos++;
                }
            }
            
            // --- 5. SEGURIDAD FINAL ---
            if (esMuro(posX + dx, posY + dy, datosPantalla)) {
                if (!esMuro(posX - dx, posY - dy, datosPantalla)) {
                    dx = -dx; dy = -dy; 
                } else {
                    dx = 0; dy = 0; 
                }
            }
        }

        // Aplicar movimiento físico
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