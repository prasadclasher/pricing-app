package com.pricingfeed.api.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PricingRecordResponse {

    private final Long id;
    private final Long storeId;
    private final String sku;
    private final String productName;
    private final BigDecimal price;
    private final LocalDate priceDate;
    private final String currencyCode;
    private final Long version;

    public PricingRecordResponse(Long id, Long storeId, String sku, String productName, BigDecimal price,
                                 LocalDate priceDate, String currencyCode, Long version) {
        this.id = id;
        this.storeId = storeId;
        this.sku = sku;
        this.productName = productName;
        this.price = price;
        this.priceDate = priceDate;
        this.currencyCode = currencyCode;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public Long getStoreId() {
        return storeId;
    }

    public String getSku() {
        return sku;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Long getVersion() {
        return version;
    }
}
