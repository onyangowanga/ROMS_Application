# ROMS Frontend Features Documentation

## Overview
The ROMS (Recruitment and Overseas Manpower Services) frontend is a modern React TypeScript application built for managing the complete recruitment lifecycle. It provides an intuitive interface for different user roles to manage candidates, employers, job orders, documents, and payments.

## Technology Stack
- **Framework**: React 18.2.0 with TypeScript 5.2.2
- **Build Tool**: Vite 5.0.8
- **Styling**: Tailwind CSS 3.3.6
- **HTTP Client**: Axios 1.6.2
- **Routing**: React Router v6.20.1
- **State Management**: React Context API
- **Development Server**: http://localhost:3000

## Core Features

### 1. Authentication System
**Location**: [LoginPage.tsx](frontend/src/pages/LoginPage.tsx), [AuthContext.tsx](frontend/src/context/AuthContext.tsx)

- **JWT-Based Authentication**: Secure login with access and refresh tokens
- **Token Storage**: Tokens stored in localStorage for session persistence
- **Auto-Login**: Automatic authentication on page reload if valid token exists
- **Logout**: Secure logout with token cleanup
- **Demo Credentials Displayed**: Login page shows available demo accounts for testing

**Demo Users**:
- `admin / password123` - Super Admin (full access)
- `operations / password123` - Operations Staff (candidate & document management)
- `finance / password123` - Finance Manager (view candidates, manage payments)
- `employer / password123` - Employer (limited access)
- `applicant / password123` - Applicant (self-service portal)

### 2. Role-Based Access Control (RBAC)
**Location**: [ProtectedRoute.tsx](frontend/src/components/ProtectedRoute.tsx)

The application implements comprehensive role-based access control with 5 distinct roles:

#### SUPER_ADMIN
- Full system access
- User management
- System configuration
- All CRUD operations on all entities

#### OPERATIONS_STAFF
- Candidate management (create, view, update)
- Document upload and verification
- Workflow state transitions
- Job order management

#### FINANCE_MANAGER
- View candidates and applications
- Payment management
- Financial reporting
- Read-only access to most data

#### EMPLOYER
- View own job orders
- View assigned candidates
- Limited candidate information access

#### APPLICANT
- Self-service portal
- Own profile management
- Application status tracking
- Document upload

### 3. Dashboard
**Location**: [DashboardPage.tsx](frontend/src/pages/DashboardPage.tsx)

- **Welcome Message**: Personalized greeting with user's full name and role
- **Quick Stats**: Overview cards showing key metrics
- **Recent Activity**: Latest candidate applications and status changes
- **Role-Specific Views**: Different dashboard widgets based on user role
- **Navigation Links**: Quick access to main features

### 4. Candidate Management
**Location**: [CandidatesPage.tsx](frontend/src/pages/CandidatesPage.tsx), [CandidateProfilePage.tsx](frontend/src/pages/CandidateProfilePage.tsx)

#### Candidates List View
- **Table Display**: Comprehensive candidate listing with key information
- **Columns**:
  - Internal Reference Number (ROM-XXX)
  - Full Name
  - Email
  - Phone Number
  - Current Status (with color-coded badges)
  - Actions (View/Edit)
- **Pagination**: Navigate through large candidate lists
- **Search & Filter**: Find candidates quickly by various criteria
- **Status Badges**: Visual indicators for candidate workflow status

#### Candidate Profile View
- **Personal Information**:
  - Full name, date of birth, gender
  - Contact details (email, phone)
  - Address and location
  - Passport information with expiry date
  
- **Employment Details**:
  - Assigned job order
  - Education background
  - Years of experience
  - Current workflow status
  - Medical status
  
- **Document Management**:
  - List of uploaded documents
  - Document verification status
  - Document type indicators
  - Upload new documents (based on role permissions)
  
- **Payment History**:
  - Transaction records
  - Payment types (Visa Fee, Processing Fee, Ticket, Medical Fee)
  - Payment amounts and methods
  - Transaction references

#### Workflow Status Management
**Valid Candidate Statuses**:
1. `APPLIED` - Initial application submitted
2. `DOCS_SUBMITTED` - Required documents uploaded
3. `INTERVIEWED` - Interview completed
4. `MEDICAL_PASSED` - Medical examination cleared
5. `OFFER_ISSUED` - Job offer sent to candidate
6. `OFFER_ACCEPTED` - Candidate accepted the offer
7. `PLACED` - Successfully deployed to employer
8. `REJECTED` - Application rejected
9. `WITHDRAWN` - Candidate withdrew application

**Status Transitions**:
- Controlled workflow with validation rules
- Only valid transitions allowed (e.g., can't jump from APPLIED to PLACED)
- Guard logic prevents invalid state changes
- Status badge color coding:
  - Blue: In progress (APPLIED, DOCS_SUBMITTED)
  - Yellow: Under review (INTERVIEWED)
  - Green: Positive outcomes (MEDICAL_PASSED, OFFER_ISSUED, OFFER_ACCEPTED, PLACED)
  - Red: Negative outcomes (REJECTED, WITHDRAWN)

### 5. Document Management
**Location**: [CandidateProfilePage.tsx](frontend/src/pages/CandidateProfilePage.tsx)

**Document Types**:
- `PASSPORT` - Passport copy
- `PHOTO` - Candidate photograph
- `EDUCATIONAL_CERTIFICATE` - Academic credentials
- `MEDICAL_CERTIFICATE` - Medical examination reports
- `POLICE_CLEARANCE` - Police clearance certificate

**Features**:
- **Upload**: File upload for new documents (role-based permissions)
- **Verification Status**: Track which documents are verified
- **Document List**: View all documents for a candidate
- **File Reference**: Google Drive file IDs for document storage
- **Verification Toggle**: Operations staff can verify documents

### 6. Status Badge Component
**Location**: [StatusBadge.tsx](frontend/src/components/StatusBadge.tsx)

Reusable component for displaying candidate status with:
- Color-coded badges based on status type
- Consistent styling across the application
- Responsive design
- Accessibility support

### 7. Layout & Navigation
**Location**: [Layout.tsx](frontend/src/components/Layout.tsx)

**Header**:
- Application logo and title
- User information display (name and role)
- Logout button

**Navigation Menu** (role-based):
- Dashboard
- Candidates
- Employers (SUPER_ADMIN, OPERATIONS_STAFF)
- Job Orders (SUPER_ADMIN, OPERATIONS_STAFF)
- Documents (SUPER_ADMIN, OPERATIONS_STAFF)
- Payments (SUPER_ADMIN, FINANCE_MANAGER)
- Users (SUPER_ADMIN only)

**Footer**:
- Copyright information
- Application version

### 8. Protected Routes
**Location**: [ProtectedRoute.tsx](frontend/src/components/ProtectedRoute.tsx)

- **Authentication Check**: Redirects to login if not authenticated
- **Role Verification**: Ensures user has required role for route access
- **Unauthorized Handling**: Shows error message for insufficient permissions
- **Token Validation**: Validates JWT token before allowing access

## API Integration

### Base Configuration
**Location**: [axios.ts](frontend/src/api/axios.ts)

- **Base URL**: http://localhost:8080/api
- **Request Interceptor**: Automatically adds JWT token to Authorization header
- **Response Interceptor**: Handles 401 errors and token refresh
- **Error Handling**: Centralized error handling with user-friendly messages

### API Modules

#### Authentication API
**Location**: [auth.ts](frontend/src/api/auth.ts)

```typescript
- login(credentials: LoginRequest): Promise<AuthResponse>
- logout(): Promise<void>
- refreshToken(refreshToken: string): Promise<AuthResponse>
- getCurrentUser(): Promise<UserResponse>
```

#### Candidates API
**Location**: [candidates.ts](frontend/src/api/candidates.ts)

```typescript
- getAllCandidates(params?: PaginationParams): Promise<CandidateListResponse>
- getCandidateById(id: number): Promise<CandidateResponse>
- createCandidate(data: CreateCandidateRequest): Promise<CandidateResponse>
- updateCandidate(id: number, data: UpdateCandidateRequest): Promise<CandidateResponse>
- deleteCandidate(id: number): Promise<void>
- updateCandidateStatus(id: number, status: CandidateStatus): Promise<CandidateResponse>
```

## TypeScript Types
**Location**: [types/index.ts](frontend/src/types/index.ts)

Comprehensive type definitions for:
- User and authentication types
- Candidate types with full field definitions
- Employer and job order types
- Document types
- Payment types
- API request and response types
- Pagination types
- Enum types (CandidateStatus, MedicalStatus, DocumentType, PaymentType)

## Styling & UI

### Tailwind CSS Configuration
**Location**: [tailwind.config.js](frontend/tailwind.config.js)

- Custom color palette
- Responsive breakpoints
- Custom utility classes
- Component-specific styles

### Design Features
- **Responsive Design**: Mobile-first approach, works on all screen sizes
- **Color Scheme**: Professional blue and gray palette
- **Typography**: Clear, readable font hierarchy
- **Spacing**: Consistent padding and margins
- **Shadows**: Subtle elevation for cards and modals
- **Hover Effects**: Interactive feedback on buttons and links
- **Form Styling**: Clean, accessible form inputs
- **Loading States**: Visual feedback during API calls
- **Error Messages**: User-friendly error notifications

## Build & Deployment

### Development
```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:3000
```

### Production Build
```bash
npm run build
# Outputs to frontend/dist/
```

### Build Configuration
**Location**: [vite.config.ts](frontend/vite.config.ts)

- React plugin configured
- Path aliases for clean imports
- Production optimization
- Asset handling

## Security Features

1. **JWT Authentication**: Secure token-based authentication
2. **Token Refresh**: Automatic token refresh to maintain sessions
3. **Role-Based Access**: Server-side and client-side permission checks
4. **Protected Routes**: Unauthorized users cannot access restricted pages
5. **Secure Storage**: Sensitive data stored securely in localStorage
6. **CORS Handling**: Proper CORS configuration with backend
7. **XSS Protection**: React's built-in XSS protection
8. **Input Validation**: Client-side validation before API calls

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
- Mobile browsers (iOS Safari, Chrome Mobile)

## Known Limitations

1. **No Offline Mode**: Requires active internet connection
2. **File Upload**: Documents reference Google Drive IDs (actual upload not implemented)
3. **Real-time Updates**: No WebSocket support (manual refresh required)
4. **Advanced Search**: Basic search only, no advanced filters yet
5. **Reporting**: No built-in reports or analytics dashboard
6. **Multi-language**: English only
7. **Notifications**: No push notifications or email alerts
8. **Bulk Operations**: No bulk import/export functionality

## Future Enhancement Opportunities

1. **Document Viewer**: In-app PDF/image preview
2. **Advanced Filtering**: Multi-criteria search and filters
3. **Export Features**: Export candidate lists to Excel/PDF
4. **Real-time Notifications**: WebSocket-based live updates
5. **Dashboard Analytics**: Charts and graphs for metrics
6. **Email Integration**: Automated email notifications
7. **Bulk Operations**: Import candidates from CSV/Excel
8. **Audit Trail**: Complete history of all changes
9. **Mobile App**: Native iOS/Android applications
10. **Multi-language Support**: Internationalization (i18n)

## Testing Recommendations

### Manual Testing Checklist

**Authentication**:
- [ ] Login with all 5 demo user roles
- [ ] Logout and verify token cleanup
- [ ] Try accessing protected routes without login
- [ ] Verify role-based menu visibility

**Candidates**:
- [ ] View candidates list
- [ ] Open candidate profile
- [ ] Verify all candidate information displays correctly
- [ ] Check status badges color coding
- [ ] Test pagination if > 10 candidates

**Documents**:
- [ ] View document list for candidate
- [ ] Check verification status display

**Payments**:
- [ ] View payment history for candidate
- [ ] Verify payment details display

**Responsiveness**:
- [ ] Test on desktop (1920x1080)
- [ ] Test on tablet (768px width)
- [ ] Test on mobile (375px width)

**Error Handling**:
- [ ] Try invalid login credentials
- [ ] Test with backend server down
- [ ] Verify error messages are user-friendly

## Troubleshooting

### Common Issues

**Login Fails**:
- Ensure backend is running on port 8080
- Check browser console for CORS errors
- Verify demo users exist in database

**Blank Dashboard**:
- Check authentication token in localStorage
- Verify API base URL is correct
- Check browser console for JavaScript errors

**Cannot See Candidates**:
- Verify user role has permission
- Check if test data is loaded in database
- Look for API errors in Network tab

**Styling Issues**:
- Clear browser cache
- Rebuild frontend with `npm run build`
- Check Tailwind CSS compilation

## Contact & Support

For issues or questions about the frontend:
1. Check browser console for errors
2. Verify backend API is responding
3. Review this documentation
4. Check demo credentials match database users

---

**Last Updated**: January 27, 2026  
**Version**: Phase 1B  
**Frontend Port**: 3000  
**Backend Port**: 8080
