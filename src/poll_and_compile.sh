#!/bin/bash

# Directory to monitor
WATCH_DIR="/home/iw72/Documents/CS5001/CS5001-p3-chatclient/src"

# Command to run on changes
echo "Recompiling all Java files in $WATCH_DIR..."

# Infinite loop to keep checking for changes and recompiling
while true; do
    # Clear the error.txt file at the start of each round
    > "$WATCH_DIR/error.txt"  # Empty the error.txt file

    # Flag to track if any errors occur
    errors_occurred=false

    # Loop over all Java files in the src directory
    for FILE in "$WATCH_DIR"/*.java; do
        [[ -e "$FILE" ]] || continue # Skip if no files are found

        # Compile the current file and append errors to error.txt
        javac "$FILE" >> "$WATCH_DIR/error.txt" 2>&1

        # Check if the javac command failed (non-zero exit code)
        if [[ $? -ne 0 ]]; then
            errors_occurred=true
        fi
    done

    # If no errors occurred, add a success message to the error file
    if ! $errors_occurred; then
        echo "Compilation successful, no errors." >> "$WATCH_DIR/error.txt"
    fi

    # Sleep for 2 seconds before recompiling again
    sleep 2
done

