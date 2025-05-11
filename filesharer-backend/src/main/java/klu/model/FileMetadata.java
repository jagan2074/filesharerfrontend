package klu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "files") // Table name for file metadata
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Original filename uploaded by the user

    @Column(nullable = false)
    private String mimeType; // e.g., "image/jpeg", "application/pdf"

    private long sizeBytes; // File size in bytes

    // This will be the unique name of the file as stored on the server's file system
    @Column(nullable = false, unique = true)
    private String storageFilename;

    @ManyToOne(fetch = FetchType.LAZY) // Many files can belong to one user
    @JoinColumn(name = "owner_user_id", nullable = false) // Foreign key column in 'files' table
    private User owner; // The user who owns this file

    private LocalDateTime uploadTimestamp;

    // No-argument constructor (required by JPA)
    public FileMetadata() {
    }

    // Constructor for convenience
    public FileMetadata(String name, String mimeType, long sizeBytes, String storageFilename, User owner) {
        this.name = name;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.storageFilename = storageFilename;
        this.owner = owner;
    }

    @PrePersist
    protected void onUpload() {
        uploadTimestamp = LocalDateTime.now();
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getStorageFilename() { return storageFilename; }
    public void setStorageFilename(String storageFilename) { this.storageFilename = storageFilename; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public LocalDateTime getUploadTimestamp() { return uploadTimestamp; }
    public void setUploadTimestamp(LocalDateTime uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMetadata that = (FileMetadata) o;
        return Objects.equals(id, that.id) && Objects.equals(storageFilename, that.storageFilename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, storageFilename);
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", sizeBytes=" + sizeBytes +
                ", storageFilename='" + storageFilename + '\'' +
                ", ownerId=" + (owner != null ? owner.getId() : "null") +
                ", uploadTimestamp=" + uploadTimestamp +
                '}';
    }
}