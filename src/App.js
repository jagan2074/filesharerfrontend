// src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Container from 'react-bootstrap/Container';

import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage'; // Keep placeholder for now
import AppNavbar from './components/AppNavbar';
import './App.scss';

function InitialRedirect() {
    const { isAuthenticated, loading } = useAuth();
    // Show a more centered loading indicator
    if (loading) return (
        <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: 'calc(100vh - 56px - 1rem)' }}> {/* 56px for navbar, 1rem for pt-3 */}
            <div>Loading application...</div>
        </Container>
    );
    return isAuthenticated ? <Navigate to="/dashboard" replace /> : <Navigate to="/login" replace />;
}

function App() {
    return (
        <AuthProvider>
            <Router>
                <AppNavbar />
                {/* Removed the outer Container from here, pages will handle their own main containers */}
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    
                    <Route element={<ProtectedRoute />}>
                        <Route path="/dashboard" element={<DashboardPage />} /> 
                    </Route>
                    
                    <Route path="/" element={<InitialRedirect />} />
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </Router>
        </AuthProvider>
    );
}

export default App;