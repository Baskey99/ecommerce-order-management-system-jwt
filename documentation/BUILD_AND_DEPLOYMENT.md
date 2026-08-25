# 🏗️ Build & Deployment Guide

## 📦 Building the Application

### Prerequisites
- Java 8 or higher: `java -version`
- Maven 3.6+: `mvn -version`
- MySQL 5.7+: Verify database is running

### Step 1: Build with Maven

```bash
cd /path/to/demo
mvn clean install
```

**What this does:**
- Cleans previous builds
- Downloads dependencies
- Compiles Java code
- Runs tests
- Packages as JAR

### Step 2: Build JAR Only (Skip Tests)

```bash
mvn clean package -DskipTests
```

**Output:**
```
target/demo-0.0.1-SNAPSHOT.jar
```

### Step 3: Verify Build

```bash
ls -la target/demo-0.0.1-SNAPSHOT.jar
```

---

## 🚀 Running the Application

### Option 1: Maven
```bash
mvn spring-boot:run
```

### Option 2: Direct JAR Execution
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Option 3: With Custom Configuration
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db \
  --spring.datasource.username=root \
  --spring.datasource.password=yourpassword \
  --server.port=9000
```

### Option 4: With Environment Variables
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/ecommerce_db
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=yourpassword
export SERVER_PORT=9000

mvn spring-boot:run
```

---

## ✅ Verification

After starting the application, verify it's running:

### 1. Health Check
```bash
curl http://localhost:9000/actuator/health
```

Expected Response:
```json
{
  "status": "UP"
}
```

### 2. Test an Endpoint
```bash
curl -X GET http://localhost:9000/api/products \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

### 3. Access Swagger UI
Open browser: `http://localhost:9000/swagger-ui.html`

### 4. Check Logs
```bash
# In console output, look for:
# Started DemoApplication in X seconds (JVM running for Y seconds)
```

---

## 🐳 Docker Deployment

### Step 1: Create Dockerfile

Create `Dockerfile` in project root:

```dockerfile
FROM openjdk:8-jre-slim

# Copy the JAR file
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 9000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Step 2: Build Docker Image

```bash
docker build -t ecommerce-app:latest .
```

### Step 3: Create Docker Compose (Optional)

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:5.7
    container_name: ecommerce-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ecommerce_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - ecommerce-network

  redis:
    image: redis:latest
    container_name: ecommerce-redis
    ports:
      - "6379:6379"
    networks:
      - ecommerce-network

  app:
    build: .
    container_name: ecommerce-app
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/ecommerce_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      SERVER_PORT: 9000
    ports:
      - "9000:9000"
    depends_on:
      - mysql
      - redis
    networks:
      - ecommerce-network

volumes:
  mysql_data:

networks:
  ecommerce-network:
```

### Step 4: Run with Docker Compose

```bash
docker-compose up -d
```

### Step 5: Verify Docker Container

```bash
docker ps
docker logs ecommerce-app
```

### Step 6: Stop Docker Containers

```bash
docker-compose down
```

---

## ☁️ Cloud Deployment

### AWS Deployment

#### 1. Using AWS Elastic Beanstalk

```bash
# Install EB CLI
pip install awsebcli --upgrade --user

# Initialize
eb init -p java-8 ecommerce-app

# Create environment and deploy
eb create ecommerce-env
eb deploy
```

#### 2. Using AWS EC2

```bash
# 1. Launch EC2 instance (Java 8, Ubuntu)
# 2. SSH into instance
ssh -i your-key.pem ubuntu@your-instance-ip

# 3. Install Java
sudo apt-get update
sudo apt-get install openjdk-8-jre -y

# 4. Install MySQL
sudo apt-get install mysql-server -y

# 5. Create database
mysql -u root -p < schema.sql

# 6. Copy JAR and run
scp -i your-key.pem target/demo-0.0.1-SNAPSHOT.jar ubuntu@your-instance-ip:/home/ubuntu/

# 7. SSH and run
java -jar demo-0.0.1-SNAPSHOT.jar
```

### Google Cloud Deployment

```bash
# Deploy to Cloud Run
gcloud run deploy ecommerce-app \
  --source . \
  --platform managed \
  --memory 512Mi \
  --set-env-vars SPRING_DATASOURCE_URL=jdbc:mysql://YOUR_CLOUD_SQL_IP:3306/ecommerce_db
```

### Azure Deployment

```bash
# Deploy to App Service
az webapp up --resource-group myResourceGroup \
  --name ecommerce-app \
  --runtime "JAVA|8-jre8"
```

---

## 📊 Performance Tuning

### JVM Optimization

```bash
java -Xms512m -Xmx1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar target/demo-0.0.1-SNAPSHOT.jar
```

**Parameters:**
- `-Xms512m`: Initial heap size
- `-Xmx1024m`: Maximum heap size
- `-XX:+UseG1GC`: Use G1 garbage collector
- `-XX:MaxGCPauseMillis=200`: GC pause time

### Database Connection Pool

In `application.properties`:

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

---

## 📋 Troubleshooting Builds

### Issue: Maven Build Fails

```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -U
```

### Issue: Compilation Errors

```bash
# Check Java version
java -version

# Verify Maven
mvn -version

# Full clean build
mvn clean compile
```

### Issue: Tests Fail

```bash
# Run specific test
mvn test -Dtest=UserServiceTest

# Skip tests (for debugging)
mvn clean package -DskipTests
```

### Issue: JAR Execution Fails

```bash
# Verify JAR exists
ls -la target/*.jar

# Check Java classpath
java -cp target/demo-0.0.1-SNAPSHOT.jar com.example.demo.DemoApplication

# Check logs
cat logs/spring.log
```

---

## 📈 Monitoring & Logging

### Enable Debug Logging

In `application.properties`:

```properties
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
logging.level.org.springframework.security=DEBUG
logging.file.name=logs/application.log
```

### Monitor Application

```bash
# Health check
curl http://localhost:9000/actuator/health

# Metrics
curl http://localhost:9000/actuator/metrics

# Specific metric
curl http://localhost:9000/actuator/metrics/jvm.memory.used
```

---

## 🔐 Security for Production

### 1. Change Default Credentials

Remove or change default admin user in `schema.sql`

### 2. Use Environment Variables

```bash
export SPRING_DATASOURCE_PASSWORD=secure_password
export JWT_SECRET=your_secret_key
```

### 3. Enable HTTPS

```properties
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=password
server.ssl.key-store-type=PKCS12
```

### 4. Disable Actuator Endpoints in Production

```properties
management.endpoints.web.exposure.include=health
```

---

## 📅 Deployment Checklist

- [ ] Database created and configured
- [ ] Application properties updated
- [ ] Build succeeds without errors
- [ ] Tests pass
- [ ] JAR file created successfully
- [ ] Application starts without errors
- [ ] Health check passes
- [ ] API endpoints responding
- [ ] Swagger UI accessible
- [ ] Security configured
- [ ] Logging configured
- [ ] Database backups configured
- [ ] Monitoring enabled
- [ ] SSL/HTTPS configured (if needed)

---

## 📝 Production Deployment Script

Create `deploy.sh`:

```bash
#!/bin/bash

# Build
echo "Building application..."
mvn clean package -DskipTests

# Check build status
if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

# Stop existing application
echo "Stopping existing application..."
pkill -f "demo-0.0.1-SNAPSHOT.jar"

# Wait for process to stop
sleep 2

# Start new application
echo "Starting application..."
java -Xms512m -Xmx1024m \
  -Dspring.config.location=classpath:/application.properties \
  -jar target/demo-0.0.1-SNAPSHOT.jar &

# Wait for application to start
sleep 5

# Verify application is running
echo "Verifying application..."
curl http://localhost:9000/actuator/health

if [ $? -eq 0 ]; then
    echo "✅ Application deployed successfully!"
else
    echo "❌ Application deployment failed!"
    exit 1
fi
```

Make executable:
```bash
chmod +x deploy.sh
./deploy.sh
```

---

## 🔄 Zero Downtime Deployment

### Using Blue-Green Deployment

```bash
# Run on port 9001 (green)
java -Dserver.port=9001 -jar target/demo-0.0.1-SNAPSHOT.jar &

# Test green instance
curl http://localhost:9001/actuator/health

# If healthy, switch traffic
# Update load balancer to route to port 9001

# Kill old instance (blue) on port 9000
pkill -f "server.port=9000"
```

---

## 📞 Support & Monitoring

### Application Logs
```bash
tail -f logs/application.log
```

### Database Logs
```bash
# MySQL logs
tail -f /var/log/mysql/error.log
```

### System Resources
```bash
# Check CPU and memory
top
ps aux | grep java

# Check disk space
df -h
```

---

**Deployment Status**: ✅ Ready to Deploy  
**Last Updated**: 2026-03-24
