package klu.service;

import klu.model.FileMetadata;
import klu.model.SharedLink; // <<--- ADD THIS IMPORT
import klu.model.User;
import klu.repository.FileMetadataRepository;
import klu.repository.SharedLinkRepository; // <<--- ADD THIS IMPORT
import klu.repository.UserRepository;
import org.slf4j.Logger;                 // <<--- ADD THIS IMPORT
import org.slf4j.LoggerFactory;          // <<--- ADD THIS IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class); // Added logger

    private final Path fileStorageLocation;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;
    private final SharedLinkRepository sharedLinkRepository; // <<--- ADD THIS FIELD

    @Autowired
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir,
                              FileMetadataRepository fileMetadataRepository,
                              UserRepository userRepository,
                              SharedLinkRepository sharedLinkRepository) { // <<--- ADD TO CONSTRUCTOR
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.fileMetadataRepository = fileMetadataRepository;
        this.userRepository = userRepository;
        this.sharedLinkRepository = sharedLinkRepository; // <<--- INITIALIZE HERE

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create the directory for uploads.", ex);
            throw new RuntimeException("Could not create the directory for uploads.", ex);
        }
    }

    // ... storeFile, loadFileAsResource, getFileMetadata, getUserFiles methods remain the same ...
    // (You can copy them from the previous "final updated full codes" if needed, or ensure they are already correct)

     @Transactional
     public FileMetadata storeFile(MultipartFile file, String username) {
         User owner = userRepository.findByUsername(username)
                 .orElseThrow(() -> new RuntimeException("User not found: " + username));

         String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
         String fileExtension = "";
         int dotIndex = originalFilename.lastIndexOf('.');
         if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
             fileExtension = originalFilename.substring(dotIndex);
         }
         String storedFilename = UUID.randomUUID().toString() + fileExtension;

         try {
             if (originalFilename.contains("..")) {
                 throw new RuntimeException("Filename contains invalid path sequence: " + originalFilename);
             }
             Path targetLocation = this.fileStorageLocation.resolve(storedFilename);
             try (InputStream inputStream = file.getInputStream()) {
                 Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
             }
             FileMetadata fileMetadata = new FileMetadata(originalFilename, file.getContentType(), file.getSize(), storedFilename, owner);
             log.info("Stored file {} for user {}", originalFilename, username);
             return fileMetadataRepository.save(fileMetadata);
         } catch (IOException ex) {
             log.error("Could not store file {} for user {}", originalFilename, username, ex);
             throw new RuntimeException("Could not store file " + originalFilename, ex);
         }
     }

     @Transactional(readOnly = true)
     public Resource loadFileAsResource(Long fileId, String username) {
         FileMetadata fileMetadata = getFileMetadata(fileId, username);
         try {
             Path filePath = this.fileStorageLocation.resolve(fileMetadata.getStorageFilename()).normalize();
             Resource resource = new UrlResource(filePath.toUri());
             if (resource.exists() && resource.isReadable()) {
                 return resource;
             } else {
                 log.warn("Could not read file (not found or not readable on disk): {}", fileMetadata.getName());
                 throw new RuntimeException("Could not read file: " + fileMetadata.getName());
             }
         } catch (MalformedURLException ex) {
             log.error("File not found (URL error) for {}: {}", fileMetadata.getName(), ex.getMessage());
             throw new RuntimeException("File not found (URL error): " + fileMetadata.getName(), ex);
         }
     }

     @Transactional(readOnly = true)
     public FileMetadata getFileMetadata(Long fileId, String username) {
          User owner = userRepository.findByUsername(username)
                 .orElseThrow(() -> {
                     log.warn("Attempt to get metadata for fileId {} by non-existent user {}", fileId, username);
                     return new RuntimeException("User not found: " + username);
                 });
         return fileMetadataRepository.findByIdAndOwner(fileId, owner)
                 .orElseThrow(() -> {
                      log.warn("File metadata not found for fileId {} and user {}", fileId, username);
                      return new RuntimeException("File not found or access denied: " + fileId);
                 });
     }

     @Transactional(readOnly = true)
     public List<FileMetadata> getUserFiles(String username) {
         User owner = userRepository.findByUsername(username)
                 .orElseThrow(() -> new RuntimeException("User not found: " + username));
         return fileMetadataRepository.findByOwnerOrderByUploadTimestampDesc(owner);
     }


    // --- MODIFIED deleteFile METHOD ---
    @Transactional
    public void deleteFile(Long fileId, String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        FileMetadata fileMetadata = fileMetadataRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new RuntimeException("File not found or access denied for deletion: " + fileId));

        // 1. Find and delete all shared links associated with this file
        List<SharedLink> linksToDelete = sharedLinkRepository.findAllByFile(fileMetadata);
        if (!linksToDelete.isEmpty()) { // Check if list is not empty before deleting
            sharedLinkRepository.deleteAll(linksToDelete);
            log.info("Deleted {} shared links for fileId: {}", linksToDelete.size(), fileId);
        }

        // 2. Then delete the actual file from storage and its metadata
        try {
            Path filePath = this.fileStorageLocation.resolve(fileMetadata.getStorageFilename()).normalize();
            Files.deleteIfExists(filePath);
            fileMetadataRepository.delete(fileMetadata); // This will now succeed
            log.info("Successfully deleted fileId: {} ({}) and its metadata for user {}", fileId, fileMetadata.getName(), username);
        } catch (IOException ex) {
            log.error("Could not delete physical file {} for fileId: {}. DB metadata was still deleted.", fileMetadata.getStorageFilename(), fileId, ex);
            // Depending on policy, you might want to re-throw or handle this differently
            // For now, we assume deleting DB metadata is primary if physical delete fails after children are gone.
            // Or, re-throw to indicate a partial failure:
            throw new RuntimeException("Could not delete physical file " + fileMetadata.getName() + ", but DB records might be cleared.", ex);
        }
    }
}