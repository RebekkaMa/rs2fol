#!/bin/bash

# Create your own .env file with the missing variables within the rs2fol file
source ../../.env

SEARCH_DIR="${PROJECT_PATH}rs2fol/examples/sequent-calculus"
OUTPUT_FILE="${PROJECT_PATH}rs2fol/examples/sequent-calculus/check_sequent-calculus.csv"
FALLBACK_FILE="default_solution.n3s.out"

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

echo "file,Vampire,EYE" > "$OUTPUT_FILE"

find "$SEARCH_DIR" -type f -name "*.n3s" | while read FILE; do
    FILENAME=$(basename "$FILE")
    echo -e -n "$FILENAME - "
    OUT_FILE="${FILE}.out"

    if [ ! -f "$OUT_FILE" ]; then
        OUT_FILE="$SEARCH_DIR/$FALLBACK_FILE"
    fi


    if [ -f "$OUT_FILE" ]; then
        RESULT=$($RS2FOL_PATH check -i "$FILE" -c "$OUT_FILE" -e "$PATH_TO_VAMPIRE" -q)

        EYE_RESULT=$(timeout 10 eye --nope --no-bnode-relabeling --quiet "$FILE")

        if [[ $? -eq 124 ]]; then
                    EYE_RESULT="timeout"
                elif [[ -z "$(echo "$EYE_RESULT" | tr -d '[:space:]')" ]]; then
                    COMPARE_RESULT="false (no result)"
                else
                    OUT_CONTENT=$(cat "$OUT_FILE")
                    if [ "$EYE_RESULT" == "$OUT_CONTENT" ]; then
                        COMPARE_RESULT="true"
                    else
                        COMPARE_RESULT="false"
                    fi
                fi

        if [[ "$EYE_RESULT" == "timeout" ]]; then
            COMPARE_RESULT="timeout"
        fi

        echo "$FILENAME,$RESULT,$COMPARE_RESULT" >> "$OUTPUT_FILE"

        if [ "$RESULT" == "true" ]; then
            RESULT_COLOR="${GREEN}$RESULT${NC}"
        else
            RESULT_COLOR="${RED}$RESULT${NC}"
        fi

        if [ "$COMPARE_RESULT" == "true" ]; then
            COMPARE_COLOR="${GREEN}$COMPARE_RESULT${NC}"
        else
            COMPARE_COLOR="${RED}$COMPARE_RESULT${NC}"
        fi

        echo -e "Vampire: $RESULT_COLOR - EYE: $COMPARE_COLOR"

    else
        echo "Fallback file not found,Fallback file not found" >> "$OUTPUT_FILE"
        echo -e "Vampire: ${RED}Fallback file not found${NC} - EYE: ${RED}Fallback file not found${NC}"
    fi
done

VAMPIRE_VERSION=$("$PATH_TO_VAMPIRE" --version 2>&1)
EYE_VERSION=$(eye --version 2>&1)

echo -e "\nVampire Version:\n$VAMPIRE_VERSION"
echo -e "\nEye Version:\n$EYE_VERSION"

echo -e "\nVampire Version:\n$VAMPIRE_VERSION" >> "$OUTPUT_FILE"
echo -e "\nEye Version:\n$EYE_VERSION" >> "$OUTPUT_FILE"