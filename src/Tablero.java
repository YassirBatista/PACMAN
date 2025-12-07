import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform; // Necesario para el escalado
import javax.swing.*;

public class Tablero extends JPanel implements ActionListener {

    private final int TAMAÑO_BLOQUE_BASE = 24; //tamano de los pixeles
    private final int NUM_BLOQUES = 15; //tamano de la matriz del mapa
    // Tamaño total del tablero
    private final int TAMAÑO_JUEGO = NUM_BLOQUES * TAMAÑO_BLOQUE_BASE; // mutiplicamos el tamano del tablero x los pixeles de los bloques
    private final int VELOCIDAD = 4;

    // IMÁGENES
    private Image pacmanArriba, pacmanAbajo, pacmanIzquierda, pacmanDerecha; //donde guardamos las imagenes en que se mostraran 
    private Image fantasma, cereza;
    private Image imagenActualPacman; //sprite de pacman 

    // VARIABLES DEL JUEGO
    private int pacmanX, pacmanY; //posicion de pacman
    private int pacmanDX, pacmanDY; //cammbio de direccion
    private int reqDX, reqDY; //guardado de hacia donde queriamos ir
    
    private int fantasmaX, fantasmaY;
    private int fantasmaDX, fantasmaDY; // Dirección del fantasma

    private int puntaje = 0;
    private boolean juegoEnCurso = true;
    private Timer timer;

    //frutas
    private Fruta miCereza;

    // MAPA (1 = Muro, 0 = Comida, 2 = Espacio Vacío)
    private final short datosNivel[] = {
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        1,0,0,0,0,0,0,1,3,0,0,0,0,0,1,
        1,0,1,1,1,1,0,1,0,1,1,1,1,0,1,
        1,0,1,0,0,1,0,1,0,1,0,0,1,0,1,
        1,0,1,0,0,0,0,0,0,0,0,0,1,0,1,
        1,0,0,0,1,1,0,2,0,1,1,0,0,0,1,
        1,1,1,0,1,2,2,2,2,2,1,0,1,1,1,
        1,0,0,0,1,2,2,2,2,2,1,0,0,0,1,
        1,0,1,1,1,1,1,1,1,1,1,1,1,0,1,
        1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,
        1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,
        1,0,1,0,0,0,0,2,0,0,0,0,1,0,1,
        1,0,1,0,1,1,1,0,1,1,1,0,1,0,1,
        1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
    };

    private short[] datosPantalla;

    public Tablero() {
        cargarImagenes();
        iniciarJuego();
        addKeyListener(new TAdapter());
        setFocusable(true);
        setBackground(Color.BLACK);
    }

        String rutaComer = "/assets/sounds/comer.wav";
        String rutaMorir = "/assets/sounds/pacman_death.wav";

        private void cargarImagenes() {
        // Definimos la ruta base que apunta a la carpeta 'assets' en el classpath del proyecto.
        // El '/' inicial indica la raíz del classpath.
        String rutaBase = "/assets/sprites/pacman/";
        String rutaBaseFantasma = "/assets/sprites/ghosts/"; 

        try {
            // Carga de imágenes de Pac-Man
            pacmanArriba = new ImageIcon(getClass().getResource(rutaBase + "pacmanUp.png")).getImage();
            pacmanAbajo = new ImageIcon(getClass().getResource(rutaBase + "pacmanDown.png")).getImage();
            pacmanIzquierda = new ImageIcon(getClass().getResource(rutaBase + "pacmanLeft.png")).getImage();
            pacmanDerecha = new ImageIcon(getClass().getResource(rutaBase + "pacmanRight.png")).getImage();
            
            // Carga de imágenes del Fantasma y la Cereza
            fantasma = new ImageIcon(getClass().getResource(rutaBaseFantasma + "blueGhost.png")).getImage();
            cereza = new ImageIcon(getClass().getResource(rutaBaseFantasma + "cherry.png")).getImage();
            //miCereza = new Fruta(1, 13, TAMAÑO_BLOQUE_BASE, cereza);
            
        } catch (NullPointerException e) {
            // si alguna imagen no se encuentra en la ruta especificada.
            System.err.println("ERROR: No se pudo cargar una o más imágenes. Asegúrate que están en la carpeta: " + rutaBase);
            e.printStackTrace();
        }
        
        imagenActualPacman = pacmanDerecha; // como pacman siempre inicia viendo a la derecha
    }

    // El gameloop
    private void iniciarJuego() {
        if (timer != null) { // esto se hace para que al darle de nuevo no se multiplique la velocidad
            timer.stop();
        }
        datosPantalla = new short[NUM_BLOQUES * NUM_BLOQUES];
        for (int i = 0; i < datosNivel.length; i++) {
            datosPantalla[i] = datosNivel[i];
        }

        // Posiciones iniciales usando el tamaño base
        pacmanX = 7 * TAMAÑO_BLOQUE_BASE; 
        pacmanY = 11 * TAMAÑO_BLOQUE_BASE;
        fantasmaX = 7 * TAMAÑO_BLOQUE_BASE;
        fantasmaY = 7 * TAMAÑO_BLOQUE_BASE;
        
        //iniciamos en 0 todas las variables de movimiento
        pacmanDX = 0; pacmanDY = 0;
        reqDX = 0; reqDY = 0;
        
        fantasmaDX = 1; fantasmaDY = 0; // Fantasma empieza moviéndose a la derecha

        //iniciamos sobre 40ms para que el juego vaya a 25 fps 1000/40
        timer = new Timer(40, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (juegoEnCurso) {
            moverPacman();
            moverFantasma();
            chequearColisiones();
        }
        repaint();
    }

    private void moverPacman() {
        // Pac-Man se mueve píxel a píxel, pero las decisiones (girar, comer, chocar). solo pueden ocurrir cuando está perfectamente centrado en un bloque (casilla).
        // Usamos el operador módulo (%) para verificar si las coordenadas son múltiplos de 24.
        if (pacmanX % TAMAÑO_BLOQUE_BASE == 0 && pacmanY % TAMAÑO_BLOQUE_BASE == 0) {

            // 2. CONVERSIÓN DE COORDENADAS (Píxeles -> Mapa)
            // Calculamos en qué columna (posX) y fila (posY) de la matriz estamos.
            int posX = pacmanX / TAMAÑO_BLOQUE_BASE;
            int posY = pacmanY / TAMAÑO_BLOQUE_BASE;
            
            // Calculamos el índice único para el array unidimensional 'datosPantalla'.
            int indice = posY * NUM_BLOQUES + posX;
            
            // CASO A: Comer un punto normal (Valor 0 en el mapa)
            if (datosPantalla[indice] == 0) {
                datosPantalla[indice] = 2; // Actualizamos el mapa: 2 significa "Vacio/Ya comido"
                puntaje += 10;             // Sumamos puntaje estándar
                
                musica.comer(rutaComer, -25);
            }
            
            // CASO B: Comer una Fruta/Cereza (Valor 3 en el mapa)
            // Esta lógica es nueva: detecta el objeto especial y da una recompensa mayor.
            else if (datosPantalla[indice] == 3) {
                datosPantalla[indice] = 2; // La fruta desaparece del mapa
                puntaje += 500;            // Recompensa alta por la fruta
                
                //musica.comer("/assets/sounds/comer.wav", -25);
            }

            // Verificamos si el usuario solicitó un cambio de dirección (reqDX/reqDY)
            if (reqDX != 0 || reqDY != 0) {
                if (!esMuro(posX + reqDX, posY + reqDY)) {
                    // Si el camino está libre, aplicamos el giro solicitado.
                    pacmanDX = reqDX;
                    pacmanDY = reqDY;
                }
            }

            // Verificamos si, siguiendo en la dirección actual, vamos a chocar contra un muro. Si hay muro, detenemos a Pacman (velocidad 0).
            if (esMuro(posX + pacmanDX, posY + pacmanDY)) {
                pacmanDX = 0;
                pacmanDY = 0;
            }
        }

        // Sumamos la velocidad a la posición actual para mover el sprite en la pantalla.
        pacmanX = pacmanX + (pacmanDX * VELOCIDAD);
        pacmanY = pacmanY + (pacmanDY * VELOCIDAD);
        
        // Cambiamos la imagen de Pac-Man según la dirección en la que se mueve
        if (pacmanDX == 1) imagenActualPacman = pacmanDerecha;
        else if (pacmanDX == -1) imagenActualPacman = pacmanIzquierda;
        else if (pacmanDY == -1) imagenActualPacman = pacmanArriba;
        else if (pacmanDY == 1) imagenActualPacman = pacmanAbajo;
    }


        private void moverFantasma() {


        // El fantasma solo toma decisiones cuando está perfectamente centrado en una casilla.
        if (fantasmaX % TAMAÑO_BLOQUE_BASE == 0 && fantasmaY % TAMAÑO_BLOQUE_BASE == 0) {
            
            // Convertimos píxeles a coordenadas de mapa (casillas)
            int posX = fantasmaX / TAMAÑO_BLOQUE_BASE;
            int posY = fantasmaY / TAMAÑO_BLOQUE_BASE;

            int pacmanX = 0; 
            int pacmanY = 0;

            int pacmanPosX = pacmanX / TAMAÑO_BLOQUE_BASE;
            int pacmanPosY = pacmanY / TAMAÑO_BLOQUE_BASE;

            boolean choqueFrente = esMuro(posX + fantasmaDX, posY + fantasmaDY);
            
            // =========================================================================
            // 2. LÓGICA DE PERSECUCIÓN PRIORITARIA (Caza)
            // =========================================================================
            
            // Si Pac-Man es visible, el fantasma lo persigue. Esta es la máxima prioridad.
            if (pacmanVisible(posX, posY, pacmanPosX, pacmanPosY)) {
                
                int nuevaDX = 0;
                int nuevaDY = 0;

                // 2.1. Cálculo de la dirección de persecución
                // Elige la dirección que acorta la distancia al objetivo.
                
                // Prioridad: Moverse en el eje donde la distancia a Pac-Man es mayor.
                
                int distanciaX = Math.abs(pacmanPosX - posX);
                int distanciaY = Math.abs(pacmanPosY - posY);

                // Intentar movimiento en el eje con mayor distancia (o el eje horizontal si son iguales)
                if (distanciaX > distanciaY || (distanciaX == distanciaY && distanciaX > 0)) {
                    // Moverse horizontalmente
                    if (pacmanPosX > posX) { nuevaDX = 1; }  // Derecha
                    else if (pacmanPosX < posX) { nuevaDX = -1; } // Izquierda
                } else if (distanciaY > 0) {
                    // Moverse verticalmente
                    if (pacmanPosY > posY) { nuevaDY = 1; } // Abajo
                    else if (pacmanPosY < posY) { nuevaDY = -1; } // Arriba
                }

                // 2.2. Aplicar el movimiento si es válido (no es un muro ni un giro de 180°)
                if ((nuevaDX != 0 || nuevaDY != 0)) {
                    // Prevenir giro de 180° si está persiguiendo
                    if (!(nuevaDX == -fantasmaDX && nuevaDY == -fantasmaDY)) {
                        // Si el camino está libre, el fantasma persigue inmediatamente.
                        if (!esMuro(posX + nuevaDX, posY + nuevaDY)) {
                            fantasmaDX = nuevaDX;
                            fantasmaDY = nuevaDY;
                            return; // ¡Sale del método! Esto asegura la prioridad absoluta.
                        }
                    }
                }
            }
            
            // =========================================================================
            // 3. LÓGICA ALEATORIA (Si no ve a Pac-Man, choca o toca cambio aleatorio)
            // =========================================================================
            
            // Si hay un choque frontal, O si toca cambio aleatorio (40%), buscamos nueva ruta.
            // **IMPORTANTE:** Este bloque SOLO se ejecuta si la Lógica de Persecución NO usó 'return'.
            if (choqueFrente || Math.random() < 0.4) {
                
                boolean direccionEncontrada = false;
                
                // Bucle de búsqueda aleatoria, prohibiendo el giro de 180°
                while (!direccionEncontrada) {
                    
                    int rand = (int) (Math.random() * 4);
                    int tempDX = 0, tempDY = 0;

                    if (rand == 0) { tempDX = -1; tempDY = 0; } // Izquierda
                    else if (rand == 1) { tempDX = 1; tempDY = 0; } // Derecha
                    else if (rand == 2) { tempDX = 0; tempDY = -1; } // Arriba
                    else if (rand == 3) { tempDX = 0; tempDY = 1; } //Abajo
                    
                    // RESTRICCIÓN: EVITAR DAR LA VUELTA (180 grados)
                    if (tempDX == -fantasmaDX && tempDY == -fantasmaDY) {
                        continue; // Reintentar con otro 'rand'
                    }

                    // 4. VERIFICAR LA NUEVA RUTA (que no sea un muro)
                    if (!esMuro(posX + tempDX, posY + tempDY)) {
                        fantasmaDX = tempDX;
                        fantasmaDY = tempDY;
                        direccionEncontrada = true;
                    }
                }
            }
        }
        
        // 5. MOVIMIENTO FÍSICO (Se ejecuta siempre, con o sin cambio de dirección)
        fantasmaX = fantasmaX + (fantasmaDX * VELOCIDAD);
        fantasmaY = fantasmaY + (fantasmaDY * VELOCIDAD);
    }

    boolean pacmanVisible(int fx, int fy, int px, int py) { 
        // Comprueba alineación horizontal
        if(fy == py){
            int minX = Math.min(fx, px);
            int maxX = Math.max(fx, px);

            // Itera sobre las casillas X intermedias para buscar muros
            for(int x = minX + 1; x < maxX; x++){
                if(esMuro(x, fy)){
                    return false; // Muro encontrado: NO visible
                }
            }
            return true; // No hay muros: SÍ visible
        }
        
        // Comprueba alineación vertical
        if(fx==px){
            int minY = Math.min(fy, py);
            int maxY = Math.max(fy, py);

            // Itera sobre las casillas Y intermedias para buscar muros
            // CORRECCIÓN: Se usa 'y' como índice del bucle.
            for(int y = minY + 1; y < maxY; y++){
                if(esMuro(fx, y)){
                    return false; // Muro encontrado: NO visible
                }
            }
            return true; // No hay muros: SÍ visible
        }
        
        // No están alineados (ni horizontal ni vertical)
        return false;
    }

    // Método auxiliar para leer el mapa
    private boolean esMuro(int x, int y) {
        // PROTECCIÓN: Si las coordenadas están fuera de los límites del tablero (<0 o >15)
        // decimos que es un muro para que nadie se salga de la pantalla.
        if (x < 0 || x >= NUM_BLOQUES || y < 0 || y >= NUM_BLOQUES) return true;
        
        // LECTURA DEL MAPA:
        // Convertimos X,Y a índice lineal (formula: y * ancho + x).
        // Si el valor en el array es 1, retorna true (Es Muro).
        return (datosPantalla[y * NUM_BLOQUES + x] == 1);
    }
    
    // Método para ver si perdiste
    private void chequearColisiones() {
        // Usamos Math.abs (Valor Absoluto) para calcular la distancia positiva.
        // Si la distancia horizontal es menor a 15px Y la vertical menor a 15px...
        // significa que los cuadrados de las imágenes se están solapando.
        if (Math.abs(pacmanX - fantasmaX) < 15 && Math.abs(pacmanY - fantasmaY) < 15) {
            
            // ¡Contacto! Detenemos el juego.
            juegoEnCurso = false;
            musica.morir(rutaMorir, -20);
        }
    }

    // AQUÍ OCURRE LA MAGIA DEL DIBUJADO Y ESCALADO
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. Guardar el estado original del "lápiz"
        AffineTransform transformacionOriginal = g2d.getTransform();

        // 2. Calcular el factor de escala y el centrado
        double escalaX = (double) getWidth() / TAMAÑO_JUEGO;
        double escalaY = (double) getHeight() / TAMAÑO_JUEGO;
        // Usamos la menor escala para mantener el aspecto cuadrado sin deformar
        double escala = Math.min(escalaX, escalaY);

        // Calculamos cuánto hay que mover el tablero para centrarlo
        int offsetX = (int) ((getWidth() - (TAMAÑO_JUEGO * escala)) / 2);
        int offsetY = (int) ((getHeight() - (TAMAÑO_JUEGO * escala)) / 2);

        // 3. Aplicar la transformación: primero mover, luego estirar
        g2d.translate(offsetX, offsetY);
        g2d.scale(escala, escala);

        // --- DIBUJAR EL JUEGO (Usando las coordenadas lógicas originales) ---
        // Todo esto se dibuja "pensando" que el juego es de 360x360,
        // pero Java lo escala automáticamente.
        
        g2d.setColor(Color.BLUE);
        for (int y = 0; y < NUM_BLOQUES; y++) {
            for (int x = 0; x < NUM_BLOQUES; x++) {
                int i = y * NUM_BLOQUES + x;
                
                if (datosPantalla[i] == 1) {
                    g2d.fillRect(x * TAMAÑO_BLOQUE_BASE, y * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE);
                } 
                else if (datosPantalla[i] == 0) {
                    g2d.setColor(Color.WHITE);
                    // Puntos centrados
                    g2d.fillRect(x * TAMAÑO_BLOQUE_BASE + 10, y * TAMAÑO_BLOQUE_BASE + 10, 4, 4);
                    g2d.setColor(Color.BLUE);
                }
                // NUEVO: Dibujar la CEREZA si el mapa dice '3'
                else if (datosPantalla[i] == 3) {
                    // Dibujamos la imagen de la cereza en la posición del bloque
                    // El '+2' es para centrarla visualmente igual que haces con Pacman
                    g2d.drawImage(cereza, x * TAMAÑO_BLOQUE_BASE + 2, y * TAMAÑO_BLOQUE_BASE + 2, this);
                }
            }
        }

        if (juegoEnCurso) {
            // --- NUEVO: CENTRADO VISUAL ---
            // Añadimos un pequeño desplazamiento (+2, +2) al dibujar los sprites
            // para que se vean visualmente centrados en los pasillos.
            g2d.drawImage(imagenActualPacman, pacmanX + 2, pacmanY + 2, this);
            g2d.drawImage(fantasma, fantasmaX + 2, fantasmaY + 2, this);
            
            // MODIFICADO: He comentado esta línea porque ahora la cereza se dibuja sola en el bucle de arriba
            // g2d.drawImage(cereza, 1 * TAMAÑO_BLOQUE_BASE + 2, 13 * TAMAÑO_BLOQUE_BASE + 2, this);
        }

        // 4. Restaurar el "lápiz" original para dibujar textos fuera del tablero escalado
        g2d.setTransform(transformacionOriginal);

        // Dibujar textos en la parte inferior de la ventana real
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Puntaje: " + puntaje, 20, getHeight() - 20);

        if (!juegoEnCurso) {
             g2d.setColor(Color.RED);
             g2d.drawString("GAME OVER - Espacio para reiniciar", getWidth()/2 - 150, getHeight()/2);
        }
    }

    // CONTROLES
    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (juegoEnCurso) {
                if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) { 
                    reqDX = -1; reqDY = 0; 
                } 
                else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) { 
                    reqDX = 1; reqDY = 0; 
                } 
                else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) { 
                    reqDX = 0; reqDY = -1; 
                } 
                else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) { 
                    reqDX = 0; reqDY = 1; 
                }
            } else {
                if (key == KeyEvent.VK_SPACE) {
                    juegoEnCurso = true;
                    puntaje = 0;
                    iniciarJuego();
                }
            }
        }
    }
}