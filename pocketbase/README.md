# PocketBase Development Setup

## One-time Setup

### Linux/macOS

1. **[Download PocketBase](https://pocketbase.io/docs/) executable** for your platform.
2. **Extract the downloaded zip-file** to a directory outside the project:
   ```bash
   mkdir -p ~/dev-services/pocketbase-runtime
   cd ~/dev-services/pocketbase-runtime
   # Extract pocketbase executable here
   chmod +x pocketbase
   ```

3**Set environment variable** (add to your shell profile):
   ```bash
   echo 'export POCKETBASE_HOME=~/dev-services/pocketbase-runtime' >> ~/.bashrc
   # Or for zsh: echo 'export POCKETBASE_HOME=~/dev-services/pocketbase-runtime' >> ~/.zshrc
   source ~/.bashrc  # or restart terminal
   ```

### Windows

1. **[Download PocketBase](https://pocketbase.io/docs/) executable** for Windows.
2. **Extract the downloaded zip-file** to a directory outside the project:
   ```cmd
   mkdir C:\dev-services\pocketbase-runtime
   cd C:\dev-services\pocketbase-runtime
   REM Extract pocketbase.exe here
   ```

3**Set environment variable**:
   - **Option A - Command Prompt (temporary)**:
     ```cmd
     set POCKETBASE_HOME=C:\dev-services\pocketbase-runtime
     ```
   
   - **Option B - PowerShell (temporary)**:
     ```powershell
     $env:POCKETBASE_HOME = "C:\dev-services\pocketbase-runtime"
     ```
   
   - **Option C - Permanent (System Properties)**:
     1. Open System Properties → Advanced → Environment Variables
     2. Add new User Variable: `POCKETBASE_HOME` = `C:\dev-services\pocketbase-runtime`
     3. Restart your terminal/IDE

## Running PocketBase

### Linux/macOS
```bash
cd pocketbase
./start-pocketbase.sh
```

### Windows
```cmd
cd pocketbase
start-pocketbase.bat
```

## Workflow

- **Schema changes**: Made via Admin UI, migrations auto-generated in `pb_migrations/`
- **Static files**: Place in `pb_public/` 
- **Custom hooks**: Place in `pb_hooks/`
- **All changes are version controlled** and shared with the team

## Troubleshooting

### Windows
- Make sure you downloaded `pocketbase.exe` (with .exe extension)
- If you get "command not found", verify `POCKETBASE_HOME` is set correctly
- Use Command Prompt or PowerShell, not Git Bash for the .bat script

### Linux/macOS
- Make sure the script is executable: `chmod +x start-pocketbase.sh`
- Verify `POCKETBASE_HOME` is exported in your current shell session

### Common Issues
- **Port conflicts**: If port 8090 is in use, modify the `--http` parameter in the startup script
- **CORS errors**: Ensure your development server URL is included in the `--origins` parameter
- **Migration errors**: Check that `pb_migrations/` directory exists and is readable
- **Permission errors**: Ensure the script has execute permissions and `POCKETBASE_HOME` directory is writable

## Development Notes

- The `pb_data/` directory (containing the actual database) stays in `POCKETBASE_HOME` and is **not** version controlled
- Only configuration files (`pb_migrations/`, `pb_public/`, `pb_hooks/`) are version controlled
- Each developer can have their own `POCKETBASE_HOME` location
- Database schema changes made through the Admin UI automatically generate migration files
- Always commit migration files after making schema changes