package com.example.coreboard.domain.common.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

import java.net.URI;

@Configuration
public class S3Config {
    // S3 SDK : Amazon이 만든 SDK인데, MinIO/R2/Backblaze 전부 이 SDK로 접속 가능하게 설계되어있음
    @Value("${cloud.aws.endpoint}") // localhost:9000 or R2 주소
    private String endpoint;
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;
    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                // endpointOverride : 주소를 localhost:9000 또는 R2주소로 바꿈 
                .endpointOverride(URI.create(endpoint))

                // ID/비밀번호 설정
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                // 서버 위치 근데 MinIO 사용 중
                .region(Region.of(region))
                
                // false : S3 기본 방식(버킷이 서브도메인) 근데 MinIO는 이거 지원 안 함 (http://coreboard-attachments.localhost:9000/cat.png)
                // true : MinIO/R2 둘 다 이 방식 씀 (http://localhost:9000/coreboard-attachments/cat.png)
                .forcePathStyle(true)
                .build();
    }
}

