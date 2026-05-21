package com.pricingfeed.api.response;

public class UploadResponse {

    private final String jobId;

    public UploadResponse(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }
}
