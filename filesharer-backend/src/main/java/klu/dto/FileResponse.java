package klu.dto;
import java.time.LocalDateTime;
 import java.util.Objects;

 public class FileResponse {
     private Long id;
     private String name;
     private String mimeType;
     private long sizeBytes;
     private LocalDateTime uploadTimestamp;
     // Later, we might add a field like String publicShareLink;

     public FileResponse() {
     }

     public FileResponse(Long id, String name, String mimeType, long sizeBytes, LocalDateTime uploadTimestamp) {
         this.id = id;
         this.name = name;
         this.mimeType = mimeType;
         this.sizeBytes = sizeBytes;
         this.uploadTimestamp = uploadTimestamp;
     }

     // Getters and Setters
     public Long getId() { return id; }
     public void setId(Long id) { this.id = id; }
     public String getName() { return name; }
     public void setName(String name) { this.name = name; }
     public String getMimeType() { return mimeType; }
     public void setMimeType(String mimeType) { this.mimeType = mimeType; }
     public long getSizeBytes() { return sizeBytes; }
     public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
     public LocalDateTime getUploadTimestamp() { return uploadTimestamp; }
     public void setUploadTimestamp(LocalDateTime uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }

     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         FileResponse that = (FileResponse) o;
         return sizeBytes == that.sizeBytes && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(mimeType, that.mimeType) && Objects.equals(uploadTimestamp, that.uploadTimestamp);
     }

     @Override
     public int hashCode() {
         return Objects.hash(id, name, mimeType, sizeBytes, uploadTimestamp);
     }

     @Override
     public String toString() {
         return "FileResponse{" +
                 "id=" + id +
                 ", name='" + name + '\'' +
                 ", mimeType='" + mimeType + '\'' +
                 ", sizeBytes=" + sizeBytes +
                 ", uploadTimestamp=" + uploadTimestamp +
                 '}';
     }
 }