package com.pricingfeed.repo;

import com.pricingfeed.entity.PricingAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PricingAuditLogRepository extends JpaRepository<PricingAuditLog, Long> {
    List<PricingAuditLog> findByPricingRecordIdOrderByChangedAtDesc(Long pricingRecordId);
}
