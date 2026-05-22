package com.pricingfeed.repo;

import com.pricingfeed.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    /**
     * Nullify the content field for the given file id (cleanup after processing).
     */
    @Modifying
    @Query("UPDATE UploadedFile f SET f.content = NULL WHERE f.id = :id")
    void deleteContentById(Long id);
}
