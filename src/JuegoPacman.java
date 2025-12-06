import javax.swing.JFrame;

public class JuegoPacman extends JFrame {

    public JuegoPacman() {
        add(new Tablero());

        setTitle("Pac-Man Java - Redimensionable");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Tamaño inicial
        setSize(400, 450); 
        setLocationRelativeTo(null);
        
        setResizable(true);
        
        setVisible(true);
    }

    public static void main(String[] args) {
        new JuegoPacman();
        //musica.reproducir("D:\\Documentos\\Universidad\\Programacion3\\Pcman\\Pac\\Pacman\\musica.wav", -10);
    }
}