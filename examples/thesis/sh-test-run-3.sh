#!/bin/bash

# peano.n3s (list as functions)

source ../../.env

OUTPUT_FILE="${PROJECT_PATH}rs2fol/examples/thesis/test-run-3/test-run-3.csv"

RED='\033[0;31m'
GREEN='\033[0;32m'
DARK_RED='\033[0;41m'
NC='\033[0m'

echo "File,Vampire,Time(s)" > "$OUTPUT_FILE"

run_check() {
    local FILE="$1"
    local FILENAME=$(basename "$FILE")

    echo -e -n "$FILENAME - "

    start=$(date +%s)

    RESULT=$($RS2FOL_PATH check --program vampire --option-id 2 -q -i "$FILE" -c "${PROJECT_PATH}rs2fol/examples/thesis/answer.n3s" --config "${PROJECT_PATH}/rs2fol/examples/thesis/config.json" -t 120 2>&1 | tr -d '\n')

    end=$(date +%s)
    duration=$((end - start))

    echo "$FILENAME,$RESULT,$duration" >> "$OUTPUT_FILE"

    if [[ "$RESULT" == "Consequence" ]]; then
        echo -e "${GREEN}$RESULT${NC} (${duration}s)"
    elif [[ "$RESULT" == "No consequence" ]]; then
        echo -e "${RED}$RESULT${NC} (${duration}s)"
    elif [[ "$RESULT" == "Unsatisfiable" ]]; then
        echo -e "${GREEN}$RESULT${NC} (${duration}s)"
    elif [[ "$RESULT" == "Satisfiable" ]]; then
        echo -e "${RED}$RESULT${NC} (${duration}s)"
    elif [[ "$RESULT" == *"Error"* ]]; then
        echo -e "${DARK_RED}$RESULT${NC} (${duration}s)"
    else
        echo -e "$RESULT (${duration}s)"
    fi

}

run_check "${PROJECT_PATH}rs2fol/examples/thesis/test-run-3/peano.n3s"