package klu.service;

import klu.model.FileMetadata;
import klu.model.SharedLink;
import klu.model.User;
import klu.repository.FileMetadataRepository;
import klu.repository.SharedLinkRepository;
import klu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ShareService {

    @Autowired
    private SharedLinkRepository sharedLinkRepository;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private UserRepository userRepository;

    private final Path fileStorageLocation;

    @Autowired
    public ShareService(@Value("${file.upload-dir}") String uploadDir) {
         this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional
    public SharedLink createShareLink(Long fileId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        FileMetadata file = fileMetadataRepository.findByIdAndOwner(fileId, user)
                .orElseThrow(() -> new RuntimeException("File not found or not owned by user: " + fileId));

        // For simplicity, always create a new unique token.
        // You could add logic here to reuse an existing link for the same file if desired.
        String token = UUID.randomUUID().toString();
        SharedLink sharedLink = new SharedLink(token, file, user);
        return sharedLinkRepository.save(sharedLink);
    }

    @Transactional(readOnly = true)
    public FileMetadata getFileMetadataByToken(String token) {
        SharedLink sharedLink = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired share link."));
        // Here you could add checks for link expiry if you implement that feature
        return sharedLink.getFile();
    }

    @Transactional(readOnly = true)
    public Resource loadSharedFileAsResource(String token) {
        FileMetadata fileMetadata = getFileMetadataByToken(token); // Use the above method
        try {
            Path filePath = this.fileStorageLocation.resolve(fileMetadata.getStorageFilename()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Shared file not found in storage: " + fileMetadata.getName());
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Shared file not found (URL error): " + fileMetadata.getName(), ex);
        }
    }
}