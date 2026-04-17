import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export const authApi = {
  login:    data => api.post('/auth/login', data),
  register: data => api.post('/auth/register', data),
};

export const energyApi = {
  submit:      data    => api.post('/energy', data),
  getReadings: ()      => api.get('/energy'),
  getStats:    ()      => api.get('/energy/stats'),
  getAlerts:   ()      => api.get('/energy/alerts'),
  deleteReading: id    => api.delete(`/energy/${id}`),
  acknowledge:   id    => api.put(`/energy/alerts/${id}/acknowledge`),
};

export default api;
