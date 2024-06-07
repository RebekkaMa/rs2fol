#!/bin/bash

PROJECT_PATH="YOUR_PROJECT_PATH"
RS2FOL_PATH="YOUR_RS2FOL_PATH"
SEARCH_DIR="${PROJECT_PATH}rs2fol/examples/lists/examples/"
PATH_TO_VAMPIRE="YOUR_PATH_TO_VAMPIRE"
OUTPUT_FILE="${PROJECT_PATH}rs2fol/examples/lists/examples/check_lists.csv"
FALLBACK_FILE="default_solution.n3s.out"

RED='\033[0;31m'
GREEN='\033[0;32m'
DARK_RED='\033[0;41m'
NC='\033[0m'

echo "file,Vampire,VAMPIRE RDF LISTS,EYE" > "$OUTPUT_FILE"

process_timeout_result() {
    local FOL_FILE=$1
    local COMMAND_OUTPUT
    COMMAND_OUTPUT=$("$PATH_TO_VAMPIRE" --mode casc -t 20m --cores 0 "$FOL_FILE" 2>&1)
    local LAST_LINES
    LAST_LINES=$(echo "$COMMAND_OUTPUT" | tail -n 10)

    if echo "$LAST_LINES" | grep -q "Termination reason: Satisfiable"; then
        echo "false"
    elif echo "$LAST_LINES" | grep -q "Termination reason: Refutation"; then
        echo "true"
    else
        echo "$LAST_LINES" | grep -oP "(?<=Termination reason: ).*"
    fi
}


find "$SEARCH_DIR" -type f -name "*.n3s" | while read -r FILE; do
    FILENAME=$(basename "$FILE")

#    if [ "$FILENAME" == "peano_short_list_uniqueness.n3s" ]; then
#        continue
#    fi

    echo -e -n "$FILENAME - "

    OUT_FILE="${FILE}.out"
    TEMP_FOL_FILE_RESULT=$(mktemp)
    TEMP_FOL_FILE_RDF=$(mktemp)

    if [ ! -f "$OUT_FILE" ]; then
        OUT_FILE="$SEARCH_DIR/$FALLBACK_FILE"
    fi

    if [ -f "$OUT_FILE" ]; then

        RESULT=$($RS2FOL_PATH check -i "$FILE" -c "$OUT_FILE" -e "$PATH_TO_VAMPIRE" -o "$TEMP_FOL_FILE_RESULT" -q 2>&1 | tr -d '\n')
        RDF_LISTS_RESULT=$($RS2FOL_PATH check -i "$FILE" -c "$OUT_FILE" -e "$PATH_TO_VAMPIRE" -o "$TEMP_FOL_FILE_RDF" -r -q 2>&1 | tr -d '\n')

#        if [[ "$RESULT" == *"timeout"* ]]; then
#            RESULT=$(process_timeout_result "$TEMP_FOL_FILE_RESULT")
#        fi
#
#        if [[ "$RDF_LISTS_RESULT" == *"timeout"* ]]; then
#            RDF_LISTS_RESULT=$(process_timeout_result "$TEMP_FOL_FILE_RDF")
#        fi

        EYE_RESULT=$(timeout 20 eye --nope --no-bnode-relabeling --quiet "$FILE")

        if [[ -z "$(echo "$EYE_RESULT" | tr -d '[:space:]')" ]]; then
            COMPARE_RESULT="false (no result)"
        else
            OUT_CONTENT=$(cat "$OUT_FILE")
            if [ "$EYE_RESULT" == "$OUT_CONTENT" ]; then
                COMPARE_RESULT="true"
            else
                COMPARE_RESULT="false"
            fi
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
        else
            COMPARE_COLOR="${RED}$COMPARE_RESULT${NC}"
        fi

        echo -e "Vampire: $RESULT_COLOR - VAMPIRE RDF LISTS: $RDF_LISTS_COLOR - EYE: $COMPARE_COLOR"

    else
        echo "Fallback file not found,Fallback file not found,Fallback file not found" >> "$OUTPUT_FILE"
        echo -e "Vampire: ${RED}Fallback file not found${NC} - VAMPIRE RDF LISTS: ${RED}Fallback file not found${NC} - EYE: ${RED}Fallback file not found${NC}"
    fi
    rm "$TEMP_FOL_FILE_RESULT"
    rm "$TEMP_FOL_FILE_RDF"
done

VAMPIRE_VERSION=$("$PATH_TO_VAMPIRE" --version 2>&1)
EYE_VERSION=$(eye --version 2>&1)

echo -e "\nVampire Version:\n$VAMPIRE_VERSION"
echo -e "\nEye Version:\n$EYE_VERSION"

echo -e "\nVampire Version:\n$VAMPIRE_VERSION" >> "$OUTPUT_FILE"
echo -e "\nEye Version:\n$EYE_VERSION" >> "$OUTPUT_FILE"
