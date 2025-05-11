// src/components/FileUpload.js
import React, { useState, useRef } from 'react';
import { Form, Button, Alert, Card, Spinner } from 'react-bootstrap'; // ProgressBar removed for simplicity
import { uploadFile as apiUploadFile } from '../services/apiService';

function FileUpload({ onUploadSuccess }) {
    const [selectedFile, setSelectedFile] = useState(null);
    const [isUploading, setIsUploading] = useState(false);
    // const [uploadProgress, setUploadProgress] = useState(0); // Removed for simplicity
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const fileInputRef = useRef(null);

    const handleFileChange = (event) => {
        if (event.target.files && event.target.files[0]) {
            setSelectedFile(event.target.files[0]);
        } else {
            setSelectedFile(null);
        }
        setError('');
        setSuccess('');
        // setUploadProgress(0);
    };

    const handleUpload = async () => {
        if (!selectedFile) {
            setError('Please select a file to upload.');
            return;
        }

        const formData = new FormData();
        formData.append('file', selectedFile); // Backend expects 'file' as key

        setIsUploading(true);
        setError('');
        setSuccess('');

        try {
            await apiUploadFile(formData);
            setSuccess(`File "${selectedFile.name}" uploaded successfully!`);
            setSelectedFile(null); // Clear selection
            if (fileInputRef.current) {
                fileInputRef.current.value = ""; // Reset the file input field
            }
            if (onUploadSuccess) {
                onUploadSuccess();
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.response?.data || err.message || 'File upload failed.';
            setError(errorMessage);
            console.error("Upload error:", err.response || err);
        } finally {
            setIsUploading(false);
        }
    };

    return (
        <Card className="mb-4">
            <Card.Header as="h5">Upload New File</Card.Header>
            <Card.Body>
                {error && <Alert variant="danger" onClose={() => setError('')} dismissible>{error}</Alert>}
                {success && <Alert variant="success" onClose={() => setSuccess('')} dismissible>{success}</Alert>}
                <Form>
                    <Form.Group controlId="formFile" className="mb-3">
                        <Form.Control 
                            type="file" 
                            onChange={handleFileChange} 
                            disabled={isUploading}
                            ref={fileInputRef}
                        />
                    </Form.Group>
                    <Button 
                        variant="success" 
                        onClick={handleUpload} 
                        disabled={!selectedFile || isUploading}
                        className="w-100"
                    >
                        {isUploading ? (
                            <>
                                <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" />
                                <span className="ms-2">Uploading...</span>
                            </>
                        ) : (
                             'Upload File'
                        )}
                    </Button>
                </Form>
            </Card.Body>
        </Card>
    );
}

export default FileUpload;