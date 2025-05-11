// src/services/apiService.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/filesharer-backend/api'; // CONFIRM THIS IS CORRECT

const getAuthToken = () => {
    const userString = localStorage.getItem('user'); // We'll store user object with token
    if (userString) {
        const user = JSON.parse(userString);
        return user?.token;
    }
    return null;
};

const apiClient = axios.create({
    baseURL: API_BASE_URL,
});

apiClient.interceptors.request.use(
    (config) => {
        const token = getAuthToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export const registerUser = (userData) => {
    return apiClient.post('/auth/register', userData);
};

// Modified to align with AuthContext storing user object
export const loginUserApi = async (credentials) => { // Renamed to avoid conflict if login is used elsewhere
    const response = await apiClient.post('/auth/login', credentials);
    if (response.data.token) {
        // Store the user object which now includes the token and username from backend
        localStorage.setItem('user', JSON.stringify(response.data)); 
    }
    return response.data; // Return the data part (which includes token and username)
};


export const uploadFile = (formData) => {
    return apiClient.post('/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data', },
    });
};

export const getUserFiles = () => {
    return apiClient.get('/files');
};

export const downloadFile = (fileId, originalFilename) => {
    return apiClient.get(`/files/${fileId}/download`, {
        responseType: 'blob',
    }).then((response) => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', originalFilename || `download-${fileId}`);
        document.body.appendChild(link);
        link.click();
        link.parentNode.removeChild(link);
        window.URL.revokeObjectURL(url);
    });
};

export const deleteFile = (fileId) => {
    return apiClient.delete(`/files/${fileId}`);
};

export const shareFile = (fileId) => {
    return apiClient.post(`/files/${fileId}/share`, {});
};

export default apiClient;