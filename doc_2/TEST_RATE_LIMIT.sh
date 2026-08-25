#!/bin/bash

echo "=========================================="
echo "  RATE LIMITING TEST SCRIPT"
echo "=========================================="
echo ""
echo "Rate Limit: 100 requests per minute per IP"
echo "Endpoint: GET /api/products (requires auth)"
echo ""

BASE_URL="http://localhost:9000"
AUTH="Authorization: Basic YWRtaW46QWRtaW5AMTIz"

# Test 1: Make 5 requests and check remaining tokens
echo "✅ Test 1: Making 5 normal requests..."
for i in {1..200}; do
    RESPONSE=$(curl -s -i -X GET "$BASE_URL/api/products?page=0&size=1" \
      -H "$AUTH" 2>&1)
    
    HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP" | awk '{print $2}')
    REMAINING=$(echo "$RESPONSE" | grep "X-Rate-Limit-Remaining" | awk '{print $2}' | tr -d '\r')
    
    echo "   Request $i - HTTP: $HTTP_CODE - Remaining tokens: $REMAINING"
done

echo ""
echo "=========================================="
echo "✅ Test 2: Check response headers"
echo "=========================================="
curl -s -i -X GET "$BASE_URL/api/products?page=0&size=1" \
  -H "$AUTH" 2>&1 | grep -E "HTTP|X-Rate-Limit"

echo ""
echo "=========================================="
echo "📊 Rate Limiting Configuration:"
echo "=========================================="
echo "• Limit: 100 requests per minute"
echo "• Scope: Per IP address"
echo "• Reset: Every 60 seconds"
echo "• Error Code: 429 (Too Many Requests)"
echo ""
echo "To test exhaustion, run this command repeatedly:"
echo "  for i in {1..101}; do curl -s -X GET '$BASE_URL/api/products' -H '$AUTH' | jq '.success'; done"
echo ""
echo "When exhausted, you'll get HTTP 429 with message:"
echo '  {"success": false, "message": "Rate limit exceeded. Try again later."}'
echo ""
