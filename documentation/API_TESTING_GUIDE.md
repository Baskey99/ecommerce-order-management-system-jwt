# API Testing Guide

## 🔑 Authentication

### Base URL
```
http://localhost:9000
```

### Auth Type: Basic Auth
- Username: `admin`
- Password: `admin123`

---

## 📚 API Endpoints Examples

### 1. Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 2,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER",
    "active": true
  },
  "timestamp": 1234567890000
}
```

---

#### Login User
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 2,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER"
  }
}
```

---

### 2. Product Endpoints

#### Create Product (Admin Only)
```http
POST /api/products
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{
  "name": "iPhone 14",
  "description": "Latest Apple smartphone",
  "price": 999.99,
  "stock": 50
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 6,
    "name": "iPhone 14",
    "description": "Latest Apple smartphone",
    "price": 999.99,
    "stock": 50,
    "active": true,
    "createdAt": "2024-03-24T10:30:00Z"
  }
}
```

---

#### Get All Products
```http
GET /api/products?page=0&size=10&sortBy=id&direction=ASC
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Laptop",
        "description": "High performance laptop",
        "price": 999.99,
        "stock": 10,
        "active": true
      },
      {
        "id": 2,
        "name": "Mouse",
        "description": "Wireless mouse",
        "price": 29.99,
        "stock": 50,
        "active": true
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

---

#### Get Product by ID
```http
GET /api/products/1
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Product retrieved successfully",
  "data": {
    "id": 1,
    "name": "Laptop",
    "description": "High performance laptop",
    "price": 999.99,
    "stock": 10,
    "active": true,
    "createdAt": "2024-01-15T08:00:00Z"
  }
}
```

---

#### Search Products
```http
GET /api/products/search?keyword=laptop&page=0&size=10
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

---

#### Update Product (Admin Only)
```http
PUT /api/products/1
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{
  "name": "Gaming Laptop",
  "description": "High-end gaming laptop",
  "price": 1299.99,
  "stock": 8
}
```

---

#### Delete Product (Admin Only)
```http
DELETE /api/products/1
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

---

### 3. Order Endpoints

#### Create Order
```http
POST /api/orders
Authorization: Basic am9obl9kb2U6cGFzc3dvcmQxMjM=
Content-Type: application/json

{
  "userId": 2,
  "items": [
    {
      "productId": 1,
      "quantity": 1
    },
    {
      "productId": 2,
      "quantity": 2
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "userId": 2,
    "totalPrice": 1059.97,
    "status": "PENDING",
    "createdAt": "2024-03-24T10:45:00Z",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Laptop",
        "quantity": 1,
        "price": 999.99
      },
      {
        "id": 2,
        "productId": 2,
        "productName": "Mouse",
        "quantity": 2,
        "price": 29.99
      }
    ]
  }
}
```

---

#### Get Order by ID
```http
GET /api/orders/1
Authorization: Basic am9obl9kb2U6cGFzc3dvcmQxMjM=
```

---

#### Get User Orders
```http
GET /api/orders/user/2?page=0&size=10
Authorization: Basic am9obl9kb2U6cGFzc3dvcmQxMjM=
```

---

#### Update Order Status
```http
PUT /api/orders/1/status?status=CONFIRMED
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

**Valid Status Values:**
- `PENDING`
- `CONFIRMED`
- `SHIPPED`
- `DELIVERED`
- `CANCELLED`

---

#### Cancel Order
```http
PUT /api/orders/1/cancel
Authorization: Basic am9obl9kb2U6cGFzc3dvcmQxMjM=
```

---

### 4. Admin Endpoints

#### Get All Users
```http
GET /api/admin/users
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@ecommerce.com",
      "firstName": "Admin",
      "lastName": "User",
      "role": "ADMIN"
    },
    {
      "id": 2,
      "username": "john_doe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "USER"
    }
  ]
}
```

---

#### Get User by ID
```http
GET /api/admin/users/2
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

---

#### Get All Orders
```http
GET /api/admin/orders?page=0&size=10
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

---

#### Delete User (Admin Only)
```http
DELETE /api/admin/users/2
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

---

## 🔄 Common HTTP Status Codes

| Status | Meaning |
|--------|---------|
| 200 | OK - Request successful |
| 201 | Created - Resource created |
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Access denied |
| 404 | Not Found - Resource not found |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error |

---

## 💡 Tips

1. **Base64 Encoding for Basic Auth:**
   - Admin: `admin:admin123` → `YWRtaW46YWRtaW4xMjM=`
   - User: `john_doe:password123` → `am9obl9kb2U6cGFzc3dvcmQxMjM=`

2. **Pagination:**
   - Default page: 0 (first page)
   - Default size: 10 records
   - Adjust as needed

3. **Sorting:**
   - Default: By ID in ASC order
   - Supported fields: id, name, createdAt, updatedAt, price, etc.

4. **Rate Limiting:**
   - 100 requests per minute per client IP
   - Response header: `X-Rate-Limit-Remaining`

---

## 🧪 Testing Workflow

### 1. Register a new user
```bash
curl -X POST http://localhost:9000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "test123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123"
  }'
```

### 3. Create an order
```bash
curl -X POST http://localhost:9000/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic dGVzdHVzZXI6dGVzdDEyMw==" \
  -d '{
    "userId": 2,
    "items": [
      {"productId": 1, "quantity": 1}
    ]
  }'
```

---

## 📖 API Documentation

Access interactive API documentation at:
```
http://localhost:9000/swagger-ui.html
```

---

**Last Updated**: 2026-03-24
