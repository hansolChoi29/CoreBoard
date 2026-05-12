package com.example.coreboard.domain.attachment.service;

import com.example.coreboard.domain.attachment.entity.Attachment;
import com.example.coreboard.domain.attachment.entity.AttachmentStatus;
import com.example.coreboard.domain.attachment.repository.AttachmentRepository;
import com.example.coreboard.domain.common.exception.Attachment.AttachmentErrorCode;
import com.example.coreboard.domain.common.exception.Attachment.AttachmentErrorException;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Mock
    private UsersRepository usersRepository;

    void setUp() {
        ReflectionTestUtils.setField(attachmentService, "bucket", "coreboard-attachments");
        ReflectionTestUtils.setField(attachmentService, "publicUrl", "http://localhost:9000/coreboard-attachments");
    }

    @Test
    @DisplayName("파일_업로드_시_TEMP_상태_저장")
    void upload_savesAsTempStatus() throws IOException {
        String username = "username";
        setUp();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cat.png",
                "image/png",
                "dummy".getBytes()
        );
        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );
        Attachment saved = Attachment.createTemp(
                user,
                "cat.png",
                "fdsfa",
                "http://localhost:9000/coreboard-attachments/cat.png",
                "image/png",
                5L
        );
        ReflectionTestUtils.setField(saved, "id", 1L);

        given(attachmentRepository.save(any())).willReturn(saved);
        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        Long id = attachmentService.upload(username, file);

        assertThat(id).isEqualTo(1L);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(attachmentRepository).save(any());
    }

    @Test
    @DisplayName("스케줄러_실행_시_24시간_지난_TEMP_고아파일을_삭제")
    void deleteOrphanFiles_deletesOldTempFiles() {
        String username = "username";
        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );

        setUp();
        Attachment orphan = Attachment.createTemp(
                user,
                "old.png",
                "fdsfa",
                "http://localhost:9000/coreboard-attachments/uuid_old.png",
                "image/png",
                10L
        );
        given(attachmentRepository.findByStatusAndCreatedAtBefore(
                eq(AttachmentStatus.TEMP),
                any(LocalDateTime.class)
        )).willReturn(List.of(orphan));

        attachmentService.cleanupAttachments();

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        verify(attachmentRepository).delete(orphan);
    }

    @Test
    @DisplayName("confirm_호출_시_첨부파일_상태가_CONFIRMED_변경")
    void confirm_changesStatusToConfirmed() {
        String username = "username";
        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(user, "userId", 1L);

        Attachment attachment = Attachment.createTemp(
                user,
                "cat.png",
                "fdsfa",
                "http://url",
                "image/png",
                5L
        );
        ReflectionTestUtils.setField(attachment, "id", 1L);

        given(attachmentRepository.findAllById(List.of(1L))).willReturn(List.of(attachment));

        attachmentService.confirm(List.of(1L), null, user);

        assertThat(attachment.getStatus()).isEqualTo(AttachmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("첨부파일_확정_attachmentIds가_null이면_조회하지_않는다")
    void confirmAttachmentIdsNull() {
        Users user = mock(Users.class);
        Post post = mock(Post.class);

        attachmentService.confirm(null, post, user);

        verify(attachmentRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("첨부파일_확정_attachmentIds가_비어있으면_조회하지_않는다")
    void confirmAttachmentIdsEmpty() {
        Users user = mock(Users.class);
        Post post = mock(Post.class);

        attachmentService.confirm(List.of(), post, user);

        verify(attachmentRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("존재하지_않는_첨부파일_ID가_있으면_예외")
    void confirmAttachmentNotFound() {
        Users user = mock(Users.class);
        Post post = mock(Post.class);

        given(attachmentRepository.findAllById(List.of(99999L))).willReturn(List.of());

        assertThatThrownBy(() -> attachmentService.confirm(List.of(99999L), post, user))
                .isInstanceOfSatisfying(AttachmentErrorException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(404);
                    assertThat(e.getMessage()).isEqualTo("존재하지 않는 첨부파일입니다.");
                });
    }

    @Test
    @DisplayName("이미_게시글에_연결된_첨부파일이면_예외")
    void confirmAlreadyConfirmedAttachment() {
        Users user = mock(Users.class);
        given(user.getUserId()).willReturn(1L);

        Post oldPost = mock(Post.class);
        Post newPost = mock(Post.class);

        Attachment attachment = Attachment.createTemp(
                user,
                "cat.png",
                "attachments/temp/cat.png",
                "http://localhost:9000/coreboard-attachments/attachments/temp/cat.png",
                "image/png",
                5L
        );
        attachment.confirm(oldPost);

        given(attachmentRepository.findAllById(List.of(1L))).willReturn(List.of(attachment));

        assertThatThrownBy(() -> attachmentService.confirm(List.of(1L), newPost, user))
                .isInstanceOfSatisfying(AttachmentErrorException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(409);
                    assertThat(e.getMessage()).isEqualTo("이미 게시글에 연결된 첨부파일입니다.");
                });
    }

    @Test
    @DisplayName("다른_사용자가_업로드한_첨부파일이면_예외")
    void confirmForbiddenWhenOwnerIsDifferent() {
        Users owner = mock(Users.class);
        given(owner.getUserId()).willReturn(1L);

        Users otherUser = mock(Users.class);
        given(otherUser.getUserId()).willReturn(2L);

        Post post = mock(Post.class);

        Attachment attachment = Attachment.createTemp(
                owner,
                "cat.png",
                "attachments/temp/cat.png",
                "http://localhost:9000/coreboard-attachments/attachments/temp/cat.png",
                "image/png",
                5L
        );

        given(attachmentRepository.findAllById(List.of(1L))).willReturn(List.of(attachment));

        assertThatThrownBy(() -> attachmentService.confirm(List.of(1L), post, otherUser))
                .isInstanceOfSatisfying(AttachmentErrorException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(403);
                    assertThat(e.getMessage()).isEqualTo("해당 첨부파일에 접근할 권한이 없습니다.");
                });
    }

    @Test
    @DisplayName("파일_업로드_시_10MB를_초과하면_예외")
    void upload_fileSizeExceeded() {
        String username = "username";
        setUp();

        byte[] over10MB = new byte[10 * 1024 * 1024 + 1];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.png",
                "image/png",
                over10MB
        );

        assertThatThrownBy(() -> attachmentService.upload(username, file))
                .isInstanceOfSatisfying(AttachmentErrorException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(413);
                    assertThat(e.getMessage()).isEqualTo("첨부파일 크기가 허용된 최대 용량을 초과했습니다.");
                });

        verify(usersRepository, never()).findByUsername(anyString());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_첨부파일_수정_keepAttachmentIds가_null이면_기존_첨부파일을_모두_유지")
    void updatePostAttachmentsKeepAttachmentIdsNullKeepsAllCurrentAttachments() {
        Users user = new Users(
                "username",
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(user, "userId", 1L);

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);

        Attachment attachment = Attachment.createTemp(
                user,
                "cat.png",
                "attachments/temp/cat.png",
                "http://url",
                "image/png",
                5L
        );
        ReflectionTestUtils.setField(attachment, "id", 10L);

        given(attachmentRepository.findByPostIdAndStatus(1L, AttachmentStatus.CONFIRMED)).willReturn(List.of(attachment));

        attachmentService.updatePostAttachments(
                post,
                user,
                null,
                List.of()
        );

        assertThat(attachment.getStatus()).isEqualTo(AttachmentStatus.TEMP);

        verify(attachmentRepository).findByPostIdAndStatus(1L, AttachmentStatus.CONFIRMED);
        verify(attachmentRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("게시글_첨부파일_수정_keepAttachmentIds에_현재_첨부파일이_아닌_ID가_있으면_예외")
    void updatePostAttachmentsInvalidKeepAttachmentId() {
        Users user = new Users(
                "username",
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(user, "userId", 1L);

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);

        Attachment currentAttachment = Attachment.createTemp(
                user,
                "cat.png",
                "attachments/temp/cat.png",
                "http://url",
                "image/png",
                5L
        );
        ReflectionTestUtils.setField(currentAttachment, "id", 10L);

        given(attachmentRepository.findByPostIdAndStatus(1L, AttachmentStatus.CONFIRMED)).willReturn(List.of(currentAttachment));

        assertThatThrownBy(() -> attachmentService.updatePostAttachments(
                post,
                user,
                List.of(999L),
                List.of()
        ))
                .isInstanceOfSatisfying(AttachmentErrorException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(404);
                    assertThat(e.getMessage()).isEqualTo("존재하지 않는 첨부파일입니다.");
                });

        verify(attachmentRepository).findByPostIdAndStatus(1L, AttachmentStatus.CONFIRMED);
        verify(attachmentRepository, never()).findAllById(any());
    }


    @Test
    @DisplayName("게시글_첨부파일_수정_keepAttachmentIds에_없는_기존_첨부파일은_DELETED_처리")
    void updatePostAttachmentsMarksRemovedAttachmentDeleted() {
        Users user = new Users(
                "username",
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(user, "userId", 1L);

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);

        Attachment keepAttachment = Attachment.createTemp(
                user,
                "keep.png",
                "attachments/temp/keep.png",
                "http://url/keep.png",
                "image/png",
                5L
        );
        ReflectionTestUtils.setField(keepAttachment, "id", 10L);
        keepAttachment.confirm(post);

        Attachment removeAttachment = Attachment.createTemp(
                user,
                "remove.png",
                "attachments/temp/remove.png",
                "http://url/remove.png",
                "image/png",
                5L
        );
        ReflectionTestUtils.setField(removeAttachment, "id", 20L);
        removeAttachment.confirm(post);

        given(attachmentRepository.findByPostIdAndStatus(1L, AttachmentStatus.CONFIRMED)).willReturn(List.of(keepAttachment, removeAttachment));

        attachmentService.updatePostAttachments(
                post,
                user,
                List.of(10L),
                List.of()
        );

        assertEquals(AttachmentStatus.CONFIRMED, keepAttachment.getStatus());
        assertEquals(AttachmentStatus.DELETED, removeAttachment.getStatus());

        verify(attachmentRepository).findByPostIdAndStatus(1L, AttachmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("스케줄러_실행_시_7일_지난_DELETED_첨부파일을_스토리지와_DB에서_삭제한다")
    void cleanupAttachmentsDeletesOldDeletedFiles() {
        setUp();

        Users user = new Users(
                "username",
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );

        Attachment deletedAttachment = Attachment.createTemp(
                user,
                "deleted.png",
                "attachments/temp/deleted.png",
                "http://localhost:9000/coreboard-attachments/attachments/temp/deleted.png",
                "image/png",
                10L
        );
        deletedAttachment.markDeleted();

        given(attachmentRepository.findByStatusAndCreatedAtBefore(
                eq(AttachmentStatus.TEMP),
                any(LocalDateTime.class)
        )).willReturn(List.of());

        given(attachmentRepository.findByStatusAndDeletedAtBefore(
                eq(AttachmentStatus.DELETED),
                any(LocalDateTime.class)
        )).willReturn(List.of(deletedAttachment));

        attachmentService.cleanupAttachments();

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        verify(attachmentRepository).delete(deletedAttachment);
    }
}