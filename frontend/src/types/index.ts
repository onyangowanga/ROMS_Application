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
  | 'APPLICATION_SUBMITTED'
  | 'UNDER_REVIEW'
  | 'DOCUMENTS_INSUFFICIENT'
  | 'DOCUMENTS_APPROVED'
  | 'INTERVIEW_SCHEDULED'
  | 'INTERVIEW_PASSED'
  | 'MEDICAL_PENDING'
  | 'MEDICAL_PASSED'
  | 'VISA_PROCESSING'
  | 'OFFER_ISSUED'
  | 'OFFER_ACCEPTED'
  | 'DEPLOYMENT_PENDING'
  | 'PLACED'
  | 'REJECTED';

export type MedicalStatus = 'PENDING' | 'PASSED' | 'FAILED' | 'WAIVED';

export type DocumentType =
  | 'PASSPORT'
  | 'CV'
  | 'EDUCATIONAL_CERTIFICATE'
  | 'POLICE_CLEARANCE'
  | 'MEDICAL_REPORT'
  | 'PHOTO'
  | 'NATIONAL_ID'
  | 'BIRTH_CERTIFICATE'
  | 'OFFER_LETTER'
  | 'CONTRACT'
  | 'VISA'
  | 'OTHER';

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
  interviewDate?: string;
  interviewTime?: string;
  interviewLocation?: string;
  interviewNotes?: string;
  jobOrder?: any;
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

// Employer Types
export interface Employer {
  id: number;
  companyName: string;
  companyRegistrationNo?: string;
  contactPerson: string;
  contactEmail: string;
  contactPhone: string;
  address: string;
  country: string;
  industry: string;
  isActive: boolean;
  createdAt: string;
  createdBy?: string;
  lastModifiedAt: string;
  lastModifiedBy?: string;
  deletedAt?: string;
}

// Job Order Types
export type JobOrderStatus = 'PENDING_APPROVAL' | 'OPEN' | 'CLOSED';

export interface JobOrder {
  id: number;
  jobOrderRef: string;
  jobTitle: string;
  headcountRequired: number;
  headcountFilled?: number;
  salaryMin?: number;
  salaryMax?: number;
  currency: string;
  location: string;
  country: string;
  contractDurationMonths?: number;
  requiredSkills?: string;
  description: string;
  status: JobOrderStatus;
  employerId?: number;
  createdAt?: string;
  updatedAt?: string;
}

// Assignment Types
export type AssignmentStatus = 'ASSIGNED' | 'OFFERED' | 'PLACED' | 'CANCELLED';

export interface Assignment {
  id: number;
  candidateId: number;
  candidateName: string;
  candidateRefNo: string;
  jobOrderId: number;
  jobOrderRef: string;
  jobTitle: string;
  status: AssignmentStatus;
  isActive: boolean;
  assignedAt: string;
  offerIssuedAt?: string;
  placementConfirmedAt?: string;
  cancelledAt?: string;
  notes?: string;
}

export interface CreateAssignmentRequest {
  candidateId: number;
  jobOrderId: number;
  notes?: string;
}
