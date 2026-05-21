package com.pricingfeed.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class UpdatePricingRecordRequest {

    @NotNull
    private final BigDecimal price;

    @NotNull
    private final Long version;

    @JsonCreator
    public UpdatePricingRecordRequest(
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("version") Long version) {
        this.price = price;
        this.version = version;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getVersion() {
        return version;
    }
}
