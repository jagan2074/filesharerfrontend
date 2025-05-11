// src/components/ShareFileModal.js
import React, { useState, useEffect } from 'react';
import { Modal, Button, Form, InputGroup, Alert, Spinner } from 'react-bootstrap';
import { shareFile as apiShareFile } from '../services/apiService';

function ShareFileModal({ show, handleClose, file }) {
    const [shareLink, setShareLink] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');
    const [copied, setCopied] = useState(false);

    useEffect(() => {
        // Only fetch if the modal is shown and a file is provided
        if (show && file && file.id) {
            setShareLink(''); // Reset previous link
            setError('');
            setCopied(false);
            setIsLoading(true);
            
            apiShareFile(file.id)
                .then(response => {
                    setShareLink(response.data); // Backend returns the full URL string
                })
                .catch(err => {
                    const errorMessage = err.response?.data?.message || err.response?.data || err.message || 'Failed to generate share link.';
                    setError(errorMessage);
                    console.error("Share link generation error:", err.response || err);
                })
                .finally(() => {
                    setIsLoading(false);
                });
        }
    }, [show, file]); // Dependency array ensures this runs when show or file changes

    const handleCopyToClipboard = () => {
        if (!shareLink) return;
        navigator.clipboard.writeText(shareLink)
            .then(() => {
                setCopied(true);
                setTimeout(() => setCopied(false), 2500);
            })
            .catch(err => {
                console.error('Failed to copy link: ', err);
                setError('Could not copy link to clipboard. Please copy manually.');
            });
    };

    // Do not render the modal if there's no file object, to prevent errors
    if (!file) {
        return null; 
    }

    return (
        <Modal show={show} onHide={handleClose} centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title>Share: <span className="fw-normal fs-6">{file.name}</span></Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {error && <Alert variant="danger" onClose={() => setError('')} dismissible>{error}</Alert>}
                
                {isLoading && (
                    <div className="text-center my-3">
                        <Spinner animation="border" variant="primary" />
                        <p className="mt-2">Generating share link...</p>
                    </div>
                )}

                {!isLoading && shareLink && (
                    <>
                        <p>Anyone with this public link can download the file:</p>
                        <InputGroup className="mb-3">
                            <Form.Control
                                type="text"
                                value={shareLink}
                                readOnly
                                aria-label="Shareable link"
                            />
                            <Button variant={copied ? "success" : "outline-primary"} onClick={handleCopyToClipboard} disabled={!shareLink}>
                                {copied ? 'Copied!' : 'Copy Link'}
                            </Button>
                        </InputGroup>
                    </>
                )}

                {!isLoading && !shareLink && !error && (
                    <Alert variant="info">No share link available or error occurred.</Alert>
                )}
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={handleClose}>
                    Close
                </Button>
            </Modal.Footer>
        </Modal>
    );
}

export default ShareFileModal;