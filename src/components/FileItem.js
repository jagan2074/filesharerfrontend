// src/components/FileItem.js
import React from 'react';
import { Button, ButtonGroup } from 'react-bootstrap';

function FileItem({ file, onDownload, onDelete, onShare }) {
    const formatBytes = (bytes, decimals = 2) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const dm = decimals < 0 ? 0 : decimals;
        const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
    };

    return (
        <tr>
            <td>{file.name}</td>
            <td className="text-nowrap">{formatBytes(file.sizeBytes)}</td>
            <td className="text-nowrap">
                {new Date(file.uploadTimestamp).toLocaleDateString()}
                <br />
                <small className="text-muted">{new Date(file.uploadTimestamp).toLocaleTimeString()}</small>
            </td>
            <td>
                <ButtonGroup size="sm" aria-label="File actions">
                    <Button variant="outline-primary" onClick={() => onDownload(file.id, file.name)} title="Download">
                        Download
                    </Button>
                    <Button variant="outline-info" onClick={() => onShare(file)} title="Share">
                        Share
                    </Button>
                    <Button variant="outline-danger" onClick={() => onDelete(file.id, file.name)} title="Delete">
                        Delete
                    </Button>
                </ButtonGroup>
            </td>
        </tr>
    );
}

export default FileItem;