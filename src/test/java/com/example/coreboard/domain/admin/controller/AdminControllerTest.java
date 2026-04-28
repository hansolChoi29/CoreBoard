package com.example.coreboard.domain.admin.controller;

import com.example.coreboard.domain.admin.dto.response.AdminGetResponse;
import com.example.coreboard.domain.admin.service.AdminService;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.common.response.PageInfo;
import com.example.coreboard.domain.support.fixture.MockMvcSupport;
import com.example.coreboard.domain.users.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    AdminService adminService;

    @InjectMocks
    AdminController adminController;


    MockMvc mockMvc;
    MockMvc mockMvcWithInterceptor;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcSupport.create(adminController);
        mockMvcWithInterceptor = MockMvcSupport.createWithInterceptor(adminController);
    }

    @Test
    @DisplayName("관리자_전체_조회")
    void getAdmins() throws Exception {
        String username = "admin";
        List<AdminGetResponse> admin = List.of(new AdminGetResponse(1L, username, UserRole.USER));
        OffsetPageResponse<AdminGetResponse> response = new OffsetPageResponse<>(
                admin,
                new PageInfo(1, 10, 11, 2)
        );
        given(adminService.get(any(Pageable.class), eq(UserRole.ADMIN), eq(username)))
                .willReturn(response);
        mockMvc.perform(
                        get("/admin/users")
                                .requestAttr("username", username)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "id,desc")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공적으로 관리자 목록을 불러왔습니다."))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content[0]").exists());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(adminService).get(
                pageableCaptor.capture(),
                eq(UserRole.ADMIN),
                eq(username)
        );
        verifyNoMoreInteractions(adminService);

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(10);

        Sort.Order order = pageable.getSort().getOrderFor("id");

        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}