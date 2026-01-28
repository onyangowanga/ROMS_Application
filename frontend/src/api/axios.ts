import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios';

const API_BASE_URL = 'http://localhost:8080';

// Create axios instance
const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - attach JWT token
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handle 401/403 globally
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    // Only logout if it's an actual authentication/authorization error
    // Don't logout for business logic errors (like "not found")
    if (error.response?.status === 401) {
      // 401 = token expired or invalid - definitely logout
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    // Don't auto-logout on 403 - let the component handle it
    // 403 could be a permission issue but user is still authenticated
    return Promise.reject(error);
  }
);

export default api;
