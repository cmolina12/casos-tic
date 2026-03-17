#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$SCRIPT_DIR/src"
REFS_DIR="$SCRIPT_DIR/referencias"
CSV="$SCRIPT_DIR/datos/resultados.csv"

mkdir -p "$REFS_DIR"
mkdir -p "$SCRIPT_DIR/datos"

# ── Compilar Actividad1 ──────────────────────────────────────────────────────
echo "Compilando Actividad1.java..."
javac "$SRC_DIR/Actividad1.java" -d "$SRC_DIR"
if [ $? -ne 0 ]; then
    echo "Error al compilar Actividad1.java"
    exit 1
fi

# ── Generar archivos de referencias ─────────────────────────────────────────
MATRICES=("8 8 8 8" "16 16 16 16" "128 128 128 128")
LABELS=("8x8" "16x16" "128x128")
TPS=(64 256 1024)

echo "Generando archivos de referencias..."
for idx in "${!MATRICES[@]}"; do
    dims="${MATRICES[$idx]}"
    label="${LABELS[$idx]}"
    for tp in "${TPS[@]}"; do
        outfile="$REFS_DIR/refs_${label}_TP${tp}.txt"
        echo "  -> $outfile"
        java -cp "$SRC_DIR" Actividad1 $dims $tp "$outfile"
    done
done

# ── Correr simulador (81 escenarios) ────────────────────────────────────────
MARCOS=(4 8 16)
POLITICAS=("FIFO" "FIFOModified" "LRU")

echo ""
echo "Iniciando simulaciones..."
echo "Matriz,TP,Marcos,Politica,TotalAccesos,TotalFallos,TasaFallos" > "$CSV"

for idx in "${!LABELS[@]}"; do
    label="${LABELS[$idx]}"
    for tp in "${TPS[@]}"; do
        reffile="$REFS_DIR/refs_${label}_TP${tp}.txt"
        for marcos in "${MARCOS[@]}"; do
            for politica in "${POLITICAS[@]}"; do
                echo -n "  $label TP=$tp marcos=$marcos pol=$politica ... "

                output=$(java -jar "$SCRIPT_DIR/simulador.jar" "$reffile" "$marcos" "$politica" 2>&1)

                # Parsear línea resumen: ********Proceso 1 Total accesos X Total fallos Y
                summary=$(echo "$output" | grep "Total accesos")
                accesos=$(echo "$summary" | grep -oP 'Total accesos \K[0-9]+')
                fallos=$(echo "$summary"  | grep -oP 'Total fallos \K[0-9]+')

                if [ -z "$accesos" ] || [ -z "$fallos" ]; then
                    echo "ERROR (sin resumen)"
                    echo "$label,$tp,$marcos,$politica,ERROR,ERROR,ERROR" >> "$CSV"
                    continue
                fi

                # Calcular tasa con awk para evitar dependencia de bc/python
                tasa=$(awk "BEGIN { printf \"%.6f\", $fallos / $accesos }")

                echo "accesos=$accesos fallos=$fallos tasa=$tasa"
                echo "$label,$tp,$marcos,$politica,$accesos,$fallos,$tasa" >> "$CSV"
            done
        done
    done
done

echo ""
echo "Listo. Resultados guardados en: $CSV"
