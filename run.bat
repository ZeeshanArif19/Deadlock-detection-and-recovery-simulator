@echo off
echo Building and running Deadlock Toolkit with Java 23 and Maven 3.9.9...
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Maven is not installed or not in PATH. Please install Maven.
    echo Visit https://maven.apache.org/download.cgi for installation instructions.
    pause
    exit /b 1
)

REM Check Java version
java -version 2>&1 | findstr /C:"version" >nul
if %ERRORLEVEL% NEQ 0 (
    echo Java is not installed or not in PATH.
    pause
    exit /b 1
)

echo Cleaning and packaging the application...
call mvn clean package

if %ERRORLEVEL% NEQ 0 (
    echo Maven build failed. Please check the errors above.
    pause
    exit /b 1
)

echo.
echo Running the application...
call mvn javafx:run

pause
