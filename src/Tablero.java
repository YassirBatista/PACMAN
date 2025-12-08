import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform; 
import javax.swing.*;
import java.util.ArrayList; 

public class Tablero extends JPanel implements ActionListener {

    private final int TAMAÑO_BLOQUE_BASE = 24;                          //? Pixeles de cada bloque
    private final int NUM_BLOQUES = 15;                                 //? Tamano del mapa
    private final int TAMAÑO_JUEGO = NUM_BLOQUES * TAMAÑO_BLOQUE_BASE;  //? 
    private final int VELOCIDAD = 4;

    // IMÁGENES
    private Image pacmanArriba, pacmanAbajo, pacmanIzquierda, pacmanDerecha; 
    private Image fantasmaAzul, fantasmaRosa, fantasmaRojo, fantasmaNaranja, cereza;
    private Image fantasmaAsustado; // Imagen del fantasma vulnerable
    private Image imagenActualPacman; 

    // VARIABLES PACMAN
    private int pacmanX, pacmanY;       //* 
    private int pacmanDX, pacmanDY;     //*
    private int reqDX, reqDY;           //*
    
    // VARIABLES FANTASMA 
    private Fantasma miFantasma; 

    // ESTADO DEL JUEGO
    private int puntaje = 0;
    private boolean juegoEnCurso = true;
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
        addKeyListener(new Controlador(this)); // Controlador externo
        setFocusable(true);
        setBackground(Color.BLACK);
    }

    // --- MÉTODOS PÚBLICOS PARA EL CONTROLADOR ---
    public boolean isJuegoEnCurso() { return juegoEnCurso; }
    
    public void setDireccion(int x, int y) {
        this.reqDX = x;
        this.reqDY = y;
    }
    
    public void reiniciarJuego() {
        juegoEnCurso = true;
        puntaje = 0;
        iniciarJuego(); 
    }

    private void cargarImagenes() {
        String rutaBase = "/assets/sprites/pacman/";
        String rutaBaseFantasma = "/assets/sprites/ghosts/"; 

        try {
            // Cargamos los sprites de Pacman
            pacmanArriba = new ImageIcon(getClass().getResource(rutaBase + "pacmanUp.png")).getImage();
            pacmanAbajo = new ImageIcon(getClass().getResource(rutaBase + "pacmanDown.png")).getImage();
            pacmanIzquierda = new ImageIcon(getClass().getResource(rutaBase + "pacmanLeft.png")).getImage();
            pacmanDerecha = new ImageIcon(getClass().getResource(rutaBase + "pacmanRight.png")).getImage();
            
            // Sprites de los fantasmas
            fantasmaAzul = new ImageIcon(getClass().getResource(rutaBaseFantasma + "blueGhost.png")).getImage();
            fantasmaRosa = new ImageIcon(getClass().getResource(rutaBaseFantasma + "pinkghost.png")).getImage();
            fantasmaRojo = new ImageIcon(getClass().getResource(rutaBaseFantasma + "redGhost.png")).getImage();
            fantasmaNaranja = new ImageIcon(getClass().getResource(rutaBaseFantasma + "orangeGhost.png")).getImage();
            fantasmaAsustado = new ImageIcon(getClass().getResource(rutaBaseFantasma + "scaredGhost.png")).getImage();

            //Srpites de la cereza
            cereza = new ImageIcon(getClass().getResource(rutaBase + "cherry.png")).getImage();
            
        } catch (Exception e) {
            System.err.println("ERROR: No se pudo cargar una o más imágenes.");
            e.printStackTrace();
        }
        
        imagenActualPacman = pacmanDerecha; 
    }

    private void iniciarJuego() {
        if (timer != null) timer.stop(); //! Detenemos el juego para evitar que al reiniciar cuando morimos la velocidad se multiplique
        
        // 1. Reiniciar Pacman
        pacmanX = 7 * TAMAÑO_BLOQUE_BASE; 
        pacmanY = 11 * TAMAÑO_BLOQUE_BASE;
        pacmanDX = 0; pacmanDY = 0; 
        reqDX = 0; reqDY = 0; 

        // 2. Reiniciar Fantasma (Usando la Clase Fantasma)
        // Pasamos las imágenes cargadas al objeto
        miFantasma = new Fantasma(7 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, NUM_BLOQUES, fantasmaAzul, fantasmaAsustado);
        miFantasma = new Fantasma(7 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, NUM_BLOQUES, fantasmaRosa, fantasmaAsustado);
        miFantasma = new Fantasma(7 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, NUM_BLOQUES, fantasmaRojo, fantasmaAsustado);
        miFantasma = new Fantasma(7 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, NUM_BLOQUES, fantasmaNaranja, fantasmaAsustado);

        juegoEnCurso = true;
        puntaje = 0;
        modoCaza = false; 

        // 3. Cargar Mapa desde Mapa.java
        elementos = new ArrayList<>();
        datosPantalla = new short[NUM_BLOQUES * NUM_BLOQUES]; 

        short[] mapaBase = Mapa.obtenerNivelBase(); 

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

        // Agregamos las cerezas
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
            
            // EL FANTASMA SE MUEVE SOLO (IA en Fantasma.java)
            miFantasma.mover(pacmanX, pacmanY, datosPantalla, modoCaza);
            
            chequearColisiones();
            
            // Gestión del tiempo de poder
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
    
    // Método necesario para Pacman (El fantasma tiene el suyo propio dentro de su clase)
    private boolean esMuro(int x, int y) {
        if (x < 0 || x >= NUM_BLOQUES || y < 0 || y >= NUM_BLOQUES) return true;
        return (datosPantalla[y * NUM_BLOQUES + x] == 1);
    }

    private void chequearColisiones() {
        
        // 1. COLISIÓN CON EL FANTASMA (Usamos el objeto miFantasma)
        if (Math.abs(pacmanX - miFantasma.getX()) < 15 && Math.abs(pacmanY - miFantasma.getY()) < 15) {
            
            if (modoCaza) {
                // COMER FANTASMA
                puntaje += 200;
                musica.comer(rutaComer, -25);
                
                // Reiniciar al fantasma (usando su método setPosicion)
                miFantasma.setPosicion(7 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE);
                System.out.println("¡Fantasma comido!");
                
            } else {
                // MORIR
                juegoEnCurso = false;
                musica.morir(rutaMorir, -20);
            }
        }

        // 2. COLISIÓN CON COMIDA
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
                    System.out.println("¡MODO CAZA ACTIVADO!");
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. ESCALADO
        AffineTransform transformacionOriginal = g2d.getTransform();
        double escalaX = (double) getWidth() / TAMAÑO_JUEGO;
        double escalaY = (double) getHeight() / TAMAÑO_JUEGO;
        double escala = Math.min(escalaX, escalaY);
        int offsetX = (int) ((getWidth() - (TAMAÑO_JUEGO * escala)) / 2);
        int offsetY = (int) ((getHeight() - (TAMAÑO_JUEGO * escala)) / 2);

        g2d.translate(offsetX, offsetY);
        g2d.scale(escala, escala);

        // 2. DIBUJAR MAPA (Muros)
        g2d.setColor(Color.BLUE);
        for (int i = 0; i < datosPantalla.length; i++) {
            if (datosPantalla[i] == 1) {
                int x = (i % NUM_BLOQUES) * TAMAÑO_BLOQUE_BASE;
                int y = (i / NUM_BLOQUES) * TAMAÑO_BLOQUE_BASE;
                g2d.fillRect(x, y, TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE);
            }
        }

        // 3. DIBUJAR OBJETOS
        if (juegoEnCurso) {
            
            // A) Elementos (Puntos y Cerezas)
            if (elementos != null) {
                for (Elemento e : elementos) {
                    e.dibujar(g2d, this);
                }
            }

            // B) Pacman
            g2d.drawImage(imagenActualPacman, pacmanX + 2, pacmanY + 2, this);
            
            // C) Fantasma (Él decide cómo dibujarse según modoCaza)
            miFantasma.dibujar(g2d, this, modoCaza);
        }

        // 4. UI
        g2d.setTransform(transformacionOriginal);
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Puntaje: " + puntaje, 20, getHeight() - 20);

        if (!juegoEnCurso) {
             g2d.setColor(Color.RED);
             String mensaje = "GAME OVER - Espacio para reiniciar";
             FontMetrics fm = g2d.getFontMetrics();
             int anchoTexto = fm.stringWidth(mensaje);
             g2d.drawString(mensaje, (getWidth() - anchoTexto)/2, getHeight()/2);
        }
    }   
}