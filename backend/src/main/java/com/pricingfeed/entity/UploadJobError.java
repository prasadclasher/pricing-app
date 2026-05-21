package com.pricingfeed.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "upload_job_errors")
@Getter
@Setter
public class UploadJobError {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_job_id", nullable = false, columnDefinition = "CHAR(36)")
    private String uploadJobId;

    @Column(name = "row_id", nullable = false)
    private int rowId;

    @Column(name = "row_data", columnDefinition = "json")
    private String rowData;

    
    @Column(name = "error_message", nullable = false, length = 1000)
    private String errorMessage;
}
