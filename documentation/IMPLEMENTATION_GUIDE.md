# E-Commerce Order Management System - Implementation Guide

## 🎯 Overview
This is a production-ready Spring Boot backend system for managing e-commerce orders, products, and users with secure access, high performance, and scalable architecture.

---

## 🏗️ Architecture

### Layered Architecture
```
Controller Layer  → REST APIs (AuthController, ProductController, OrderController, AdminController)
Service Layer     → Business Logic (UserService, ProductService, OrderService)
Repository Layer  → Database Access (JPA Repositories)
Entity Layer      → Database Models (User, Product, Order, OrderItem)
DTO Layer         → API Request/Response (UserDTO, ProductDTO, OrderDTO, etc.)
```

---

## 📦 Project Structure

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── config/              # Configuration classes
│   │   │   ├── ApplicationConfig.java      # Bean configurations
│   │   │   ├── SecurityConfig.java         # Security configuration
│   │   │   ├── SwaggerConfig.java          # API documentation
│   │   │   ├── CacheConfig.java            # Caching configuration
│   │   │   └── RateLimitService.java       # Rate limiting service
│   │   │
│   │   ├── controller/          # REST Controllers
│   │   │   ├── AuthController.java         # Authentication endpoints
│   │   │   ├── ProductController.java      # Product management
│   │   │   ├── OrderController.java        # Order management
│   │   │   └── AdminController.java        # Admin operations
│   │   │
│   │   ├── service/             # Business Logic Interfaces
│   │   │   └── impl/            # Service Implementations
│   │   │       ├── UserServiceImpl.java
│   │   │       ├── ProductServiceImpl.java
│   │   │       └── OrderServiceImpl.java
│   │   │
│   │   ├── repository/          # Data Access Layer
│   │   │   ├── UserRepository.java
│   │   │   ├── ProductRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   └── OrderItemRepository.java
│   │   │
│   │   ├── entity/              # JPA Entities
│   │   │   ├── User.java
│   │   │   ├── Product.java
│   │   │   ├── Order.java
│   │   │   ├── OrderItem.java
│   │   │   ├── Role.java (Enum)
│   │   │   └── OrderStatus.java (Enum)
│   │   │
│   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── UserDTO.java
│   │   │   ├── UserRegisterDTO.java
│   │   │   ├── UserLoginDTO.java
│   │   │   ├── ProductDTO.java
│   │   │   ├── OrderDTO.java
│   │   │   ├── OrderCreateDTO.java
│   │   │   ├── OrderItemDTO.java
│   │   │   ├── OrderItemCreateDTO.java
│   │   │   └── ApiResponse.java
│   │   │
│   │   ├── exception/           # Custom Exceptions
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── UserNotFoundException.java
│   │   │   ├── ProductNotFoundException.java
│   │   │   ├── OrderNotFoundException.java
│   │   │   ├── UnauthorizedException.java
│   │   │   └── BadRequestException.java
│   │   │
│   │   ├── filter/              # Filters
│   │   │   └── RateLimitFilter.java
│   │   │
│   │   └── DemoApplication.java # Main application class
│   │
│   └── resources/
│       ├── application.properties    # Application configuration
│       └── schema.sql               # Database schema
│
└── test/
    └── java/com/example/demo/
        ├── service/
        │   └── UserServiceTest.java
        └── controller/
            └── AuthControllerTest.java
```

---

## 🔐 Security Features

### Authentication & Authorization
- **Basic Authentication**: Username and password based
- **Role-Based Access Control (RBAC)**:
  - `ADMIN`: Full access including user and product management
  - `USER`: Limited access to orders and products
- **Password Encryption**: BCrypt for secure password storage
- **Session Management**: Stateless (JWT-ready for future)

### Security Endpoints
- `/api/auth/**` - Public endpoints (register, login)
- `/api/admin/**` - Admin only
- `/api/products` - Read access for all authenticated users; Write/Delete for admin
- `/api/orders` - User specific
- `/actuator/**` - Public monitoring endpoints

---

## 💾 Database Schema

### Users Table
```sql
- id (PK)
- username (UNIQUE)
- email (UNIQUE)
- password (encrypted)
- firstName
- lastName
- role (ADMIN/USER)
- active
- createdAt
- updatedAt
```

### Products Table
```sql
- id (PK)
- name
- description
- price
- stock
- active
- createdAt
- updatedAt
```

### Orders Table
```sql
- id (PK)
- user_id (FK → users)
- totalPrice
- status (PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED)
- createdAt
- updatedAt
```

### OrderItems Table
```sql
- id (PK)
- order_id (FK → orders)
- product_id (FK → products)
- quantity
- price (at time of order)
```

---

## 🚀 API Endpoints

### Authentication (`/api/auth`)
- `POST /register` - Register new user
- `POST /login` - User login

### Products (`/api/products`)
- `GET /` - Get all products (paginated)
- `GET /{id}` - Get product details
- `GET /search?keyword=...` - Search products
- `POST /` - Create product (Admin)
- `PUT /{id}` - Update product (Admin)
- `DELETE /{id}` - Delete product (Admin)

### Orders (`/api/orders`)
- `POST /` - Create order
- `GET /{id}` - Get order details
- `GET /user/{userId}` - Get user orders
- `PUT /{id}/status?status=...` - Update order status
- `PUT /{id}/cancel` - Cancel order

### Admin (`/api/admin`)
- `GET /users` - Get all users
- `GET /users/{id}` - Get user details
- `GET /orders` - Get all orders
- `GET /orders/{id}` - Get order details
- `DELETE /users/{id}` - Delete user

---

## ⚙️ Configuration

### application.properties
```properties
# Server
server.port=9000

# Database (MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=root

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Redis (Caching)
spring.redis.host=localhost
spring.redis.port=6379

# Swagger/OpenAPI
springdoc.swagger-ui.path=/swagger-ui.html
```

---

## 📊 Key Features

### 1. **Performance Optimization**
- **Pagination & Sorting**: All list endpoints support pagination
- **Caching**: Redis-based caching for products
- **Lazy Loading**: Collections use LAZY loading by default
- **Eager Loading**: Related entities loaded when necessary
- **Query Optimization**: JOIN FETCH to prevent N+1 problems

### 2. **Input Validation**
- Email format validation
- Required fields validation
- Size constraints
- Custom validation rules

### 3. **Rate Limiting**
- 100 requests per minute per IP/client
- Bucket4j library for rate limiting
- HTTP 429 response when limit exceeded

### 4. **Logging & Monitoring**
- SLF4J for logging
- Spring Actuator for health checks and metrics
- Request/response logging
- Error tracking and debugging

### 5. **Exception Handling**
- Global exception handler
- Custom exception classes
- Consistent error response format

### 6. **API Documentation**
- Swagger/OpenAPI integration
- Auto-generated API documentation
- Request/response examples
- Authentication details

---

## 🧪 Testing Strategy

### Unit Testing
- Service layer testing with Mockito
- Mocking repositories and dependencies
- Example: `UserServiceTest`

### Integration Testing
- Controller testing with MockMvc
- Full request/response validation
- Example: `AuthControllerTest`

### Running Tests
```bash
mvn test
```

---

## 🚀 Getting Started

### Prerequisites
- Java 8 or higher
- Maven 3.6+
- MySQL 5.7+
- Redis (optional, for caching)

### Setup Instructions

1. **Clone the repository**
   ```bash
   cd /path/to/project
   ```

2. **Create database**
   ```sql
   CREATE DATABASE ecommerce_db;
   ```

3. **Run SQL schema** (optional, Hibernate will create tables)
   ```bash
   mysql -u root -p ecommerce_db < schema.sql
   ```

4. **Install dependencies**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

6. **Access the application**
   - API: `http://localhost:9000`
   - Swagger UI: `http://localhost:9000/swagger-ui.html`
   - Health Check: `http://localhost:9000/actuator/health`

---

## 📝 Sample Credentials

### Admin User
- Username: `admin`
- Password: `admin123`
- Role: ADMIN

### Sample Products
- Laptop ($999.99)
- Mouse ($29.99)
- Keyboard ($79.99)
- Monitor ($399.99)
- USB Cable ($9.99)

---

## 🔄 Transaction Management

- `@Transactional` annotation on service methods
- Automatic rollback on exceptions
- Order creation is transactional
- Product stock updates are atomic

---

## 💡 Best Practices Implemented

1. ✅ **DTO Pattern**: Separate DTOs for request/response
2. ✅ **Repository Pattern**: Repository interfaces for data access
3. ✅ **Service Layer Pattern**: Business logic in services
4. ✅ **Global Exception Handling**: Centralized error handling
5. ✅ **RBAC**: Role-based access control
6. ✅ **Input Validation**: Annotation-based validation
7. ✅ **Caching**: Redis-based caching
8. ✅ **Pagination**: Page-based pagination
9. ✅ **Lazy Loading**: Performance optimization
10. ✅ **Logging**: SLF4J logging

---

## 🚧 Future Enhancements

1. JWT Authentication with refresh tokens
2. Audit logging for compliance
3. Soft delete for users and orders
4. Search functionality with ElasticSearch
5. Microservices architecture
6. API versioning (v1, v2, etc.)
7. GraphQL support
8. WebSocket for real-time notifications
9. Payment gateway integration
10. Email notifications

---

## 📚 Dependencies

### Core
- Spring Boot 2.7.5
- Spring Data JPA
- Spring Security
- Spring Validation

### Database
- MySQL Connector Java
- Spring Data Redis

### Utilities
- Lombok
- ModelMapper

### Documentation
- SpringDoc OpenAPI (Swagger)

### Performance
- Bucket4j (Rate Limiting)

### Testing
- JUnit 5
- Mockito
- Spring Security Test

---

## 🐛 Troubleshooting

### Database Connection Issues
```bash
# Check MySQL is running
# Verify credentials in application.properties
# Ensure database exists
```

### Port Already in Use
```bash
# Change port in application.properties
# Or kill process using port 9000
```

### Redis Connection Issues
```bash
# Redis is optional, caching will be disabled if unavailable
# Or install Redis: brew install redis
```

---

## 📞 Support & Documentation

- Swagger Documentation: `http://localhost:9000/swagger-ui.html`
- Spring Boot Docs: https://spring.io/projects/spring-boot
- Spring Security Docs: https://spring.io/projects/spring-security
- JPA Documentation: https://spring.io/projects/spring-data-jpa

---

**Version**: 1.0.0  
**Last Updated**: 2026-03-24  
**Status**: Production Ready ✅
