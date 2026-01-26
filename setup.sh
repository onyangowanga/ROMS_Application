#!/bin/bash

echo "================================================"
echo "ROMS - Recruitment Operations Management System"
echo "Setup Script for Linux/Mac"
echo "================================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Java version
echo "[1/6] Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo -e "${GREEN}[OK]${NC} Java version $JAVA_VERSION is installed"
else
    echo -e "${RED}[ERROR]${NC} Java is not installed"
    echo "Please install Java 17 or higher from: https://adoptium.net/"
    exit 1
fi
echo ""

# Check Maven
echo "[2/6] Checking Maven installation..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -v | head -n 1)
    echo -e "${GREEN}[OK]${NC} $MVN_VERSION is installed"
else
    echo -e "${RED}[ERROR]${NC} Maven is not installed"
    echo "Please install Maven from: https://maven.apache.org/download.cgi"
    exit 1
fi
echo ""

# Check PostgreSQL
echo "[3/6] Checking PostgreSQL installation..."
if command -v psql &> /dev/null; then
    PSQL_VERSION=$(psql --version)
    echo -e "${GREEN}[OK]${NC} $PSQL_VERSION is installed"
else
    echo -e "${YELLOW}[WARNING]${NC} PostgreSQL is not installed or not in PATH"
    echo "Please install PostgreSQL 15+ or use Docker Compose:"
    echo "  docker-compose up -d"
fi
echo ""

# Build project
echo "[4/6] Building project with Maven..."
mvn clean install -DskipTests
if [ $? -eq 0 ]; then
    echo -e "${GREEN}[OK]${NC} Build successful"
else
    echo -e "${RED}[ERROR]${NC} Build failed"
    exit 1
fi
echo ""

# Setup environment file
echo "[5/6] Setting up environment configuration..."
if [ ! -f ".env" ]; then
    if [ -f ".env.example" ]; then
        cp .env.example .env
        echo -e "${GREEN}[OK]${NC} Created .env file from template"
        echo -e "${YELLOW}[ACTION REQUIRED]${NC} Please edit .env file with your configuration"
    else
        echo -e "${YELLOW}[WARNING]${NC} .env.example not found"
    fi
else
    echo -e "${GREEN}[OK]${NC} .env file already exists"
fi
echo ""

# Instructions
echo "[6/6] Setup complete!"
echo ""
echo "================================================"
echo "Next Steps:"
echo "================================================"
echo ""
echo "1. Make sure PostgreSQL is running:"
echo "   - Check status: sudo systemctl status postgresql"
echo "   - Start: sudo systemctl start postgresql"
echo "   - Or use Docker: docker-compose up -d"
echo ""
echo "2. Update configuration in:"
echo "   - src/main/resources/application.yaml"
echo "   - .env (if using environment variables)"
echo ""
echo "3. Create database (if not using Docker):"
echo "   psql -U postgres"
echo "   CREATE DATABASE roms_db;"
echo "   CREATE USER roms_user WITH PASSWORD 'roms_password';"
echo "   GRANT ALL PRIVILEGES ON DATABASE roms_db TO roms_user;"
echo ""
echo "4. Run the application:"
echo "   mvn spring-boot:run"
echo ""
echo "5. Access the API:"
echo "   http://localhost:8080/api"
echo ""
echo "6. Quick test:"
echo "   See QUICKSTART.md for API examples"
echo ""
echo "================================================"
