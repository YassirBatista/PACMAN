import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform; 
import javax.swing.*;
import java.util.ArrayList; 

public class Tablero extends JPanel implements ActionListener {

    private final int TAMAÑO_BLOQUE_BASE = 24;
    private final int NUM_BLOQUES = 15;
    private final int TAMAÑO_JUEGO = NUM_BLOQUES * TAMAÑO_BLOQUE_BASE;  
    private final int VELOCIDAD = 4;

    // IMÁGENES
    private Image pacmanArriba, pacmanAbajo, pacmanIzquierda, pacmanDerecha; 
    private Image fantasmaAzul, fantasmaRosa, fantasmaRojo, fantasmaNaranja, cereza;
    private Image fantasmaAsustado; 
    private Image imagenActualPacman; 

    // VARIABLES PACMAN
    private int pacmanX, pacmanY;       
    private int pacmanDX, pacmanDY;     
    private int reqDX, reqDY;           
    
    // VARIABLES FANTASMAS (Lista)
    private ArrayList<Fantasma> fantasmas; 

    // ESTADO DEL JUEGO
    private int puntaje = 0;
    private int nivelActual = 1;  
    private int vidas = 3;               //? NUEVO: Vidas del Pacman       
    private boolean juegoEnCurso = true;
    private boolean juegoGanado = false; 
    private Timer timer;

    // LISTA DE OBJETOS
    private ArrayList<Elemento> elementos;

    // VARIABLES DEL MODO CAZA
    private boolean modoCaza = false;     
    private int tiempoModoCaza = 0;       
    private final int DURACION_CAZA = 250; 

    // RUTAS DE SONIDO
    String rutaComer = "/assets/sounds/comer.wav";
    String rutaMorir = "/assets/sounds/pacman_death.wav";

    // MAPA EN MEMORIA
    private short[] datosPantalla; 

    // --- CONSTRUCTOR ---
    public Tablero() {
        cargarImagenes();
        iniciarJuego();
        addKeyListener(new Controlador(this)); 
        setFocusable(true);
        setBackground(Color.BLACK);
    }

    // --- MÉTODOS PÚBLICOS ---
    public boolean isJuegoEnCurso() { return juegoEnCurso; }
    
    public void setDireccion(int x, int y) {
        this.reqDX = x;
        this.reqDY = y;
    }
    
    public void reiniciarJuego() {
        juegoEnCurso = true;
        juegoGanado = false;
        puntaje = 0;
        iniciarJuego(); 
    }

    private void cargarImagenes() {
        String rutaBase = "/assets/sprites/pacman/";
        String rutaBaseFantasma = "/assets/sprites/ghosts/"; 

        try {
            pacmanArriba = new ImageIcon(getClass().getResource(rutaBase + "pacmanUp.png")).getImage();
            pacmanAbajo = new ImageIcon(getClass().getResource(rutaBase + "pacmanDown.png")).getImage();
            pacmanIzquierda = new ImageIcon(getClass().getResource(rutaBase + "pacmanLeft.png")).getImage();
            pacmanDerecha = new ImageIcon(getClass().getResource(rutaBase + "pacmanRight.png")).getImage();
            
            // Fantasmas
            fantasmaAzul = new ImageIcon(getClass().getResource(rutaBaseFantasma + "blueGhost.png")).getImage();
            fantasmaRosa = new ImageIcon(getClass().getResource(rutaBaseFantasma + "pinkGhost.png")).getImage();   
            fantasmaRojo = new ImageIcon(getClass().getResource(rutaBaseFantasma + "redGhost.png")).getImage();     
            fantasmaNaranja = new ImageIcon(getClass().getResource(rutaBaseFantasma + "orangeGhost.png")).getImage(); 
            
            fantasmaAsustado = new ImageIcon(getClass().getResource(rutaBaseFantasma + "scaredGhost.png")).getImage(); 
            cereza = new ImageIcon(getClass().getResource(rutaBase + "cherry.png")).getImage();
            
        } catch (Exception e) {
            System.err.println("ERROR: No se pudo cargar una imagen.");
        }
        
        imagenActualPacman = pacmanDerecha; 
    }

    private void iniciarJuego() {
        nivelActual = 1;
        puntaje = 0;
        vidas = 3; //? Reiniciamos vidas al empezar juego nuevo
        cargarNivel(); 
    }

    //? Método nuevo: Solo mueve a los personajes al inicio, NO borra el mapa
    private void resetearPosiciones() {
        pacmanX = 7 * TAMAÑO_BLOQUE_BASE; 
        pacmanY = 11 * TAMAÑO_BLOQUE_BASE;
        pacmanDX = 0; pacmanDY = 0; 
        reqDX = 0; reqDY = 0; 

        // Reiniciamos los fantasmas a sus posiciones originales
        // (Nota: Esto los recrea, si quisieras solo moverlos, habría que agregar setPosicion a Fantasma)
        crearFantasmas(); 
        
        timer.start(); // Reanudamos el juego
    }

    private void crearFantasmas() {
        fantasmas = new ArrayList<>();
        
        // Constructor: (x, y, tamaño, num, TIPO, ESPERA, imagen1, imagen2)
        
        // 1. Rojo (Agresivo): Espera 0 (Sale de inmediato)
        fantasmas.add(new Fantasma(7 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, NUM_BLOQUES, 0, 0, fantasmaRojo, fantasmaAsustado)); 
        
        // 2. Rosa (Medio): Espera 50 ciclos (aprox 2 seg)
        fantasmas.add(new Fantasma(6 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, NUM_BLOQUES, 1, 50, fantasmaRosa, fantasmaAsustado)); 
        
        // 3. Azul (Aleatorio): Espera 100 ciclos (aprox 4 seg)
        fantasmas.add(new Fantasma(8 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, NUM_BLOQUES, 2, 100, fantasmaAzul, fantasmaAsustado)); 
        
        // 4. Naranja (Aleatorio): Espera 150 ciclos (aprox 6 seg)
        fantasmas.add(new Fantasma(7 * TAMAÑO_BLOQUE_BASE, 6 * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, NUM_BLOQUES, 2, 150, fantasmaNaranja, fantasmaAsustado)); 
    }

    private void cargarNivel() {
        if (timer != null) timer.stop();
        
        // 1. Posiciones iniciales
        pacmanX = 7 * TAMAÑO_BLOQUE_BASE; 
        pacmanY = 11 * TAMAÑO_BLOQUE_BASE;
        pacmanDX = 0; pacmanDY = 0; 
        reqDX = 0; reqDY = 0; 

        // 2. Crear Fantasmas
        crearFantasmas();

        juegoEnCurso = true;
        modoCaza = false; 

        // 3. Cargar Mapa
        elementos = new ArrayList<>();
        datosPantalla = new short[NUM_BLOQUES * NUM_BLOQUES]; 

        short[] mapaBase = Mapa.obtenerNivel(nivelActual); 

        if (mapaBase == null) {
            juegoEnCurso = false;
            juegoGanado = true;
            repaint();
            return;
        }

        for (int i = 0; i < mapaBase.length; i++) {
            int xGrid = i % NUM_BLOQUES;
            int yGrid = i / NUM_BLOQUES;

            if (mapaBase[i] == 0) {
                elementos.add(new Elemento(xGrid, yGrid, TAMAÑO_BLOQUE_BASE, 0, null));
                datosPantalla[i] = 2; 
            } else {
                datosPantalla[i] = mapaBase[i];
            }
        }

        // 4. Agregar Cerezas
        if (cereza != null) {
            elementos.add(new Elemento(1, 13, TAMAÑO_BLOQUE_BASE, 1, cereza));
            elementos.add(new Elemento(11, 7, TAMAÑO_BLOQUE_BASE, 1, cereza));
        }

        timer = new Timer(40, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (juegoEnCurso) {
            moverPacman();
            
            for (Fantasma f : fantasmas) {
                f.mover(pacmanX, pacmanY, datosPantalla, modoCaza);
            }
            
            chequearColisiones();
            
            if (modoCaza) {
                tiempoModoCaza--; 
                if (tiempoModoCaza <= 0) {
                    modoCaza = false; 
                    System.out.println("Modo caza terminado.");
                }
            }
        }
        repaint();
    }

    private void moverPacman() {
        if (pacmanX % TAMAÑO_BLOQUE_BASE == 0 && pacmanY % TAMAÑO_BLOQUE_BASE == 0) {
            int posX = pacmanX / TAMAÑO_BLOQUE_BASE;
            int posY = pacmanY / TAMAÑO_BLOQUE_BASE;

            if (reqDX != 0 || reqDY != 0) {
                if (!esMuro(posX + reqDX, posY + reqDY)) {
                    pacmanDX = reqDX;
                    pacmanDY = reqDY;
                }
            }

            if (esMuro(posX + pacmanDX, posY + pacmanDY)) {
                pacmanDX = 0;
                pacmanDY = 0;
            }
        }
        pacmanX += pacmanDX * VELOCIDAD;
        pacmanY += pacmanDY * VELOCIDAD;
        
        if (pacmanDX == 1) imagenActualPacman = pacmanDerecha;
        else if (pacmanDX == -1) imagenActualPacman = pacmanIzquierda;
        else if (pacmanDY == -1) imagenActualPacman = pacmanArriba;
        else if (pacmanDY == 1) imagenActualPacman = pacmanAbajo;
    }
    
    private boolean esMuro(int x, int y) {
        if (x < 0 || x >= NUM_BLOQUES || y < 0 || y >= NUM_BLOQUES) return true;
        return (datosPantalla[y * NUM_BLOQUES + x] == 1);
    }

    private void chequearColisiones() {
        
        // 1. FANTASMAS
        for (Fantasma f : fantasmas) {
            if (Math.abs(pacmanX - f.getX()) < 15 && Math.abs(pacmanY - f.getY()) < 15) {
                if (modoCaza) {
                    // Comer fantasma
                    puntaje += 200;
                    musica.comer(rutaComer, -25);
                    f.setPosicion(7 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE); 
                } else {
                    //? TE HAN MATADO
                    musica.morir(rutaMorir, -20);
                    vidas--; // Restamos una vida
                    
                    if (vidas > 0) {
                        // Si quedan vidas, solo reseteamos posiciones y continuamos
                        timer.stop(); // Pausa breve
                        resetearPosiciones();
                        System.out.println("Te quedan " + vidas + " vidas.");
                    } else {
                        // Si no quedan vidas, Game Over
                        juegoEnCurso = false;
                    }
                }
            }
        }

        // 2. COMIDA
        for (Elemento e : elementos) {
            if (e.esVisible() && e.chequearColision(pacmanX, pacmanY, TAMAÑO_BLOQUE_BASE)) {
                if (e.getTipo() == 0) { // Punto
                    puntaje += 10;
                    musica.comer(rutaComer, -25);
                } 
                else if (e.getTipo() == 1) { // Cereza
                    puntaje += 500;
                    musica.comer(rutaComer, -25);
                    modoCaza = true;
                    tiempoModoCaza = DURACION_CAZA;
                }
            }
        }

        // 3. FIN DE NIVEL
        boolean nivelLimpio = true;
        for (Elemento e : elementos) {
            if (e.esVisible()) {
                nivelLimpio = false;
                break;
            }
        }

        if (nivelLimpio) {
            nivelActual++; 
            cargarNivel(); 
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. ESCALADO
        AffineTransform transformOriginal = g2d.getTransform();
        double escalaX = (double) getWidth() / TAMAÑO_JUEGO;
        double escalaY = (double) getHeight() / TAMAÑO_JUEGO;
        double escala = Math.min(escalaX, escalaY);
        int offsetX = (int) ((getWidth() - (TAMAÑO_JUEGO * escala)) / 2);
        int offsetY = (int) ((getHeight() - (TAMAÑO_JUEGO * escala)) / 2);

        g2d.translate(offsetX, offsetY);
        g2d.scale(escala, escala);

        // 2. MAPA
        g2d.setColor(Color.BLUE);
        if (datosPantalla != null) {
            for (int i = 0; i < datosPantalla.length; i++) {
                if (datosPantalla[i] == 1) {
                    int x = (i % NUM_BLOQUES) * TAMAÑO_BLOQUE_BASE;
                    int y = (i / NUM_BLOQUES) * TAMAÑO_BLOQUE_BASE;
                    g2d.fillRect(x, y, TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE);
                }
            }
        }

        // 3. OBJETOS
        if (juegoEnCurso) {
            if (elementos != null) {
                for (Elemento e : elementos) {
                    e.dibujar(g2d, this);
                }
            }
            g2d.drawImage(imagenActualPacman, pacmanX + 2, pacmanY + 2, this);
            
            if (fantasmas != null) {
                for (Fantasma f : fantasmas) {
                    f.dibujar(g2d, this, modoCaza);
                }
            }
        }

        // 4. UI
        g2d.setTransform(transformOriginal);
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Puntaje: " + puntaje, 20, getHeight() - 20);
        g2d.drawString("Nivel: " + nivelActual, getWidth() - 100, getHeight() - 20);
        
        //? Dibujamos las vidas
        g2d.setColor(Color.RED);
        g2d.drawString("Vidas: " + vidas, 200, getHeight() - 20);

        if (!juegoEnCurso) {
             g2d.setColor(Color.RED);
             String mensaje = juegoGanado ? "¡VICTORIA!" : "GAME OVER";
             FontMetrics fm = g2d.getFontMetrics();
             int anchoTexto = fm.stringWidth(mensaje);
             g2d.drawString(mensaje, (getWidth() - anchoTexto)/2, getHeight()/2);
        }
    }   
}