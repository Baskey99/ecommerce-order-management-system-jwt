# Docker Deployment - Step by Step Commands

## Prerequisites
- Ubuntu machine with Docker and Docker Compose installed
- Git access to the project

## Step 1: Navigate to Project
```bash
cd /home/domain.rage-india.com/amitkumar.b/projects/java/springboot/demo
```

## Step 2: Build JAR (if not already built)
```bash
./mvnw clean package -DskipTests=true
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXXs
[INFO] Building jar: /app/springboot/demo/target/demo-0.0.1-SNAPSHOT.jar
```

## Step 3: Verify JAR File
```bash
ls -lh target/demo-0.0.1-SNAPSHOT.jar
```

Expected: File should be ~63MB

## Step 4: Navigate to Docker Directory
```bash
cd /home/domain.rage-india.com/amitkumar.b/projects/docker
```

## Step 5: Stop Previous Services (if any)
```bash
docker-compose down
```

## Step 6: Start All Services
```bash
docker-compose up -d
```

Expected output:
```
Creating springboot-app ... done
Creating mariadb        ... done
Creating redis          ... done
... (other services)
```

## Step 7: Check Services Status
```bash
docker-compose ps
```

You should see:
```
NAME              STATUS           PORTS
springboot-app    Up 1 minute      0.0.0.0:9000->9000/tcp
mariadb           Up 2 minutes     0.0.0.0:3306->3306/tcp
redis             Up 2 minutes     0.0.0.0:6379->6379/tcp
... (other services)
```

## Step 8: Wait for App to Start
```bash
# Watch the logs until you see "Started DemoApplication"
docker-compose logs -f springboot-app
```

Stop watching with: `Ctrl + C`

## Step 9: Verify App is Healthy
```bash
curl http://localhost:9000/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

## Step 10: Access the Application

### Option A: Via Browser
- **Swagger UI**: http://localhost:9000/swagger-ui.html
- **API Docs**: http://localhost:9000/v3/api-docs
- **Health**: http://localhost:9000/actuator/health

### Option B: Via API

Register a user:
```bash
curl -X POST http://localhost:9000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "Admin@123456",
    "firstName": "Admin",
    "lastName": "User"
  }'
```

Login:
```bash
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123456"
  }'
```

## Troubleshooting Commands

### Check App Logs
```bash
docker-compose logs -f springboot-app | head -100
```

### Check Database Logs
```bash
docker-compose logs -f mariadb
```

### Check Redis Logs
```bash
docker-compose logs -f redis
```

### Test Database Connection from App
```bash
docker-compose exec springboot-app ping mariadb
```

### Access MySQL Terminal
```bash
docker-compose exec mariadb mysql -u root -proot ecommerce_db
```

### Access Redis CLI
```bash
docker-compose exec redis redis-cli
```

### Restart App
```bash
docker-compose restart springboot-app
```

### Rebuild and Restart
```bash
docker-compose down
docker-compose up -d
```

## Common Issues and Solutions

### Issue: Connection refused
```bash
# Check if services are running
docker-compose ps

# Check if port is in use
lsof -i :9000

# Solution: Stop conflicting service
kill -9 <PID>
```

### Issue: Database locked
```bash
# Solution: Remove volumes and restart
docker-compose down -v
docker-compose up -d
```

### Issue: Out of disk space
```bash
# Clean Docker
docker system prune -a

# Check disk usage
docker system df
```

### Issue: App crashes on startup
```bash
# Check detailed logs
docker-compose logs springboot-app | tail -50

# Restart with fresh database
docker-compose down -v
docker-compose up -d
```

## Verification Checklist

- [ ] JAR file exists: `ls -lh target/demo-0.0.1-SNAPSHOT.jar`
- [ ] Docker running: `docker ps`
- [ ] Services started: `docker-compose ps`
- [ ] App healthy: `curl http://localhost:9000/actuator/health`
- [ ] Swagger accessible: `curl http://localhost:9000/swagger-ui.html`
- [ ] Can register user: (see Step 10 Option B)
- [ ] Can login: (see Step 10 Option B)

## Quick Commands Summary

```bash
# Build
cd ~/projects/java/springboot/demo && ./mvnw clean package -DskipTests=true

# Deploy
cd ~/projects/docker && docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f springboot-app

# Stop
docker-compose down

# Full reset
docker-compose down -v && docker-compose up -d

# Test API
curl http://localhost:9000/actuator/health
```

## What Happens After Running docker-compose up -d

1. **MariaDB starts** (takes ~5 seconds)
   - Listens on port 3306
   - Creates `ecommerce_db` database

2. **Redis starts** (takes ~2 seconds)
   - Listens on port 6379

3. **Spring Boot app starts** (takes ~10-15 seconds)
   - Waits for MariaDB to be ready
   - Waits for Redis to be ready
   - Initializes JPA entities
   - Creates tables (if using create-drop mode)
   - Starts on port 9000

4. **All services ready** (~20-30 seconds total)
   - API accessible at http://localhost:9000
   - Database accessible at localhost:3306
   - Redis accessible at localhost:6379

## Monitoring

### Real-time Dashboard
```bash
watch -n 1 docker-compose ps
```

### Container Resource Usage
```bash
docker stats
```

### Docker Compose Logs
```bash
# Follow all logs
docker-compose logs -f

# Last 100 lines of app logs
docker-compose logs --tail 100 springboot-app

# Logs from specific time
docker-compose logs --since 5m springboot-app
```

## Performance Tips

### Increase JVM Memory
Edit `docker-compose.yml`:
```yaml
java:
  environment:
    - JAVA_OPTS=-Xms1024m -Xmx2048m
```

### Use H2 for Development
Set environment variable:
```yaml
environment:
  - SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop
  - SPRING_PROFILES_ACTIVE=h2
```

### Reduce Startup Time
Skip tests during build:
```bash
./mvnw clean package -DskipTests=true
```

## Production Deployment

For production, consider:

1. Use `.env` file for secrets:
```bash
MYSQL_ROOT_PASSWORD=secure_password
JAVA_OPTS=-Xms2048m -Xmx4096m
```

2. Use health checks:
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:9000/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```

3. Use restart policy:
```yaml
restart_policy:
  condition: on-failure
  delay: 5s
  max_attempts: 5
```

---

**Last Updated**: March 24, 2026
**Version**: 1.0
**Status**: ✅ Complete and Tested
