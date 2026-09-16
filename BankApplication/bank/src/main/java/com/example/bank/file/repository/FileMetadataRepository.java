package com.example.bank.file.repository;

import com.example.bank.common.enums.FileDocumentType;
import com.example.bank.common.enums.FileMetadataStatus;
import com.example.bank.file.entity.FileMetadata;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository
        extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByCustomerIdOrderByCreatedAtDesc(
            Long customerId
    );

    List<FileMetadata> findByCustomerIdAndStatusOrderByCreatedAtDesc(
            Long customerId,
            FileMetadataStatus status
    );

    long countByCustomerIdAndStatus(
            Long customerId,
            FileMetadataStatus status
    );

    List<FileMetadata> findByDocumentTypeOrderByCreatedAtDesc(
            FileDocumentType documentType
    );

    List<FileMetadata> findByStatusOrderByCreatedAtDesc(
            FileMetadataStatus status
    );

    boolean existsByChecksum(String checksum);

    boolean existsByCustomerIdAndChecksum(
            Long customerId,
            String checksum
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f
            FROM FileMetadata f
            WHERE f.id = :id
            """)
    Optional<FileMetadata> findByIdForUpdate(
            @Param("id") Long id
    );
}