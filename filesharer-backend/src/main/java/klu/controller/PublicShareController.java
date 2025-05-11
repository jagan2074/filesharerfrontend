package klu.controller;

import klu.model.FileMetadata;
import klu.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/s") // Public base path for shared links (matches SecurityConfig permitAll)
public class PublicShareController {

    @Autowired
    private ShareService shareService;

    @GetMapping("/{token}")
    public ResponseEntity<Resource> accessSharedFile(@PathVariable String token) {
        try {
            FileMetadata metadata = shareService.getFileMetadataByToken(token);
            Resource resource = shareService.loadSharedFileAsResource(token);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getName() + "\"")
                    .body(resource);
        } catch (RuntimeException e) {
             System.err.println("Shared link access failed for token " + token + ": " + e.getMessage());
             e.printStackTrace();
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found or file unavailable.", e);
        }
    }
}