// src/pages/DashboardPage.js
import React, { useState, useEffect, useCallback } from 'react';
import { Container, Row, Col, Alert } from 'react-bootstrap';
import FileUpload from '../components/FileUpload';
import FileList from '../components/FileList';
import ShareFileModal from '../components/ShareFileModal';
import { getUserFiles, downloadFile as apiDownloadFile, deleteFile as apiDeleteFile } from '../services/apiService';
// We don't need useAuth here directly as Navbar handles logout/user info display
// import { useAuth } from '../context/AuthContext'; 

function DashboardPage() {
    const [files, setFiles] = useState([]);
    const [isLoadingFiles, setIsLoadingFiles] = useState(true); // Start true to load on mount
    const [fileListError, setFileListError] = useState('');
    
    const [showShareModal, setShowShareModal] = useState(false);
    const [selectedFileForShare, setSelectedFileForShare] = useState(null);

    const fetchFiles = useCallback(async () => {
        setIsLoadingFiles(true);
        setFileListError('');
        try {
            const response = await getUserFiles();
            setFiles(response.data);
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.response?.data || err.message || 'Failed to fetch files.';
            setFileListError(errorMessage);
            console.error("Fetch files error:", err.response || err);
            setFiles([]); // Clear files on error to avoid showing stale data
        } finally {
            setIsLoadingFiles(false);
        }
    }, []);

    useEffect(() => {
        fetchFiles();
    }, [fetchFiles]);

    const handleDownload = async (fileId, fileName) => {
        try {
            // Add visual feedback if desired (e.g., a temporary "Downloading..." message)
            await apiDownloadFile(fileId, fileName);
        } catch (err) {
            alert(`Download failed for "${fileName}". Please try again.`);
            console.error("Download error:", err.response || err);
        }
    };

    const handleDelete = async (fileId, fileName) => {
        if (window.confirm(`Are you sure you want to delete "${fileName}"? This action cannot be undone.`)) {
            try {
                // Add visual feedback if desired
                await apiDeleteFile(fileId);
                // Optionally show a success alert before fetching
                fetchFiles(); 
            } catch (err) {
                const errorMessage = err.response?.data?.message || err.response?.data || `Failed to delete "${fileName}".`;
                alert(`Delete failed: ${errorMessage}`);
                console.error("Delete error:", err.response || err);
            }
        }
    };

    const handleOpenShareModal = (file) => {
        setSelectedFileForShare(file);
        setShowShareModal(true);
    };

    const handleCloseShareModal = () => {
        setShowShareModal(false);
        // Delay clearing selected file to allow modal to fade out gracefully if desired,
        // but for simplicity, clearing immediately is fine.
        setSelectedFileForShare(null);
    };

    return (
        <Container fluid className="pt-3 pb-5">
            {/* File Upload Section */}
            <Row className="mb-4">
                <Col> {/* Taking full width for upload section for better focus */}
                    <FileUpload onUploadSuccess={fetchFiles} />
                </Col>
            </Row>

            {/* File List Section */}
            <Row>
                <Col>
                    {fileListError && <Alert variant="danger" onClose={() => setFileListError('')} dismissible>{fileListError}</Alert>}
                    <FileList
                        files={files}
                        isLoading={isLoadingFiles}
                        onDownload={handleDownload}
                        onDelete={handleDelete}
                        onShare={handleOpenShareModal}
                    />
                </Col>
            </Row>

            {/* Share Modal - only renders if selectedFileForShare is not null */}
            {selectedFileForShare && (
                <ShareFileModal
                    show={showShareModal}
                    handleClose={handleCloseShareModal}
                    file={selectedFileForShare}
                />
            )}
        </Container>
    );
}

export default DashboardPage;