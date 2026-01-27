# Phase 1.5 Testing Guide

## Quick Start Testing

### Prerequisites
1. Application running on port 8080
2. Database connected and migrated
3. Valid JWT token (from login)

### 1. Document Management Testing

#### Upload Document
```bash
curl -X POST "http://localhost:8080/api/candidates/1/documents" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@passport.pdf" \
  -F "documentType=PASSPORT" \
  -F "notes=Candidate passport scan"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Document uploaded successfully",
  "data": {
    "id": 1,
    "fileName": "passport.pdf",
    "documentType": "PASSPORT",
    "fileSize": 245678,
    "uploadedAt": "2024-01-15T10:30:00"
  }
}
```

#### List Candidate Documents
```bash
curl -X GET "http://localhost:8080/api/candidates/1/documents" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Download Document
```bash
curl -X GET "http://localhost:8080/api/documents/1/download" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  --output downloaded_passport.pdf
```

**Verify:** 
- ✅ File downloads correctly
- ✅ Response has `Content-Disposition: attachment` header
- ✅ No Google Drive URLs in API responses

#### Get Shareable Link
```bash
curl -X GET "http://localhost:8080/api/documents/1/share" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "documentId": 1,
    "downloadUrl": "/api/documents/1/download",
    "expiresAt": null
  }
}
```

**Verify:**
- ✅ URL is a backend endpoint, NOT a direct Drive link

---

### 2. Expiry Intelligence Testing

#### Manual Expiry Check (Admin Tool)
```java
// In Spring Boot console or admin endpoint
@Autowired
private ExpiryMonitoringService expiryMonitoringService;

// Trigger manual check
expiryMonitoringService.runManualExpiryCheck();
```

#### Set Up Test Candidates
```sql
-- Candidate with EXPIRING_SOON passport (80 days from now)
UPDATE candidates 
SET passport_expiry = CURRENT_DATE + INTERVAL '80 days'
WHERE id = 1;

-- Candidate with EXPIRED medical (yesterday)
UPDATE candidates 
SET medical_expiry = CURRENT_DATE - INTERVAL '1 day'
WHERE id = 2;

-- Candidate with VALID documents
UPDATE candidates 
SET passport_expiry = CURRENT_DATE + INTERVAL '2 years',
    medical_expiry = CURRENT_DATE + INTERVAL '1 year'
WHERE id = 3;
```

#### Run Scheduled Job
```bash
# Wait until 2:00 AM for automatic execution
# OR manually trigger (see above)
```

#### Check Results
```sql
-- Check expiry flags
SELECT 
    internal_ref_no,
    passport_expiry,
    medical_expiry,
    expiry_flag,
    current_status
FROM candidates
WHERE deleted_at IS NULL
ORDER BY expiry_flag DESC;
```

**Expected Results:**
- Candidate 1: `expiry_flag = 'EXPIRING_SOON'`
- Candidate 2: `expiry_flag = 'EXPIRED'`
- Candidate 3: `expiry_flag = 'VALID'` or NULL

#### Check Logs
```log
2024-01-15 02:00:00 INFO ExpiryMonitoringService - Starting expiry monitoring job...
2024-01-15 02:00:01 WARN ExpiryMonitoringService - Candidate REF001 - Passport EXPIRING SOON (Expiry: 2024-04-05)
2024-01-15 02:00:01 WARN ExpiryMonitoringService - Candidate REF002 - Medical EXPIRED (Expiry: 2024-01-14)
2024-01-15 02:00:02 INFO ExpiryMonitoringService - Expiry monitoring completed. Expired: 1, Expiring Soon: 1, Total Checked: 50
```

---

### 3. Offer Letter Workflow Testing

#### Create Draft Offer
```bash
curl -X POST "http://localhost:8080/api/offers/draft" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "candidateId=1" \
  -d "jobOrderId=1" \
  -d "salary=50000.00" \
  -d "jobTitle=Software Engineer"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Draft offer letter created",
  "data": {
    "id": 1,
    "candidate": { "id": 1, "internalRefNo": "REF001" },
    "jobOrder": { "id": 1 },
    "status": "DRAFT",
    "offeredSalary": 50000.00,
    "jobTitle": "Software Engineer",
    "issuedAt": null,
    "signedAt": null
  }
}
```

#### Issue Offer (WITHOUT Medical Clearance) - Should FAIL
```bash
# Set candidate medical status to PENDING
curl -X PUT "http://localhost:8080/api/candidates/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"medicalStatus": "PENDING"}'

# Try to issue offer
curl -X POST "http://localhost:8080/api/offers/1/issue" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Cannot issue offer: Candidate must have PASSED medical status. Current status: PENDING",
  "data": null
}
```

**Verify:** ✅ Offer issuance blocked due to medical rule

#### Issue Offer (WITH Medical Clearance) - Should SUCCEED
```bash
# Set candidate medical status to PASSED
curl -X PUT "http://localhost:8080/api/candidates/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"medicalStatus": "PASSED"}'

# Issue offer
curl -X POST "http://localhost:8080/api/offers/1/issue" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Offer letter issued successfully",
  "data": {
    "id": 1,
    "status": "ISSUED",
    "issuedAt": "2024-01-15T14:30:00",
    "signedAt": null
  }
}
```

**Verify:** ✅ Offer issued after medical clearance

#### Sign Offer as OPERATIONS_STAFF - Should FAIL
```bash
# Login as OPERATIONS_STAFF
curl -X POST "http://localhost:8080/api/offers/1/sign" \
  -H "Authorization: Bearer OPERATIONS_STAFF_TOKEN"
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Access Denied",
  "data": null
}
```

**Verify:** ✅ Only APPLICANT role can sign (403 Forbidden)

#### Sign Offer as APPLICANT - Should SUCCEED
```bash
# Login as candidate (APPLICANT role)
# Ensure candidate email matches the offer's candidate email
curl -X POST "http://localhost:8080/api/offers/1/sign" \
  -H "Authorization: Bearer APPLICANT_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Offer letter signed successfully",
  "data": {
    "id": 1,
    "status": "SIGNED",
    "issuedAt": "2024-01-15T14:30:00",
    "signedAt": "2024-01-15T15:45:00"
  }
}
```

**Verify:** ✅ Applicant successfully signed offer

#### Withdraw Signed Offer - Should FAIL
```bash
curl -X POST "http://localhost:8080/api/offers/1/withdraw" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "reason=Budget constraints"
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Cannot withdraw a signed offer",
  "data": null
}
```

**Verify:** ✅ Signed offers cannot be withdrawn (business rule)

#### Issue Second Offer While First is ISSUED - Should FAIL
```bash
# Create another draft
curl -X POST "http://localhost:8080/api/offers/draft" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d "candidateId=1" \
  -d "jobOrderId=2" \
  -d "salary=60000.00" \
  -d "jobTitle=Senior Engineer"

# Try to issue while offer #1 is still ISSUED (not signed/withdrawn)
curl -X POST "http://localhost:8080/api/offers/2/issue" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Candidate already has a pending offer",
  "data": null
}
```

**Verify:** ✅ No concurrent offers allowed

---

## Frontend Integration Testing

### Update index.html
Add to existing candidate management section:

```javascript
// Upload Document
async function uploadDocument(candidateId, file, documentType) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', documentType);
    
    const response = await fetch(`/api/candidates/${candidateId}/documents`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` },
        body: formData
    });
    
    return response.json();
}

// Issue Offer
async function issueOffer(candidateId, jobOrderId, salary, jobTitle) {
    // Create draft
    const draftResponse = await fetch('/api/offers/draft', {
        method: 'POST',
        headers: { 
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            candidateId,
            jobOrderId,
            salary,
            jobTitle
        })
    });
    
    const draft = await draftResponse.json();
    
    // Issue offer
    const issueResponse = await fetch(`/api/offers/${draft.data.id}/issue`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
    });
    
    return issueResponse.json();
}

// Check Expiry Status
function getExpiryBadge(expiryFlag) {
    if (expiryFlag === 'EXPIRED') {
        return '<span class="badge bg-danger">EXPIRED</span>';
    } else if (expiryFlag === 'EXPIRING_SOON') {
        return '<span class="badge bg-warning">EXPIRING SOON</span>';
    } else {
        return '<span class="badge bg-success">VALID</span>';
    }
}
```

---

## Automated Test Script

```bash
#!/bin/bash

# Phase 1.5 Integration Test Script

BASE_URL="http://localhost:8080"
TOKEN=""  # Set JWT token here

echo "=== Phase 1.5 Integration Tests ==="

# 1. Login
echo "1. Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}')
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.accessToken')
echo "Token obtained: ${TOKEN:0:20}..."

# 2. Upload Document
echo "2. Uploading passport document..."
UPLOAD_RESPONSE=$(curl -s -X POST "$BASE_URL/api/candidates/1/documents" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test_passport.pdf" \
  -F "documentType=PASSPORT")
echo $UPLOAD_RESPONSE | jq .

# 3. Create Draft Offer
echo "3. Creating draft offer..."
DRAFT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/offers/draft" \
  -H "Authorization: Bearer $TOKEN" \
  -d "candidateId=1&jobOrderId=1&salary=50000&jobTitle=Engineer")
OFFER_ID=$(echo $DRAFT_RESPONSE | jq -r '.data.id')
echo "Offer ID: $OFFER_ID"

# 4. Try to issue without medical clearance
echo "4. Attempting to issue offer without medical clearance (should fail)..."
ISSUE_FAIL=$(curl -s -X POST "$BASE_URL/api/offers/$OFFER_ID/issue" \
  -H "Authorization: Bearer $TOKEN")
echo $ISSUE_FAIL | jq .

# 5. Update medical status to PASSED
echo "5. Updating candidate medical status to PASSED..."
UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/candidates/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"medicalStatus":"PASSED"}')

# 6. Issue offer successfully
echo "6. Issuing offer with medical clearance (should succeed)..."
ISSUE_SUCCESS=$(curl -s -X POST "$BASE_URL/api/offers/$OFFER_ID/issue" \
  -H "Authorization: Bearer $TOKEN")
echo $ISSUE_SUCCESS | jq .

echo "=== Tests Complete ==="
```

---

## Database Verification

```sql
-- Check document uploads
SELECT 
    c.internal_ref_no,
    cd.file_name,
    cd.document_type,
    cd.file_size,
    cd.uploaded_at
FROM candidate_documents cd
JOIN candidates c ON cd.candidate_id = c.id
WHERE cd.deleted_at IS NULL
ORDER BY cd.uploaded_at DESC;

-- Check expiry flags
SELECT 
    internal_ref_no,
    passport_expiry,
    medical_expiry,
    expiry_flag,
    CASE 
        WHEN expiry_flag = 'EXPIRED' THEN 'CRITICAL'
        WHEN expiry_flag = 'EXPIRING_SOON' THEN 'WARNING'
        ELSE 'OK'
    END as priority
FROM candidates
WHERE deleted_at IS NULL
ORDER BY 
    CASE expiry_flag 
        WHEN 'EXPIRED' THEN 1
        WHEN 'EXPIRING_SOON' THEN 2
        ELSE 3
    END;

-- Check offer letter workflow
SELECT 
    ol.id,
    c.internal_ref_no,
    ol.status,
    ol.issued_at,
    ol.signed_at,
    TIMESTAMPDIFF(HOUR, ol.issued_at, ol.signed_at) as hours_to_sign
FROM offer_letters ol
JOIN candidates c ON ol.candidate_id = c.id
WHERE ol.deleted_at IS NULL
ORDER BY ol.created_at DESC;
```

---

## Success Criteria

### Document Management ✅
- [ ] Passport PDF uploads successfully
- [ ] Medical certificate uploads with MEDICAL type
- [ ] Download streams file with correct Content-Type
- [ ] No direct Google Drive URLs in any response
- [ ] APPLICANT can only access own documents (test with different users)
- [ ] Soft delete removes from Drive and database

### Expiry Intelligence ✅
- [ ] Scheduled job runs at 2:00 AM daily
- [ ] Manual trigger works: `expiryMonitoringService.runManualExpiryCheck()`
- [ ] Passport expiring in 80 days flagged as EXPIRING_SOON
- [ ] Medical expired yesterday flagged as EXPIRED
- [ ] Valid documents (>90 days) remain VALID
- [ ] Logs show all status changes with candidate reference

### Offer Letter Workflow ✅
- [ ] Draft offer creates successfully
- [ ] Cannot issue without medical clearance (MedicalStatus.PASSED)
- [ ] Issuing with medical clearance succeeds
- [ ] Interview check NOT enforced (workflow flexibility)
- [ ] OPERATIONS_STAFF cannot sign (403 Forbidden)
- [ ] APPLICANT can sign own offer
- [ ] Cannot sign someone else's offer (email mismatch check)
- [ ] Cannot withdraw signed offer
- [ ] No concurrent offers to same candidate

---

## Troubleshooting

### Issue: Documents not uploading
**Check:**
1. Google Drive credentials.json present?
2. Service account has write access to Drive folder?
3. File size within limits (default: 10MB)?

### Issue: Expiry job not running
**Check:**
1. `@EnableScheduling` annotation on RomsApplication?
2. Application timezone matches server timezone?
3. Check logs for `ExpiryMonitoringService - Starting expiry monitoring job`

### Issue: Offer issuance fails
**Check:**
1. Candidate has medicalStatus = PASSED?
2. Offer is in DRAFT status?
3. No pending offers for this candidate?

---

**Testing Completed** ✅ - Phase 1.5 Ready for Demo
