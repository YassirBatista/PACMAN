import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform; 
import javax.swing.*;
import java.util.ArrayList; 

public class Tablero extends JPanel implements ActionListener {

    private final int TAMAÑO_BLOQUE_BASE = 24;                              //* Pixeles de los bloques
    private final int NUM_BLOQUES = 15;                                     //* cantidad de cuadrados del mapa
    private final int TAMAÑO_JUEGO = NUM_BLOQUES * TAMAÑO_BLOQUE_BASE;      //* Total de pixeles en el mapa
    private final int VELOCIDAD = 4;                                        //* Velocidad del juego

    // IMÁGENES
    private Image pacmanArriba, pacmanAbajo, pacmanIzquierda, pacmanDerecha; 
    private Image fantasmaAzul, fantasmaRosa, fantasmaRojo, fantasmaNaranja, cereza;
    private Image fantasmaAsustado; 
    private Image imagenActualPacman; 

    // VARIABLES PACMAN
    private int pacmanX, pacmanY;           //* Posicion X,Y de pacman      
    private int pacmanDX, pacmanDY;         //* Dirreccion de movimiento  (-1, 0, 1)
    private int reqDX, reqDY;               //* Memroia donde guardamos hacia donde queriamos ir
    
    // VARIABLES FANTASMAS (Lista)
    private ArrayList<Fantasma> fantasmas;  //* Lista que los contiene

    // ESTADO DEL JUEGO
    private int puntaje = 0;
    private int nivelActual = 1;            //? Nivel del mapa
    private int vidas = 3;                  //? Vidas de Pacman       
    private boolean juegoEnCurso = true;    //* Determina si se sigue jugando
    private boolean juegoGanado = false;    //* Se vuelve True si se termina todos los niveles
    private Timer timer;                    //* Ejecuta el juego cada 30 mls

    // LISTA DE OBJETOS /  Frutas cereza
    private ArrayList<Elemento> elementos;

    // VARIABLES DEL MODO CAZA
    private boolean modoCaza = false;       //* Para saber si estan asustados o no
    private int tiempoModoCaza = 0;         //* Contador que irá bajando hasta 0
    private final int DURACION_CAZA = 250;  //* 250 ciclos * 40ms (velocidad del timer) = 10 segundos de poder

    // RUTAS DE SONIDO
    String rutaComer = "/assets/sounds/comer.wav";
    String rutaMorir = "/assets/sounds/pacman_death.wav";

    // MAPA EN MEMORIA
    private short[] datosPantalla;  //! Arreglo que indica cada posicion del mapa

    //? Boton para volver al menu
    private JButton btnVolver;

    // --- CONSTRUCTOR ---
    public Tablero() {
        //? Usamos layout nulo para poder usar setBounds en el boton
        setLayout(null);

        cargarImagenes();
        iniciarJuego();
        addKeyListener(new Controlador(this)); 
        setFocusable(true);
        setBackground(Color.BLACK);

        crearBotonVolver(); //* Agregamos el boton a la pantalla
    }

    //? METODO NUEVO: Crea y configura el boton de salir
    private void crearBotonVolver() {
        btnVolver = new JButton("Volver");
        btnVolver.setBounds(500, 10, 80, 30); //? Coordenadas (X, Y, Ancho, Alto)
        
        //? Estilo visual tipo Pacman
        btnVolver.setBackground(new Color(0xffeb3b)); // Amarillo
        btnVolver.setForeground(Color.BLACK);
        btnVolver.setFont(new Font("Arial", Font.BOLD, 12));
        btnVolver.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        
        //! Esto evita que el boton robe el foco del teclado
        //! Si no pone esto, al hacer clic, Pacman dejara de moverse.
        btnVolver.setFocusable(false); 

        //* Accion al hacer clic
        btnVolver.addActionListener(e -> {
            timer.stop(); //* Pausamos el juego
            
            //? Buscamos la ventana principal y llamamos al metodo de volver
            JFrame ventanaActual = (JFrame) SwingUtilities.getWindowAncestor(this);
            JuegoPacman.volverAlMenu(ventanaActual);
        });

        this.add(btnVolver);
    }

    // --- MÉTODOS PÚBLICOS ---
    public boolean isJuegoEnCurso() { return juegoEnCurso; } //* El curso retorna si el juego esta en curso.
    
    public void setDireccion(int x, int y) { //! Guarda la direccion que solicita el jugador.
        this.reqDX = x;
        this.reqDY = y;
    }
    
    public void reiniciarJuego() {
        juegoEnCurso = true;
        juegoGanado = false;
        puntaje = 0;
        iniciarJuego(); 
    }

    private void cargarImagenes() { //? Simplemente guardamos las direcciones, en una variables para buscarlas individualmente las imagines.
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

    //? Solo mueve a los personajes al inicio, NO borra el mapa
    private void resetearPosiciones() {
        pacmanX = 7 * TAMAÑO_BLOQUE_BASE; 
        pacmanY = 11 * TAMAÑO_BLOQUE_BASE;
        pacmanDX = 0; pacmanDY = 0; 
        reqDX = 0; reqDY = 0; 

        // Reiniciamos los fantasmas a sus posiciones originales
        crearFantasmas(); 
        
        timer.start(); // Reanudamos el juego
    }

    private void crearFantasmas() {
        fantasmas = new ArrayList<>();
        
        // Constructor: (x, y, tamaño, num, TIPO, ESPERA, imagen1, imagen2)
        
        // 1. Rojo (Agresivo): Espera 0 (Sale de inmediato)
        fantasmas.add(new Fantasma(7 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, NUM_BLOQUES, 0, 0, fantasmaRojo, fantasmaAsustado)); 
        
        // 2. Rosa (Medio): Espera 50 ciclos (aprox 2 seg)
        //fantasmas.add(new Fantasma(6 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, NUM_BLOQUES, 1, 50, fantasmaRosa, fantasmaAsustado)); 
        
        // 3. Azul (Aleatorio): Espera 100 ciclos (aprox 4 seg)
        //fantasmas.add(new Fantasma(8 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, NUM_BLOQUES, 2, 100, fantasmaAzul, fantasmaAsustado)); 
        
        // 4. Naranja (Aleatorio): Espera 150 ciclos (aprox 6 seg)
        //fantasmas.add(new Fantasma(7 * TAMAÑO_BLOQUE_BASE, 6 * TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE, NUM_BLOQUES, 2, 150, fantasmaNaranja, fantasmaAsustado)); 
    }

    private void cargarNivel() {
        if (timer != null) timer.stop(); //! Detenemos el reloj anterior por seguridad para evitar que se multiplique la velocidad.
        
        //* Reiniciamos la posición y movimiento de Pacman según el nivel
        if (nivelActual == 1) {
            pacmanX = 7 * TAMAÑO_BLOQUE_BASE; 
            pacmanY = 11 * TAMAÑO_BLOQUE_BASE;   // POSICIÓN NIVEL 1
        } 
        else if (nivelActual == 2) {
            pacmanX = 7 * TAMAÑO_BLOQUE_BASE; 
            pacmanY = 12 * TAMAÑO_BLOQUE_BASE;   // POSICIÓN NIVEL 2
        } 
        else {
            //* Si en el futuro agregas más niveles, puedes ajustar la posición default aquí
            pacmanX = 7 * TAMAÑO_BLOQUE_BASE; 
            pacmanY = 11 * TAMAÑO_BLOQUE_BASE;   // DEFAULT
        }

        //* Reiniciamos dirección y movimiento
        pacmanDX = 0; 
        pacmanDY = 0; 
        reqDX = 0; 
        reqDY = 0; 

        crearFantasmas();   //? Creamos los 4 Fantasmas de nuevo

        juegoEnCurso = true;    //* Activamos el juego
        modoCaza = false;       //* Desactivamos el poder si lo tenia activo

        //* Preparamos las listas y arrays para el nuevo mapa
        elementos = new ArrayList<>();
        datosPantalla = new short[NUM_BLOQUES * NUM_BLOQUES]; 

        short[] mapaBase = Mapa.obtenerNivel(nivelActual); //? Pedimos el diseño del nivel actual

        //! Si el mapa es null, significa que ya no hay mas niveles (Victoria)
        if (mapaBase == null) {
            timer.stop();
            juegoEnCurso = false;
            
            JOptionPane.showMessageDialog(this, "FELICIDADES!\nHas completado todos los niveles.", "Victoria", JOptionPane.INFORMATION_MESSAGE);
            
            //* Guardamos el record tambien al ganar
            guardarRecord(); 
            return;
        }

        //* Recorremos todo el mapa para colocar muros y crear los puntos
        for (int i = 0; i < mapaBase.length; i++) {
            int xGrid = i % NUM_BLOQUES;
            int yGrid = i / NUM_BLOQUES;

            if (mapaBase[i] == 0) {
                elementos.add(new Elemento(xGrid, yGrid, TAMAÑO_BLOQUE_BASE, 0, null)); //? Creamos el punto como objeto
                datosPantalla[i] = 2; //* Marcamos el camino como transitable en la logica
            } else {
                datosPantalla[i] = mapaBase[i]; //? Copiamos muros (1) o vacios (2) tal cual
            }
        }

        //* Agregamos las cerezas extra si la imagen cargo bien
        if (cereza != null) {
            elementos.add(new Elemento(1, 13, TAMAÑO_BLOQUE_BASE, 1, cereza));
            elementos.add(new Elemento(11, 7, TAMAÑO_BLOQUE_BASE, 1, cereza));
        }

        timer = new Timer(40, this); //? Configuramos la velocidad (40ms = 25 FPS)
        timer.start();               //* Arrancamos el juego
    } 

    @Override
    public void actionPerformed(ActionEvent e) {
        //* Si el juego esta en curso, ejecutamos la logica del movimiento
        if (juegoEnCurso) {
            moverPacman();
            
            //? Bucle clasico: Recorremos la lista de fantasmas desde el 0 hasta el ultimo
            for (int i = 0; i < fantasmas.size(); i++) {
                Fantasma f = fantasmas.get(i); //* Sacamos el fantasma guardado en la posicion 'i'
                f.mover(pacmanX, pacmanY, datosPantalla, modoCaza); //* Le decimos a ese fantasma que se mueva
            }
            
            chequearColisiones(); //* Verificamos choques despues de mover a todos
            
            //? Gestion del tiempo de poder (si comimos una cereza)
            if (modoCaza) {
                tiempoModoCaza--; //* Restamos 1 al contador de tiempo
                
                if (tiempoModoCaza <= 0) {
                    modoCaza = false; //* Se acabo el tiempo, volvemos a la normalidad
                    System.out.println("Modo caza terminado.");
                }
            }
        }
        repaint(); //* Redibujamos la pantalla con las nuevas posiciones
    }

    private void moverPacman() {
        //* Pac-Man se mueve pixel a pixel, pero solo decidimos giros cuando esta centrado en un bloque
        if (pacmanX % TAMAÑO_BLOQUE_BASE == 0 && pacmanY % TAMAÑO_BLOQUE_BASE == 0) {

            //? Convertimos coordenadas de pixeles a coordenadas del mapa (casillas)
            int posX = pacmanX / TAMAÑO_BLOQUE_BASE;
            int posY = pacmanY / TAMAÑO_BLOQUE_BASE;

            //* 1. Verificamos si el jugador solicito un giro con las teclas
            if (reqDX != 0 || reqDY != 0) {
                //! Si el bloque destino NO es un muro, aplicamos el giro
                if (!esMuro(posX + reqDX, posY + reqDY)) {
                    pacmanDX = reqDX; 
                    pacmanDY = reqDY;
                }
            }

            //* Verificamos si vamos a chocar contra un muro en la direccion actual
            if (esMuro(posX + pacmanDX, posY + pacmanDY)) {
                pacmanDX = 0; //! Si hay muro enfrente, nos detenemos
                pacmanDY = 0;
            }
        }
        
        //* Sumamos la velocidad a la posicion actual
        pacmanX += pacmanDX * VELOCIDAD;
        pacmanY += pacmanDY * VELOCIDAD;
        
        //* Cambiamos el sprite segun hacia donde nos movemos
        if (pacmanDX == 1) imagenActualPacman = pacmanDerecha;
        else if (pacmanDX == -1) imagenActualPacman = pacmanIzquierda;
        else if (pacmanDY == -1) imagenActualPacman = pacmanArriba;
        else if (pacmanDY == 1) imagenActualPacman = pacmanAbajo;
    }
    
    // Metodo auxiliar para leer el mapa
    private boolean esMuro(int x, int y) {
        if (x < 0 || x >= NUM_BLOQUES || y < 0 || y >= NUM_BLOQUES) return true; //! Si las coordenadas estan fuera de los limites del tablero (<0 o >15)
        return (datosPantalla[y * NUM_BLOQUES + x] == 1); //! Fila * Ancho + Columna) para buscar en el mapa; si vale 1 es Muro
    }

    private void chequearColisiones() {
        
        //? COLISION CON LOS FANTASMAS
        for (Fantasma f : fantasmas) { //* Calculamos la distancia absoluta entre Pacman y el Fantasma (menos de 15px es choque)
            if (Math.abs(pacmanX - f.getX()) < 15 && Math.abs(pacmanY - f.getY()) < 15) {
                
                if (modoCaza) { //* Nos comemos al fantasma
                    puntaje += 200;
                    musica.comer(rutaComer, -25);
                    f.setPosicion(7 * TAMAÑO_BLOQUE_BASE, 7 * TAMAÑO_BLOQUE_BASE); //* Lo mandamos a la casa
                } else { 
                    //! Si estamos en modo normal el fantasma nos mata y perdemos una vida
                    musica.morir(rutaMorir, -20);
                    vidas--;
                    
                    if (vidas > 0) { //* Si aun quedan vidas, pausamos y reiniciamos posiciones (sin borrar el mapa)
                        timer.stop(); 
                        resetearPosiciones();
                        System.out.println("Te quedan " + vidas + " vidas.");
                    } else {
                        //! GAME OVER
                        timer.stop();
                        juegoEnCurso = false;
                        
                        // ANTES: JOptionPane.showMessageDialog... y volverAlMenu
                        // AHORA: Llamamos a guardarRecord() que hace todo eso
                        musica.detener(); // Detenemos musica por si acaso
                        guardarRecord();
                    }
                }
            }
        }

        //? COLISION CON COMIDA
        for (Elemento e : elementos) { //* Si el elemento existe (es visible) y lo tocamos
            if (e.esVisible() && e.chequearColision(pacmanX, pacmanY, TAMAÑO_BLOQUE_BASE)) {
                
                if (e.getTipo() == 0) { //* Sumamos 10
                    puntaje += 10; 
                    musica.comer(rutaComer, -25);
                } 
                else if (e.getTipo() == 1) { //* Sumamos 500 y activamos el Poder
                    puntaje += 500;
                    musica.comer(rutaComer, -25);
                    modoCaza = true;
                    tiempoModoCaza = DURACION_CAZA;
                }
            }
        }

        boolean nivelLimpio = true;
        for (Elemento e : elementos) { //* Si encontramos AL MENOS UN objeto visible, el nivel no ha terminado
            if (e.esVisible()) {
                nivelLimpio = false;
                break;
            }
        }

        //! SI NIVEL LIMPIO ES TRUE, GANAMOS EL NIVEL
        if (nivelLimpio) { //* Si el nivel esta limpio, aumentamos el contador de nivel para pasar al siguiente
            nivelActual++; 
            cargarNivel();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // ESCALADO
        AffineTransform transformOriginal = g2d.getTransform(); //* Guardamos el estado original para restaurarlo al dibujar textos

        //? Calculamos cuanto debemos estirar el juego segun el tamaño de la ventana
        double escalaX = (double) getWidth() / TAMAÑO_JUEGO;
        double escalaY = (double) getHeight() / TAMAÑO_JUEGO;

        double escala = Math.min(escalaX, escalaY); //* Usamos la escala menor para mantener el juego cuadrado sin deformarlo

        //? Calculamos el margen necesario para centrar el tablero
        int offsetX = (int) ((getWidth() - (TAMAÑO_JUEGO * escala)) / 2);
        int offsetY = (int) ((getHeight() - (TAMAÑO_JUEGO * escala)) / 2);

        //* Aplicamos la transformacion: Mover al centro y luego hacer Zoom
        g2d.translate(offsetX, offsetY);
        g2d.scale(escala, escala);

        // MAPA
        g2d.setColor(Color.BLUE); //? Colocamos el color azul de los muros
        
        if (datosPantalla != null) { //! Proteccion: Verificamos que el mapa exista para evitar errores
            //* Recorremos todo el array del mapa
            for (int i = 0; i < datosPantalla.length; i++) {
                if (datosPantalla[i] == 1) {
                    //? Calculamos la posicion X,Y basada en el indice del array
                    int x = (i % NUM_BLOQUES) * TAMAÑO_BLOQUE_BASE;
                    int y = (i / NUM_BLOQUES) * TAMAÑO_BLOQUE_BASE;
                    g2d.fillRect(x, y, TAMAÑO_BLOQUE_BASE, TAMAÑO_BLOQUE_BASE); //* Dibujamos el muro
                }
            }
        }

        // OBJETOS
        if (juegoEnCurso) { //? Dibujamos los puntos y las cerezas
            if (elementos != null) {
                for (Elemento e : elementos) {
                    e.dibujar(g2d, this);
                }
            }
            
            //* Dibujamos a Pacman con un pequeño ajuste (+2) para centrarlo
            g2d.drawImage(imagenActualPacman, pacmanX + 2, pacmanY + 2, this);
            
            //? Dibujamos a los fantasmas y le pasamos el modoCaza para que sepan como se dibujan
            if (fantasmas != null) {
                for (Fantasma f : fantasmas) {
                    f.dibujar(g2d, this, modoCaza);
                }
            }
        }

        // La Interfaz de Usuario
        g2d.setTransform(transformOriginal); //! Restauramos la vista original para que el texto no se estire ni se mueva
        
        //? Los colores y fuentes dde los textos
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Puntaje: " + puntaje, 20, getHeight() - 20); //* Puntaje a la izquierda
        g2d.drawString("Nivel: " + nivelActual, getWidth() - 100, getHeight() - 20); //* Nivel a la derecha
        
        //? Dibujamos las vidas en rojo al centro
        g2d.setColor(Color.RED);
        g2d.drawString("Vidas: " + vidas, 200, getHeight() - 20);

        //* Pantalla de Fin de Juego o Victoria
        if (!juegoEnCurso) {
             g2d.setColor(Color.RED);
             String mensaje = juegoGanado ? "¡VICTORIA!" : "GAME OVER";
             
             //? Calculamos el ancho del texto para centrarlo
             FontMetrics fm = g2d.getFontMetrics();
             int anchoTexto = fm.stringWidth(mensaje);
             
             g2d.drawString(mensaje, (getWidth() - anchoTexto)/2, getHeight()/2);
        }
    }

    //? METODO AUXILIAR: Pide el nombre y guarda el record
    private void guardarRecord() {
        //* Pedimos el nombre al usuario con una ventana emergente
        String nombre = JOptionPane.showInputDialog(this, "Juego Terminado. Puntaje: " + puntaje + "\nIngresa tu nombre:");
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            Puntajes.guardarPuntaje(nombre, puntaje); //* Guardamos en el archivo
        }
        
        //? Despues de guardar, volvemos al menu
        JFrame ventanaActual = (JFrame) SwingUtilities.getWindowAncestor(this);
        JuegoPacman.volverAlMenu(ventanaActual);
    }
}