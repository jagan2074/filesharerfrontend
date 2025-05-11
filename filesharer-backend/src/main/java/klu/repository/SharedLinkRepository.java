package klu.repository;

import klu.model.FileMetadata;
import klu.model.SharedLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <<--- ADD THIS IMPORT
import java.util.Optional;

@Repository
public interface SharedLinkRepository extends JpaRepository<SharedLink, Long> {
    Optional<SharedLink> findByToken(String token);
    List<SharedLink> findAllByFile(FileMetadata file); // <<--- ADD THIS METHOD
}