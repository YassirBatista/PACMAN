import java.io.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class Puntajes {

    private static final String ARCHIVO_PUNTAJES = "puntajes.txt";

    //? METODO PARA GUARDAR UN NUEVO RECORD
    public static void guardarPuntaje(String nombre, int puntos) {
        try {
            //* true indica que agregamos al final del archivo (append), no sobrescribimos
            FileWriter escritor = new FileWriter(ARCHIVO_PUNTAJES, true);
            BufferedWriter buffer = new BufferedWriter(escritor);

            //* Escribimos: Nombre - Puntos
            buffer.write(nombre + " - " + puntos);
            buffer.newLine(); //? Salto de linea para el siguiente
            
            buffer.close(); //? Cerramos para guardar cambios
        } catch (IOException e) {
            System.out.println("Error al guardar puntaje: " + e.getMessage());
        }
    }

    //? METODO PARA LEER TODOS LOS PUNTAJES
    public static String leerPuntajes() {
        StringBuilder texto = new StringBuilder();
        
        try {
            File archivo = new File(ARCHIVO_PUNTAJES);
            //! Si el archivo no existe aun, retornamos mensaje vacio
            if (!archivo.exists()) {
                return "Aun no hay records guardados.";
            }

            FileReader lector = new FileReader(archivo);
            BufferedReader buffer = new BufferedReader(lector);
            String linea;

            //* Leemos linea por linea
            while ((linea = buffer.readLine()) != null) {
                texto.append(linea).append("\n");
            }
            
            buffer.close();
        } catch (IOException e) {
            return "Error al leer puntajes.";
        }

        return texto.toString();
    }
}