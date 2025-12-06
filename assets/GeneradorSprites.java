import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GeneradorSprites {

    // Tamaño de los sprites (32x32 es estándar para aprender)
    private static final int SIZE = 32;

    public static void main(String[] args) {
        try {
            System.out.println("Generando sprites...");

            // --- PACMAN ---
            Color pacmanColor = Color.YELLOW;
            // Pacman Right (Boca abierta derecha)
            generarPacman("pacmanRight.png", pacmanColor, 45); 
            // Pacman Left (Boca abierta izquierda)
            generarPacman("pacmanLeft.png", pacmanColor, 225);
            // Pacman Up (Boca abierta arriba)
            generarPacman("pacmanUp.png", pacmanColor, 135);
            // Pacman Down (Boca abierta abajo)
            generarPacman("pacmanDown.png", pacmanColor, 315);
            // Pacman Closed (Círculo completo)
            generarPacman("pacmanClosed.png", pacmanColor, 0, 360);

            // --- GHOSTS ---
            // Forma básica de fantasma (cuerpo + pies)
            generarFantasma("ghostRed.png", Color.RED);
            generarFantasma("ghostPink.png", Color.PINK);
            generarFantasma("ghostBlue.png", Color.CYAN); // Inky
            generarFantasma("ghostOrange.png", Color.ORANGE);
            generarFantasma("ghostScared.png", new Color(0, 0, 139)); // Azul oscuro

            // --- EYES ---
            generarOjos("ghostEyesUp.png", 0, -4);
            generarOjos("ghostEyesDown.png", 0, 4);
            generarOjos("ghostEyesLeft.png", -4, 0);
            generarOjos("ghostEyesRight.png", 4, 0);

            System.out.println("¡Éxito! 14 Imagenes generadas en la carpeta del proyecto.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para dibujar a Pacman
    private static void generarPacman(String nombre, Color color, int startAngle) throws Exception {
        generarPacman(nombre, color, startAngle, 270); // 270 grados es la boca abierta estándar
    }

    private static void generarPacman(String nombre, Color color, int startAngle, int arcAngle) throws Exception {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        
        // Configuración para bordes suaves (Antialiasing)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(color);
        g2d.fillArc(2, 2, SIZE-4, SIZE-4, startAngle, arcAngle);
        
        g2d.dispose();
        ImageIO.write(img, "png", new File(nombre));
    }

    // Método para dibujar Fantasmas
    private static void generarFantasma(String nombre, Color color) throws Exception {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(color);
        // Cabeza redonda
        g2d.fillArc(4, 4, SIZE-8, SIZE-8, 0, 180);
        // Cuerpo cuadrado
        g2d.fillRect(4, SIZE/2, SIZE-8, SIZE/2 - 4);
        
        // Ojos (blanco y pupila) básicos
        g2d.setColor(Color.WHITE);
        g2d.fillOval(8, 10, 6, 8);
        g2d.fillOval(18, 10, 6, 8);
        g2d.setColor(Color.BLUE);
        g2d.fillOval(10, 12, 2, 2);
        g2d.fillOval(20, 12, 2, 2);

        g2d.dispose();
        ImageIO.write(img, "png", new File(nombre));
    }

    // Método para los Ojos solos (cuando el fantasma muere)
    private static void generarOjos(String nombre, int offsetX, int offsetY) throws Exception {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Ojos Blancos
        g2d.setColor(Color.WHITE);
        g2d.fillOval(6, 10, 8, 10);
        g2d.fillOval(18, 10, 8, 10);

        // Pupilas Azules (direccionadas por offset)
        g2d.setColor(Color.BLUE);
        g2d.fillOval(8 + offsetX, 13 + offsetY, 4, 4);
        g2d.fillOval(20 + offsetX, 13 + offsetY, 4, 4);

        g2d.dispose();
        ImageIO.write(img, "png", new File(nombre));
    }
}