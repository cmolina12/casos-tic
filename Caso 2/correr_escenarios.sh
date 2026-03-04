#!/bin/bash
# Corre los 81 escenarios de la Actividad 2 y guarda resultados en CSV

CARPETA="$(dirname "$0")"
CSV="$CARPETA/resultados.csv"

# Parametros del caso
SIZES=("8 8 8 8" "16 16 16 16" "128 128 128 128")
LABELS=("8x8" "16x16" "128x128")
TPS=(64 256 1024)
MARCOS=(4 8 16)
POLITICAS=("FIFO" "FIFOModified" "LRU")

# Cabecera del CSV
echo "Matriz,TP,Marcos,Politica,NR,Fallos,Hits,TasaFallos" > "$CSV"

echo "=== Generando archivos de referencias ==="
for idx in 0 1 2; do
    LABEL="${LABELS[$idx]}"
    DIMS="${SIZES[$idx]}"
    for TP in "${TPS[@]}"; do
        REFFILE="$CARPETA/refs_${LABEL}_TP${TP}.txt"
        echo "Generando $REFFILE ..."
        java -cp "$CARPETA" Actividad1 $DIMS $TP "$REFFILE"
    done
done

echo ""
echo "=== Corriendo 81 escenarios ==="
for idx in 0 1 2; do
    LABEL="${LABELS[$idx]}"
    for TP in "${TPS[@]}"; do
        REFFILE="$CARPETA/refs_${LABEL}_TP${TP}.txt"
        # Leer NR del archivo de referencias
        NR=$(grep "^NR=" "$REFFILE" | cut -d'=' -f2)
        for M in "${MARCOS[@]}"; do
            for POL in "${POLITICAS[@]}"; do
                echo "  $LABEL | TP=$TP | Marcos=$M | $POL ..."
                OUTPUT=$(java -jar "$CARPETA/simulador.jar" "$REFFILE" "$M" "$POL" 2>/dev/null)
                FALLOS=$(echo "$OUTPUT" | grep -o "Total fallos [0-9]*" | awk '{print $3}')
                HITS=$((NR - FALLOS))
                TASA=$(echo "scale=6; $FALLOS / $NR" | bc)
                echo "$LABEL,$TP,$M,$POL,$NR,$FALLOS,$HITS,$TASA" >> "$CSV"
            done
        done
    done
done

echo ""
echo "=== Listo! Resultados en: $CSV ==="
