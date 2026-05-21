package com.pricingfeed.service;

import com.pricingfeed.api.request.UpdatePricingRecordRequest;
import com.pricingfeed.api.response.PricingRecordAuditResponse;
import com.pricingfeed.api.response.PricingRecordResponse;
import com.pricingfeed.entity.PricingAuditLog;
import com.pricingfeed.entity.PricingRecord;
import com.pricingfeed.repo.PricingAuditLogRepository;
import com.pricingfeed.repo.PricingRecordRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.pricingfeed.service.ApiExceptions.ConflictException;
import static com.pricingfeed.service.ApiExceptions.ForbiddenException;
import static com.pricingfeed.service.ApiExceptions.NotFoundException;

@Service
public class PricingRecordService {
    private final PricingRecordRepository pricingRecordRepository;
    private final PricingAuditLogRepository pricingAuditLogRepository;

    public PricingRecordService(PricingRecordRepository pricingRecordRepository, PricingAuditLogRepository pricingAuditLogRepository) {
        this.pricingRecordRepository = pricingRecordRepository;
        this.pricingAuditLogRepository = pricingAuditLogRepository;
    }

    @Transactional(readOnly = true)
    public Page<PricingRecordResponse> search(Long storeId, String sku, String productName, BigDecimal priceMin, BigDecimal priceMax,
                                                   LocalDate dateFrom, LocalDate dateTo, int page, int size, String sort, ActorContext.Actor actor) {
        Sort sortObj = Sort.by("priceDate").descending();
        if (sort != null && sort.contains(",")) {
            String[] split = sort.split(",");
            sortObj = Sort.by(Sort.Direction.fromString(split[1]), split[0]);
        }
        Long scopedStore = actor.isHq() ? storeId : actor.storeId();
        return pricingRecordRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (scopedStore != null) predicates.add(cb.equal(root.get("storeId"), scopedStore));
            if (sku != null && !sku.isBlank()) predicates.add(cb.equal(root.get("sku"), sku));
            if (productName != null && !productName.isBlank()) predicates.add(cb.like(cb.lower(root.get("productName")), "%" + productName.toLowerCase() + "%"));
            if (priceMin != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), priceMin));
            if (priceMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), priceMax));
            if (dateFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("priceDate"), dateFrom));
            if (dateTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("priceDate"), dateTo));
            return cb.and(predicates.toArray(Predicate[]::new));
        }, PageRequest.of(page, size, sortObj)).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PricingRecordResponse getById(Long id, ActorContext.Actor actor) {
        PricingRecord record = pricingRecordRepository.findById(id).orElseThrow(() -> new NotFoundException("Pricing record not found"));
        if (!actor.isHq() && !record.getStoreId().equals(actor.storeId())) {
            throw new ForbiddenException("Record is outside user store scope");
        }
        return toResponse(record);
    }

    @Transactional
    public PricingRecordResponse update(Long id, UpdatePricingRecordRequest request, ActorContext.Actor actor) {
        PricingRecord record = pricingRecordRepository.findById(id).orElseThrow(() -> new NotFoundException("Pricing record not found"));
        if (!actor.isHq() && !record.getStoreId().equals(actor.storeId())) {
            throw new ForbiddenException("Record is outside user store scope");
        }
        if (!record.getVersion().equals(request.getVersion())) {
            throw new ConflictException("Version conflict");
        }
        String oldValue = "{\"price\":\"" + record.getPrice() + "\",\"version\":" + record.getVersion() + "}";
        record.setPrice(request.getPrice());
        try {
            pricingRecordRepository.saveAndFlush(record);
        } catch (OptimisticLockingFailureException ex) {
            throw new ConflictException("Version conflict");
        }
        String newValue = "{\"price\":\"" + record.getPrice() + "\",\"version\":" + record.getVersion() + "}";
        PricingAuditLog audit = new PricingAuditLog();
        audit.setPricingRecordId(record.getId());
        audit.setChangedBy(actor.userId());
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        pricingAuditLogRepository.save(audit);
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<PricingRecordAuditResponse> getAudit(Long id, ActorContext.Actor actor) {
        PricingRecord record = pricingRecordRepository.findById(id).orElseThrow(() -> new NotFoundException("Pricing record not found"));
        if (!actor.isHq() && !record.getStoreId().equals(actor.storeId())) {
            throw new ForbiddenException("Record is outside user store scope");
        }
        return pricingAuditLogRepository.findByPricingRecordIdOrderByChangedAtDesc(id).stream()
                .map(a -> new PricingRecordAuditResponse(a.getId(), a.getChangedBy(), a.getOldValue(), a.getNewValue(), a.getChangedAt()))
                .toList();
    }

    private PricingRecordResponse toResponse(PricingRecord p) {
        return new PricingRecordResponse(p.getId(), p.getStoreId(), p.getSku(), p.getProductName(), p.getPrice(), p.getPriceDate(), p.getCurrencyCode(), p.getVersion());
    }
}
