#!/bin/bash

make clean
make
echo "Build completed successfully!"
echo "-----------------------------------"

mkdir -p results


echo "Execution of examples:"

for input_file in examples/*; do
    if [ -f "$input_file" ]; then
        
        basename=$(basename "$input_file")
        filename_no_ext="${basename%.*}"
        output_dir="results/${filename_no_ext}"
        mkdir -p "$output_dir"
        output_file="${output_dir}/Main.java"
        temp_err="${output_dir}/temp_error.log"
        
        java -cp java-cup-11b-runtime.jar:. Compiler < "$input_file" > "$output_file" 2> "$temp_err"
        if [ -s "$temp_err" ]; then
            echo "Error in file $basename"
            cat "$temp_err" > "$output_file" 
        else
            echo "Successfully processed $basename"
        fi
        rm -f "$temp_err"
    fi
done

echo "-----------------------------------"
echo "All done! You can view your files in the results/ folder."