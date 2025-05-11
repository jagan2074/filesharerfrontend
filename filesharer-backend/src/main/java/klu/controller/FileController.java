package klu.controller;

import klu.dto.FileResponse;
import klu.model.FileMetadata;
import klu.model.SharedLink;
import klu.service.FileStorageService;
import klu.service.ShareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException; // <<--- ADD THIS IMPORT
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ShareService shareService;

    // ... mapToFileResponse, uploadFile, listUserFiles, downloadFile, shareFile methods remain the same ...
    // (You can copy them from the previous "final updated full codes" if needed, or ensure they are already correct)
    private FileResponse mapToFileResponse(FileMetadata metadata) {
        return new FileResponse(
                metadata.getId(),
                metadata.getName(),
                metadata.getMimeType(),
                metadata.getSizeBytes(),
                metadata.getUploadTimestamp()
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            FileMetadata metadata = fileStorageService.storeFile(file, userDetails.getUsername());
            return ResponseEntity.ok(mapToFileResponse(metadata));
        } catch (RuntimeException e) {
            log.error("Upload failed for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed. Please try again.", e);
        }
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> listUserFiles(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            List<FileMetadata> files = fileStorageService.getUserFiles(userDetails.getUsername());
            List<FileResponse> fileResponses = files.stream().map(this::mapToFileResponse).collect(Collectors.toList());
            return ResponseEntity.ok(fileResponses);
        } catch (RuntimeException e) {
            log.error("Failed to list files for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not retrieve files.", e);
        }
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            FileMetadata metadata = fileStorageService.getFileMetadata(fileId, userDetails.getUsername());
            Resource resource = fileStorageService.loadFileAsResource(fileId, userDetails.getUsername());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getName() + "\"")
                    .body(resource);
        } catch (RuntimeException e) {
             log.warn("Download failed for fileId {} by user {}: {}", fileId, userDetails.getUsername(), e.getMessage());
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found or access denied.", e);
        }
    }

    @PostMapping("/{fileId}/share")
    public ResponseEntity<String> shareFile(@PathVariable Long fileId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            SharedLink link = shareService.createShareLink(fileId, userDetails.getUsername());
            String shareUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/s/")
                                .path(link.getToken())
                                .build()
                                .toUriString();
            return ResponseEntity.ok(shareUrl);
        } catch (RuntimeException e) {
             log.error("File sharing failed for fileId {} by user {}: {}", fileId, userDetails.getUsername(), e.getMessage(), e);
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not share file: " + e.getMessage(), e);
        }
    }


    // --- MODIFIED deleteFile METHOD ---
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            fileStorageService.deleteFile(fileId, userDetails.getUsername());
            return ResponseEntity.ok("File deleted successfully: " + fileId);
        } catch (DataIntegrityViolationException e) { // Catch specific database integrity issues
            log.warn("Deletion failed for fileId {} by user {}: Data integrity violation (e.g., file is shared). {}", fileId, userDetails.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot delete file as it might be referenced by other data (e.g., active shares).");
        } catch (RuntimeException e) { // General catch for other errors from service
             log.error("Deletion failed for fileId {} by user {}: {}", fileId, userDetails.getUsername(), e.getMessage(), e);
             // Consider if NOT_FOUND is always appropriate or if INTERNAL_SERVER_ERROR is better
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete file: " + e.getMessage(), e);
        }
    }
}