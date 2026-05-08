package com.example.coreboard.domain.attachment.service;

import com.example.coreboard.domain.attachment.entity.Attachment;
import com.example.coreboard.domain.attachment.entity.AttachmentStatus;
import com.example.coreboard.domain.attachment.repository.AttachmentRepository;
import com.example.coreboard.domain.common.exception.Attachment.AttachmentErrorCode;
import com.example.coreboard.domain.common.exception.Attachment.AttachmentErrorException;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
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
    private final UsersRepository usersRepository;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.endpoint}")
    private String endpoint;

    public AttachmentService(
            S3Client s3Client,
            AttachmentRepository attachmentRepository,
            UsersRepository usersRepository
    ) {
        this.s3Client = s3Client;
        this.attachmentRepository = attachmentRepository;
        this.usersRepository = usersRepository;
    }

    // 파일 임시 업로드 (게시글 작성 전)
    @Transactional
    public Long upload(String username, MultipartFile file) throws IOException {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AttachmentErrorException(AttachmentErrorCode.FILE_SIZE_EXCEEDED);
        }

        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.NOT_FOUND));

        String objectKey = "attachments/temp/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );
        String url = endpoint + "/" + bucket + "/" + objectKey;

        Attachment attachment = Attachment.createTemp(
                user,
                file.getOriginalFilename(),
                objectKey,
                url,
                file.getContentType(),
                file.getSize()
        );

        return attachmentRepository.save(attachment).getId();
    }

    // 게시글 저장 완료 시 TEMP → CONFIRMED
    @Transactional
    public void confirm(List<Long> attachmentIds, Post post, Users user) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return;
        }
        List<Attachment> attachments = attachmentRepository.findAllById(attachmentIds);

        if (attachments.size() != attachmentIds.size()) {
            throw new AttachmentErrorException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND);
        }

        attachments.forEach(attachment -> {
            attachment.validateOwner(user);
            attachment.validateTemp();
            attachment.confirm(post);
        });
    }

    // 고아 파일 정리 스케줄러 (매일 새벽 3시)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupAttachments() {
        deleteTempOrphanFiles();
        deleteDeletedFiles();
    }

    @Transactional
    public void markDeletedByPost(Long postId) {
        List<Attachment> attachments = attachmentRepository.findByPostIdAndStatus(
                postId,
                AttachmentStatus.CONFIRMED
        );

        attachments.forEach(Attachment::markDeleted);
    }

    private void deleteTempOrphanFiles() {
        List<Attachment> orphans = attachmentRepository.findByStatusAndCreatedAtBefore(
                AttachmentStatus.TEMP,
                LocalDateTime.now().minusHours(24)
        );

        orphans.forEach(attachment -> {
            deleteFromStorage(attachment);
            attachmentRepository.delete(attachment);
        });
    }

    private void deleteDeletedFiles() {
        List<Attachment> deletedAttachments = attachmentRepository.findByStatusAndDeletedAtBefore(
                AttachmentStatus.DELETED,
                LocalDateTime.now().minusDays(7)
        );

        deletedAttachments.forEach(attachment -> {
            deleteFromStorage(attachment);
            attachmentRepository.delete(attachment);
        });
    }

    private void deleteFromStorage(Attachment attachment) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(attachment.getObjectKey())
                .build());
    }
}
