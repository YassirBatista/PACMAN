import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Controlador extends KeyAdapter {

    private Tablero tablero; // Referencia al tablero para poder controlarlo

    // Constructor: Recibe el tablero para saber a quién dar órdenes
    public Controlador(Tablero tablero) {
        this.tablero = tablero;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Le preguntamos al tablero si el juego está corriendo
        if (tablero.isJuegoEnCurso()) {
            
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
                tablero.setDireccion(-1, 0); // Ordenamos ir a la izquierda
            } 
            else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
                tablero.setDireccion(1, 0);
            } 
            else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
                tablero.setDireccion(0, -1);
            } 
            else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
                tablero.setDireccion(0, 1);
            }
            
        } else {
            // Si el juego terminó y presionan Espacio
            if (key == KeyEvent.VK_SPACE) {
                tablero.reiniciarJuego();
            }
        }
    }
}