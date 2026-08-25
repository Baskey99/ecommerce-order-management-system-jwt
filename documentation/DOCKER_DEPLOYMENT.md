# Docker Deployment Guide - Spring Boot E-Commerce Application

## Overview
This guide explains how to run your Spring Boot application in Docker with proper MySQL and Redis connectivity.

## Prerequisites
- Docker and Docker Compose installed on Ubuntu
- The JAR file built: `demo-0.0.1-SNAPSHOT.jar`

## Quick Start

### 1. Rebuild the JAR (Optional)
If you haven't built the JAR yet:
```bash
cd /home/domain.rage-india.com/amitkumar.b/projects/java/springboot/demo
./mvnw clean package -DskipTests=true
```

### 2. Start All Services with Docker Compose
From the docker directory:
```bash
cd /home/domain.rage-india.com/amitkumar.b/projects/docker
docker-compose up -d
```

This will start:
- **MariaDB** (MySQL) - port 3306
- **Redis** - port 6379
- **Spring Boot App** - port 9000

### 3. Verify Services are Running
```bash
docker-compose ps
```

You should see all services running:
```
CONTAINER ID   IMAGE                        PORTS                    STATUS
...            mariadb:11.7.2               0.0.0.0:3306->3306/tcp   Up
...            redis:7.4.2                  0.0.0.0:6379->6379/tcp   Up
...            openjdk:11-jre-slim          0.0.0.0:9000->9000/tcp   Up
```

### 4. Check Application Logs
```bash
docker-compose logs -f springboot-app
```

Wait for messages like:
```
Tomcat initialized with port(s): 9000 (http)
Started DemoApplication in X.XXX seconds
```

### 5. Access the Application

#### API Endpoints:
- **Health Check**: http://localhost:9000/actuator/health
- **Swagger UI**: http://localhost:9000/swagger-ui.html
- **API Docs**: http://localhost:9000/v3/api-docs

#### Test API:
```bash
# Register a new user
curl -X POST http://localhost:9000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'

# Login
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

## Docker Compose Configuration Details

The `docker-compose.yml` contains the following configuration for the Java application:

```yaml
java:
  image: openjdk:11-jre-slim
  container_name: springboot-app
  ports:
    - "9000:9000"
  volumes:
    - ../java/springboot/demo/target:/app
  environment:
    - SPRING_DATASOURCE_URL=jdbc:mysql://mariadb:3306/ecommerce_db
    - SPRING_DATASOURCE_USERNAME=root
    - SPRING_DATASOURCE_PASSWORD=root
    - SPRING_REDIS_HOST=redis
    - SPRING_REDIS_PORT=6379
  depends_on:
    - mariadb
    - redis
  command: ["java", "-jar", "demo-0.0.1-SNAPSHOT.jar"]
```

### Key Features:
1. **Service Discovery**: Uses container names (mariadb, redis) instead of localhost
2. **Environment Variables**: Configurable database and Redis connections
3. **Volume Mounting**: JAR file from host machine
4. **Dependency Management**: Ensures DB starts before the app

## Environment Variables

You can override these in docker-compose.yml:

```yaml
environment:
  - SPRING_DATASOURCE_HOST=mariadb        # MySQL host
  - SPRING_DATASOURCE_PORT=3306            # MySQL port
  - SPRING_DATASOURCE_DB=ecommerce_db      # Database name
  - SPRING_DATASOURCE_USERNAME=root        # DB user
  - SPRING_DATASOURCE_PASSWORD=root        # DB password
  - SPRING_REDIS_HOST=redis                # Redis host
  - SPRING_REDIS_PORT=6379                 # Redis port
  - SPRING_JPA_HIBERNATE_DDL_AUTO=update   # create-drop|update|validate
```

## Troubleshooting

### 1. Connection Refused Error
**Problem**: `java.net.ConnectException: Connection refused`

**Solution**: Ensure all services are running:
```bash
docker-compose ps
docker-compose logs mariadb
docker-compose logs redis
```

### 2. Database Already Exists Error
**Problem**: `ERROR 1007 (HY000): Can't create database 'ecommerce_db'`

**Solution**: Clean and restart:
```bash
docker-compose down -v
docker-compose up -d
```

The `-v` flag removes all volumes (databases).

### 3. Port Already in Use
**Problem**: `Error: Port 9000 is already in use`

**Solution**: Change the port in docker-compose.yml:
```yaml
ports:
  - "8080:9000"  # Use 8080 instead of 9000
```

### 4. Check Logs
View application logs:
```bash
docker-compose logs -f springboot-app
```

View database logs:
```bash
docker-compose logs -f mariadb
```

View Redis logs:
```bash
docker-compose logs -f redis
```

## Useful Commands

### Stop All Services
```bash
docker-compose stop
```

### Stop and Remove All Services
```bash
docker-compose down
```

### Remove All Services and Data
```bash
docker-compose down -v
```

### Restart a Specific Service
```bash
docker-compose restart springboot-app
```

### Execute Commands in Container
```bash
# Access MySQL directly
docker-compose exec mariadb mysql -u root -proot ecommerce_db

# Check Redis
docker-compose exec redis redis-cli PING
```

### View Real-time Logs
```bash
docker-compose logs -f --tail=100
```

## Database Management

### Access MySQL Terminal
```bash
docker-compose exec mariadb mysql -u root -proot ecommerce_db
```

### Common MySQL Commands
```sql
-- Show tables
SHOW TABLES;

-- View users table
SELECT * FROM users;

-- View products
SELECT * FROM products;

-- View orders
SELECT * FROM orders;

-- View order items
SELECT * FROM order_items;
```

## Network Communication

Services communicate using their container names:
- Application → Database: `jdbc:mysql://mariadb:3306/ecommerce_db`
- Application → Redis: `redis://redis:6379`

All services are on the same `app-tier` network defined in docker-compose.yml.

## Performance Tips

1. **Increase JVM Memory** (if needed):
```yaml
environment:
  - JAVA_OPTS=-Xms512m -Xmx1024m
```

2. **Use create-drop for Development**:
```yaml
environment:
  - SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop
```

3. **Persist Database Data** between restarts:
```bash
docker-compose stop
docker-compose start
```

## Production Considerations

1. **Use .env file** for sensitive data:
```bash
# .env
MYSQL_ROOT_PASSWORD=secure_password
REDIS_PASSWORD=secure_password
```

2. **Use separate docker-compose.prod.yml**:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

3. **Add health checks**:
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:9000/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
```

## Next Steps

1. ✅ Build the JAR file
2. ✅ Update docker-compose.yml (DONE)
3. ✅ Update application.properties (DONE)
4. Start Docker services
5. Test the API endpoints
6. Deploy to production

## Support

For issues, check:
- Application logs: `docker-compose logs -f springboot-app`
- Database connectivity: `docker-compose exec springboot-app ping mariadb`
- Redis connectivity: `docker-compose exec springboot-app redis-cli ping`

---

**Last Updated**: March 24, 2026
**Version**: 1.0
