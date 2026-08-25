# Docker Setup - Quick Reference

## Problem Solved ✅
Database connectivity issues when running Java in Docker are fixed by:
1. Using container names instead of `localhost`
2. Setting proper environment variables
3. Using Docker network for service discovery
4. Making services depend on each other

## Quick Start (3 Steps)

### Step 1: Build the JAR
```bash
cd /home/domain.rage-india.com/amitkumar.b/projects/java/springboot/demo
./mvnw clean package -DskipTests=true
```

### Step 2: Start Docker Services
```bash
cd /home/domain.rage-india.com/amitkumar.b/projects/docker
docker-compose up -d
```

### Step 3: Verify Everything Works
```bash
# Check if services are running
docker-compose ps

# Check if app is healthy
curl http://localhost:9000/actuator/health
```

## What Changed

### 1. docker-compose.yml - Java Service
**Before**: Tried to connect to `localhost`
**After**: Uses `mariadb` and `redis` container names with proper environment variables

```yaml
java:
  image: openjdk:11-jre-slim
  container_name: springboot-app
  environment:
    - SPRING_DATASOURCE_URL=jdbc:mysql://mariadb:3306/ecommerce_db
    - SPRING_DATASOURCE_USERNAME=root
    - SPRING_DATASOURCE_PASSWORD=root
    - SPRING_REDIS_HOST=redis
    - SPRING_REDIS_PORT=6379
  depends_on:
    - mariadb
    - redis
```

### 2. application.properties - Environment Variables
**Before**: Hardcoded `localhost`
**After**: Uses environment variables from Docker

```properties
spring.datasource.url=jdbc:mysql://${SPRING_DATASOURCE_HOST:localhost}:${SPRING_DATASOURCE_PORT:3306}/${SPRING_DATASOURCE_DB:ecommerce_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}
spring.redis.host=${SPRING_REDIS_HOST:localhost}
spring.redis.port=${SPRING_REDIS_PORT:6379}
```

## How Docker Service Discovery Works

Inside Docker network:
- `mariadb` → resolves to MariaDB container
- `redis` → resolves to Redis container
- `springboot-app` → resolves to Spring Boot container

All containers are on the same `app-tier` network and can communicate by container name.

## Common Commands

### View Running Services
```bash
docker-compose ps
```

### View Logs
```bash
# All logs
docker-compose logs

# Follow logs
docker-compose logs -f

# Only app logs
docker-compose logs -f springboot-app

# Only database logs
docker-compose logs -f mariadb
```

### Restart Services
```bash
docker-compose restart
docker-compose restart springboot-app
docker-compose restart mariadb
```

### Stop Services
```bash
# Stop but keep data
docker-compose stop

# Stop and remove containers (keep volumes)
docker-compose down

# Stop and remove everything (including database)
docker-compose down -v
```

### Access Services

#### MySQL Terminal
```bash
docker-compose exec mariadb mysql -u root -proot ecommerce_db
```

#### Redis CLI
```bash
docker-compose exec redis redis-cli
```

#### Check Connectivity from App
```bash
docker-compose exec springboot-app ping mariadb
docker-compose exec springboot-app redis-cli ping
```

## API Endpoints

Once running:

### Health Check
```bash
curl http://localhost:9000/actuator/health
```

### Swagger UI
```
http://localhost:9000/swagger-ui.html
```

### API Docs
```
http://localhost:9000/v3/api-docs
```

### Register User
```bash
curl -X POST http://localhost:9000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Login
```bash
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

## Troubleshooting

### App Can't Connect to Database
**Check**: 
```bash
docker-compose logs -f springboot-app | grep -i "connection"
```

**Solution**: Ensure mariadb service is running:
```bash
docker-compose ps mariadb
docker-compose logs mariadb
```

### Port Already in Use
**Error**: `ERROR: driver failed programming external connectivity`

**Solution**: Change port in docker-compose.yml:
```yaml
ports:
  - "8080:9000"  # Use port 8080 instead of 9000
```

### Database Already Exists
**Error**: `Can't create database 'ecommerce_db'`

**Solution**:
```bash
docker-compose down -v
docker-compose up -d
```

### Services Won't Start
**Check all logs**:
```bash
docker-compose logs
```

**Clean and rebuild**:
```bash
docker-compose down -v
docker-compose up -d
```

## Files Modified

1. ✅ `/docker/docker-compose.yml` - Added springboot-app service
2. ✅ `application.properties` - Added environment variable support

## Next Steps

1. ✅ JAR is built and ready
2. ✅ Docker configuration is complete
3. Start with: `docker-compose up -d`
4. Test with: `curl http://localhost:9000/actuator/health`
5. Access Swagger: `http://localhost:9000/swagger-ui.html`

## Support

For detailed information, see: `DOCKER_DEPLOYMENT.md`

---

**Date**: March 24, 2026
**Status**: ✅ Ready for Docker Deployment
