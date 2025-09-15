#!/bin/bash

# Check if POCKETBASE_HOME is defined
if [ -z "$POCKETBASE_HOME" ]; then
    echo "❌ Error: POCKETBASE_HOME environment variable is not set"
    echo "Please set it to the directory containing your pocketbase executable and pb_data"
    echo "Example: export POCKETBASE_HOME=~/dev-services/pocketbase-runtime"
    exit 1
fi

# Check if POCKETBASE_HOME directory exists
if [ ! -d "$POCKETBASE_HOME" ]; then
    echo "❌ Error: POCKETBASE_HOME directory does not exist: $POCKETBASE_HOME"
    exit 1
fi

# Check if pocketbase executable exists
if [ ! -f "$POCKETBASE_HOME/pocketbase" ]; then
    echo "❌ Error: pocketbase executable not found in $POCKETBASE_HOME"
    exit 1
fi

# Get the absolute path to the directory where this script is located, which should be the project's
# pocketbase config directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "🚀 Starting PocketBase..."
echo "📁 Runtime directory: $POCKETBASE_HOME"
echo "⚙️  Config directory: $SCRIPT_DIR"

# Create directories if they don't exist
mkdir -p "$SCRIPT_DIR/pb_migrations"
mkdir -p "$SCRIPT_DIR/pb_public"
mkdir -p "$SCRIPT_DIR/pb_hooks"

# Start pocketbase with external configuration
cd "$POCKETBASE_HOME" || exit 1
./pocketbase serve \
    --dir="$POCKETBASE_HOME/pb_data/event-demo" \
    --migrationsDir="$SCRIPT_DIR/pb_migrations" \
    --publicDir="$SCRIPT_DIR/pb_public" \
    --hooksDir="$SCRIPT_DIR/pb_hooks" \
    --http="0.0.0.0:8090" \
    --origins="http://localhost:8080,http://0.0.0.0:8080"