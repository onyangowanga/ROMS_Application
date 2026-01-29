import api from './axios';
import { Employer } from '../types';

export const employersApi = {
  registerEmployer: async (data: {
    username: string;
    email: string;
    password: string;
    companyName: string;
    contactPerson: string;
    contactEmail: string;
    contactPhone: string;
    address: string;
    country: string;
    industry: string;
  }): Promise<void> => {
    const response = await api.post('/api/employers', data);
    return response.data;
  },

  getAllEmployers: async (): Promise<Employer[]> => {
    const response = await api.get('/api/employers');
    console.log('Raw response:', response);
    console.log('response.data:', response.data);
    
    // Extract data from ApiResponse wrapper
    const apiResponse = response.data;
    console.log('apiResponse.data:', apiResponse.data);
    
    // Return the actual data array
    return apiResponse.data || [];
  },

  getEmployerById: async (id: number): Promise<Employer> => {
    const response = await api.get(`/api/employers/${id}`);
    return response.data.data;
  }
};
