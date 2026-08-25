# 🚪 JWT Logout Implementation Guide

## ✅ Logout Endpoint Added

A new logout endpoint has been added to your application:

```
POST /api/auth/logout
```

---

## 📋 How JWT Logout Works

Since JWT is **stateless**, logout works differently than session-based authentication:

### Traditional Session Logout
```
1. Client sends logout request
   ↓
2. Server destroys session in database
   ↓
3. Client discards session ID
   ↓
4. User logged out
```

### JWT Logout (Stateless)
```
1. Client sends logout request (optional)
   ↓
2. Server clears SecurityContext (optional)
   ↓
3. Client MUST discard JWT token locally
   ↓
4. Without token, all API calls return 401
   ↓
5. User effectively logged out
```

---

## 🎯 How to Implement Logout

### Option 1: Simple Client-Side Logout (Recommended)
**No server request needed!**

```javascript
// Just delete the token from client storage
localStorage.removeItem('token');
// or
sessionStorage.removeItem('token');
// or
delete window.token;

// User is now logged out - all API calls will fail without token
```

### Option 2: Call Logout Endpoint First
**Then delete token on client**

```bash
# 1. Call logout endpoint with your token
curl -X POST http://localhost:9000/api/auth/logout \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 2. Delete token from client storage (localStorage, sessionStorage, cookie, etc.)

# User is now logged out
```

---

## 🔄 Complete Logout Flow

### Step 1: User Clicks Logout Button
```
┌─────────────────┐
│  Logout Button  │
└────────┬────────┘
         ↓
```

### Step 2: Frontend Makes Logout Request (Optional)
```javascript
async function logout() {
  try {
    // Call logout endpoint (optional)
    const response = await fetch('/api/auth/logout', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });

    console.log(await response.json());
    // Output: "Logout successful - Please delete the token from client"
  } catch (error) {
    console.error('Logout error:', error);
  }
}
```

### Step 3: Delete Token from Client
```javascript
// After logout endpoint call (or skip endpoint and do this directly)
localStorage.removeItem('token');
sessionStorage.removeItem('token');

// Or if stored in cookie:
document.cookie = "token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

// Or if stored in memory:
window.token = null;
```

### Step 4: Redirect to Login Page
```javascript
window.location.href = '/login';
```

---

## 🧪 Test Logout in Swagger

### Step 1: Authorize with Valid Token
1. Click **Authorize** button (top-right)
2. Paste your JWT token
3. Click **Authorize**

### Step 2: Call Logout Endpoint
1. Expand **Authentication** section
2. Find **POST /api/auth/logout**
3. Click **Try it out**
4. Click **Execute**

### Step 3: Expected Response
```json
{
  "success": true,
  "message": "Logout successful - Please delete the token from client",
  "timestamp": 1774774275570
}
```

### Step 4: Verify Logout
1. Click the **Authorize** button again
2. Click **Logout** in the authorization dialog
3. Try to call a protected endpoint (GET /api/products)
4. **Expected:** 401 Unauthorized

---

## 📝 Logout Endpoint Details

### Endpoint
```
POST /api/auth/logout
```

### Authentication Required
```
Authorization: Bearer YOUR_JWT_TOKEN
```

### Request Body
**None required** (empty)

### Success Response (200 OK)
```json
{
  "success": true,
  "message": "Logout successful - Please delete the token from client",
  "timestamp": 1774774275570
}
```

### Error Response (400 Bad Request)
```json
{
  "success": false,
  "message": "No active session to logout",
  "timestamp": 1774774275570
}
```

### Error Response (401 Unauthorized)
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "timestamp": 1774774275570
}
```

---

## 💻 Complete Frontend Example

### React Example
```javascript
import React, { useState } from 'react';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);

  // Login function
  const handleLogin = async (username, password) => {
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });

      const data = await response.json();

      if (data.success) {
        // Store token
        localStorage.setItem('token', data.data.token);
        localStorage.setItem('user', JSON.stringify(data.data.user));
        
        setIsLoggedIn(true);
        setUser(data.data.user);
      }
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  // Logout function
  const handleLogout = async () => {
    try {
      const token = localStorage.getItem('token');

      // Call logout endpoint (optional)
      await fetch('/api/auth/logout', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      // Delete token from client storage
      localStorage.removeItem('token');
      localStorage.removeItem('user');

      setIsLoggedIn(false);
      setUser(null);

      // Redirect to login page
      window.location.href = '/login';
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  return (
    <div>
      {isLoggedIn ? (
        <>
          <p>Welcome, {user?.firstName}!</p>
          <button onClick={handleLogout}>Logout</button>
        </>
      ) : (
        <p>Please login</p>
      )}
    </div>
  );
}

export default App;
```

---

## 🔐 Security Considerations

### Why Client-Side Token Deletion is Enough

1. **Token is Stateless**
   - Server doesn't store tokens
   - Can't "revoke" tokens on server
   - Deleting from client prevents future use

2. **Token Expires Automatically**
   - Default: 24 hours
   - Even if token is leaked, it expires

3. **Signature Validation**
   - Tampered tokens are rejected
   - Server validates every request

### Best Practices

✅ **DO:**
- Delete token immediately on logout
- Clear from all possible storage (localStorage, sessionStorage, cookies, memory)
- Redirect to login page after logout
- Implement token refresh before expiration
- Use HTTPS to prevent token interception

❌ **DON'T:**
- Send token in URL (XSS risk)
- Store token in localStorage if sensitive (XSS risk)
- Keep expired token
- Reuse old tokens

---

## 📊 Token Lifecycle

```
┌──────────────────────────────────────────────────┐
│                Token Lifecycle                    │
├──────────────────────────────────────────────────┤
│                                                  │
│  1. LOGIN                                        │
│     ↓                                            │
│     Generate token (expires in 24h)             │
│     ↓                                            │
│  2. STORE TOKEN                                 │
│     ↓                                            │
│     Client stores in localStorage/cookie        │
│     ↓                                            │
│  3. USE TOKEN                                   │
│     ↓                                            │
│     Include in API requests                     │
│     ↓                                            │
│  4. LOGOUT (Option A: Early Logout)            │
│     ↓                                            │
│     Call /api/auth/logout (optional)           │
│     Delete token from client                    │
│     Can't make API calls anymore                │
│     ↓                                            │
│  OR                                              │
│                                                  │
│  4. TOKEN EXPIRES (Option B: Natural Expiry)   │
│     ↓                                            │
│     After 24 hours                              │
│     Token rejected by server (401)              │
│     Call /api/auth/refresh to get new token    │
│     ↓                                            │
│  5. RE-LOGIN (If expired)                       │
│     ↓                                            │
│     Call /api/auth/login again                 │
│     Get new token                               │
│     ↓                                            │
│  Back to step 2                                 │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## 🎯 Logout Scenarios

### Scenario 1: User-Initiated Logout
```
User clicks "Logout" button
  ↓
Frontend calls POST /api/auth/logout
  ↓
Server clears SecurityContext
  ↓
Frontend deletes token from storage
  ↓
Redirect to login page
  ↓
User is logged out ✅
```

### Scenario 2: Token Expired
```
User makes API request with expired token
  ↓
Server returns 401 Unauthorized
  ↓
Frontend detects 401
  ↓
Frontend deletes token
  ↓
Redirect to login page
  ↓
User must login again
```

### Scenario 3: Browser Closed
```
User closes browser
  ↓
If token in sessionStorage → Automatically deleted ✅
If token in localStorage → Still there (persist logout)
If token in cookie with HttpOnly + Secure → Automatically deleted ✅
  ↓
On next browser session
  ↓
No token = Must login again
```

---

## ✅ Implementation Checklist

- [x] Logout endpoint created: POST /api/auth/logout
- [x] Requires JWT authentication
- [x] Clears SecurityContext on server
- [x] Returns success response
- [x] Handles unauthorized requests
- [x] Integrated with Swagger UI

---

## 📞 Logout API Reference

### cURL Example
```bash
# Logout with token
curl -X POST http://localhost:9000/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."

# Expected response
{
  "success": true,
  "message": "Logout successful - Please delete the token from client"
}
```

### JavaScript Fetch Example
```javascript
const logout = async () => {
  const token = localStorage.getItem('token');

  const response = await fetch('http://localhost:9000/api/auth/logout', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  const data = await response.json();

  if (data.success) {
    localStorage.removeItem('token');
    // Redirect to login
    window.location.href = '/login';
  }
};
```

---

## 🎉 Logout Feature Complete!

Your application now supports:
- ✅ User login with JWT
- ✅ Token refresh before expiration
- ✅ **User logout** (new!)
- ✅ Protected endpoints
- ✅ Role-based access control
- ✅ Swagger UI integration

---

## 🚀 Next Steps

1. **Test logout in Swagger**
   - Login → Get token
   - Authorize with token
   - Call POST /api/auth/logout
   - Verify 200 OK response

2. **Implement in frontend**
   - Add logout button
   - Delete token on logout
   - Redirect to login page

3. **Test scenarios**
   - Logout and try protected endpoint (should get 401)
   - Token expiration (after 24 hours)
   - Refresh token before expiration

---

**Logout implementation complete! 🔐**
