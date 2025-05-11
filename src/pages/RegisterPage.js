// src/pages/RegisterPage.js
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { registerUser } from '../services/apiService';
import { Container, Row, Col, Card, Form, Button, Alert, Spinner } from 'react-bootstrap';

function RegisterPage() {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (!username.trim() || !email.trim() || !password) {
            setError("All fields are required.");
            return;
        }
        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }
        // Basic password strength (example: min 6 chars)
        if (password.length < 6) {
            setError("Password must be at least 6 characters long.");
            return;
        }

        setLoading(true);
        try {
            await registerUser({ username, email, password });
            setSuccess('Registration successful! Redirecting to login...');
            setUsername('');
            setEmail('');
            setPassword('');
            setConfirmPassword('');
            setTimeout(() => navigate('/login'), 2500);
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.response?.data || err.message || 'Registration failed. Please try again.';
            setError(errorMessage);
            console.error("Registration error:", err.response || err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container style={{ paddingTop: '2rem', paddingBottom: '2rem' }}>
            <Row className="justify-content-md-center">
                <Col md={8} lg={6} xl={5}>
                    <Card className="shadow-lg border-0">
                        <Card.Header as="h4" className="text-center bg-primary text-white">Create Account</Card.Header>
                        <Card.Body className="p-4">
                            {error && <Alert variant="danger" onClose={() => setError('')} dismissible>{error}</Alert>}
                            {success && <Alert variant="success">{success}</Alert>}
                            <Form onSubmit={handleSubmit}>
                                <Form.Group className="mb-3" controlId="formRegisterUsername">
                                    <Form.Label>Username</Form.Label>
                                    <Form.Control
                                        type="text"
                                        placeholder="Choose a username"
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                        required
                                        disabled={loading}
                                        autoComplete="username"
                                    />
                                </Form.Group>

                                <Form.Group className="mb-3" controlId="formRegisterEmail">
                                    <Form.Label>Email address</Form.Label>
                                    <Form.Control
                                        type="email"
                                        placeholder="Enter your email"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        required
                                        disabled={loading}
                                        autoComplete="email"
                                    />
                                </Form.Group>

                                <Form.Group className="mb-3" controlId="formRegisterPassword">
                                    <Form.Label>Password</Form.Label>
                                    <Form.Control
                                        type="password"
                                        placeholder="Create a password (min. 6 characters)"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        required
                                        disabled={loading}
                                        autoComplete="new-password"
                                    />
                                </Form.Group>

                                <Form.Group className="mb-4" controlId="formRegisterConfirmPassword">
                                    <Form.Label>Confirm Password</Form.Label>
                                    <Form.Control
                                        type="password"
                                        placeholder="Confirm your password"
                                        value={confirmPassword}
                                        onChange={(e) => setConfirmPassword(e.target.value)}
                                        required
                                        disabled={loading}
                                        autoComplete="new-password"
                                    />
                                </Form.Group>

                                <div className="d-grid">
                                    <Button variant="primary" type="submit" disabled={loading} size="lg">
                                        {loading ? (
                                            <>
                                                <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true"/>
                                                <span className="ms-2">Registering...</span>
                                            </>
                                        ) : 'Register'}
                                    </Button>
                                </div>
                            </Form>
                            <div className="mt-4 text-center">
                                Already have an account? <Link to="/login">Login here</Link>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
}

export default RegisterPage;