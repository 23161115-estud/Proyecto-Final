import api from './axios';

const clean = (p = {}) => Object.fromEntries(Object.entries(p).filter(([, v]) => v !== '' && v !== null && v !== undefined));
const get = (url, params) => api.get(url, { params: clean(params) }).then((r) => r.data);
const post = (url, body) => api.post(url, body).then((r) => r.data);
const put = (url, body) => api.put(url, body).then((r) => r.data);
const patch = (url) => api.patch(url).then((r) => r.data);
const del = (url) => api.delete(url).then((r) => r.data);

const crud = (base) => ({
  list: (params) => get(base, params),
  create: (body) => post(base, body),
  update: (id, body) => put(`${base}/${id}`, body),
  remove: (id) => del(`${base}/${id}`),
});

export const authApi = {
  login: (b) => post('/api/auth/login', b),
  register: (b) => post('/api/auth/register', b),
  logout: () => post('/api/auth/logout'),
  forgot: (b) => post('/api/auth/forgot-password', b),
  reset: (b) => post('/api/auth/reset-password', b),
  verify: (token) => get('/api/auth/verify-email', { token }),
  socialConfig: () => get('/api/auth/social-config'),
  google: (idToken) => post('/api/auth/google', { idToken }),
  facebook: (accessToken) => post('/api/auth/facebook', { accessToken }),
};

export const profileApi = {
  get: () => get('/api/profile'),
  update: (b) => put('/api/profile', b),
  password: (b) => put('/api/profile/password', b),
  avatar: (file) => {
    const fd = new FormData();
    fd.append('file', file);
    return api.post('/api/profile/avatar', fd).then((r) => r.data);
  },
  remove: () => del('/api/profile'),
};

export const simApi = {
  list: (params) => get('/api/simulations', params),
  get: (id) => get(`/api/simulations/${id}`),
  create: (b) => post('/api/simulations', b),
  remove: (id) => del(`/api/simulations/${id}`),
  pdf: (id) => api.get(`/api/simulations/${id}/pdf`, { responseType: 'blob' }).then((r) => r.data),
  sendEmail: (id) => post(`/api/simulations/${id}/send-email`),
  sendSms: (id, phone) => post(`/api/simulations/${id}/send-sms`, phone ? { phone } : undefined),
  sendWhatsapp: (id, phone) => post(`/api/simulations/${id}/send-whatsapp`, phone ? { phone } : undefined),
};

export const neighborhoodApi = { ...crud('/api/neighborhoods'), options: () => get('/api/neighborhoods/options') };
export const amenityApi = { ...crud('/api/amenities'), options: () => get('/api/amenities/options') };
export const aiDataApi = crud('/api/ai-data');

export const adminApi = {
  users: {
    ...crud('/api/admin/users'),
    activate: (id) => patch(`/api/admin/users/${id}/activate`),
    deactivate: (id) => patch(`/api/admin/users/${id}/deactivate`),
  },
  dashboard: () => get('/api/admin/dashboard'),
  emailLogs: (params) => get('/api/admin/email-logs', params),
};
