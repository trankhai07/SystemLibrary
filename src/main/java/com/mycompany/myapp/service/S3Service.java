package com.mycompany.myapp.service;

import com.mycompany.myapp.web.rest.S3Resource;
import io.undertow.util.BadRequestException;
import java.net.URL;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final Logger log = LoggerFactory.getLogger(S3Resource.class);
    private final String region;
    private final String bucketName;
    private final String accessKey;
    private final String secretKey;

    public S3Service(Environment env) {
        this.region = env.getProperty("aws-s3.region");
        this.bucketName = env.getProperty("aws-s3.bucketName");
        this.accessKey = env.getProperty("aws-s3.accessKey");
        this.secretKey = env.getProperty("aws-s3.secretKey");

        this.s3Client =
            S3Client
                .builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(this.accessKey, this.secretKey)))
                .region(Region.of(this.region))
                .build();
    }

    public String saveS3(MultipartFile file) throws BadRequestException {
        log.debug("Request to S3 : {}", file);
        try {
            String originalFileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = uuid + fileExtension;
            PutObjectRequest request = PutObjectRequest.builder().bucket(this.bucketName).key(newFileName).build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            Region awsRegion = Region.of(this.region);
            URL imageUrl = s3Client.utilities().getUrl(builder -> builder.bucket(this.bucketName).region(awsRegion).key(newFileName));
            return imageUrl.toString();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BadRequestException("System error please try again in a few minutes");
        }
    }
}
