#!/bin/bash

source ../../.env

SEARCH_DIR="${PROJECT_PATH}rs2fol/examples/rdfsurfaces-tests/pure"
OUTPUT_FILE="${PROJECT_PATH}rs2fol/examples/rdfsurfaces-tests/check_rdfsurfaces-tests_rdf-lists.csv"

RED='\033[0;31m'
GREEN='\033[0;32m'
DARK_RED='\033[0;41m'
NC='\033[0m'

echo "File,Vampire" > "$OUTPUT_FILE"

find "$SEARCH_DIR" -type f -name "*_FAIL.n3s" -print0 | while IFS= read -r -d '' FILE; do
    FILENAME=$(basename "$FILE")
    echo -e -n "$FILENAME - "

    RESULT=$($RS2FOL_PATH check --program vampire --option-id 2 -q -i "$FILE" -c "${PROJECT_PATH}rs2fol/examples/rdfsurfaces-tests/answer.n3s" -cf "${PROJECT_PATH}/rs2fol/bin/config.json" -r 2>&1 | tr -d '\n')

    echo "$FILENAME,$RESULT" >> "$OUTPUT_FILE"

    if [[ "$RESULT" == "true" ]]; then
        echo -e "${GREEN}$RESULT${NC}"
    elif [[ "$RESULT" == "false" ]]; then
        echo -e "${RED}$RESULT${NC}"
    elif [[ "$RESULT" == *"Error"* ]]; then
        echo -e "${DARK_RED}$RESULT${NC}"
    else
        echo -e "$RESULT"
    fi
done