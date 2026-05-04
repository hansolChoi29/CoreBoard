package com.example.coreboard.domain.attachment.controller;

import com.example.coreboard.domain.attachment.service.AttachmentService;
import com.example.coreboard.domain.support.fixture.MockMvcSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AttachmentControllerTest {
    private MockMvc mockMvc;
    private AttachmentService attachmentService;

    @BeforeEach
    void setUp() {
        attachmentService = mock(AttachmentService.class);
        mockMvc = MockMvcSupport.create(new AttachmentController(attachmentService));
    }

    @Test
    @DisplayName("파일업로드_200OK_attachment_ID_반환")
    void upload_returnsAttachmentId() throws Exception {
        given(attachmentService.upload(any())).willReturn(1L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "dummy".getBytes()
        );

        mockMvc.perform(multipart("/attachment").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1L))
                .andExpect(jsonPath("$.message").value("파일 업로드 성공"));
    }
}