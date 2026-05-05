package com.example.coreboard.domain.attachment.service;

import com.example.coreboard.domain.attachment.entity.Attachment;
import com.example.coreboard.domain.attachment.entity.AttachmentStatus;
import com.example.coreboard.domain.attachment.repository.AttachmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {
    @Mock
    private S3Client s3Client;

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private AttachmentService attachmentService;

    void setUp() {
        ReflectionTestUtils.setField(attachmentService, "bucket", "coreboard-attachments");
        ReflectionTestUtils.setField(attachmentService, "endpoint", "http://localhost:9000");
    }

    @Test
    @DisplayName("파일 업로드 시 TEMP 상태로 저장된다")
    void upload_savesAsTempStatus() throws IOException {
        setUp();
        MockMultipartFile file = new MockMultipartFile(
                "file", "cat.png", "image/png", "dummy".getBytes()
        );
        Attachment saved = Attachment.createTemp("cat.png", "http://localhost:9000/coreboard-attachments/cat.png", "image/png", 5L);
        ReflectionTestUtils.setField(saved, "id", 1L);

        given(attachmentRepository.save(any())).willReturn(saved);

        Long id = attachmentService.upload(file);

        assertThat(id).isEqualTo(1L);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(attachmentRepository).save(any());
    }

    @Test
    @DisplayName("스케줄러 실행 시 24시간 지난 TEMP 파일이 삭제된다")
    void deleteOrphanFiles_deletesOldTempFiles() {
        setUp();
        Attachment orphan = Attachment.createTemp(
                "old.png",
                "http://localhost:9000/coreboard-attachments/uuid_old.png",
                "image/png",
                10L
        );
        given(attachmentRepository.findByStatusAndCreatedAtBefore(
                eq(AttachmentStatus.TEMP),
                any(LocalDateTime.class)
        )).willReturn(List.of(orphan));

        attachmentService.deleteOrphanFiles();

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        verify(attachmentRepository).delete(orphan);
    }

    @Test
    @DisplayName("confirm 호출 시 첨부파일 상태가 CONFIRMED로 변경된다")
    void confirm_changesStatusToConfirmed() {
        Attachment attachment = Attachment.createTemp("cat.png", "http://url", "image/png", 5L);
        ReflectionTestUtils.setField(attachment, "id", 1L);
        given(attachmentRepository.findAllById(List.of(1L))).willReturn(List.of(attachment));

        attachmentService.confirm(List.of(1L), null);

        assertThat(attachment.getStatus()).isEqualTo(AttachmentStatus.CONFIRMED);
    }
}