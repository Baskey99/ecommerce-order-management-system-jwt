package com.example.demo.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Idempotency Filter - Prevents duplicate request processing
 * 
 * How it works:
 * 1. Client sends Idempotency-Key header with unique request ID (UUID)
 * 2. Filter checks if request with this key was already processed
 * 3. If duplicate detected, returns cached response without processing
 * 4. If first request, processes and caches response
 * 
 * Use Cases:
 * - Prevent duplicate order creation on network retry
 * - Prevent duplicate payment processing
 * - Ensure exactly-once delivery semantics
 * 
 * Note: In production, use Redis instead of ConcurrentHashMap for distributed systems
 */
@Slf4j
@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    // In-memory store for request tracking (use Redis in production)
    // Key format: "METHOD:IDEMPOTENCY_KEY" (e.g., "POST:550e8400-e29b-41d4-a716-446655440000")
    private static final ConcurrentHashMap<String, IdempotencyRecord> requestCache = new ConcurrentHashMap<>();
    
    // Cache cleanup interval: 1 hour (3600000 ms)
    private static final long REQUEST_CACHE_TIMEOUT = 3600000;
    
    // Maximum cache size before cleanup
    private static final int MAX_CACHE_SIZE = 10000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idempotencyKey = request.getHeader("Idempotency-Key");
        String method = request.getMethod();

        // Only apply idempotency to mutation requests (POST, PUT, DELETE)
        if (idempotencyKey != null && (method.equals("POST") || method.equals("PUT") || method.equals("DELETE"))) {
            String cacheKey = method + ":" + idempotencyKey;
            long currentTime = System.currentTimeMillis();

            // Check if this is a duplicate request
            IdempotencyRecord record = requestCache.get(cacheKey);
            
            if (record != null && (currentTime - record.getTimestamp()) < REQUEST_CACHE_TIMEOUT) {
                log.warn("⚠️  Duplicate request detected with Idempotency-Key: {}", idempotencyKey);
                
                // Return cached response
                response.setStatus(record.getStatusCode());
                response.setHeader("X-Idempotency-Replayed", "true");
                response.setHeader("X-Idempotency-Key", idempotencyKey);
                response.setHeader("Content-Type", "application/json");
                
                response.getOutputStream().write(record.getResponseBody());
                response.getOutputStream().flush();
                return;
            }

            // New request - store in cache with captured response
            log.info("✅ Processing request with Idempotency-Key: {}", idempotencyKey);
            response.setHeader("X-Idempotency-Key", idempotencyKey);

            // Wrap response to capture it
            CachedHttpServletResponse cachedResponse = new CachedHttpServletResponse(response);

            try {
                filterChain.doFilter(request, cachedResponse);
                
                // Store response for future duplicate requests
                IdempotencyRecord newRecord = new IdempotencyRecord(
                    cachedResponse.getStatus(),
                    cachedResponse.getCapturedData(),
                    currentTime
                );
                requestCache.put(cacheKey, newRecord);
                
                log.debug("📝 Cached response for Idempotency-Key: {} (Status: {})", 
                    idempotencyKey, cachedResponse.getStatus());
                
                // Cleanup old cache entries if too large
                if (requestCache.size() > MAX_CACHE_SIZE) {
                    cleanupExpiredEntries();
                }
                
                // Copy cached response back to actual response
                byte[] data = cachedResponse.getCapturedData();
                response.getOutputStream().write(data);
                response.getOutputStream().flush();
                
            } catch (Exception e) {
                log.error("❌ Error processing request with Idempotency-Key: {}", idempotencyKey, e);
                filterChain.doFilter(request, response);
            }

        } else {
            // Not a mutation request or no Idempotency-Key provided
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Remove expired entries from cache
     */
    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        requestCache.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue().getTimestamp()) > REQUEST_CACHE_TIMEOUT
        );
        log.info("🗑️  Cleaned up expired idempotency cache entries. Remaining: {}", requestCache.size());
    }

    /**
     * Record for storing cached request/response
     */
    private static class IdempotencyRecord {
        private final int statusCode;
        private final byte[] responseBody;
        private final long timestamp;

        public IdempotencyRecord(int statusCode, byte[] responseBody, long timestamp) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
            this.timestamp = timestamp;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public byte[] getResponseBody() {
            return responseBody;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
