import java.awt.*;
import javax.swing.ImageIcon;

public class Fantasma {

    //? VARIABLES DE POSICION Y MOVIMIENTO
    private int x, y;
    private int dx, dy;
    private final int VELOCIDAD = 4;
    
    //? CONFIGURACION DEL MAPA
    private int tamanoBloque;
    private int numBloques;
    private int tipo;   //? 0 = Agresivo, 1 = Medio, 2 = Aleatorio
    private int espera; //? Contador para salir de la casa poco a poco

    //? IMAGENES
    private Image imagenNormal;
    private Image imagenAsustado;

    // CONSTRUCTOR
    public Fantasma(int x, int y, int tamanoBloque, int numBloques, int tipo, int espera, Image normal, Image asustado) {
        this.x = x;
        this.y = y;
        this.tamanoBloque = tamanoBloque;
        this.numBloques = numBloques;
        this.tipo = tipo; 
        this.espera = espera; //? Guardamos cuanto tiempo debe esperar antes de salir
        this.imagenNormal = normal;
        this.imagenAsustado = asustado;
        
        this.dx = 1; //* Inicia moviendose a la derecha
        this.dy = 0;
    }

    public void dibujar(Graphics2D g2d, Component observador, boolean modoCaza) {
        //* Si esta en modo caza dibujamos al fantasma asustado, si no el normal
        if (modoCaza && imagenAsustado != null) {
            g2d.drawImage(imagenAsustado, x + 2, y + 2, observador);
        } else {
            g2d.drawImage(imagenNormal, x + 2, y + 2, observador);
        }
    }

    public void mover(int pacmanX, int pacmanY, short[] datosPantalla, boolean modoCaza) {
        
        //? Logica de la salida escalonada
        if (espera > 0) {
            espera--; //! Restamos 1 al contador
            return;   //! NO HACEMOS NADA MAS, se queda quieto hasta que termine la espera
        }

        if (x % tamanoBloque == 0 && y % tamanoBloque == 0) { //* Solo tomamos decisiones de giro cuando estamos perfectamente centrados en un bloque
            
            int posX = x / tamanoBloque;
            int posY = y / tamanoBloque;

            //? 2. LOGICA DE SALIDA (ANTI-ATRAPE)
            //* Si esta dentro de la "casa" (filas 6-7), lo obligamos a subir para salir
            if (posY >= 6 && posY <= 7 && posX > 5 && posX < 9) {
                dx = 0;
                dy = -1; //* Fuerza ir ARRIBA
                x += dx * VELOCIDAD;
                y += dy * VELOCIDAD;
                return; //! Salimos del metodo para no calcular mas inteligencia
            }

            // IA xd
            
            int pacmanPosX = pacmanX / tamanoBloque;
            int pacmanPosY = pacmanY / tamanoBloque;

            boolean decisionTomada = false;

            //? PERSONALIDAD
            double probabilidadInteligencia = 0.0;
            
            //* Definimos la inteligencia segun el tipo de fantasma
            if (tipo == 0) probabilidadInteligencia = 0.9;      //* Muy listo (Agresivo)
            else if (tipo == 1) probabilidadInteligencia = 0.6; //* Medio listo
            else probabilidadInteligencia = 0.1;                //* Muy tonto (Aleatorio)

            //! En modo caza la inteligencia baja a 0 para que huyan erratiamente
            if (modoCaza) probabilidadInteligencia = 0.0; 

            //? CALCULOS DE RUTA
            int distX = Math.abs(pacmanPosX - posX);
            int distY = Math.abs(pacmanPosY - posY);

            int idealDX = 0; 
            int idealDY = 0;

            //* Determinamos la direccion ideal hacia Pacman
            if (pacmanPosX > posX) idealDX = 1; else idealDX = -1;
            if (pacmanPosY > posY) idealDY = 1; else idealDY = -1;

            if (modoCaza) {
                idealDX = -idealDX; //* Invertimos la direccion si debe huir
                idealDY = -idealDY;
            }

            //? INTENTAR MOVERSE INTELIGENTEMENTE
            //* Si el azar (probabilidad) lo permite, intentamos tomar la ruta optima
            if (Math.random() < probabilidadInteligencia) {
                if (distX > distY) {
                    //* Prioridad Horizontal
                    if (!esMuro(posX + idealDX, posY, datosPantalla) && !(idealDX == -dx)) {
                        dx = idealDX; dy = 0; decisionTomada = true;
                    } else if (!esMuro(posX, posY + idealDY, datosPantalla) && !(idealDY == -dy)) {
                        dx = 0; dy = idealDY; decisionTomada = true;
                    }
                } else {
                    //* Prioridad Vertical
                    if (!esMuro(posX, posY + idealDY, datosPantalla) && !(idealDY == -dy)) {
                        dx = 0; dy = idealDY; decisionTomada = true;
                    } else if (!esMuro(posX + idealDX, posY, datosPantalla) && !(idealDX == -dx)) {
                        dx = idealDX; dy = 0; decisionTomada = true;
                    }
                }
            }

            //? MOVIMIENTO ALEATORIO 
            boolean choqueFrente = esMuro(posX + dx, posY + dy, datosPantalla);
            
            if (!decisionTomada && (choqueFrente || Math.random() < 0.2)) { //* Si no tomo decision inteligente, O si va a chocar, buscamos direccion al azar
                int intentos = 0;
                while (intentos < 10) {
                    int rand = (int) (Math.random() * 4);
                    int tempDX = 0, tempDY = 0;

                    if (rand == 0) { tempDX = -1; tempDY = 0; }
                    else if (rand == 1) { tempDX = 1; tempDY = 0; }
                    else if (rand == 2) { tempDX = 0; tempDY = -1; }
                    else if (rand == 3) { tempDX = 0; tempDY = 1; }

                    if (!esMuro(posX + tempDX, posY + tempDY, datosPantalla)) { //* Si la nueva direccion es valida y no es devolverse (salvo que este acorralado)
                        if (choqueFrente || !(tempDX == -dx && tempDY == -dy)) {
                            dx = tempDX;
                            dy = tempDY;
                            break; //* Encontramos camino, salimos del bucle
                        }
                    }
                    intentos++;
                }
            }
            
            //? SEGURIDAD FINAL
            if (esMuro(posX + dx, posY + dy, datosPantalla)) { //! Si despues de todo sigue apuntando a un muro, lo detenemos o invertimos
                if (!esMuro(posX - dx, posY - dy, datosPantalla)) {
                    dx = -dx; dy = -dy; //* Media vuelta de emergencia
                } else {
                    dx = 0; dy = 0; //* Parada en seco
                }
            }
        }

        //* Aplicar movimiento fisico en pixeles
        x += dx * VELOCIDAD;
        y += dy * VELOCIDAD;
    }

    private boolean esMuro(int x, int y, short[] datosPantalla) {
        
        if (x < 0 || x >= numBloques || y < 0 || y >= numBloques) return true; //! Proteccion: Fuera del mapa cuenta como muro
        
        return (datosPantalla[y * numBloques + x] == 1); //* Retorna true si el valor en el mapa es 1
    }

    // Getters y Setters
    public int getX() { return x; }
    public int getY() { return y; }
    
    public void setPosicion(int x, int y) {
        this.x = x;
        this.y = y;
        this.dx = 1; 
        this.dy = 0;
    }
}