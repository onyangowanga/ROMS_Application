import api from './axios';
import { Assignment, CreateAssignmentRequest } from '../types';

export const assignmentsApi = {
  // Create new assignment
  createAssignment: async (data: CreateAssignmentRequest): Promise<Assignment> => {
    const response = await api.post('/api/assignments', data);
    return response.data.data || response.data;
  },

  // Get all assignments
  getAllAssignments: async (): Promise<Assignment[]> => {
    const response = await api.get('/api/assignments');
    return response.data.data || response.data;
  },

  // Get assignments for a job order
  getAssignmentsByJobOrder: async (jobOrderId: number): Promise<Assignment[]> => {
    const response = await api.get(`/api/assignments/job-order/${jobOrderId}`);
    return response.data.data || response.data;
  },

  // Get assignments for a candidate
  getAssignmentsByCandidate: async (candidateId: number): Promise<Assignment[]> => {
    const response = await api.get(`/api/assignments/candidate/${candidateId}`);
    return response.data.data || response.data;
  },

  // Get active assignment for a candidate
  getActiveAssignment: async (candidateId: number): Promise<Assignment | null> => {
    const response = await api.get(`/api/assignments/candidate/${candidateId}/active`);
    return response.data.data || null;
  },

  // Cancel an assignment
  cancelAssignment: async (assignmentId: number): Promise<void> => {
    await api.delete(`/api/assignments/${assignmentId}`);
  },

  // Issue offer for assignment
  issueOffer: async (assignmentId: number): Promise<Assignment> => {
    const response = await api.put(`/api/assignments/${assignmentId}/issue-offer`);
    return response.data.data || response.data;
  },

  // Confirm placement for assignment
  confirmPlacement: async (assignmentId: number): Promise<Assignment> => {
    const response = await api.put(`/api/assignments/${assignmentId}/confirm-placement`);
    return response.data.data || response.data;
  }
};
