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
        java -cp java-cup-11b-runtime.jar:. Compiler < "$input_file" > "$output_file"
        
    fi
done

echo "-----------------------------------"
echo "All done! You can view your files in the results/ folder."