// src/pages/LoginPage.js
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Container, Row, Col, Card, Form, Button, Alert, Spinner } from 'react-bootstrap';

function LoginPage() {
    const [usernameOrEmail, setUsernameOrEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            await login({ usernameOrEmail, password });
            navigate('/dashboard');
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.response?.data || err.message || 'Login failed. Please check credentials.';
            setError(errorMessage);
            console.error("Login error details:", err.response || err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container style={{ paddingTop: '2rem', paddingBottom: '2rem' }}>
            <Row className="justify-content-md-center">
                <Col md={8} lg={6} xl={5}>
                    <Card className="shadow-lg border-0">
                         <Card.Header as="h4" className="text-center bg-primary text-white">User Login</Card.Header>
                        <Card.Body className="p-4">
                            {error && <Alert variant="danger" onClose={() => setError('')} dismissible>{error}</Alert>}
                            <Form onSubmit={handleSubmit}>
                                <Form.Group className="mb-3" controlId="formLoginUsernameOrEmail">
                                    <Form.Label>Username or Email</Form.Label>
                                    <Form.Control
                                        type="text"
                                        placeholder="Enter username or email"
                                        value={usernameOrEmail}
                                        onChange={(e) => setUsernameOrEmail(e.target.value)}
                                        required
                                        disabled={loading}
                                        autoComplete="username"
                                    />
                                </Form.Group>

                                <Form.Group className="mb-4" controlId="formLoginPassword">
                                    <Form.Label>Password</Form.Label>
                                    <Form.Control
                                        type="password"
                                        placeholder="Password"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        required
                                        disabled={loading}
                                        autoComplete="current-password"
                                    />
                                </Form.Group>

                                <div className="d-grid">
                                    <Button variant="primary" type="submit" disabled={loading} size="lg">
                                        {loading ? (
                                            <>
                                                <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" />
                                                <span className="ms-2">Logging in...</span>
                                            </>
                                        ) : 'Login'}
                                    </Button>
                                </div>
                            </Form>
                            <div className="mt-4 text-center">
                                Don't have an account? <Link to="/register">Register here</Link>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
}

export default LoginPage;