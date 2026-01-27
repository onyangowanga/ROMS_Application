// User and Authentication Types
export type UserRole = 'SUPER_ADMIN' | 'FINANCE_MANAGER' | 'OPERATIONS_STAFF' | 'EMPLOYER' | 'APPLICANT';

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: UserRole;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}

// Candidate Types
export type CandidateStatus = 
  | 'APPLIED'
  | 'SHORTLISTED'
  | 'INTERVIEW_SCHEDULED'
  | 'SELECTED'
  | 'MEDICAL_IN_PROGRESS'
  | 'MEDICAL_CLEARED'
  | 'DEPLOYED'
  | 'PLACED'
  | 'REJECTED'
  | 'WITHDRAWN';

export type MedicalStatus = 'PENDING' | 'PASSED' | 'FAILED' | 'WAIVED';

export type DocumentType = 'PASSPORT' | 'MEDICAL' | 'OFFER' | 'CONTRACT' | 'VISA' | 'OTHER';

export interface Candidate {
  id: number;
  internalRefNo: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: string;
  passportNo: string;
  passportExpiry: string;
  email: string;
  phoneNumber: string;
  country: string;
  currentStatus: CandidateStatus;
  medicalStatus?: MedicalStatus;
  currentAddress?: string;
  expectedPosition?: string;
  expiryFlag?: 'EXPIRING_SOON' | 'EXPIRED' | 'VALID';
  medicalExpiry?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CandidateDocument {
  id: number;
  fileName: string;
  docType: DocumentType;
  fileSize: number;
  contentType: string;
  expiryDate?: string;
  documentNumber?: string;
  description?: string;
  isVerified: boolean;
  uploadedAt: string;
}

// Offer Letter Types
export type OfferLetterStatus = 'DRAFT' | 'ISSUED' | 'SIGNED' | 'WITHDRAWN';

export interface OfferLetter {
  id: number;
  status: OfferLetterStatus;
  offeredSalary: number;
  jobTitle: string;
  issuedAt?: string;
  signedAt?: string;
  notes?: string;
}

// API Response Wrapper
export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data: T;
}

// Dashboard Stats
export interface DashboardStats {
  totalCandidates: number;
  statusBreakdown: Record<CandidateStatus, number>;
  expiringDocuments: number;
  pendingOffers: number;
}

// Workflow Transition
export interface WorkflowTransition {
  fromStatus: CandidateStatus;
  toStatus: CandidateStatus;
  reason?: string;
}
