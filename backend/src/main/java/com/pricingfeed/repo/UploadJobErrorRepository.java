package com.pricingfeed.repo;

import com.pricingfeed.entity.UploadJobError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadJobErrorRepository extends JpaRepository<UploadJobError, Long> {
    List<UploadJobError> findByUploadJobIdOrderByRowIdAsc(String uploadJobId);
}
