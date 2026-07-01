#!/bin/bash
make
EXEC_CMD="java Main" 

PASS_DIR="inputs/1_examples_passing_type_checking"
EXPECTED_DIR="inputs/1_offset_results"
ERR_DIR="inputs/errors"
ALL_PASSED=true

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# =============================== first folder checking the offsets ===============================
echo -e ""
echo "============== 1_Examples Passing Type Checking ================"
echo -e ""

for file in "$PASS_DIR"/*; do
    filename=$(basename "$file")
    expected_file="$EXPECTED_DIR/${filename%.*}.txt"
    
    if [ ! -f "$expected_file" ]; then
        echo -e "${RED}[Error]${NC} No expected output file found: $expected_file"
        ALL_PASSED=false
        continue
    fi

    actual_output=$($EXEC_CMD "$file" |grep -v '^-' | awk 'NF')
    
    # grep -v removes lines starting with '-' 
    # awk 'NF' removes empty lines
    filtered_expected=$(grep -v '^-' "$expected_file" | awk 'NF')

    if diff -w <(echo "$actual_output") <(echo "$filtered_expected") > /dev/null; then
        echo -e "${GREEN}[PASS]${NC} $filename"
    else
        echo -e "${RED}[FAIL]${NC} $filename (Error in file $expected_file)"
        ALL_PASSED=false
    fi
done


# =============================== second folder checking the offsets ===============================
echo -e ""
echo "============== 2_Examples Passing Type Checking ================"
echo -e ""
PASS_DIR="inputs/2_examples_passing_type_checking"
EXPECTED_DIR="inputs/2_offset_results"

for file in "$PASS_DIR"/*; do
    filename=$(basename "$file")
    expected_file="$EXPECTED_DIR/${filename%.*}.txt"
    
    if [ ! -f "$expected_file" ]; then
        echo -e "${RED}[Error]${NC} No expected output file found: $expected_file"
        ALL_PASSED=false
        continue
    fi

    actual_output=$($EXEC_CMD "$file" | grep -v '^-' | awk 'NF')
    
    # grep -v removes lines starting with '-' 
    # awk 'NF' removes empty lines
    filtered_expected=$(grep -v '^-' "$expected_file" | awk 'NF')

    if diff -w <(echo "$actual_output") <(echo "$filtered_expected") > /dev/null; then
        echo -e "${GREEN}[PASS]${NC} $filename"
    else
        echo -e "${RED}[FAIL]${NC} $filename (Error in file $expected_file)"
        ALL_PASSED=false
    fi
done

# =============================== TYPE ERRORS ===============================
echo -e ""
echo "============== ERRORS ================"
echo -e ""

for file in "$ERR_DIR"/*.java; do
    filename=$(basename "$file")
    actual_output=$($EXEC_CMD "$file" 2>&1 | grep -v '^-' | awk 'NF')
    exit_status=$?
    if echo "$actual_output" | grep -q "TYPE ERROR"; then
        echo -e "${GREEN}[PASS]${NC} $filename (Caught TYPE ERROR)"
    else
        echo -e "${RED}[FAIL]${NC} $filename (Caught error, but not 'TYPE ERROR')"
        echo "Output was: $actual_output" 
        ALL_PASSED=false
    fi
done


echo -e ""
echo "========================================"

if [ "$ALL_PASSED" = true ]; then
    echo -e "${GREEN}All tests passed successfully! ${NC}"
else
    echo -e "${RED}Some tests failed. ${NC}"
fi
echo "========================================"