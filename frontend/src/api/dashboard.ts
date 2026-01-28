import api from './axios';
import { ApiResponse } from '../types';

export interface DashboardStats {
  totalCandidates: number;
  totalEmployers: number;
  totalJobOrders: number;
  openPositions: number;
}

export interface EmployerDashboardStats {
  totalJobsPosted: number;
  totalHeadcountRequired: number;
  pendingPositionsHeadcount: number;
  totalApplicationsReceived: number;
  totalPlaced: number;
  filledPositions: number;
}

export const dashboardApi = {
  getStats: async (): Promise<DashboardStats> => {
    const response = await api.get<ApiResponse<DashboardStats>>('/api/dashboard/stats');
    return response.data.data;
  },
  
  getEmployerStats: async (email: string): Promise<EmployerDashboardStats> => {
    const response = await api.get<ApiResponse<EmployerDashboardStats>>('/api/dashboard/employer/stats', {
      params: { email }
    });
    return response.data.data;
  },
};
