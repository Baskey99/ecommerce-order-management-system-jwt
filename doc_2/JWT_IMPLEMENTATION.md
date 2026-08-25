# JWT Authentication Implementation Guide

## Overview
This document describes the JWT (JSON Web Token) authentication implementation that replaces Basic Authentication in the ecommerce application.

## Implementation Summary

### 1. **Dependencies Added** (pom.xml)
```xml
<!-- JWT Dependencies -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

### 2. **JWT Components Created**

#### A. JwtTokenProvider (`security/JwtTokenProvider.java`)
- Generates JWT tokens from Authentication objects
- Extracts username and authorities from tokens
- Validates token integrity and expiration
- Uses HS512 (HMAC with SHA-512) signing algorithm
- **Key Methods:**
  - `generateToken(Authentication)` - Generate token from user authentication
  - `generateTokenFromUsername(String)` - Generate token directly from username
  - `getUsernameFromToken(String)` - Extract username from token
  - `getAuthoritiesFromToken(String)` - Extract user roles from token
  - `validateToken(String)` - Verify token signature and expiration

#### B. JwtAuthenticationFilter (`security/JwtAuthenticationFilter.java`)
- Extends `OncePerRequestFilter` to process each request once
- Extracts JWT token from "Authorization: Bearer <token>" header
- Validates token and loads user details
- Sets up Spring Security context for authenticated requests
- **Key Method:**
  - `doFilterInternal()` - Processes JWT authentication for each request

#### C. JwtEntryPoint (`security/JwtEntryPoint.java`)
- Implements `AuthenticationEntryPoint`
- Returns JSON error response for unauthorized access attempts
- Replaces Basic Authentication challenge
- **Response Example:**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "timestamp": 1711604900000
}
```

#### D. SecurityConfig (`config/SecurityConfig.java`)
**Key Changes:**
- Removed Basic Authentication
- Added JWT Filter to filter chain
- Configured stateless session management (STATELESS)
- Added JWT entry point for error handling
- Maintained role-based access control (RBAC)

**Security Chain:**
1. Request arrives → JWT Filter processes
2. Token extracted from Authorization header
3. Token validated and user loaded
4. User context set in SecurityContextHolder
5. Request proceeds or is rejected based on roles

### 3. **Controller Updates**

#### AuthController Updates (`controller/AuthController.java`)
**New/Updated Endpoints:**

1. **POST /api/auth/login**
   - Request: `{ "username": "user", "password": "pass" }`
   - Response: JWT token with user details and expiration
   - Returns: `JwtAuthResponse` with token, type, expiration, and user info

2. **POST /api/auth/refresh**
   - Requires valid authentication
   - Generates new token using current authentication
   - Useful for token refresh before expiration

3. **POST /api/auth/register**
   - Creates new user account
   - No token required (public endpoint)

### 4. **New DTOs**

#### JwtAuthResponse (`dto/JwtAuthResponse.java`)
```java
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "john",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER"
  }
}
```

### 5. **Configuration** (application.properties)
```properties
# JWT Configuration
jwt.secret=your-super-secret-jwt-key-with-minimum-32-characters-for-hs512-algorithm-security
jwt.expiration=86400000  # 24 hours in milliseconds
```

## Authentication Flow

### Login Flow
```
1. Client sends credentials to POST /api/auth/login
   ↓
2. AuthController authenticates with AuthenticationManager
   ↓
3. JwtTokenProvider generates JWT token
   ↓
4. Response returns token + user details
   ↓
5. Client stores token (localStorage, session, etc.)
```

### Subsequent Requests Flow
```
1. Client sends request with "Authorization: Bearer <token>"
   ↓
2. JwtAuthenticationFilter extracts token
   ↓
3. JwtTokenProvider validates token
   ↓
4. User details loaded from database
   ↓
5. SecurityContext populated with user + roles
   ↓
6. Request proceeds with authorization checks
```

### Token Expiration
```
1. Token contains 'exp' (expiration) claim
2. Default: 24 hours (86400000 ms)
3. After expiration, token becomes invalid
4. Client must login again or use refresh endpoint
5. Expired tokens return 401 Unauthorized
```

## How to Use

### 1. Register User
```bash
curl -X POST http://localhost:9000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. Login and Get Token
```bash
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "expiresIn": 86400000,
    "user": {...}
  },
  "timestamp": 1711604900000
}
```

### 3. Use Token for Authenticated Requests
```bash
curl -X GET http://localhost:9000/api/products \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

### 4. Refresh Token
```bash
curl -X POST http://localhost:9000/api/auth/refresh \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

## Security Features

### Token Security
- **Algorithm:** HS512 (HMAC SHA-512)
- **Secret Key:** Minimum 32 characters recommended
- **Signature:** Validates token integrity
- **Expiration:** Configurable TTL

### Session Management
- **Stateless:** No server-side sessions
- **Scalable:** Works across multiple servers
- **RESTful:** Each request contains all needed info

### Authorization
- **Role-Based:** ADMIN, USER roles supported
- **Endpoint Protection:** Different endpoints require different roles
- **Granular:** Method-level authorization support

## Differences from Basic Authentication

| Feature | Basic Auth | JWT |
|---------|-----------|-----|
| Credentials | Sent every request | Sent once, get token |
| Token | None | JWT with expiration |
| Stateless | Yes, but expensive | Yes, efficient |
| Security | Base64 encoded | Cryptographically signed |
| Server Load | High (verify every time) | Low (validate signature) |
| Expiration | N/A | Built-in TTL |
| Refresh | Re-login | Use refresh endpoint |

## Protected Endpoints Examples

### Admin Only
```
DELETE /api/products/{id}          - Delete product
POST /api/products                 - Create product
PUT /api/products/{id}             - Update product
GET /api/admin/**                  - Admin operations
```

### User & Admin
```
GET /api/products                  - List products
GET /api/products/{id}             - Get product details
GET /api/user/**                   - User operations
```

### Public (No Auth Required)
```
POST /api/auth/register            - Register user
POST /api/auth/login               - Login
GET /swagger-ui.html               - API docs
GET /v3/api-docs                   - OpenAPI spec
GET /actuator/health               - Health check
```

## Troubleshooting

### 1. 401 Unauthorized - Missing Token
**Issue:** Request without Authorization header
**Solution:** Include `Authorization: Bearer <token>` header

### 2. 401 Unauthorized - Invalid Token
**Issue:** Token malformed or tampered
**Solution:** Get new token via login

### 3. 401 Unauthorized - Expired Token
**Issue:** Token expiration time passed
**Solution:** Use refresh endpoint or login again

### 4. 403 Forbidden - Insufficient Permissions
**Issue:** Valid token but user lacks required role
**Solution:** Use account with appropriate role

### 5. Bean Definition Error
**Issue:** Duplicate `passwordEncoder` bean
**Solution:** Keep only in ApplicationConfig.java

## Configuration Customization

### Change Token Expiration
Edit `application.properties`:
```properties
jwt.expiration=604800000  # 7 days instead of 24 hours
```

### Change Secret Key (IMPORTANT FOR PRODUCTION)
Edit `application.properties`:
```properties
jwt.secret=your-production-secret-key-minimum-32-characters
```

⚠️ **Security Warning:** 
- Never commit actual secret keys to version control
- Use environment variables or secrets manager
- Minimum 32 characters recommended
- Use strong random string for production

## Files Modified/Created

### Created Files:
- `security/JwtTokenProvider.java` - Token generation and validation
- `security/JwtAuthenticationFilter.java` - Request filter for JWT
- `security/JwtEntryPoint.java` - Unauthorized response handler
- `dto/JwtAuthResponse.java` - JWT response DTO

### Modified Files:
- `config/SecurityConfig.java` - Replaced Basic Auth with JWT
- `controller/AuthController.java` - Added JWT endpoints
- `pom.xml` - Added JWT dependencies
- `application.properties` - Added JWT configuration

## Testing with Swagger

1. Visit: `http://localhost:9000/swagger-ui.html`
2. Use `POST /api/auth/login` to get token
3. Click "Authorize" button (top-right)
4. Enter: `Bearer <your-token>`
5. All protected endpoints now work!

## Next Steps

1. ✅ Implement JWT authentication
2. ⏳ Add token refresh mechanism
3. ⏳ Implement token blacklist for logout
4. ⏳ Add CORS configuration for frontend
5. ⏳ Implement rate limiting per user
6. ⏳ Add API key authentication as alternative

## References

- [JWT.io](https://jwt.io)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [Spring Security](https://spring.io/projects/spring-security)
- [RFC 7519 - JSON Web Token (JWT)](https://tools.ietf.org/html/rfc7519)
