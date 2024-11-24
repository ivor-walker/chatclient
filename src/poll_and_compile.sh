#!/bin/bash

# Directory to monitor
WATCH_DIR="/home/iw72/Documents/CS5001/CS5001-p3-chatclient/src"

# File to compile
TARGET_FILE="$WATCH_DIR/ChatClient.java"

# Command to run on changes
echo "Watching all files in $WATCH_DIR for changes, but compiling only ChatClient.java..."

# Declare an associative array to store the last modification times
declare -A LAST_MOD_TIMES

# Initialize the modification times for all files
for FILE in "$WATCH_DIR"/*; do
    [[ -e "$FILE" ]] || continue # Skip if no files are found
    LAST_MOD_TIMES["$FILE"]=$(stat -c %Y "$FILE")
done

# Infinite loop to poll for changes
while true; do
    for FILE in "$WATCH_DIR"/*; do
        [[ -e "$FILE" ]] || continue # Skip if the file doesn't exist anymore

        # Get the current modification time of the file
        CURRENT_MOD_TIME=$(stat -c %Y "$FILE")

        # Check if the modification time has changed
        if [[ "$CURRENT_MOD_TIME" != "${LAST_MOD_TIMES["$FILE"]}" ]]; then
            # Compile the target file
            javac "$TARGET_FILE" > "$WATCH_DIR/error.txt" 2>&1

            # Update the last modification time
            LAST_MOD_TIMES["$FILE"]=$CURRENT_MOD_TIME
        fi
    done

    # Sleep to reduce CPU usage
    sleep 2
done

