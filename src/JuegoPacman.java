import javax.swing.JFrame;

public class JuegoPacman extends JFrame {

    public JuegoPacman() {
        add(new Tablero());

        setTitle("Pac-Man Java    ");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Tamaño inicial
        setSize(600, 700); 
        setLocationRelativeTo(null);
        
        setResizable(true);
        
        setVisible(true);
    }

    public static void main(String[] args) {
        new JuegoPacman();
        musica.reproducir("/assets/sounds/musica.wav", -40);
    }
}