# 🚀 Quick Start Guide - E-Commerce Order Management System

## ⚡ Prerequisites

- Java 8 or higher
- Maven 3.6+
- MySQL 5.7+
- Redis (optional, for caching)

---

## 📋 Step-by-Step Setup

### 1. Database Setup

Create the database:
```sql
CREATE DATABASE ecommerce_db;
```

(Optional) Run schema with sample data:
```bash
mysql -u root -p ecommerce_db < src/main/resources/schema.sql
```

Or use Hibernate auto-creation by setting in `application.properties`:
```properties
spring.jpa.hibernate.ddl-auto=update
```

---

### 2. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=your_password

# Redis (Optional)
spring.redis.host=localhost
spring.redis.port=6379
```

---

### 3. Build Project

```bash
cd /path/to/demo
mvn clean install
```

---

### 4. Run Application

```bash
mvn spring-boot:run
```

Or:
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

---

### 5. Verify Application

#### Health Check
```bash
curl http://localhost:9000/actuator/health
```

#### Swagger UI
Open browser: `http://localhost:9000/swagger-ui.html`

#### API Test
```bash
curl -X GET http://localhost:9000/api/products \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

---

## 🔑 Default Credentials

| User | Username | Password | Role |
|------|----------|----------|------|
| Admin | admin | admin123 | ADMIN |

**Note**: Default admin user is created automatically via schema.sql.

---

## 🎯 Quick API Test

### 1. Register New User
```bash
curl -X POST http://localhost:9000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "john123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "john123"
  }'
```

### 3. Create Order
```bash
curl -X POST http://localhost:9000/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic am9objpqb2huMTIz" \
  -d '{
    "userId": 2,
    "items": [
      {"productId": 1, "quantity": 1},
      {"productId": 2, "quantity": 2}
    ]
  }'
```

### 4. View Products (as Admin)
```bash
curl http://localhost:9000/api/products \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

---

## 📝 Project Highlights

✅ **Fully Implemented**:
- User registration & authentication
- Product CRUD operations
- Order management
- Admin dashboard
- Role-based access control
- Input validation
- Global exception handling
- Caching (Redis)
- Rate limiting
- Pagination & sorting
- Swagger documentation
- Transactional operations
- Comprehensive logging

---

## 📚 Documentation Files

1. **IMPLEMENTATION_GUIDE.md** - Detailed architecture & features
2. **API_TESTING_GUIDE.md** - API endpoints with examples
3. **schema.sql** - Database schema with sample data

---

## 🔧 Troubleshooting

### Issue: Database Connection Error
```
Check:
1. MySQL is running
2. Database name is correct
3. Username/password are correct
4. Database exists
```

### Issue: Port 9000 Already in Use
```
Change in application.properties:
server.port=9001
```

### Issue: Redis Connection Failed
```
Redis is optional, application will work without it.
To enable caching, start Redis:
redis-server
```

### Issue: Compilation Error
```bash
# Clean and rebuild
mvn clean compile
mvn clean install
```

---

## 📊 Testing

Run tests:
```bash
mvn test
```

Run specific test:
```bash
mvn test -Dtest=UserServiceTest
```

---

## 🚀 Running in Production

### Build JAR
```bash
mvn clean package
```

### Run JAR
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Docker (Optional)
```dockerfile
FROM openjdk:8-jre-slim
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build Docker image:
```bash
docker build -t ecommerce-app .
docker run -p 9000:9000 ecommerce-app
```

---

## 📖 API Documentation

After starting the application:

**Swagger UI**: `http://localhost:9000/swagger-ui.html`

**Health & Metrics**: `http://localhost:9000/actuator/health`

---

## 💡 Key Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register user |
| POST | `/api/auth/login` | Login user |
| GET | `/api/products` | Get all products |
| POST | `/api/products` | Create product (Admin) |
| GET | `/api/orders` | Get user orders |
| POST | `/api/orders` | Create order |
| GET | `/api/admin/users` | Get all users (Admin) |
| GET | `/api/admin/orders` | Get all orders (Admin) |

---

## 🎓 Next Steps

1. **Test APIs**: Use Swagger UI or provided cURL examples
2. **Explore Code**: Check `src/main/java/com/example/demo`
3. **Read Documentation**: Check IMPLEMENTATION_GUIDE.md and API_TESTING_GUIDE.md
4. **Extend Features**: Add more endpoints, caching, etc.
5. **Deploy**: Deploy to cloud (AWS, GCP, Azure, etc.)

---

## ✅ Checklist

- [ ] Java 8+ installed
- [ ] Maven installed
- [ ] MySQL running
- [ ] Database created
- [ ] Application configured (application.properties)
- [ ] Dependencies downloaded
- [ ] Application running on port 9000
- [ ] Swagger UI accessible
- [ ] Sample APIs tested

---

## 🤝 Support

For issues or questions:
1. Check the error message
2. Review IMPLEMENTATION_GUIDE.md
3. Check troubleshooting section above
4. Review logs for detailed error info

---

**Version**: 1.0.0  
**Status**: Production Ready ✅



  602  mvn spring-boot:run
  603  mvn clean install
  604  ls -lh /home/domain.rage-india.com/amitkumar.b/projects/java/springboot/demo/target/*.jar 2>&1
  605  ./mvnw clean package -DskipTests=true 2>&1 | tail -30
  606  ls -lh /app/springboot/demo/target/demo-0.0.1-SNAPSHOT.jar
  607  java -jar /app/springboot/demo/target/demo-0.0.1-SNAPSHOT.jar
  608  ./mvnw clean package -DskipTests=true 2>&1 | tail -30
  609  java -jar /app/springboot/demo/target/demo-0.0.1-SNAPSHOT.jar
