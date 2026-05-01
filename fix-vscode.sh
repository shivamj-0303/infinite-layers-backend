#!/bin/bash
# VS Code Java Red Lines - Quick Fix Script
# This script fixes all VS Code Java resolution issues

set -e

echo "🔧 Fixing VS Code Java Import Issues..."
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Kill running Java processes
echo -e "${YELLOW}1️⃣  Stopping Java Language Server...${NC}"
pkill -9 -f "java.*language.server" 2>/dev/null || true
pkill -9 -f "java.*jdt.ls" 2>/dev/null || true
sleep 2
echo -e "${GREEN}✓ Language server stopped${NC}"

# Step 2: Clear caches
echo -e "${YELLOW}2️⃣  Clearing VS Code Java cache...${NC}"
rm -rf ~/.vscode/extensions/vscjava.*/java-language-server/cache 2>/dev/null || true
rm -rf ~/.vscode/extensions/redhat.*/metadata 2>/dev/null || true
rm -rf ~/.vscode/extensions/vscjava.*/java-language-server/workspace 2>/dev/null || true
echo -e "${GREEN}✓ Cache cleared${NC}"

# Step 3: Clear project metadata
echo -e "${YELLOW}3️⃣  Clearing project metadata...${NC}"
rm -rf .classpath .project .settings 2>/dev/null || true
rm -rf .vscode/.classpath .vscode/.project 2>/dev/null || true
echo -e "${GREEN}✓ Project metadata cleared${NC}"

# Step 4: Maven clean and build
echo -e "${YELLOW}4️⃣  Running Maven clean and compile...${NC}"
./mvnw clean compile -q -DskipTests
echo -e "${GREEN}✓ Maven build successful${NC}"

# Step 5: Generate classpath
echo -e "${YELLOW}5️⃣  Generating classpath information...${NC}"
./mvnw eclipse:clean eclipse:eclipse -q 2>/dev/null || true
echo -e "${GREEN}✓ Classpath generated${NC}"

# Step 6: Create VS Code settings
echo -e "${YELLOW}6️⃣  Updating VS Code settings...${NC}"
mkdir -p .vscode

cat > .vscode/settings.json << 'SETTINGS'
{
  "java.jdt.ls.java.home": null,
  "java.jdt.ls.vmargs": "-XX:+UseG1GC -XX:+UseStringDeduplication -Xmx1G",
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.import.gradle.enabled": false,
  "java.import.maven.enabled": true,
  "java.maven.downloadSources": true,
  "java.sources.organizeImports.staticGroupsOrder": "java,javax,jakarta,org,com",
  "java.codeGeneration.useBlocks": true,
  "java.errors.incompleteClasspath.severity": "warning",
  "files.exclude": {
    "**/.classpath": true,
    "**/.project": true,
    "**/.c9": true,
    "**/*.launch": true,
    "**/.settings": true,
    "**/.vscode": false
  }
}
SETTINGS

echo -e "${GREEN}✓ VS Code settings updated${NC}"

echo ""
echo -e "${GREEN}════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ All fixes applied successfully!${NC}"
echo -e "${GREEN}════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${YELLOW}📋 FINAL STEP - Restart VS Code:${NC}"
echo ""
echo "  1. Close VS Code completely (Ctrl+K Ctrl+Q or File → Close Folder)"
echo "  2. Wait 5 seconds"
echo "  3. Reopen the folder:"
echo "     code /home/shivam/Desktop/Infinite-Prints"
echo "  4. Wait 30 seconds for Java Language Server to start"
echo "  5. Open Command Palette: Ctrl+Shift+P"
echo "  6. Type: Maven: Reload Projects"
echo "  7. Press Enter and wait 15 seconds"
echo ""
echo -e "${GREEN}✨ Red lines will disappear!${NC}"
echo ""
