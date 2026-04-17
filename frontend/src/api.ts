import axios from 'axios';

const API = axios.create({ baseURL: 'http://localhost:8080' });

API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const login = (email: string, password: string) =>
  API.post('/api/auth/login', { email, password });

export const register = (fullName: string, email: string, password: string, phone: string) =>
  API.post('/api/auth/register', { fullName, email, password, phone });

export const getJobs = (page = 0) =>
  API.get(`/api/jobs?page=${page}&size=12`);

export const searchJobs = (keyword: string, page = 0) =>
  API.get(`/api/jobs/search?keyword=${keyword}&page=${page}&size=12`);

export const filterByType = (jobType: string, page = 0) =>
  API.get(`/api/jobs/filter/type?jobType=${jobType}&page=${page}&size=12`);

export default API;
