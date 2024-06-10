#!/bin/bash

# Create your own .env file with the missing variables within the rs2fol file
source ../../.env

SEARCH_DIR="${PROJECT_PATH}rs2fol/examples/rdfsurfaces-tests/"
OUTPUT_FILE="${PROJECT_PATH}rs2fol/examples/rdfsurfaces-tests/check_rdfsurfaces-tests_rdf-lists.csv"
FALLBACK_FILE="solution.n3s.out"

RED='\033[0;31m'
GREEN='\033[0;32m'
DARK_RED='\033[0;41m'
NC='\033[0m'

echo "File,Vampire,ParentFolder" > "$OUTPUT_FILE"

find "$SEARCH_DIR" -type f -name "*.n3s" | while read -r FILE; do
    FILENAME=$(basename "$FILE")

    echo -e -n "$FILENAME - "

    OUT_FILE="${FILE}.out"
    PARENT_FOLDER=$(basename "$(dirname "$FILE")")

    if [ ! -f "$OUT_FILE" ]; then
        OUT_FILE="$SEARCH_DIR/$FALLBACK_FILE"
    fi

    if [ -f "$OUT_FILE" ]; then
        RESULT=$($RS2FOL_PATH check -q -i "$FILE" -e "$PATH_TO_VAMPIRE" -c "$OUT_FILE" -r 2>&1 | tr -d '\n')

        echo "$FILENAME,$RESULT,$PARENT_FOLDER" >> "$OUTPUT_FILE"

        if [[ "$RESULT" == "true" ]]; then
            echo -e "${GREEN}$RESULT${NC} - $PARENT_FOLDER"
        elif [[ "$RESULT" == "false" ]]; then
            echo -e "${RED}$RESULT${NC} - $PARENT_FOLDER"
        elif [[ "$RESULT" == *"Error"* ]]; then
            echo -e "${DARK_RED}$RESULT${NC} - $PARENT_FOLDER"
        else
            echo -e "$RESULT - $PARENT_FOLDER"
        fi
    else
        echo "Fallback file not found,$PARENT_FOLDER" >> "$OUTPUT_FILE"
        echo -e "${RED}Fallback file not found${NC} - $PARENT_FOLDER"
    fi
done

VAMPIRE_VERSION=$("$PATH_TO_VAMPIRE" --version 2>&1)

echo -e "\nVampire Version:\n$VAMPIRE_VERSION"
echo -e "\nVampire Version:\n$VAMPIRE_VERSION" >> "$OUTPUT_FILE"