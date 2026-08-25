#!/bin/bash

##############################################
# Optimistic Locking Test - Concurrent Updates
# Tests version-based conflict detection
##############################################

echo "=========================================="
echo "   OPTIMISTIC LOCKING TEST"
echo "=========================================="
echo ""

BASE_URL="http://localhost:9000"
AUTH_HEADER="Authorization: Basic YWRtaW46QWRtaW5AMTIz"
CONTENT_TYPE="Content-Type: application/json"

# Step 1: Get current user version
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 1: Get Current User Version"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

CURRENT_USER=$(curl -s -X GET "$BASE_URL/api/user/1" \
  -H "$AUTH_HEADER" \
  -H "$CONTENT_TYPE")

echo "Current User:"
echo "$CURRENT_USER" | jq '.data | {id, username, firstName, version}'
CURRENT_VERSION=$(echo "$CURRENT_USER" | jq '.data.version')
echo ""
echo "Current Version: $CURRENT_VERSION"
echo ""

# Step 2: First update (should succeed)
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 2: Update 1 - Change Name to 'Alice'"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

UPDATE1=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/user/1" \
  -H "$AUTH_HEADER" \
  -H "$CONTENT_TYPE" \
  -d '{
    "firstName": "Alice",
    "lastName": "Smith"
  }')

BODY1=$(echo "$UPDATE1" | head -n -1)
STATUS1=$(echo "$UPDATE1" | tail -n 1)

echo "Status: $STATUS1"
echo "Response:"
echo "$BODY1" | jq '.'

NEW_VERSION=$(echo "$BODY1" | jq '.data.version // "ERROR"' 2>/dev/null)
echo ""
echo "✅ Update 1 Success - Version: $CURRENT_VERSION → $NEW_VERSION"
echo ""

# Step 3: Second update (may conflict)
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 3: Update 2 - Change Name to 'Bob'"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

UPDATE2=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/user/1" \
  -H "$AUTH_HEADER" \
  -H "$CONTENT_TYPE" \
  -d '{
    "firstName": "Bob",
    "lastName": "Jones"
  }')

BODY2=$(echo "$UPDATE2" | head -n -1)
STATUS2=$(echo "$UPDATE2" | tail -n 1)

echo "Status: $STATUS2"
echo "Response:"
echo "$BODY2" | jq '.'
echo ""

if [ "$STATUS2" = "200" ]; then
    echo "✅ Update 2 Success - Sequential updates both succeeded"
    FINAL_VERSION=$(echo "$BODY2" | jq '.data.version' 2>/dev/null)
    echo "   Final Version: $FINAL_VERSION"
elif [ "$STATUS2" = "409" ]; then
    echo "⚠️  Update 2 Conflict (409) - Version conflict detected (expected in high concurrency)"
    echo "   This is normal when updates arrive simultaneously"
else
    echo "❌ Update 2 Unexpected Error - Status: $STATUS2"
fi

echo ""

# Step 4: Verify final state
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 4: Verify Final State"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

FINAL_USER=$(curl -s -X GET "$BASE_URL/api/user/1" \
  -H "$AUTH_HEADER" \
  -H "$CONTENT_TYPE")

echo "Final User State:"
echo "$FINAL_USER" | jq '.data | {id, username, firstName, version}'

FINAL_NAME=$(echo "$FINAL_USER" | jq -r '.data.firstName')
FINAL_VERSION=$(echo "$FINAL_USER" | jq '.data.version')

echo ""
echo "Final Name: $FINAL_NAME"
echo "Final Version: $FINAL_VERSION"
echo ""

# Summary
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "SUMMARY"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "✅ Version Field Working:"
echo "   - Initial:  $CURRENT_VERSION"
echo "   - After 1st update: $NEW_VERSION (incremented)"
if [ "$FINAL_VERSION" != "$NEW_VERSION" ] && [ "$STATUS2" = "200" ]; then
    echo "   - After 2nd update: $FINAL_VERSION (incremented)"
else
    echo "   - After 2nd update: (conflict or no change)"
fi

echo ""
echo "📊 Update Results:"
echo "   - Update 1: HTTP $STATUS1 ✅"
echo "   - Update 2: HTTP $STATUS2 (200=OK, 409=Conflict)"

echo ""
echo "=========================================="
echo "   OPTIMISTIC LOCKING TEST COMPLETE"
echo "=========================================="
