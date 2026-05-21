package com.pricingfeed.repo;

import com.pricingfeed.entity.UploadJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadJobRepository extends JpaRepository<UploadJob, String> {
}
