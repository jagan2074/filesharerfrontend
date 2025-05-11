package klu.repository;

import klu.model.FileMetadata;
import klu.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByOwnerOrderByUploadTimestampDesc(User owner);
    Optional<FileMetadata> findByIdAndOwner(Long id, User owner);
}