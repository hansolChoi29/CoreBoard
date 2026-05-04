package com.example.coreboard.domain.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = S3Config.class)
@TestPropertySource(properties = {
        "cloud.aws.credentials.access-key=minioadmin",
        "cloud.aws.credentials.secret-key=minioadmin",
        "cloud.aws.region.static=us-east-1",
        "cloud.aws.s3.bucket=coreboard-attachments",
        "cloud.aws.endpoint=http://localhost:9000"
})
class S3ConfigTest {
    @Autowired
    private S3Client s3Client;

    @Test
    @DisplayName("S3Client 빈이 정상적으로 등록된다")
    void s3ClientBeanIsRegistered() {
        assertThat(s3Client).isNotNull();
    }
}