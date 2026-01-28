import api from './axios';
import { JobOrder } from '../types';

export const jobsApi = {
  createJob: async (data: {
    jobOrderRef: string;
    jobTitle: string;
    headcountRequired: number;
    salaryMin: number;
    salaryMax: number;
    currency: string;
    location: string;
    country: string;
    contractDurationMonths: number;
    requiredSkills: string;
    description: string;
  }): Promise<JobOrder> => {
    const response = await api.post('/api/job-orders', data);
    return response.data;
  },

  getAllJobs: async (): Promise<JobOrder[]> => {
    const response = await api.get('/api/job-orders');
    // Backend returns ApiResponse format: { success: true, message: "", data: [...] }
    return response.data.data || response.data;
  },

  getJobById: async (id: number): Promise<JobOrder> => {
    const response = await api.get(`/api/job-orders/${id}`);
    return response.data;
  },

  updateJobStatus: async (id: number, status: string): Promise<JobOrder> => {
    const response = await api.patch(`/api/job-orders/${id}/status`, { status });
    return response.data;
  }
};
