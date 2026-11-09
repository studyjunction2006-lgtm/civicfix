import axios from 'axios';

const rawBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
const API_BASE_URL = rawBaseUrl.replace(/\/+$/, '');

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('civicfix_token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to unwrap data and handle 401s
apiClient.interceptors.response.use(
  (response) => {
    // If backend returns ApiResponse { success, message, data }, return data or response
    if (response.data && response.data.hasOwnProperty('data')) {
      return response.data.data;
    }
    return response.data;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      // Clear token if expired or invalid
      const currentPath = window.location.pathname;
      if (!currentPath.includes('/login') && !currentPath.includes('/register')) {
        localStorage.removeItem('civicfix_token');
        localStorage.removeItem('civicfix_user');
      }
    }
    const message = error.response?.data?.message || error.message || 'An unexpected error occurred';
    return Promise.reject(new Error(message));
  }
);

// ----------------- Auth API -----------------
export const authApi = {
  login: (email, password) => apiClient.post('/auth/login', { email, password }),
  register: (data) => apiClient.post('/auth/register', data),
  getCurrentUser: () => apiClient.get('/auth/me'),
  logout: () => apiClient.post('/auth/logout'),
};

// ----------------- Issues API -----------------
export const issueApi = {
  createIssue: (data) => apiClient.post('/issues', data),
  searchIssues: (params) => apiClient.get('/issues', { params }),
  getMyReports: (params) => apiClient.get('/issues/my-reports', { params }),
  getIssueById: (id) => apiClient.get(`/issues/${id}`),
  getIssueByTrackingNumber: (trackingNumber) => apiClient.get(`/issues/tracking/${trackingNumber}`),
  transitionStatus: (id, data) => apiClient.patch(`/issues/${id}/status`, data),
  overridePriority: (id, data) => apiClient.patch(`/issues/${id}/priority`, data),
  getPublicMapIssues: () => apiClient.get('/issues/public/map'),
};

// ----------------- Confirmations API -----------------
export const confirmationApi = {
  confirmIssue: (issueId, note) => apiClient.post(`/confirmations/issues/${issueId}`, { note }),
  getConfirmations: (issueId) => apiClient.get(`/confirmations/issues/${issueId}`),
  checkUserConfirmed: (issueId) => apiClient.get(`/confirmations/issues/${issueId}/check`),
};

// ----------------- Worker Assignments API -----------------
export const assignmentApi = {
  assignWorker: (issueId, data) => apiClient.post(`/assignments/issues/${issueId}`, data),
  respondToAssignment: (assignmentId, data) => apiClient.post(`/assignments/${assignmentId}/respond`, data),
  getMyAssignments: (params) => apiClient.get('/assignments/my', { params }),
  getIssueAssignments: (issueId) => apiClient.get(`/assignments/issues/${issueId}`),
};

// ----------------- Categories API -----------------
export const categoryApi = {
  getAllCategories: (departmentId) => apiClient.get('/categories', { params: departmentId ? { departmentId } : {} }),
  getCategoryById: (id) => apiClient.get(`/categories/${id}`),
  createCategory: (data) => apiClient.post('/categories', data),
  updateCategory: (id, data) => apiClient.put(`/categories/${id}`, data),
};

// ----------------- Departments API -----------------
export const departmentApi = {
  getAllDepartments: () => apiClient.get('/departments'),
  getDepartmentById: (id) => apiClient.get(`/departments/${id}`),
  createDepartment: (data) => apiClient.post('/departments', data),
  updateDepartment: (id, data) => apiClient.put(`/departments/${id}`, data),
};

// ----------------- Ratings API -----------------
export const ratingApi = {
  submitRating: (issueId, data) => apiClient.post(`/ratings/issues/${issueId}`, data),
  getRating: (issueId) => apiClient.get(`/ratings/issues/${issueId}`),
};

// ----------------- Notifications API -----------------
export const notificationApi = {
  getMyNotifications: (params) => apiClient.get('/notifications', { params }),
  getUnreadCount: () => apiClient.get('/notifications/unread-count'),
  markAsRead: (id) => apiClient.patch(`/notifications/${id}/read`),
  markAllAsRead: () => apiClient.patch('/notifications/read-all'),
};

// ----------------- Analytics API -----------------
export const analyticsApi = {
  getPublicStats: () => apiClient.get('/analytics/public'),
  getAnalyticsSummary: () => apiClient.get('/analytics/summary'),
};

// ----------------- Storage / File Upload API -----------------
export const storageApi = {
  uploadImage: async (file, folder = 'issues') => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('folder', folder);
    const response = await axios.post(`${API_BASE_URL}/storage/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        Authorization: `Bearer ${localStorage.getItem('civicfix_token')}`,
      },
    });
    return response.data?.data?.url || response.data?.url;
  },
};

// ----------------- User & Admin API -----------------
export const userApi = {
  getWorkers: (departmentId) => apiClient.get('/users/workers', { params: departmentId ? { departmentId } : {} }),
  updateProfile: (data) => apiClient.put('/users/profile', data),
};

export const adminApi = {
  getUsers: (params) => apiClient.get('/admin/users', { params }),
  createUser: (data) => apiClient.post('/admin/users', data),
  updateUserRole: (id, data) => apiClient.patch(`/admin/users/${id}/role`, data),
  toggleUserStatus: (id, active) => apiClient.patch(`/admin/users/${id}/status`, { active }),
  getAuditLogs: (params) => apiClient.get('/admin/audit-logs', { params }),
};
