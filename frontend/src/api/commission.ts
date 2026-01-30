import axios from './axios';

export interface CommissionAgreement {
  id: string;
  candidateId: number;
  candidateName: string;
  assignmentId: number;
  totalCommissionAmount: number;
  requiredDownpaymentAmount: number;
  totalPaid: number;
  outstandingBalance: number;
  currency: string;
  agreementDate: string;
  signed: boolean;
  signedAt?: string;
  signedAgreementDocumentUrl?: string;
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
  notes?: string;
  createdAt: string;
  createdBy: string;
}

export interface CreateCommissionAgreementRequest {
  candidateId: number;
  assignmentId: number;
  totalCommissionAmount: number;
  requiredDownpaymentAmount: number;
  currency?: string;
  notes?: string;
}

export interface Payment {
  id: number;
  candidateId: number;
  assignmentId: number;
  agreementId: string;
  amount: number;
  type: 'DEBIT' | 'CREDIT';
  transactionType: 'AGENCY_COMMISSION_DOWNPAYMENT' | 'AGENCY_COMMISSION_INSTALLMENT' | 'AGENCY_COMMISSION_BALANCE' | 'REVERSAL';
  transactionRef: string;
  paymentDate: string;
  paymentMethod?: string;
  mpesaRef?: string;
  description?: string;
  isReversal: boolean;
  linkedTransactionId?: number;
  reversalReason?: string;
}

export interface CommissionPaymentRequest {
  agreementId: string;
  amount: number;
  paymentMethod?: string;
  mpesaRef?: string;
  description?: string;
}

export interface CommissionStatement {
  candidateId: number;
  candidateName: string;
  totalCommissionAmount: number;
  requiredDownpaymentAmount: number;
  totalPaid: number;
  outstandingBalance: number;
  downpaymentComplete: boolean;
  fullPaymentComplete: boolean;
  paymentHistory: Payment[];
}

const commissionApi = {
  // Agreement endpoints
  getAllAgreements: () => 
    axios.get('/api/agreements'),

  createAgreement: (data: CreateCommissionAgreementRequest) => 
    axios.post('/api/agreements', data),

  getAgreement: (id: string) => 
    axios.get(`/api/agreements/${id}`),

  getCandidateAgreements: (candidateId: number) => 
    axios.get(`/api/agreements/candidate/${candidateId}`),

  getAssignmentAgreement: (assignmentId: number) => 
    axios.get(`/api/agreements/assignment/${assignmentId}`),

  signAgreement: (id: string, documentUrl?: string) => 
    axios.put(`/api/agreements/${id}/sign`, null, { params: { documentUrl } }),

  cancelAgreement: (id: string, reason: string) => 
    axios.delete(`/api/agreements/${id}`, { params: { reason } }),

  // Payment endpoints
  recordDownpayment: (data: CommissionPaymentRequest) => 
    axios.post('/api/payments/downpayment', data),

  recordInstallment: (data: CommissionPaymentRequest) => 
    axios.post('/api/payments/installment', data),

  reversePayment: (paymentId: number, reason: string) => 
    axios.post(`/api/payments/${paymentId}/reverse`, null, { params: { reason } }),

  getCandidateStatement: (candidateId: number, agreementId: string) => 
    axios.get(`/api/payments/candidate/${candidateId}/statement`, { params: { agreementId } }),

  checkDownpaymentStatus: (assignmentId: number) => 
    axios.get(`/api/payments/assignment/${assignmentId}/downpayment-status`),

  checkFullPaymentStatus: (assignmentId: number) => 
    axios.get(`/api/payments/assignment/${assignmentId}/fullpayment-status`),
};

export default commissionApi;
