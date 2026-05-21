package com.pricingfeed.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "pricing_audit_log")
@Getter
@Setter
public class PricingAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pricing_record_id", nullable = false)
    private Long pricingRecordId;

    @Column(name = "changed_by", nullable = false)
    private Long changedBy;

    @Column(name = "old_value", nullable = false, columnDefinition = "json")
    private String oldValue;

    @Column(name = "new_value", nullable = false, columnDefinition = "json")
    private String newValue;

    @Column(name = "changed_at", insertable = false, updatable = false)
    private OffsetDateTime changedAt;
}
