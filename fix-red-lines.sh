#!/bin/bash
# fix-red-lines.sh - Resolve VS Code Java red line import errors

set -e

echo "🔧 Fixing VS Code Java red line issues..."
echo ""

# Step 1: Kill Java language server
echo "1️⃣  Stopping Java language server..."
pkill -f "java.*jdt" 2>/dev/null || echo "   No jdt processes running"
sleep 2

# Step 2: Remove stale IDE metadata
echo "2️⃣  Clearing IDE metadata..."
rm -f .classpath .project .c9 2>/dev/null || true
rm -rf .settings 2>/dev/null || true
echo "   ✓ Removed: .classpath, .project, .settings"

# Step 3: Clear JDT cache (VS Code workspace storage)
echo "3️⃣  Clearing JDT language server cache..."
JDT_CACHE="$HOME/.config/Code/User/workspaceStorage/ea994e432c51aadaee10b02e47030e71/redhat.java/jdt_ws"
if [ -d "$JDT_CACHE" ]; then
  rm -rf "$JDT_CACHE"
  echo "   ✓ Cleared JDT cache"
else
  echo "   ℹ️  JDT cache not found (normal for new workspace)"
fi

# Step 4: Maven clean and compile
echo "4️⃣  Resolving Maven dependencies and compiling..."
./mvnw clean dependency:resolve compile -q
echo "   ✓ Maven operations completed"

# Step 5: Done
echo ""
echo "✅ All fixes applied!"
echo ""
echo "👉 Next steps:"
echo "   1. Restart VS Code or run: Ctrl+Shift+P → 'Reload Window'"
echo "   2. Run: Ctrl+Shift+P → 'Maven: Reload Projects'"
echo "   3. Wait 30 seconds for language server to reinitialize"
echo ""
echo "Red lines should now disappear. If they persist:"
echo "   - Check: Ctrl+Shift+P → 'Java: Open All Logs Folder'"
echo "   - Look for errors in log files"
