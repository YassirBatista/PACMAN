import javax.sound.sampled.*;
import java.net.URL;

public class musica { 

    public static void reproducir(String rutaRelativa, float volumenDecibeles) { 
        // true indica que queremos que se repita (loop)
        reproducirAudio(rutaRelativa, volumenDecibeles, true);
    }

    public static void comer(String rutaRelativa, float volumenDecibeles){
        // false indica que solo suena una vez
        reproducirAudio(rutaRelativa, volumenDecibeles, false);
    }

    public static void morir(String rutaRelativa, float volumenDecibeles){
        reproducirAudio(rutaRelativa, volumenDecibeles, false);
    }

    private static void reproducirAudio(String ruta, float volumen, boolean loop) {
        
        // Creamos un nuevo hilo para que cargar el sonido en el y que el juego no se cargue con el sonido a la vez en uno solo
        new Thread(() -> {
            try {
                URL urlSonido = musica.class.getResource(ruta);
                
                if (urlSonido != null) {
                    AudioInputStream audioInput = AudioSystem.getAudioInputStream(urlSonido);
                    Clip clip = AudioSystem.getClip();
                    
                    // Añadimos un "escucha" para cerrar el clip cuando termine
                    // Si no hacemos esto, la memoria RAM se llena y el juego crashea al rato
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            if (!loop) { // Solo cerramos si NO es música de fondo
                                clip.close(); 
                            }
                        }
                    });

                    clip.open(audioInput);

                    // Control de Volumen
                    FloatControl ganancia = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    ganancia.setValue(volumen);
                    
                    if (loop) {
                        clip.loop(Clip.LOOP_CONTINUOUSLY); 
                    }
                    
                    clip.start(); 
                } else {
                    System.err.println("ERROR: No encuentro el sonido en: " + ruta);
                }
            } catch (Exception e) {
                System.err.println("Error reproduciendo audio: " + e.getMessage());
            }
        }).start(); // .start() inicia el proceso paralelo
    }
}