package com.pricingfeed.api.response;

import com.pricingfeed.domain.UploadJobStatus;

import java.time.OffsetDateTime;
import java.util.List;

public class UploadJobResponse {

    private final String id;
    private final UploadJobStatus status;
    private final int totalRows;
    private final int successfulRows;
    private final int failedRows;
    private final OffsetDateTime startedAt;
    private final OffsetDateTime completedAt;
    private final List<UploadJobErrorRowResponse> errorRows;

    public UploadJobResponse(String id, UploadJobStatus status, int totalRows, int successfulRows, int failedRows,
                           OffsetDateTime startedAt, OffsetDateTime completedAt, List<UploadJobErrorRowResponse> errorRows) {
        this.id = id;
        this.status = status;
        this.totalRows = totalRows;
        this.successfulRows = successfulRows;
        this.failedRows = failedRows;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.errorRows = errorRows;
    }

    public String getId() {
        return id;
    }

    public UploadJobStatus getStatus() {
        return status;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getSuccessfulRows() {
        return successfulRows;
    }

    public int getFailedRows() {
        return failedRows;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public List<UploadJobErrorRowResponse> getErrorRows() {
        return errorRows;
    }
}
