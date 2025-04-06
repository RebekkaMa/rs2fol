#!/bin/bash

source ../../.env

SEARCH_DIR="${PROJECT_PATH}rs2fol/examples/rdfsurfaces-tests-qa"

find "$SEARCH_DIR" -type f -name "*.n3s" | while read -r FILE; do
    FILENAME=$(basename "$FILE")
    RESULT=$($RS2FOL_PATH transform-qa --program vampire-qa --option-id 2 -q -i "$FILE" --config "${PROJECT_PATH}/rs2fol/examples/rdfsurfaces-tests-qa/config.json" -t 5 2>&1)
    echo "$FILENAME:"
    echo "$RESULT"
    echo ""
done