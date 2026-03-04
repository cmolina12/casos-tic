import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Para compilar: java Actividad1 <NF1> <NC1> <NF2> <NC2> <TP> <archivo_salida>
 *   NF1: filas de M1
 *   NC1: columnas de M1 (debe ser igual a NF2)
 *   NF2: filas de M2
 *   NC2: columnas de M2
 *   TP : tamano de pagina en bytes
 *   archivo_salida: nombre del archivo donde se escribe la lista de DVs
 *
 * Distribucion de memoria (row-major order, 4 bytes por entero):
 *   M1 inicia en byte 0
 *   M2 inicia en byte NF1*NC1*4
 *   M3 inicia en byte (NF1*NC1 + NF2*NC2)*4
 *
 * Algoritmo de multiplicacion simulado:
 *   para i en [0, NF1):
 *     para j en [0, NC2):
 *       para k en [0, NC1):
 *         leer M1[i][k]   <- read
 *         leer M2[k][j]   <- read
 *       escribir M3[i][j] <- write
 */
public class Actividad1 {

    public static void main(String[] args) {

        if (args.length < 6) {
            System.out.println("Uso: java Actividad1 <NF1> <NC1> <NF2> <NC2> <TP> <archivo_salida>");
            return;
        }

        int NF1 = Integer.parseInt(args[0]); // filas de M1
        int NC1 = Integer.parseInt(args[1]); // columnas de M1
        int NF2 = Integer.parseInt(args[2]); // filas de M2 (debe ser igual a NC1)
        int NC2 = Integer.parseInt(args[3]); // columnas de M2
        int TP  = Integer.parseInt(args[4]); // tamano de pagina en bytes
        String archivoSalida = args[5];

        if (NC1 != NF2) {
            System.out.println("Error: NC1 debe ser igual a NF2 para multiplicar matrices.");
            return;
        }

        // Bases de cada matriz en memoria (bytes)
        long baseM1 = 0L;
        long baseM2 = (long) NF1 * NC1 * 4;
        long baseM3 = baseM2 + (long) NF2 * NC2 * 4;

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
