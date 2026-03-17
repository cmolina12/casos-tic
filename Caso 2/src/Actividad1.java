import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Actividad1 {

    public static void main(String[] args) {

        if (args.length < 6) { // Verificar que se recibieron todos los argumentos necesarios
            System.out.println("Uso: java Actividad1 <NF1> <NC1> <NF2> <NC2> <TP> <archivo_salida>");
            return;
        }

        int NF1 = Integer.parseInt(args[0]); // filas de M1
        int NC1 = Integer.parseInt(args[1]); // columnas de M1
        int NF2 = Integer.parseInt(args[2]); // filas de M2 (debe ser igual a NC1)
        int NC2 = Integer.parseInt(args[3]); // columnas de M2
        int TP  = Integer.parseInt(args[4]); // tamaño de pagina en bytes
        String archivoSalida = args[5]; // nombre del archivo de salida

        if (NC1 != NF2) { // Verificar que las dimensiones sean compatibles para multiplicar
            System.out.println("Error: NC1 debe ser igual a NF2 para multiplicar matrices.");
            return;
        }

        // Bases de cada matriz en memoria (bytes)
        long baseM1 = 0L; // M1 inicia en byte 0, long por si acaso supera el rango de int
        long baseM2 = (long) NF1 * NC1 * 4; // M2 inicia después de M1
        long baseM3 = baseM2 + (long) NF2 * NC2 * 4; // M3 inicia después de M1 y M2

        // Total de bytes ocupados por las tres matrices
        long totalBytes = baseM3 + (long) NF1 * NC2 * 4;

        // Numero de paginas virtuales necesarias (redondeo hacia arriba)
        long NP = (totalBytes + TP - 1) / TP;

        // Numero total de referencias: por cada M3[i][j] se leen NC1 pares (M1,M2) y se escribe M3
        long NR = (long) NF1 * NC2 * (2L * NC1 + 1);

        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(archivoSalida)))) {

            // Cabecera
            pw.println("TP=" + TP);
            pw.println("NF1=" + NF1);
            pw.println("NC1=" + NC1);
            pw.println("NF2=" + NF2);
            pw.println("NC2=" + NC2);
            pw.println("NR=" + NR);
            pw.println("NP=" + NP);

            // Lista de referencias
            for (int i = 0; i < NF1; i++) {
                for (int j = 0; j < NC2; j++) {
                    for (int k = 0; k < NC1; k++) {

                        // Leer M1[i][k]
                        long byteM1 = baseM1 + ((long) i * NC1 + k) * 4;
                        pw.println("[M1-" + i + "-" + k + "]," + (byteM1 / TP) + "," + (byteM1 % TP));

                        // Leer M2[k][j]
                        long byteM2 = baseM2 + ((long) k * NC2 + j) * 4;
                        pw.println("[M2-" + k + "-" + j + "]," + (byteM2 / TP) + "," + (byteM2 % TP));
                    }

                    // Escribir M3[i][j]
                    long byteM3 = baseM3 + ((long) i * NC2 + j) * 4;
                    pw.println("[M3-" + i + "-" + j + "]," + (byteM3 / TP) + "," + (byteM3 % TP));
                }
            }

        } catch (IOException e) {
            System.out.println("Error al escribir el archivo: " + e.getMessage());
        }

        System.out.println("Archivo generado: " + archivoSalida);
        System.out.println("NR=" + NR + "  NP=" + NP + "  totalBytes=" + totalBytes);
    }
}
