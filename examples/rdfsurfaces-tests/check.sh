#!/bin/bash

# Create your own .env file with the missing variables within the rs2fol file
source ../../.env

SEARCH_DIR="/home/rebekka/Programs/EyeReasoner/rdfsurfaces-tests/test/pure"
OUTPUT_FILE="${PROJECT_PATH}rs2fol/examples/rdfsurfaces-tests/check_rdfsurfaces-tests_rdf-lists.csv"

RED='\033[0;31m'
GREEN='\033[0;32m'
DARK_RED='\033[0;41m'
NC='\033[0m'

echo "File,Vampire,ParentFolder" > "$OUTPUT_FILE"

find "$SEARCH_DIR" -type f -name "*.n3s" | while read -r FILE; do
    FILENAME=$(basename "$FILE")

    echo -e -n "$FILENAME - "

    PARENT_FOLDER=$(basename "$(dirname "$FILE")")

#    # Run eye command and store the result
#    EYE_RESULT=$(eye --nope --no-bnode-relabeling --quiet "$FILE")
#
#    # Save the EYE_RESULT to a temporary file
#    TEMP_EYE_OUT=$(mktemp)
#    echo "$EYE_RESULT" > "$TEMP_EYE_OUT"

    RESULT=$($RS2FOL_PATH check --program vampire --option-id 2 -q -i "$FILE" -c "/home/rebekka/Programs/EyeReasoner/rdfsurfaces-tests/test/answer.n3s" -cf /home/rebekka/Projects/rs2fol/bin/config.json -r 2>&1 | tr -d '\n')

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

    # Remove the temporary file
   #  rm "$TEMP_EYE_OUT"

done

EYE_VERSION=$(eye --version 2>&1)

echo -e "\nEye Version: $EYE_VERSION"

EYE_VERSION_LINES=$(echo "$EYE_VERSION" | sed 's/^/,,/g')

{
    echo -e "Eye Version:,,"
    echo -e "$EYE_VERSION_LINES"
} >> "$OUTPUT_FILE"
