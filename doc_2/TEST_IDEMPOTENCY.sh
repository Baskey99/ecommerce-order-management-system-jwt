#!/bin/bash

##############################################
# Idempotency Test - Duplicate Request Detection
# Tests that duplicate requests return same response
##############################################

echo "=========================================="
echo "   IDEMPOTENCY TEST - DUPLICATE DETECTION"
echo "=========================================="
echo ""

BASE_URL="http://localhost:9000"
AUTH_HEADER="Authorization: Basic YWRtaW46QWRtaW5AMTIz"
CONTENT_TYPE="Content-Type: application/json"

# Generate UUID for idempotency key
IDEMPOTENCY_KEY=$(uuidgen)
echo "Generated Idempotency-Key: $IDEMPOTENCY_KEY"
echo ""

# Request 1: Initial order creation
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "REQUEST 1: Create Order (First Time)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

RESPONSE1=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/orders" \
  -H "Idempotency-Key: $IDEMPOTENCY_KEY" \
  -H "$AUTH_HEADER" \
  -H "$CONTENT_TYPE" \
  -d '{
    "userId": 1,
    "items": [{"productId": 1, "quantity": 2}],
    "status": "PENDING"
  }')

BODY1=$(echo "$RESPONSE1" | head -n -1)
STATUS1=$(echo "$RESPONSE1" | tail -n 1)
ORDER_ID=$(echo "$BODY1" | jq -r '.data.id // "ERROR"' 2>/dev/null)

echo "Status: $STATUS1"
echo "Response:"
echo "$BODY1" | jq '.'
echo ""
echo "✅ Order ID: $ORDER_ID"
echo ""

# Wait a bit
sleep 2

# Request 2: Duplicate request with same Idempotency-Key
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "REQUEST 2: Retry with Same Idempotency-Key (DUPLICATE)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

RESPONSE2=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/orders" \
  -H "Idempotency-Key: $IDEMPOTENCY_KEY" \
  -H "$AUTH_HEADER" \
  -H "$CONTENT_TYPE" \
  -d '{
    "userId": 1,
    "items": [{"productId": 1, "quantity": 2}],
    "status": "PENDING"
  }')

BODY2=$(echo "$RESPONSE2" | head -n -1)
STATUS2=$(echo "$RESPONSE2" | tail -n 1)
ORDER_ID2=$(echo "$BODY2" | jq -r '.data.id // "ERROR"' 2>/dev/null)
REPLAYED=$(echo "$BODY2" | jq -r '.headers."X-Idempotency-Replayed" // "false"' 2>/dev/null)

echo "Status: $STATUS2"
echo "Response:"
echo "$BODY2" | jq '.'
echo ""
echo "🔄 Order ID: $ORDER_ID2"
echo "📝 Replayed: $REPLAYED"
echo ""

# Validation
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "VALIDATION"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ "$ORDER_ID" = "$ORDER_ID2" ]; then
    echo "✅ PASS: Same Order ID returned for duplicate request"
    echo "   Order 1: $ORDER_ID"
    echo "   Order 2: $ORDER_ID2"
else
    echo "❌ FAIL: Different Order IDs (duplicate not detected)"
    echo "   Order 1: $ORDER_ID"
    echo "   Order 2: $ORDER_ID2"
fi

if [ "$STATUS1" = "201" ] && [ "$STATUS2" = "200" ]; then
    echo "✅ PASS: Correct HTTP status codes (201, 200)"
else
    echo "⚠️  WARNING: Status codes not as expected ($STATUS1, $STATUS2)"
fi

echo ""
echo "=========================================="
echo "   TEST COMPLETE"
echo "=========================================="
