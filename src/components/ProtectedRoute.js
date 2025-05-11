// src/components/ProtectedRoute.js
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Container, Spinner } from 'react-bootstrap'; // For loading UI

const ProtectedRoute = () => {
    const { isAuthenticated, loading } = useAuth();

    if (loading) {
        return (
            <Container className="d-flex justify-content-center align-items-center" style={{ height: '80vh' }}>
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">Loading authentication...</span>
                </Spinner>
            </Container>
        );
    }

    return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
};

export default ProtectedRoute;