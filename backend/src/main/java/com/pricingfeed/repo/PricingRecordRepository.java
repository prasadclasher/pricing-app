package com.pricingfeed.repo;

import com.pricingfeed.entity.PricingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;

public interface PricingRecordRepository extends JpaRepository<PricingRecord, Long>, JpaSpecificationExecutor<PricingRecord> {
    Optional<PricingRecord> findByStoreIdAndSkuAndPriceDate(Long storeId, String sku, LocalDate priceDate);
}
