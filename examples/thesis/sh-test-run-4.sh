#!/bin/bash

# peano.n3s (list as functions)

source ../../.env

OUTPUT_FILE="${PROJECT_PATH}rs2fol/examples/thesis/test-run-4/test-run-4.csv"

RED='\033[0;31m'
GREEN='\033[0;32m'
DARK_RED='\033[0;41m'
NC='\033[0m'

echo "File,Vampire" > "$OUTPUT_FILE"

run_check() {
    local FILE="$1"
    local FILENAME=$(basename "$FILE")

    echo -e -n "$FILENAME - "

    RESULT=$($RS2FOL_PATH check --program vampire --option-id 3 -q -i "$FILE" -c "${PROJECT_PATH}rs2fol/examples/thesis/answer.n3s" -cf "${PROJECT_PATH}/rs2fol/examples/thesis/config.json" -r -t 43200 2>&1 | tr -d '\n')

    echo "$FILENAME,$RESULT" >> "$OUTPUT_FILE"

    if [[ "$RESULT" == "Consequence" ]]; then
        echo -e "${GREEN}$RESULT${NC}"
    elif [[ "$RESULT" == "No consequence" ]]; then
        echo -e "${RED}$RESULT${NC}"
    elif [[ "$RESULT" == "Unsatisfiable" ]]; then
        echo -e "${GREEN}$RESULT${NC}"
    elif [[ "$RESULT" == "Satisfiable" ]]; then
        echo -e "${RED}$RESULT${NC}"
    elif [[ "$RESULT" == *"Error"* ]]; then
        echo -e "${DARK_RED}$RESULT${NC}"
    else
        echo -e "$RESULT"
    fi
}


run_check "${PROJECT_PATH}rs2fol/examples/thesis/test-run-3/peano.n3s"