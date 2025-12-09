import javax.sound.sampled.*;
import java.net.URL;

public class musica { 

    //? Variable global para poder controlar la musica de fondo desde fuera
    private static Clip clipFondo;

    public static void reproducir(String rutaRelativa, float volumenDecibeles) { 
        //* true indica que queremos que se repita (loop)
        reproducirAudio(rutaRelativa, volumenDecibeles, true);
    }

    public static void comer(String rutaRelativa, float volumenDecibeles){
        //* false indica que solo suena una vez
        reproducirAudio(rutaRelativa, volumenDecibeles, false);
    }

    public static void morir(String rutaRelativa, float volumenDecibeles){
        reproducirAudio(rutaRelativa, volumenDecibeles, false);
    }

    //? METODO NUEVO: Para detener la musica al salir al menu
    public static void detener() {
        if (clipFondo != null) {
            if (clipFondo.isRunning()) {
                clipFondo.stop(); //! Paramos el sonido
            }
            clipFondo.close(); //! Liberamos memoria
            clipFondo = null;
        }
    }

    private static void reproducirAudio(String ruta, float volumen, boolean loop) {
        
        //? Creamos un nuevo hilo para cargar el sonido y que el juego no se trabe
        new Thread(() -> {
            try {
                //? Si vamos a poner musica de fondo nueva, paramos la anterior primero
                if (loop) {
                    detener();
                }

                URL urlSonido = musica.class.getResource(ruta);
                
                if (urlSonido != null) {
                    AudioInputStream audioInput = AudioSystem.getAudioInputStream(urlSonido);
                    Clip clip = AudioSystem.getClip();
                    
                    //* Si es musica de fondo, guardamos la referencia en la variable global
                    if (loop) {
                        clipFondo = clip;
                    }
                    
                    //? Añadimos un "escucha" para cerrar el clip cuando termine
                    //! Si no hacemos esto, la memoria RAM se llena y el juego crashea al rato
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            if (!loop) { //* Solo cerramos auto si NO es musica de fondo
                                clip.close(); 
                            }
                        }
                    });

                    clip.open(audioInput);

                    //? Control de Volumen
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
        }).start(); //* .start() inicia el proceso paralelo
    }
}