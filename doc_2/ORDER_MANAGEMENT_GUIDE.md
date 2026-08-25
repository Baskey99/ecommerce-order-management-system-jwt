# 📦 Order Management - Logged-in User Orders

## ✅ Updates Complete

Your order endpoints have been updated to work seamlessly with JWT authentication!

---

## 🎯 Key Changes

### Before
```java
// User had to send userId in request
POST /api/orders
{
  "userId": 5,
  "items": [...]
}
```

### After
```java
// userId is automatically fetched from logged-in user
POST /api/orders
{
  "items": [...]
  // userId is NOT needed - automatically set from JWT token!
}
```

---

## 🚀 How to Use

### Step 1: Login and Get JWT Token
```bash
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "samiksha",
    "password": "Admin@123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 86400000,
  "user": {
    "id": 6,
    "username": "samiksha",
    "role": "ADMIN"
  }
}
```

---

### Step 2: Create Order (User ID Automatic!)

```bash
curl -X POST http://localhost:9000/api/orders \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "price": 99.99
      },
      {
        "productId": 2,
        "quantity": 1,
        "price": 49.99
      }
    ],
    "status": "PENDING"
  }'
```

**Response (200 Created):**
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "userId": 6,
    "items": [...],
    "status": "PENDING",
    "totalAmount": 250.00,
    "createdAt": "2026-03-29T10:30:00"
  }
}
```

---

### Step 3: View Your Orders

#### Get All Your Orders
```bash
curl -X GET "http://localhost:9000/api/orders/my-orders?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Your orders retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 6,
        "username": "samiksha",
        "items": [...],
        "status": "PENDING",
        "totalAmount": 250.00,
        "createdAt": "2026-03-29T10:30:00"
      },
      {
        "id": 2,
        "userId": 6,
        "username": "samiksha",
        "items": [...],
        "status": "CONFIRMED",
        "totalAmount": 150.00,
        "createdAt": "2026-03-28T14:20:00"
      }
    ],
    "totalElements": 2,
    "totalPages": 1,
    "currentPage": 0
  }
}
```

---

### Step 4: Get Specific Order
```bash
curl -X GET http://localhost:9000/api/orders/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Order retrieved successfully",
  "data": {
    "id": 1,
    "userId": 6,
    "username": "samiksha",
    "items": [...],
    "status": "PENDING",
    "totalAmount": 250.00,
    "createdAt": "2026-03-29T10:30:00"
  }
}
```

---

## 🧪 Test in Swagger UI

### Step 1: Authorize
1. Click green **Authorize** button
2. Paste JWT token from login
3. Click **Authorize**

### Step 2: Create Order
1. Expand **Orders** section
2. Find **POST /api/orders**
3. Click **Try it out**
4. Enter request body (without userId!):
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 99.99
    }
  ],
  "status": "PENDING"
}
```
5. Click **Execute**

### Step 3: View My Orders
1. Find **GET /api/orders/my-orders**
2. Click **Try it out**
3. Set parameters:
   - `page`: 0
   - `size`: 10
4. Click **Execute**
5. See all your orders! ✅

---

## 📋 Order Endpoints Summary

| Method | Endpoint | Purpose | Auth | Auto User |
|--------|----------|---------|------|-----------|
| **POST** | `/api/orders` | Create order | ✅ | ✅ Auto |
| **GET** | `/api/orders/{id}` | Get order by ID | ✅ | ❌ |
| **GET** | `/api/orders/my-orders` | Get my orders | ✅ | ✅ Auto |
| **GET** | `/api/orders/user/{userId}` | Get user's orders | ✅ | ❌ |
| **PUT** | `/api/orders/{id}/status` | Update status | ✅ | ❌ |
| **PUT** | `/api/orders/{id}/cancel` | Cancel order | ✅ | ❌ |

---

## 🔐 How It Works

### Request Flow
```
1. Client sends POST /api/orders with JWT token
   ├─ Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
   └─ Body: { "items": [...] }

2. JwtAuthenticationFilter validates token
   └─ Extracts username from token

3. OrderController extracts authenticated user
   ├─ Gets username from SecurityContext
   ├─ Queries UserService for userId
   └─ Sets userId in OrderCreateDTO

4. Order created with logged-in user's ID
   ├─ userId automatically set to 6 (samiksha's ID)
   ├─ Order saved to database
   └─ Response returned to client

5. Client receives created order
   └─ Order shows userId: 6 (currently logged-in user)
```

---

## 💡 Benefits

✅ **Security**: Only logged-in users can create orders
✅ **Simplicity**: No need to pass userId in request
✅ **Data Integrity**: Orders automatically linked to correct user
✅ **JWT Integration**: Seamless JWT authentication
✅ **Error Prevention**: Can't accidentally create order for wrong user
✅ **Audit Trail**: All orders linked to authenticated user

---

## 🧪 Test Scenarios

### Scenario 1: Normal Order Creation
```bash
# 1. Login as samiksha
TOKEN=$(get_token "samiksha" "Admin@123")

# 2. Create order (no userId needed)
curl -X POST http://localhost:9000/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"items": [{"productId": 1, "quantity": 2, "price": 99.99}]}'

# ✅ Order created with userId: 6 (samiksha's ID)
```

### Scenario 2: Multiple Users
```bash
# User 1: samiksha (ID: 6)
TOKEN1=$(get_token "samiksha" "Admin@123")
# Creates order → userId: 6 ✅

# User 2: john (ID: 7)
TOKEN2=$(get_token "john" "password123")
# Creates order → userId: 7 ✅

# Both users' orders are separate!
```

### Scenario 3: View My Orders
```bash
# Login as samiksha
TOKEN=$(get_token "samiksha" "Admin@123")

# Get all my orders
curl -X GET "http://localhost:9000/api/orders/my-orders" \
  -H "Authorization: Bearer $TOKEN"

# ✅ Returns only samiksha's orders
```

### Scenario 4: Without Authentication
```bash
# Try to create order without token
curl -X POST http://localhost:9000/api/orders \
  -H "Content-Type: application/json" \
  -d '{"items": [...]}'

# ❌ Returns 401 Unauthorized
```

---

## 📊 Request/Response Examples

### Create Order Request
```bash
POST /api/orders
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 99.99
    },
    {
      "productId": 3,
      "quantity": 1,
      "price": 49.99
    }
  ],
  "status": "PENDING"
}
```

### Create Order Response
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "userId": 6,
    "username": "samiksha",
    "items": [
      {
        "id": 1,
        "orderId": 1,
        "productId": 1,
        "quantity": 2,
        "price": 99.99,
        "subtotal": 199.98
      },
      {
        "id": 2,
        "orderId": 1,
        "productId": 3,
        "quantity": 1,
        "price": 49.99,
        "subtotal": 49.99
      }
    ],
    "status": "PENDING",
    "totalAmount": 249.97,
    "createdAt": "2026-03-29T10:30:00",
    "updatedAt": "2026-03-29T10:30:00"
  },
  "timestamp": 1774774200000
}
```

---

## ✅ Verification Checklist

- [x] OrderController imports UserService
- [x] UserService autowired in OrderController
- [x] createOrder method gets logged-in user
- [x] userId automatically set from authentication
- [x] Logging shows username and userId
- [x] getMyOrders endpoint returns user's orders
- [x] Works with JWT authentication
- [x] Swagger UI integration
- [x] Error handling for unauthenticated requests

---

## 🎯 Usage Flow

```
┌─────────────────────────────┐
│  1. User Logs In            │
│  POST /api/auth/login       │
│  Gets JWT token             │
└──────────┬──────────────────┘
           ↓
┌─────────────────────────────┐
│  2. User Creates Order      │
│  POST /api/orders           │
│  Send items (no userId!)    │
│  Include JWT token          │
└──────────┬──────────────────┘
           ↓
┌─────────────────────────────┐
│  3. System Extracts User    │
│  Get username from token    │
│  Look up userId from DB     │
│  Set userId in order        │
└──────────┬──────────────────┘
           ↓
┌─────────────────────────────┐
│  4. Order Created           │
│  Linked to logged-in user   │
│  userId = 6 (samiksha)      │
│  Return created order       │
└──────────┬──────────────────┘
           ↓
┌─────────────────────────────┐
│  5. View My Orders          │
│  GET /api/orders/my-orders  │
│  See all my orders          │
│  Include JWT token          │
└─────────────────────────────┘
```

---

## 🚀 Ready to Use!

Your order management system now:
- ✅ Automatically associates orders with logged-in user
- ✅ Prevents accidental order creation for wrong user
- ✅ Integrates seamlessly with JWT authentication
- ✅ Provides endpoints to view user's orders
- ✅ Maintains security and data integrity

**Start creating orders! 📦**
