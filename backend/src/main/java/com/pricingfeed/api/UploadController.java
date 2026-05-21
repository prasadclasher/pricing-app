package com.pricingfeed.api;

import com.pricingfeed.api.response.UploadJobResponse;
import com.pricingfeed.api.response.UploadResponse;
import com.pricingfeed.service.ActorContext;
import com.pricingfeed.service.UploadService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final UploadService uploadService;
    private final ActorContext actorContext;

    public UploadController(UploadService uploadService, ActorContext actorContext) {
        this.uploadService = uploadService;
        this.actorContext = actorContext;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestPart("file") MultipartFile file) {
        return new UploadResponse(uploadService.createAndProcess(file, actorContext.requireActor()));
    }

    @GetMapping("/{jobId}")
    public UploadJobResponse getStatus(@PathVariable String jobId) {
        return uploadService.getJob(jobId, actorContext.requireActor());
    }
}
