package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.S3Service;
import io.undertow.util.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class S3Resource {

    private final S3Service s3Service;

    public S3Resource(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> createS3(@RequestBody MultipartFile file) throws BadRequestException {
        String imageUrl = s3Service.saveS3(file);
        return ResponseEntity.ok(imageUrl);
    }
}
