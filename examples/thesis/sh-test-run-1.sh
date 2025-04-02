#!/bin/bash

source ../../.env

SEARCH_DIR="${PROJECT_PATH}rs2fol/examples/thesis/test-run-1"
OUTPUT_FILE="${PROJECT_PATH}rs2fol/examples/thesis/test-run-1/test-run-1.csv"

RED='\033[0;31m'
GREEN='\033[0;32m'
DARK_RED='\033[0;41m'
NC='\033[0m'

echo "File,Vampire" > "$OUTPUT_FILE"

run_check() {
    local FILE="$1"
    local FILENAME=$(basename "$FILE")
    local USE_C="$2"

    echo -e -n "$FILENAME - "

    if [[ "$USE_C" == "yes" ]]; then
        RESULT=$($RS2FOL_PATH check --program vampire --option-id 2 -q -i "$FILE" -c "${PROJECT_PATH}rs2fol/examples/thesis/answer.n3s" --config "${PROJECT_PATH}/rs2fol/examples/thesis/config.json" -r -t 120 2>&1 | tr -d '\n')
    else
        RESULT=$($RS2FOL_PATH check --program vampire --option-id 2 -q -i "$FILE" --config "${PROJECT_PATH}/rs2fol/examples/thesis/config.json" -r -t 120 2>&1 | tr -d '\n')
    fi

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

# Zuerst alle *_FAIL.n3s-Dateien
find "$SEARCH_DIR" -type f -name "*_FAIL.n3s" -print0 | while IFS= read -r -d '' FILE; do
    run_check "$FILE" "no"
done

# Dann alle *_LIE.n3s-Dateien
find "$SEARCH_DIR" -type f -name "*_LIE.n3s" -print0 | while IFS= read -r -d '' FILE; do
    run_check "$FILE" "yes"
done

# Dann alle übrigen .n3s-Dateien (nicht *_FAIL.n3s und nicht *_LIE.n3s)
find "$SEARCH_DIR" -type f -name "*.n3s" ! -name "*_FAIL.n3s" ! -name "*_LIE.n3s" -print0 | while IFS= read -r -d '' FILE; do
    run_check "$FILE" "yes"
done
