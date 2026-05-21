package com.pricingfeed.api.response;

import java.time.OffsetDateTime;

public class PricingRecordAuditResponse {

    private final Long id;
    private final Long changedBy;
    private final String oldValue;
    private final String newValue;
    private final OffsetDateTime changedAt;

    public PricingRecordAuditResponse(Long id, Long changedBy, String oldValue, String newValue, OffsetDateTime changedAt) {
        this.id = id;
        this.changedBy = changedBy;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }
}
