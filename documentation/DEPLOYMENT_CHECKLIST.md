# Docker Deployment Checklist

## ✅ Pre-Deployment Checklist

### Environment
- [ ] Ubuntu machine with Docker installed
- [ ] Docker Compose installed
- [ ] At least 4GB available disk space
- [ ] Ports 9000, 3306, 6379 available
- [ ] Network connectivity working

### Code Preparation
- [ ] Navigate to project: `/home/domain.rage-india.com/amitkumar.b/projects/java/springboot/demo`
- [ ] JAR file exists: `target/demo-0.0.1-SNAPSHOT.jar`
- [ ] If not, build with: `./mvnw clean package -DskipTests=true`
- [ ] Verify JAR size: Should be ~63 MB
- [ ] File timestamp recent (not from old build)

### Configuration Files Updated
- [ ] `docker-compose.yml` - Java service added ✅
- [ ] `application.properties` - Environment variables added ✅
- [ ] Check no hardcoded `localhost` in database config ✅

---

## 🚀 Deployment Steps

### Step 1: Build JAR (One-time)
```bash
cd /home/domain.rage-india.com/amitkumar.b/projects/java/springboot/demo
./mvnw clean package -DskipTests=true
```
- [ ] Build successful (look for "BUILD SUCCESS")
- [ ] JAR file created at `target/demo-0.0.1-SNAPSHOT.jar`
- [ ] File size approximately 63 MB

### Step 2: Navigate to Docker Directory
```bash
cd /home/domain.rage-india.com/amitkumar.b/projects/docker
```
- [ ] Confirm in correct directory (`pwd` shows `/docker`)
- [ ] Verify `docker-compose.yml` exists
- [ ] Verify `.env` file (if using secrets)

### Step 3: Stop Previous Services (if any)
```bash
docker-compose down
```
- [ ] Command completes without errors
- [ ] All containers stopped
- [ ] No port conflicts

### Step 4: Start Docker Services
```bash
docker-compose up -d
```
- [ ] Command completes without errors
- [ ] See "done" at end of output
- [ ] No permission denied errors

### Step 5: Verify Services Running
```bash
docker-compose ps
```
- [ ] All services show "Up" status
- [ ] springboot-app shows port 9000
- [ ] mariadb shows port 3306
- [ ] redis shows port 6379

### Step 6: Wait for Application Startup
```bash
docker-compose logs -f springboot-app | grep "Started DemoApplication"
```
- [ ] Wait for "Started DemoApplication" message
- [ ] Exit with Ctrl+C
- [ ] Should appear within 30 seconds

### Step 7: Test Health Endpoint
```bash
curl http://localhost:9000/actuator/health
```
- [ ] Returns: `{"status":"UP"}`
- [ ] No connection errors
- [ ] Response time < 1 second

### Step 8: Verify Database Connection
```bash
docker-compose logs springboot-app | grep -i "database\|mysql\|connection"
```
- [ ] No error messages visible
- [ ] See successful connection logs
- [ ] Tables created automatically

### Step 9: Test API Endpoint
```bash
curl -X GET http://localhost:9000/api/products
```
- [ ] Returns valid JSON response (may be empty list)
- [ ] Status code 200
- [ ] No 5xx errors

### Step 10: Access Swagger UI
```bash
# Open in browser:
http://localhost:9000/swagger-ui.html
```
- [ ] Swagger UI loads
- [ ] Can see API endpoints listed
- [ ] Try "Execute" on one endpoint

---

## 📊 Post-Deployment Verification

### Application Health
- [ ] Health endpoint returns UP: `curl http://localhost:9000/actuator/health`
- [ ] Swagger UI accessible: `http://localhost:9000/swagger-ui.html`
- [ ] API responds to requests without errors
- [ ] No 5xx errors in application logs

### Database Verification
- [ ] Can access MySQL: `docker-compose exec mariadb mysql -u root -proot ecommerce_db`
- [ ] Tables exist: `SHOW TABLES;`
- [ ] Tables contain expected columns
- [ ] Can insert/query data

### Redis Verification
- [ ] Can access Redis CLI: `docker-compose exec redis redis-cli`
- [ ] Redis responds: `PING` → `PONG`
- [ ] Can set/get keys: `SET test value` → `GET test`

### API Verification
- [ ] Register user succeeds: `POST /api/auth/register`
- [ ] Login works: `POST /api/auth/login`
- [ ] Get products works: `GET /api/products`
- [ ] Create product works: `POST /api/products`
- [ ] Get orders works: `GET /api/orders`

### Performance Checks
- [ ] API response time < 500ms
- [ ] No memory leaks visible in `docker stats`
- [ ] CPU usage reasonable (< 50% when idle)
- [ ] Disk usage within expectations

---

## 🔧 Troubleshooting During Deployment

### Issue: Connection Refused
- [ ] Check services: `docker-compose ps`
- [ ] Check logs: `docker-compose logs`
- [ ] Verify ports: `lsof -i :9000`
- [ ] Solution: `docker-compose down && docker-compose up -d`

### Issue: Database Locked
- [ ] Check database logs: `docker-compose logs mariadb`
- [ ] Clean restart: `docker-compose down -v && docker-compose up -d`
- [ ] Wait 30 seconds before accessing

### Issue: Port Already in Use
- [ ] Find process: `lsof -i :9000`
- [ ] Kill process: `kill -9 <PID>`
- [ ] Or change port in `docker-compose.yml`

### Issue: Disk Space
- [ ] Clean Docker: `docker system prune -a`
- [ ] Check space: `df -h`
- [ ] Remove old volumes: `docker volume prune`

---

## 📋 Testing Scenarios

### Scenario 1: Register and Login
```bash
# Register
curl -X POST http://localhost:9000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test@1234",
    "firstName": "Test",
    "lastName": "User"
  }'

# Login
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@1234"
  }'
```
- [ ] Register succeeds (201 or 200)
- [ ] Login succeeds with token in response

### Scenario 2: Manage Products
```bash
# Get all products
curl http://localhost:9000/api/products

# Create product (as admin, if auth required)
curl -X POST http://localhost:9000/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "A test product",
    "price": 99.99,
    "stock": 100
  }'
```
- [ ] Get products returns list (may be empty initially)
- [ ] Create product succeeds
- [ ] New product appears in list

### Scenario 3: Create Order
```bash
# Create order
curl -X POST http://localhost:9000/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```
- [ ] Order creates successfully
- [ ] Order appears in order list
- [ ] Items correctly associated

---

## 🔍 Logging and Monitoring

### View All Logs
```bash
docker-compose logs
```
- [ ] All logs print without errors
- [ ] Can identify any obvious issues

### Follow App Logs
```bash
docker-compose logs -f springboot-app
```
- [ ] Real-time log updates
- [ ] Can see requests and responses
- [ ] Can identify issues as they happen

### Monitor Resource Usage
```bash
docker stats
```
- [ ] View CPU, memory, network usage
- [ ] All containers using reasonable resources
- [ ] Memory not constantly increasing

### Check Specific Service
```bash
docker-compose logs springboot-app | tail -50
```
- [ ] Last 50 lines of app logs
- [ ] No error messages visible
- [ ] Application started successfully

---

## ✅ Final Deployment Sign-Off

### Functionality
- [ ] Application starts without errors
- [ ] All services healthy and connected
- [ ] API endpoints respond correctly
- [ ] Database operations work
- [ ] Caching working (Redis)

### Performance
- [ ] Response times acceptable (< 500ms)
- [ ] No excessive CPU usage
- [ ] Memory usage stable
- [ ] No obvious bottlenecks

### Reliability
- [ ] Services restart on failure
- [ ] No orphaned processes
- [ ] Proper error handling
- [ ] Graceful shutdown possible

### Documentation
- [ ] All deployment docs reviewed
- [ ] Commands verified working
- [ ] Troubleshooting guide available
- [ ] Team trained on operations

---

## 🚨 Rollback Plan (If Issues)

If deployment fails:

### Quick Rollback
```bash
docker-compose down
docker-compose up -d
```

### Full Reset
```bash
docker-compose down -v  # Remove all data
rm -rf data/            # Remove volumes
docker-compose up -d    # Start fresh
```

### Previous Version (if available)
```bash
git checkout previous-tag
./mvnw clean package -DskipTests=true
docker-compose down -v
docker-compose up -d
```

---

## 📞 Support Documentation

| Issue | File | Command |
|-------|------|---------|
| How to deploy | DOCKER_DEPLOYMENT.md | Read file |
| Quick reference | DOCKER_QUICK_START.md | Read file |
| Commands | DOCKER_COMMANDS.md | Read file |
| View logs | Terminal | `docker-compose logs -f` |
| SSH to container | Terminal | `docker-compose exec springboot-app bash` |
| Database access | Terminal | `docker-compose exec mariadb mysql -u root -proot ecommerce_db` |

---

## 📊 Deployment Statistics

**Build Time**: ~1 minute  
**Deployment Time**: ~2 minutes  
**Startup Time**: ~10-15 seconds  
**Total Time**: ~3-4 minutes  

**Resources Required**:
- CPU: 1+ cores
- RAM: 2GB minimum, 4GB recommended
- Disk: 5GB for Docker images + 1GB for data

**Ports Used**:
- 9000: Application
- 3306: MySQL
- 6379: Redis
- Others: Nginx, PHP, Node (if enabled)

---

## ✨ Post-Deployment Tasks

- [ ] Update team documentation
- [ ] Train team on operations
- [ ] Set up monitoring alerts
- [ ] Set up backup strategy
- [ ] Schedule regular health checks
- [ ] Document environment variables
- [ ] Create runbooks for common tasks

---

## 🎉 Deployment Complete!

Once all checkboxes are ✅, your application is:
- ✅ Successfully deployed
- ✅ Fully operational
- ✅ Ready for production use
- ✅ Properly documented
- ✅ Team trained

---

**Last Updated**: March 24, 2026  
**Version**: 1.0  
**Status**: Ready for Deployment ✅
