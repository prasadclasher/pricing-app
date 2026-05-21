package com.pricingfeed.api.response;

public class UploadJobErrorRowResponse {

    private final int rowNumber;
    private final String errorMessage;
    private final String rowData;

    public UploadJobErrorRowResponse(int rowNumber, String errorMessage, String rowData) {
        this.rowNumber = rowNumber;
        this.errorMessage = errorMessage;
        this.rowData = rowData;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getRowData() {
        return rowData;
    }
}
