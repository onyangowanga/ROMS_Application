# Phase 1B Frontend Testing Guide

Complete end-to-end testing guide for ROMS Phase 1B frontend.

## Prerequisites

1. **Backend Running**: Spring Boot application on port 8080
2. **Frontend Running**: React development server on port 3000
3. **Test User**: Ensure admin user exists in database

## Quick Start

### 1. Start Backend
```batch
cd c:\Programing\Realtime projects\ROMS\Roms\Roms
start-roms.bat
```

Wait for: `Started RomsApplication in X seconds`

### 2. Start Frontend
```batch
cd c:\Programing\Realtime projects\ROMS\Roms\Roms\frontend
start-frontend.bat
```

Browser opens at: http://localhost:3000

---

## Test Scenarios

### Scenario 1: Authentication ✅

#### 1.1 Successful Login
1. Navigate to http://localhost:3000
2. Enter credentials:
   - Username: `admin`
   - Password: `password123`
3. Click "Sign in"

**Expected Result:**
- Redirects to `/dashboard`
- Navigation bar shows user name and role
- "Logout" button visible

#### 1.2 Failed Login
1. Enter invalid credentials
2. Click "Sign in"

**Expected Result:**
- Red error message appears
- Stays on login page
- Error: "Invalid username or password"

#### 1.3 Protected Route Access
1. Log out
2. Manually navigate to http://localhost:3000/candidates

**Expected Result:**
- Automatically redirects to `/login`

---

### Scenario 2: Dashboard ✅

#### 2.1 View Statistics
1. Login as admin
2. View dashboard

**Expected Result:**
- "Total Candidates" card shows count
- "Expiring Documents" card (if any exist)
- Status breakdown section shows candidates per status
- Color-coded status badges

#### 2.2 Quick Navigation
1. Click "View All Candidates" button

**Expected Result:**
- Navigates to `/candidates`
- Candidates table loads

---

### Scenario 3: Candidates List ✅

#### 3.1 View All Candidates
1. Navigate to "Candidates" from menu
2. View table

**Expected Result:**
- Table displays:
  - Internal Ref No
  - Full Name
  - Status (badge)
  - Passport Expiry
  - Expiry Alert (if applicable)
  - Actions (View button)
- No soft-deleted candidates shown

#### 3.2 Expiry Alerts
1. Find candidate with passport expiring soon

**Expected Result:**
- "EXPIRING_SOON" badge in yellow
- "EXPIRED" badge in red (if expired)
- "VALID" badge in green (if >90 days)

#### 3.3 Navigate to Profile
1. Click "View" on any candidate

**Expected Result:**
- Navigates to `/candidates/{id}`
- Profile page loads with full details

---

### Scenario 4: Candidate Profile - Information ✅

#### 4.1 View Candidate Details
1. Open any candidate profile
2. Review information card

**Expected Result:**
- Displays all fields:
  - Date of Birth
  - Gender
  - Email
  - Phone
  - Country
  - Passport No
  - Passport Expiry
  - Current Status (badge)
  - Medical Status (if set)
  - Document Alert (if flagged)

#### 4.2 Back Navigation
1. Click "← Back to Candidates"

**Expected Result:**
- Returns to candidates list
- Maintains scroll position

---

### Scenario 5: Document Upload ✅

#### 5.1 Upload Passport Document
1. Open candidate profile
2. In "Documents" section, fill upload form:
   - Document Type: `PASSPORT`
   - File: Select any PDF/image
   - Document Number: `AB123456`
   - Expiry Date: Set to 2 years from now
3. Click "Upload Document"

**Expected Result:**
- "Document uploaded successfully!" alert
- Document appears in list below form
- Shows: filename, type badge, file size, expiry date
- "Download" button available

#### 5.2 Upload Medical Certificate
1. Upload another document:
   - Document Type: `MEDICAL`
   - File: Select file
   - Leave optional fields empty
2. Click "Upload Document"

**Expected Result:**
- Upload succeeds
- Medical document appears in list

#### 5.3 Download Document
1. Click "Download" on any document

**Expected Result:**
- File downloads to browser
- Filename matches uploaded file
- File opens correctly

#### 5.4 Upload Error Handling
1. Try uploading without selecting file
2. Try uploading very large file (>10MB)

**Expected Result:**
- Validation error shown
- Red error message displayed
- Form remains filled with entered data

---

### Scenario 6: Workflow Transitions ✅

#### 6.1 Valid Transition (No Guard Logic)
1. Find candidate with status `APPLIED`
2. In "Workflow Transitions" section:
   - Select "SHORTLISTED"
   - Click "Execute Transition"

**Expected Result:**
- Success alert: "Candidate transitioned to SHORTLISTED successfully!"
- Status badge updates immediately
- Available transitions update
- No error message

#### 6.2 Guard Logic - Medical Rule (CRITICAL TEST)
1. Find candidate with status `MEDICAL_IN_PROGRESS`
2. Ensure medical status is NOT "PASSED"
3. Select transition to `MEDICAL_CLEARED`
4. Click "Execute Transition"

**Expected Result:**
- **Red error box appears** with message:
  - "Guard Logic Error:"
  - "Cannot transition: Medical status must be PASSED"
- Alert shows: "Transition Failed: [error message]"
- Status does NOT change
- Form remains interactive

#### 6.3 Guard Logic - Document Rule
1. Find candidate with expired passport
2. Try transitioning to advanced status
3. Click "Execute Transition"

**Expected Result:**
- Guard logic error displayed
- Backend validation message shown
- Transition blocked

#### 6.4 No Available Transitions
1. Find candidate with status `PLACED` or `REJECTED`
2. View workflow section

**Expected Result:**
- Message: "No transitions available from current status"
- No dropdown shown
- No "Execute Transition" button

---

### Scenario 7: Role-Based Access Control ✅

#### 7.1 OPERATIONS_STAFF Access
1. Login as operations staff user
2. View navigation menu

**Expected Result:**
- Dashboard link visible
- Candidates link visible
- Can view candidate profiles
- Can upload documents
- Can perform transitions

#### 7.2 FINANCE_MANAGER Access
1. Login as finance manager
2. View navigation menu

**Expected Result:**
- Dashboard link visible
- Candidates link visible
- Can view candidates list
- **Cannot** access candidate profile (403 error)

#### 7.3 APPLICANT Access
1. Login as applicant user
2. View navigation menu

**Expected Result:**
- Dashboard link visible
- Candidates link NOT visible
- Direct URL to /candidates redirects or shows "Access Denied"

---

### Scenario 8: Error Handling ✅

#### 8.1 Network Error
1. Stop backend server
2. Try loading candidates list

**Expected Result:**
- Error message displayed
- User-friendly error text
- Application doesn't crash

#### 8.2 Session Timeout
1. Login successfully
2. Manually delete token from localStorage:
   ```javascript
   // In browser console:
   localStorage.removeItem('accessToken')
   ```
3. Try navigating to candidates

**Expected Result:**
- Automatically redirects to login
- Token cleared
- No error popup

#### 8.3 Invalid Candidate ID
1. Navigate to http://localhost:3000/candidates/99999

**Expected Result:**
- Error message: "Candidate not found"
- "Back to Candidates" button works

---

### Scenario 9: Complete Demo Flow (BOARDROOM DEMO) 🎬

This is the flow for pitching to clients:

**1. Login** (10 seconds)
- Show clean, professional login screen
- Enter credentials and authenticate

**2. Dashboard Overview** (20 seconds)
- Point out total candidates
- Show status breakdown
- Highlight expiring documents alert
- "This gives recruiters instant visibility"

**3. Browse Candidates** (30 seconds)
- Navigate to candidates list
- "Here are all active candidates"
- Point out status badges
- Show expiry alerts
- "Red means expired, yellow means expiring soon"

**4. Open Candidate Profile** (1 minute)
- Click "View" on a candidate
- Show comprehensive candidate info
- "All details in one place"

**5. Document Management** (1 minute)
- Scroll to documents section
- Upload a passport document
- Show it appears in the list
- Download the document
- "All documents stored securely in Google Drive"
- "No direct URLs exposed - everything goes through our backend"

**6. Workflow Demonstration** (2 minutes)
- Show current status
- "Let's move this candidate to the next stage"
- Select a valid transition (e.g., SHORTLISTED)
- Execute successfully
- Watch status update

**7. Guard Logic Demo** (2 minutes) - **CRITICAL**
- "Now watch what happens when we try to skip required steps"
- Try transitioning to MEDICAL_CLEARED without medical clearance
- **Show the error message**
- "This is our business logic enforcement"
- "The system prevents invalid state transitions"
- "Medical clearance is mandatory before offer issuance"

**8. Wrap Up** (30 seconds)
- Navigate back to dashboard
- "And that's the complete candidate lifecycle"
- "Ready for your team to start using immediately"

**Total Demo Time**: 7-8 minutes

---

## API Testing (Backend Validation)

### Using Browser DevTools

1. Open Chrome DevTools (F12)
2. Go to Network tab
3. Perform any action (login, upload, transition)
4. View API calls:
   - Request headers (Authorization: Bearer token)
   - Response data (ApiResponse wrapper)
   - Status codes (200, 400, 401, 403)

### Expected API Calls

**Login:**
```
POST /api/auth/login
Request: { username, password }
Response: { success, message, data: { accessToken, user } }
```

**Get Candidates:**
```
GET /api/candidates
Headers: Authorization: Bearer <token>
Response: { success, message, data: [candidates...] }
```

**Upload Document:**
```
POST /api/candidates/1/documents
Content-Type: multipart/form-data
Headers: Authorization: Bearer <token>
Response: { success, message, data: { document } }
```

**Transition:**
```
POST /api/candidates/1/transition?toStatus=SHORTLISTED
Headers: Authorization: Bearer <token>
Response: { success, message, data: { candidate } }
```

---

## Performance Testing

### Load Time Benchmarks
- Login page: < 1 second
- Dashboard load: < 2 seconds
- Candidates list (50 records): < 2 seconds
- Candidate profile: < 1.5 seconds
- Document upload (1MB): < 3 seconds

### Browser Compatibility
- ✅ Chrome 120+
- ✅ Edge 120+
- ✅ Firefox 120+
- ⚠️ Safari 16+ (may need testing)

---

## Troubleshooting

### Frontend won't start
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### CORS errors
Backend SecurityConfig.java should have:
```java
.cors(cors -> cors
    .configurationSource(request -> {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        config.setAllowedMethods(Arrays.asList("*"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        return config;
    })
)
```

### Documents not uploading
- Check Google Drive credentials
- Verify multipart/form-data support
- Check file size limits
- View console for errors

### Transitions failing
- Check backend logs
- Verify guard logic rules
- Ensure candidate has required fields
- Check medical status

---

## Success Criteria Checklist

### Phase 1B Definition of Done

- [x] User can log in with JWT
- [x] Dashboard shows stats and status breakdown
- [x] Candidates list displays all active candidates
- [x] Candidate profile shows complete information
- [x] Documents can be uploaded with type and metadata
- [x] Documents can be downloaded (backend streaming)
- [x] Workflow transitions execute successfully
- [x] Guard logic errors display prominently
- [x] Role-based access control enforced
- [x] Error messages clear and helpful
- [x] UI is boardroom-ready (professional styling)
- [x] No Postman needed for testing
- [x] Can demo end-to-end in <10 minutes

### Additional Quality Checks

- [x] No console errors
- [x] No TypeScript errors
- [x] All API calls use proper types
- [x] Loading states implemented
- [x] Error boundaries in place
- [x] Responsive design works
- [x] Back buttons functional
- [x] Navigation intuitive

---

## Next Steps (Phase 2)

After Phase 1B validation:

1. Offer letter viewing and signing UI
2. Financial dashboard
3. Employer portal
4. Advanced search and filtering
5. Bulk operations
6. Real-time notifications
7. Export functionality

---

**Phase 1B Testing Status: COMPLETE ✅**

All scenarios tested and validated.
Ready for client demonstrations and backend validation.
