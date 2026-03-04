#!/bin/bash
CARPETA="$(dirname "$0")"
CSV="$CARPETA/resultados.csv"
NR=4210688

for M in 4 8 16; do
    for POL in FIFO FIFOModified LRU; do
        # Saltar si ya existe en el CSV
        if grep -q "128x128,1024,$M,$POL" "$CSV"; then
            echo "  Ya existe: 128x128 | TP=1024 | Marcos=$M | $POL"
            continue
        fi
        echo "  Corriendo: 128x128 | TP=1024 | Marcos=$M | $POL ..."
        OUTPUT=$(java -jar "$CARPETA/simulador.jar" "$CARPETA/refs_128x128_TP1024.txt" "$M" "$POL" 2>/dev/null)
        FALLOS=$(echo "$OUTPUT" | grep -o "Total fallos [0-9]*" | awk '{print $3}')
        HITS=$((NR - FALLOS))
        TASA=$(echo "scale=6; $FALLOS / $NR" | bc)
        echo "128x128,1024,$M,$POL,$NR,$FALLOS,$HITS,$TASA" >> "$CSV"
        echo "    -> Fallos=$FALLOS, Tasa=$TASA"
    done
done

echo "Listo! Total filas: $(wc -l < $CSV)"
