#!/bin/bash

##############################################
# Simple Idempotency Test - Easy to Follow
##############################################

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║          IDEMPOTENCY TEST - SIMPLE VERSION                 ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

BASE_URL="http://localhost:9000"
AUTH="Authorization: Basic YWRtaW46QWRtaW5AMTIz"
CONTENT_TYPE="Content-Type: application/json"

# Step 1: Health check
echo "▶ Step 1: Checking if server is running..."
HEALTH=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL/actuator/health)

if [ "$HEALTH" != "200" ]; then
    echo "❌ Server not responding (HTTP $HEALTH)"
    echo "   Start server first: docker-compose up -d"
    exit 1
fi
echo "✅ Server is running"
echo ""

# Step 2: Generate Idempotency Key
echo "▶ Step 2: Generating Idempotency Key..."
IDEMPOTENCY_KEY=$(uuidgen)
echo "   Generated: $IDEMPOTENCY_KEY"
echo ""

# Step 3: First request
echo "▶ Step 3: Sending FIRST request..."
echo "   POST /api/orders"
echo "   Idempotency-Key: $IDEMPOTENCY_KEY"
echo ""

RESPONSE1=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/orders" \
  -H "Idempotency-Key: $IDEMPOTENCY_KEY" \
  -H "$AUTH" \
  -H "$CONTENT_TYPE" \
  -d '{
    "userId": 1,
    "items": [{"productId": 1, "quantity": 2}],
    "status": "PENDING"
  }')

BODY1=$(echo "$RESPONSE1" | head -n -1)
STATUS1=$(echo "$RESPONSE1" | tail -n 1)
ORDER_ID1=$(echo "$BODY1" | jq -r '.data.id // "ERROR"' 2>/dev/null)

echo "   Status: HTTP $STATUS1"
echo "   Order ID: $ORDER_ID1"
echo ""

if [ "$STATUS1" != "201" ]; then
    echo "❌ First request failed!"
    echo "   Response: $BODY1"
    exit 1
fi

echo "✅ First request successful"
echo ""

# Step 4: Wait
echo "▶ Step 4: Waiting 2 seconds..."
sleep 2
echo "✅ Done"
echo ""

# Step 5: Second request (duplicate)
echo "▶ Step 5: Sending DUPLICATE request..."
echo "   (same Idempotency-Key)"
echo ""

RESPONSE2=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/orders" \
  -H "Idempotency-Key: $IDEMPOTENCY_KEY" \
  -H "$AUTH" \
  -H "$CONTENT_TYPE" \
  -d '{
    "userId": 1,
    "items": [{"productId": 1, "quantity": 2}],
    "status": "PENDING"
  }')

BODY2=$(echo "$RESPONSE2" | head -n -1)
STATUS2=$(echo "$RESPONSE2" | tail -n 1)
ORDER_ID2=$(echo "$BODY2" | jq -r '.data.id // "ERROR"' 2>/dev/null)
REPLAYED=$(echo "$BODY2" | jq -r '.headers."X-Idempotency-Replayed" // "NOT SET"' 2>/dev/null)

echo "   Status: HTTP $STATUS2"
echo "   Order ID: $ORDER_ID2"
echo "   Replayed: $REPLAYED"
echo ""

echo "✅ Second request completed"
echo ""

# Step 6: Verification
echo "▶ Step 6: VERIFICATION"
echo "   ────────────────────────────────────────"

PASS=0
TOTAL=0

# Check 1: Same Order ID
TOTAL=$((TOTAL + 1))
if [ "$ORDER_ID1" = "$ORDER_ID2" ]; then
    echo "   ✅ Same Order ID"
    echo "      First:  $ORDER_ID1"
    echo "      Second: $ORDER_ID2"
    PASS=$((PASS + 1))
else
    echo "   ❌ Different Order IDs (FAIL!)"
    echo "      First:  $ORDER_ID1"
    echo "      Second: $ORDER_ID2"
fi
echo ""

# Check 2: Correct HTTP status
TOTAL=$((TOTAL + 1))
if [ "$STATUS1" = "201" ] && [ "$STATUS2" = "200" ]; then
    echo "   ✅ Correct HTTP Status"
    echo "      First:  201 (Created)"
    echo "      Second: 200 (OK, from cache)"
    PASS=$((PASS + 1))
else
    echo "   ❌ Wrong HTTP Status"
    echo "      First:  $STATUS1 (expected 201)"
    echo "      Second: $STATUS2 (expected 200)"
fi
echo ""

# Check 3: Replayed header
TOTAL=$((TOTAL + 1))
if [ "$REPLAYED" = "true" ]; then
    echo "   ✅ X-Idempotency-Replayed Header"
    echo "      Present and set to: true"
    PASS=$((PASS + 1))
else
    echo "   ⚠️  Replayed Header"
    echo "      Value: $REPLAYED (expected: true)"
fi
echo ""

# Step 7: Summary
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                       RESULT                               ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

if [ "$PASS" -eq "$TOTAL" ]; then
    echo "   🎉 SUCCESS! Idempotency is working correctly"
    echo ""
    echo "   Summary:"
    echo "   ✅ Both requests returned same Order ID ($ORDER_ID1)"
    echo "   ✅ First request: HTTP 201 (Created)"
    echo "   ✅ Second request: HTTP 200 (Cached)"
    echo "   ✅ Response properly marked as replayed"
    echo ""
    echo "   → No duplicate order was created in database!"
else
    echo "   ❌ FAILED - Only $PASS of $TOTAL checks passed"
fi

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo ""
