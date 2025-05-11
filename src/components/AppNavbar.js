// src/components/AppNavbar.js
import React from 'react';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function AppNavbar() {
    const { isAuthenticated, currentUser, logout } = useAuth(); // currentUser now holds username
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <Navbar bg="primary" variant="dark" expand="lg" className="mb-4 shadow-sm" sticky="top">
            <Container fluid>
                <Navbar.Brand as={Link} to="/" style={{ fontWeight: 'bold' }}>FileShare Pro</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="ms-auto align-items-center">
                        {isAuthenticated && currentUser ? (
                            <>
                                <Navbar.Text className="me-3 text-light">
                                    Welcome, <span className="fw-bold">{currentUser.username}</span>
                                </Navbar.Text>
                                <Button variant="outline-light" onClick={handleLogout} size="sm">Logout</Button>
                            </>
                        ) : (
                            <>
                                <Nav.Link as={Link} to="/login" className="text-light">Login</Nav.Link>
                                <Nav.Link as={Link} to="/register" className="text-light">Register</Nav.Link>
                            </>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}

export default AppNavbar;