@echo off
setlocal EnableDelayedExpansion

REM Check if POCKETBASE_HOME is defined
if "%POCKETBASE_HOME%"=="" (
    echo ❌ Error: POCKETBASE_HOME environment variable is not set
    echo Please set it to the directory containing your pocketbase executable and pb_data
    echo Example: set POCKETBASE_HOME=C:\dev-services\pocketbase-runtime
    pause
    exit /b 1
)

REM Check if POCKETBASE_HOME directory exists
if not exist "%POCKETBASE_HOME%" (
    echo ❌ Error: POCKETBASE_HOME directory does not exist: %POCKETBASE_HOME%
    pause
    exit /b 1
)

REM Check if pocketbase executable exists
if not exist "%POCKETBASE_HOME%\pocketbase.exe" (
    echo ❌ Error: pocketbase.exe not found in %POCKETBASE_HOME%
    pause
    exit /b 1
)

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0
REM Remove trailing backslash
set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%

echo 🚀 Starting PocketBase...
echo 📁 Runtime directory: %POCKETBASE_HOME%
echo ⚙️  Config directory: %SCRIPT_DIR%

REM Create directories if they don't exist
if not exist "%SCRIPT_DIR%\pb_migrations" mkdir "%SCRIPT_DIR%\pb_migrations"
if not exist "%SCRIPT_DIR%\pb_public" mkdir "%SCRIPT_DIR%\pb_public"
if not exist "%SCRIPT_DIR%\pb_hooks" mkdir "%SCRIPT_DIR%\pb_hooks"

REM Change to runtime directory and start pocketbase
cd /d "%POCKETBASE_HOME%"
pocketbase.exe serve ^
    --migrationsDir="%SCRIPT_DIR%\pb_migrations" ^
    --publicDir="%SCRIPT_DIR%\pb_public" ^
    --hooksDir="%SCRIPT_DIR%\pb_hooks" ^
    --http="0.0.0.0:8090" ^
    --origins="http://localhost:8080,http://0.0.0.0:8080"

pause