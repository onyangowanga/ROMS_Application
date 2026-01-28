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
    return response.data;
  },

  getEmployerById: async (id: number): Promise<Employer> => {
    const response = await api.get(`/api/employers/${id}`);
    return response.data;
  }
};
