#!/bin/bash

source ../../.env

SEARCH_DIR="${PROJECT_PATH}rs2fol/examples/rdfsurfaces-tests/pure-qa"

RED='\033[0;31m'
GREEN='\033[0;32m'
DARK_RED='\033[0;41m'
NC='\033[0m'

find "$SEARCH_DIR" -type f -name "*_FAIL.n3s" | while read -r FILE; do
    FILENAME=$(basename "$FILE")

    RESULT=$($RS2FOL_PATH transform-qa --program vampire-qa -q -i "$FILE" --config "${PROJECT_PATH}/rs2fol/bin/config.json" -r 2>&1)

    echo "$FILENAME:"
    echo "$RESULT"
    echo ""

done


