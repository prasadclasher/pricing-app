package com.pricingfeed.api;

import com.pricingfeed.api.request.UpdatePricingRecordRequest;
import com.pricingfeed.api.response.PricingRecordAuditResponse;
import com.pricingfeed.api.response.PricingRecordResponse;
import com.pricingfeed.service.ActorContext;
import com.pricingfeed.service.PricingRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pricing-records")
public class PricingRecordController {
    private final PricingRecordService pricingRecordService;
    private final ActorContext actorContext;

    public PricingRecordController(PricingRecordService pricingRecordService, ActorContext actorContext) {
        this.pricingRecordService = pricingRecordService;
        this.actorContext = actorContext;
    }

    @GetMapping
    public Page<PricingRecordResponse> search(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return pricingRecordService.search(storeId, sku, productName, priceMin, priceMax, dateFrom, dateTo, page, size, sort, actorContext.requireActor());
    }

    @GetMapping("/{id}")
    public PricingRecordResponse get(@PathVariable Long id) {
        return pricingRecordService.getById(id, actorContext.requireActor());
    }

    @PutMapping("/{id}")
    public PricingRecordResponse update(@PathVariable Long id, @Valid @RequestBody UpdatePricingRecordRequest request) {
        return pricingRecordService.update(id, request, actorContext.requireActor());
    }

    @GetMapping("/{id}/audit")
    public List<PricingRecordAuditResponse> audit(@PathVariable Long id) {
        return pricingRecordService.getAudit(id, actorContext.requireActor());
    }
}
