# ROMS Phase 1B Frontend

Enterprise-grade React + TypeScript frontend for the Recruitment Operations Management System.

## 🎯 Purpose

This is a **demo-ready, testable UI** for:
- End-to-end backend validation
- Client pitching and demonstrations
- API contract hardening

**NOT a full product** - Phase 1B scope only.

## 🛠️ Tech Stack

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool
- **Tailwind CSS** - Styling
- **Axios** - API communication
- **React Router v6** - Routing

## 📋 Features Implemented

### ✅ Authentication
- JWT-based login
- Role-based access control (RBAC)
- Token stored in localStorage
- Auto-logout on 401/403
- Protected routes

### ✅ Dashboard
- Total candidates count
- Status breakdown with badges
- Expiring documents alert
- Quick navigation

### ✅ Candidates List
- Table view with all active candidates
- Status badges
- Passport expiry dates
- Expiry alerts (EXPIRING_SOON, EXPIRED)
- Quick view navigation

### ✅ Candidate Profile (MAIN DEMO SCREEN)
- **Candidate Information**
  - Personal details
  - Contact information
  - Passport details
  - Current workflow status
  - Medical status
  - Document expiry alerts

- **Document Management**
  - Upload documents with type selection
  - Document number and expiry date
  - List all uploaded documents
  - Download documents (backend streaming)
  - File size display

- **Workflow Transitions**
  - View current status
  - Select allowed transitions
  - Execute status changes
  - **Guard logic error display** (backend validation)
  - Workflow notes

## 🚀 Getting Started

### Prerequisites
- Node.js 18+ installed
- Backend running on http://localhost:8080

### Installation

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install
```

### Development

```bash
# Start development server
npm run dev
```

Application will open at: http://localhost:3000

### Build for Production

```bash
# Create optimized production build
npm run build

# Preview production build
npm run preview
```

## 🔑 Demo Credentials

Login with:
- **Username**: admin
- **Password**: password123

## 🎨 UI/UX Design

- **Clean enterprise look** - neutral colors, professional styling
- **Status badges** - color-coded workflow states
- **Responsive layout** - desktop-first design
- **Clear error messages** - backend errors displayed prominently
- **Boardroom-ready** - no flashy animations

## 📁 Project Structure

```
frontend/
├── src/
│   ├── api/              # API layer
│   │   ├── axios.ts      # Axios instance with interceptors
│   │   ├── auth.ts       # Authentication API
│   │   └── candidates.ts # Candidates API
│   ├── components/       # Reusable components
│   │   ├── Layout.tsx    # Main layout with navigation
│   │   ├── ProtectedRoute.tsx
│   │   └── StatusBadge.tsx
│   ├── context/          # React Context
│   │   └── AuthContext.tsx
│   ├── pages/            # Page components
│   │   ├── LoginPage.tsx
│   │   ├── DashboardPage.tsx
│   │   ├── CandidatesPage.tsx
│   │   └── CandidateProfilePage.tsx
│   ├── types/            # TypeScript types
│   │   └── index.ts
│   ├── App.tsx           # Main app component
│   ├── main.tsx          # Entry point
│   └── index.css         # Global styles
├── package.json
├── vite.config.ts
├── tailwind.config.js
└── tsconfig.json
```

## 🔐 Security Features

- JWT token attached to all API requests
- Automatic logout on authentication failure
- Role-based route protection
- No sensitive data in frontend code
- HTTPS ready for production

## 🎬 Demo Flow

1. **Login** → Enter credentials
2. **Dashboard** → View stats and status breakdown
3. **Candidates** → Browse all candidates in table
4. **Select Candidate** → Click "View" to open profile
5. **Upload Document** → Select type, choose file, submit
6. **Execute Transition** → Select new status, execute (see guard logic in action)

## ⚠️ Guard Logic Demo

Try these scenarios to see backend validation:

1. **Medical Rule**: Try transitioning to `MEDICAL_CLEARED` without medical status = PASSED
2. **Document Rule**: Try transitioning without valid passport expiry
3. **Concurrent Offers**: Try issuing multiple offers to same candidate

Error messages will display prominently in the UI.

## 🚫 What's NOT Included (By Design)

- Financial screens
- Employer portal
- Applicant self-service
- Notifications
- Charts/analytics
- PDF generation
- Complex animations

## 🔧 API Integration

All API calls go to: `http://localhost:8080`

Vite proxy configured:
```typescript
'/api' → 'http://localhost:8080'
```

### Key Endpoints Used

- `POST /api/auth/login` - Authentication
- `GET /api/candidates` - List candidates
- `GET /api/candidates/:id` - Get candidate details
- `POST /api/candidates/:id/transition` - Workflow transition
- `GET /api/candidates/:id/documents` - List documents
- `POST /api/candidates/:id/documents` - Upload document
- `GET /api/documents/:id/download` - Download document

## 📊 Status Badges

Color-coded status badges for:
- Workflow states (APPLIED, INTERVIEWED, etc.)
- Medical status (PENDING, PASSED, FAILED)
- Expiry flags (EXPIRING_SOON, EXPIRED, VALID)
- Document types (PASSPORT, MEDICAL, etc.)

## 🎯 Definition of Done

Phase 1B is complete when:

✅ User can log in  
✅ View dashboard with stats  
✅ Browse candidates list  
✅ Open candidate profile  
✅ Upload documents  
✅ Perform workflow transitions  
✅ See guard logic errors visually  
✅ Demo system end-to-end without Postman  

## 🐛 Troubleshooting

### Backend Connection Failed
- Ensure Spring Boot backend is running on port 8080
- Check CORS configuration in SecurityConfig.java

### Authentication Issues
- Clear localStorage: `localStorage.clear()`
- Check JWT token validity
- Verify user exists in database

### Document Upload Fails
- Check file size limits
- Verify Google Drive credentials
- Check multipart/form-data support

## 📝 Future Enhancements (Phase 2+)

- Offer letter viewing and signing
- Financial dashboard
- Employer portal
- Real-time notifications
- Advanced search and filters
- Bulk operations
- Export to Excel/PDF

## 🤝 Contributing

This is a demo UI for Phase 1B. For production features, refer to Phase 2 planning.

---

**Version**: 1.5.0  
**Last Updated**: January 2026  
**Status**: Phase 1B Complete ✅
