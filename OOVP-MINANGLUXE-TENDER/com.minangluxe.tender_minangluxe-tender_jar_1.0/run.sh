#!/bin/bash
# MinangLuxe Tender System — Run Script
# Requires: Java 17+ JDK and Maven 3.x

echo "========================================="
echo "  ♦  MinangLuxe Tender System  ♦"
echo "  Platform Tender Premium"
echo "========================================="

# Check for javac
if ! command -v javac &> /dev/null; then
    echo "[ERROR] Java JDK not found. Please install JDK 17+."
    echo "  Ubuntu/Debian: sudo apt install openjdk-21-jdk"
    echo "  Mac: brew install openjdk"
    exit 1
fi

# Check for Maven
if ! command -v mvn &> /dev/null; then
    echo "[ERROR] Maven not found. Please install Maven 3.x."
    echo "  Ubuntu/Debian: sudo apt install maven"
    echo "  Mac: brew install maven"
    exit 1
fi

echo "[INFO] Building MinangLuxe..."
mvn -q package -DskipTests

if [ $? -eq 0 ]; then
    echo "[INFO] Launching MinangLuxe..."
    java -jar target/minangluxe.jar
else
    echo "[ERROR] Build failed. Check output above."
    exit 1
fi
