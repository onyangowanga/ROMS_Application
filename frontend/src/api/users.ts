import axios from './axios';
import { UserRole } from '../types';

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: UserRole;
  isActive: boolean;
  isLocked: boolean;
  isEmailVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
  role: UserRole;
}

export const usersApi = {
  // Get all users
  getAllUsers: async (): Promise<User[]> => {
    const response = await axios.get('/api/users');
    return response.data.data;
  },

  // Create new user
  createUser: async (data: CreateUserRequest): Promise<User> => {
    const response = await axios.post('/api/users', data);
    return response.data.data;
  },

  // Get user by ID
  getUserById: async (id: number): Promise<User> => {
    const response = await axios.get(`/api/users/${id}`);
    return response.data.data;
  },

  // Update user
  updateUser: async (id: number, data: CreateUserRequest): Promise<User> => {
    const response = await axios.put(`/api/users/${id}`, data);
    return response.data.data;
  },

  // Delete user
  deleteUser: async (id: number): Promise<void> => {
    await axios.delete(`/api/users/${id}`);
  },

  // Toggle user active status
  toggleUserActive: async (id: number): Promise<User> => {
    const response = await axios.patch(`/api/users/${id}/toggle-active`);
    return response.data.data;
  },
};
