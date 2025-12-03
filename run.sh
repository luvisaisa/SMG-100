#!/usr/bin/env zsh

# Use Java 21+ for running the application (compiled with Java 21)
export JAVA_HOME=$(/usr/libexec/java_home -v 25 2>/dev/null || /usr/libexec/java_home -v 21 2>/dev/null)
export PATH="$JAVA_HOME/bin:$PATH"

echo "Using Java: $JAVA_HOME"

# Build the application
echo "Building The Completionist..."
./gradlew installDist

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "\nStarting The Completionist...\n"
    # Run the application directly (not through Gradle)
    # This gives JLine3 direct terminal access for better arrow key support
    ./app/build/install/app/bin/app
else
    echo "Build failed. Please fix errors and try again."
    exit 1
fi
