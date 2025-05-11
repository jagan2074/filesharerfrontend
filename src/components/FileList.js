// src/components/FileList.js
import React from 'react';
import { Table, Alert, Spinner, Card } from 'react-bootstrap';
import FileItem from './FileItem';

function FileList({ files, isLoading, onDownload, onDelete, onShare }) {
    // Error state is now handled in DashboardPage directly for this component's parent
    if (isLoading) {
        return (
            <div className="text-center my-5 full-page-center"> {/* Use new CSS class */}
                <Spinner animation="border" variant="primary" style={{ width: '3rem', height: '3rem' }}/>
                <p className="mt-3 fs-5">Loading your files...</p>
            </div>
        );
    }

    if (!files || files.length === 0) {
        return (
             <Card className="text-center">
                 <Card.Body>
                     <Card.Title>No Files Yet!</Card.Title>
                     <Card.Text>
                         Start by uploading your first file using the form above.
                     </Card.Text>
                 </Card.Body>
             </Card>
        );
    }

    return (
        <Card>
            <Card.Header as="h5">My Files</Card.Header>
            <Table striped bordered hover responsive className="mb-0 align-middle">
                <thead className="table-light">
                    <tr>
                        <th>Name</th>
                        <th>Size</th>
                        <th>Uploaded On</th>
                        <th className="text-center">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {files.map(file => (
                        <FileItem
                            key={file.id}
                            file={file}
                            onDownload={onDownload}
                            onDelete={onDelete}
                            onShare={onShare}
                        />
                    ))}
                </tbody>
            </Table>
        </Card>
    );
}

export default FileList;