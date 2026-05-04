package com.example.coreboard.domain.attachment.service;

import com.example.coreboard.domain.attachment.entity.Attachment;
import com.example.coreboard.domain.attachment.entity.AttachmentStatus;
import com.example.coreboard.domain.attachment.repository.AttachmentRepository;
import com.example.coreboard.domain.post.entity.Post;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {
    private final S3Client s3Client;
    private final AttachmentRepository attachmentRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.endpoint}")
    private String endpoint;

    public AttachmentService(
            S3Client s3Client,
            AttachmentRepository attachmentRepository
    ) {
        this.s3Client = s3Client;
        this.attachmentRepository = attachmentRepository;
    }

    // 파일 임시 업로드 (게시글 작성 전)
    @Transactional
    public Long upload(MultipartFile file) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );
        String url = endpoint + "/" + bucket + "/" + key;

        Attachment attachment = Attachment.createTemp(
                file.getOriginalFilename(),
                url,
                file.getContentType(),
                file.getSize()
        );

        return attachmentRepository.save(attachment).getId();
    }

    // 게시글 저장 완료 시 TEMP → CONFIRMED
    @Transactional
    public void confirm(List<Long> attachmentIds, Post post) {
        List<Attachment> attachments = attachmentRepository.findAllById(attachmentIds);

        attachments.forEach(attachment -> attachment.confirm(post));
    }

    // 고아 파일 정리 스케줄러 (매일 새벽 3시)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteOrphanFiles() {
        List<Attachment> orphans = attachmentRepository.findByStatusAndCreatedAtBefore(
                AttachmentStatus.TEMP,
                LocalDateTime.now().minusHours(24)
        );
        orphans.forEach(attachment -> {
            String storedUrl = attachment.getStoreUrl();
            int lastSlash = storedUrl.lastIndexOf("/");
            String key = lastSlash >= 0 ? storedUrl.substring(lastSlash + 1) : storedUrl;
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            attachmentRepository.delete(attachment);
        });
    }
}
