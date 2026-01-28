import api from './axios';
import { ApiResponse, Candidate, CandidateDocument, CandidateStatus } from '../types';

export const candidateApi = {
  getAll: async (): Promise<Candidate[]> => {
    const response = await api.get<ApiResponse<Candidate[]>>('/api/candidates');
    return response.data.data || [];
  },

  getMyApplications: async (email: string): Promise<Candidate[]> => {
    const response = await api.get<ApiResponse<Candidate[]>>('/api/candidates/me', {
      params: { email }
    });
    return response.data.data || [];
  },

  getById: async (id: number): Promise<Candidate> => {
    const response = await api.get<ApiResponse<Candidate>>(`/api/candidates/${id}`);
    return response.data.data;
  },

  create: async (candidate: Partial<Candidate>): Promise<Candidate> => {
    const response = await api.post<ApiResponse<Candidate>>('/api/candidates', candidate);
    return response.data.data;
  },

  update: async (id: number, candidate: Partial<Candidate>): Promise<Candidate> => {
    const response = await api.put<ApiResponse<Candidate>>(`/api/candidates/${id}`, candidate);
    return response.data.data;
  },

  transition: async (id: number, toStatus: CandidateStatus): Promise<Candidate> => {
    const response = await api.post<ApiResponse<Candidate>>(`/api/candidates/${id}/transition`, {
      status: toStatus
    });
    return response.data.data;
  },

  canTransition: async (id: number, toStatus: CandidateStatus): Promise<boolean> => {
    const response = await api.get<ApiResponse<boolean>>(`/api/candidates/${id}/can-transition/${toStatus}`);
    return response.data.data;
  },

  // Document operations
  getDocuments: async (candidateId: number): Promise<CandidateDocument[]> => {
    const response = await api.get<ApiResponse<CandidateDocument[]>>(`/api/candidates/${candidateId}/documents`);
    return response.data.data;
  },

  uploadDocument: async (candidateId: number, file: File, docType: string, expiryDate?: string, documentNumber?: string): Promise<CandidateDocument> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('docType', docType);
    if (expiryDate) formData.append('expiryDate', expiryDate);
    if (documentNumber) formData.append('documentNumber', documentNumber);

    const response = await api.post<ApiResponse<CandidateDocument>>(
      `/api/candidates/${candidateId}/documents`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data.data;
  },

  downloadDocument: async (documentId: number): Promise<Blob> => {
    const response = await api.get(`/api/documents/${documentId}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },

  deleteDocument: async (documentId: number): Promise<void> => {
    await api.delete(`/api/documents/${documentId}`);
  },

  // Job application endpoint
  applyForJob: async (applicationData: {
    username: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    dateOfBirth: string;
    gender: string;
    passportNo: string;
    passportExpiry: string;
    phoneNumber: string;
    country: string;
    currentAddress: string;
    expectedPosition?: string;
    jobOrderId: number;
  }): Promise<Candidate> => {
    const response = await api.post<ApiResponse<Candidate>>('/api/candidates/apply', applicationData);
    return response.data.data || response.data;
  },
};
