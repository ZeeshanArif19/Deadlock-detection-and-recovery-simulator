@echo off
echo Compiling and running Deadlock Toolkit...
echo.

REM Set classpath
set CLASSPATH=.;src\main\java;src\main\resources

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

REM Compile all Java files
echo Compiling Java files...
javac -d bin src\main\java\deadlocktoolkit\*.java src\main\java\deadlocktoolkit\core\*.java src\main\java\deadlocktoolkit\visualization\*.java src\main\java\deadlocktoolkit\ui\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed. Please check the errors above.
    pause
    exit /b 1
)

REM Run the application
echo.
echo Running the application...
java -cp bin;src\main\resources deadlocktoolkit.DeadlockToolkitApp

pause
