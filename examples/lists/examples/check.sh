#!/bin/bash

# Create your own .env file with the missing variables within the rs2fol file
source ../../../.env

SEARCH_DIR="${PROJECT_PATH}rs2fol/examples/lists/examples/"
OUTPUT_FILE="${PROJECT_PATH}rs2fol/examples/lists/examples/check_lists.csv"

RED='\033[0;31m'
GREEN='\033[0;32m'
DARK_RED='\033[0;41m'
NC='\033[0m'

echo "file,Vampire,VAMPIRE RDF LISTS,EYE" > "$OUTPUT_FILE"

find "$SEARCH_DIR" -type f -name "*.n3s" | while read -r FILE; do
    FILENAME=$(basename "$FILE")
    OUT_FILE="${FILE}.out"

    echo -e -n "$FILENAME - "

    RESULT=$($RS2FOL_PATH check --program vampire -i "$FILE" -c "$OUT_FILE" -v 1 -q 2>&1 | tr -d '\n')
    RDF_LISTS_RESULT=$($RS2FOL_PATH check --program vampire -i "$FILE" -c "$OUT_FILE" -v 1 -r -q 2>&1 | tr -d '\n')

    EYE_RESULT=$(timeout 20 eye --nope --no-bnode-relabeling --quiet "$FILE")

    if [[ $? -eq 124 ]]; then
        EYE_RESULT="timeout"
    elif [[ -z "$(echo "$EYE_RESULT" | tr -d '[:space:]')" ]]; then
        COMPARE_RESULT="false (no result)"
    else
        OUT_CONTENT=$(tr -d '[:space:]' < "$OUT_FILE")
        EYE_RESULT_TRIMMED=$(echo "$EYE_RESULT" | tr -d '[:space:]')
        if [ "$EYE_RESULT_TRIMMED" == "$OUT_CONTENT" ]; then
            COMPARE_RESULT="true"
        else
            COMPARE_RESULT="false"
        fi
    fi

    if [[ "$EYE_RESULT" == "timeout" ]]; then
        COMPARE_RESULT="timeout"
    fi

    echo "$FILENAME,$RESULT,$RDF_LISTS_RESULT,$COMPARE_RESULT" >> "$OUTPUT_FILE"

    if [[ "$RESULT" == "true" ]]; then
        RESULT_COLOR="${GREEN}$RESULT${NC}"
    elif [[ "$RESULT" == "false" ]]; then
        RESULT_COLOR="${RED}$RESULT${NC}"
    elif [[ "$RESULT" == *"Error"* ]]; then
        RESULT_COLOR="${DARK_RED}$RESULT${NC}"
    else
        RESULT_COLOR="$RESULT"
    fi

    if [ "$RDF_LISTS_RESULT" == "true" ]; then
        RDF_LISTS_COLOR="${GREEN}$RDF_LISTS_RESULT${NC}"
    elif [[ "$RDF_LISTS_RESULT" == "false" ]]; then
        RDF_LISTS_COLOR="${RED}$RDF_LISTS_RESULT${NC}"
    elif [[ "$RDF_LISTS_RESULT" == *"Error"* ]]; then
        RDF_LISTS_COLOR="${DARK_RED}$RDF_LISTS_RESULT${NC}"
    else
        RDF_LISTS_COLOR="$RDF_LISTS_RESULT"
    fi

    if [ "$COMPARE_RESULT" == "true" ]; then
        COMPARE_COLOR="${GREEN}$COMPARE_RESULT${NC}"
    elif [ "$COMPARE_RESULT" == "false" ]; then
        COMPARE_COLOR="${RED}$COMPARE_RESULT${NC}"
    elif [ "$COMPARE_RESULT" == "timeout" ]; then
        COMPARE_COLOR="${DARK_RED}$COMPARE_RESULT${NC}"
    else
        COMPARE_COLOR="${RED}$COMPARE_RESULT${NC}"
    fi

    echo -e "Vampire: $RESULT_COLOR - VAMPIRE RDF LISTS: $RDF_LISTS_COLOR - EYE: $COMPARE_COLOR"
done

VAMPIRE_VERSION=$("$PATH_TO_VAMPIRE" --version 2>&1)
EYE_VERSION=$(eye --version 2>&1)

echo -e "\nVampire Version:\n$VAMPIRE_VERSION"
echo -e "\nEye Version:\n$EYE_VERSION"

VAMPIRE_VERSION_LINES=$(echo "$VAMPIRE_VERSION" | sed 's/^/,,,/g')
EYE_VERSION_LINES=$(echo "$EYE_VERSION" | sed 's/^/,,,/g')

{
    echo -e ",,,\nVampire Version:,,,"
    echo -e "$VAMPIRE_VERSION_LINES"
    echo -e "Eye Version:,,,"
    echo -e "$EYE_VERSION_LINES"
} >> "$OUTPUT_FILE"