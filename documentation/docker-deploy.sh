#!/bin/bash

# Docker Deployment Setup Script for Spring Boot Application
# This script automates the deployment process

set -e

echo "================================"
echo "Spring Boot Docker Setup"
echo "================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Build the JAR
echo -e "${BLUE}[1/4] Building the JAR file...${NC}"
cd /home/domain.rage-india.com/amitkumar.b/projects/java/springboot/demo

if [ -f "target/demo-0.0.1-SNAPSHOT.jar" ]; then
    echo -e "${GREEN}✓ JAR file already exists${NC}"
else
    echo "Building JAR..."
    ./mvnw clean package -DskipTests=true
    echo -e "${GREEN}✓ JAR built successfully${NC}"
fi

echo ""
echo -e "${BLUE}[2/4] Verifying JAR file...${NC}"
if [ -f "target/demo-0.0.1-SNAPSHOT.jar" ]; then
    JAR_SIZE=$(du -h target/demo-0.0.1-SNAPSHOT.jar | cut -f1)
    echo -e "${GREEN}✓ JAR file found: $JAR_SIZE${NC}"
else
    echo -e "${YELLOW}✗ JAR file not found!${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}[3/4] Starting Docker services...${NC}"
cd /home/domain.rage-india.com/amitkumar.b/projects/docker

# Stop existing services
if docker-compose ps | grep -q "springboot-app"; then
    echo "Stopping existing containers..."
    docker-compose down
fi

# Start services
echo "Starting Docker Compose services..."
docker-compose up -d

echo -e "${GREEN}✓ Docker services started${NC}"

echo ""
echo -e "${BLUE}[4/4] Waiting for services to be healthy...${NC}"

# Wait for MariaDB to be ready
echo "Waiting for MariaDB..."
for i in {1..30}; do
    if docker-compose exec -T mariadb mysql -u root -proot -e "SELECT 1;" &>/dev/null; then
        echo -e "${GREEN}✓ MariaDB is ready${NC}"
        break
    fi
    echo "  Attempt $i/30..."
    sleep 1
done

# Wait for Redis to be ready
echo "Waiting for Redis..."
for i in {1..30}; do
    if docker-compose exec -T redis redis-cli PING &>/dev/null; then
        echo -e "${GREEN}✓ Redis is ready${NC}"
        break
    fi
    echo "  Attempt $i/30..."
    sleep 1
done

# Wait for Spring Boot App
echo "Waiting for Spring Boot application..."
for i in {1..60}; do
    if curl -s http://localhost:9000/actuator/health | grep -q "UP"; then
        echo -e "${GREEN}✓ Spring Boot application is ready${NC}"
        break
    fi
    echo "  Attempt $i/60..."
    sleep 1
done

echo ""
echo "================================"
echo -e "${GREEN}Setup Complete!${NC}"
echo "================================"
echo ""
echo "Services are running at:"
echo -e "  ${BLUE}Application${NC}: http://localhost:9000"
echo -e "  ${BLUE}Swagger UI${NC}: http://localhost:9000/swagger-ui.html"
echo -e "  ${BLUE}Health Check${NC}: http://localhost:9000/actuator/health"
echo -e "  ${BLUE}MySQL${NC}: localhost:3306"
echo -e "  ${BLUE}Redis${NC}: localhost:6379"
echo ""
echo "View logs with:"
echo "  docker-compose logs -f springboot-app"
echo ""
echo "Stop services with:"
echo "  docker-compose down"
echo ""
