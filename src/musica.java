import javax.sound.sampled.*;
import java.io.File;

public class musica { 

    // 2. Cambié el nombre del método a "reproducir" para evitar conflictos
    public static void reproducir(String rutaArchivo, float volumenDecibeles) { 
        try {
            File archivo = new File(rutaArchivo);
            if (archivo.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(archivo);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);

                // --- AQUI CAMBIAMOS EL VOLUMEN ---
                // Obtenemos el control de ganancia (volumen)
                FloatControl ganancia = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                // Le asignamos el valor en decibeles
                ganancia.setValue(volumenDecibeles);
                // ---------------------------------
                
                // Repetir infinitamente
                clip.loop(Clip.LOOP_CONTINUOUSLY); 
                
                // Iniciar
                clip.start(); 
            } else {
                System.out.println("No encuentro el archivo de sonido.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void comer(String rutaArchivo, float volumenDecibeles){
        try {
            File archivo = new File(rutaArchivo);
            if (archivo.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(archivo);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);

                FloatControl ganancia = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                ganancia.setValue(volumenDecibeles);
                
                // Iniciar
                clip.start(); 
            } else {
                System.out.println("No encuentro el archivo de sonido.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void morir(String rutaArchivo, float volumenDecibeles){
        try {
            File archivo = new File(rutaArchivo);
            if (archivo.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(archivo);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);

                FloatControl ganancia = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                ganancia.setValue(volumenDecibeles);
                
                // Iniciar
                clip.start(); 
            } else {
                System.out.println("No encuentro el archivo de sonido.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 
