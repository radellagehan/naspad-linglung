@echo off
echo =========================================
echo   ^♦  MinangLuxe Tender System  ^♦
echo   Platform Tender Premium
echo =========================================

where javac >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java JDK not found. Install JDK 17+ from https://adoptium.net
    pause & exit /b 1
)

where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven not found. Install from https://maven.apache.org/download.cgi
    pause & exit /b 1
)

echo [INFO] Building MinangLuxe...
mvn -q package -DskipTests

if %errorlevel% equ 0 (
    echo [INFO] Launching MinangLuxe...
    java -jar target\minangluxe.jar
) else (
    echo [ERROR] Build failed.
    pause & exit /b 1
)
