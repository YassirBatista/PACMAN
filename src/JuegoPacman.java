import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JuegoPacman extends JFrame {

    // --- 1. CONSTRUCTOR DEL JUEGO (Se ejecuta al dar clic en "Jugar") ---
    public JuegoPacman() {
        // Agregamos el Tablero (donde está toda la lógica del juego)
        add(new Tablero());

        setTitle("Pac-Man Java");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 700); 
        setLocationRelativeTo(null); // Centra la ventana en la pantalla
        setResizable(true);
        setVisible(true);

        // Activamos la música al iniciar la ventana del juego
        try {
            musica.reproducir("/assets/sounds/musica.wav", -40);
        } catch (Exception e) {
            System.out.println("Error al reproducir música: " + e.getMessage());
        }
    }

    // --- 2. PUNTO DE ENTRADA (Inicia el Menú primero) ---
    public static void main(String[] args) {
        mostrarMenu();
    }

    // --- 3. LÓGICA DEL MENÚ (Tu código de Prueba_2) ---
    public static void mostrarMenu() {
        JFrame ventanaMenu = new JFrame("Menú Principal");
        ventanaMenu.setSize(720, 576);
        ventanaMenu.setLayout(null);
        ventanaMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventanaMenu.setLocationRelativeTo(null); // Centrar el menú

        // --- ESTILO ---
        ventanaMenu.getContentPane().setBackground(new Color(0x001f3f)); // Fondo Azul Oscuro

        Font fuenteTitulo = new Font("Arial Black", Font.BOLD, 42);
        Font fuenteSub = new Font("Arial", Font.BOLD, 26);
        Font fuenteBoton = new Font("Arial", Font.BOLD, 18);

        // --- TEXTOS ---
        JLabel titulo = new JLabel("PAC-MAN");
        titulo.setFont(fuenteTitulo);
        titulo.setForeground(Color.WHITE);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setBounds(110, 20, 500, 50);

        JLabel opciones = new JLabel("Menú de Opciones");
        opciones.setFont(fuenteSub);
        opciones.setForeground(Color.LIGHT_GRAY);
        opciones.setHorizontalAlignment(SwingConstants.CENTER);
        opciones.setBounds(110, 80, 500, 40);

        // --- IMAGEN DEL MENÚ ---
        try {
            String rutaImagen = "PACMAN/src/assets/sprites/interfaz/pac.gif"; 

            ImageIcon pacmanGif = new ImageIcon(rutaImagen);
            JLabel imagenPacman = new JLabel(pacmanGif);
            
            // Ajuste de seguridad por si la imagen no carga para que no de error
            if (pacmanGif.getIconWidth() > 0) {
                imagenPacman.setBounds(40, 160, pacmanGif.getIconWidth(), pacmanGif.getIconHeight());
                ventanaMenu.add(imagenPacman);
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen del menú.");
        }

        // --- BOTONES ---
        Color amarilloPac = new Color(0xffeb3b);
        Color negro = Color.BLACK;

        // BOTÓN JUGAR
        JButton jugar = new JButton("Jugar");
        jugar.setBounds(340, 180, 150, 45);
        jugar.setFont(fuenteBoton);
        jugar.setBackground(amarilloPac);
        jugar.setForeground(negro);
        jugar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        jugar.setFocusPainted(false);

        // ACCIÓN: Cerrar menú y abrir juego
        jugar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ventanaMenu.dispose(); // Cierra la ventana del menú
                new JuegoPacman();     // Abre la ventana del juego
            }
        });

        // BOTÓN PUNTAJES
        JButton puntajes = new JButton("Puntajes");
        puntajes.setBounds(420, 240, 150, 45);
        puntajes.setFont(fuenteBoton);
        puntajes.setBackground(amarilloPac);
        puntajes.setForeground(negro);
        puntajes.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        puntajes.setFocusPainted(false);

        // BOTÓN SALIR
        JButton salir = new JButton("Salir");
        salir.setBounds(500, 300, 150, 45);
        salir.setFont(fuenteBoton);
        salir.setBackground(amarilloPac);
        salir.setForeground(negro);
        salir.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        salir.setFocusPainted(false);

        // ACCIÓN: Cerrar todo
        salir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // --- AGREGAR COMPONENTES AL MENÚ ---
        ventanaMenu.add(titulo);
        ventanaMenu.add(opciones);
        ventanaMenu.add(jugar);
        ventanaMenu.add(puntajes);
        ventanaMenu.add(salir);

        ventanaMenu.setVisible(true);
    }
}