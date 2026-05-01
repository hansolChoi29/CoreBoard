package com.example.coreboard.domain.admin.controller;

import com.example.coreboard.domain.admin.dto.AdminPatchDto;
import com.example.coreboard.domain.admin.dto.command.AdminPatchCommand;
import com.example.coreboard.domain.admin.dto.query.AdminUserListQuery;
import com.example.coreboard.domain.admin.dto.response.AdminGetResponse;
import com.example.coreboard.domain.admin.service.AdminService;
import com.example.coreboard.domain.auth.dto.SignUpDto;
import com.example.coreboard.domain.auth.dto.command.SignUpCommand;
import com.example.coreboard.domain.auth.dto.request.SignUpRequest;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.common.response.PageInfo;
import com.example.coreboard.domain.support.fixture.MockMvcSupport;
import com.example.coreboard.domain.users.entity.UserRole;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    @Mock
    AdminService adminService;

    @InjectMocks
    AdminController adminController;

    MockMvc mockMvc;
    MockMvc mockMvcWithInterceptor;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcSupport.create(adminController);
        mockMvcWithInterceptor = MockMvcSupport.createWithInterceptor(adminController);
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("관리자_setup_성공")
    void setup_success() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "admin01",
                "관리자",
                "password123!",
                "password123!",
                "admin@test.com",
                "01012345678",
                UserRole.ADMIN
        );

        given(adminService.adminSetup(any(SignUpCommand.class)))
                .willReturn(new SignUpDto("admin01", UserRole.ADMIN));

        mockMvc.perform(post("/admin/setup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입 성공"))
                .andExpect(jsonPath("$.data.username").value("admin01"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        ArgumentCaptor<SignUpCommand> captor = ArgumentCaptor.forClass(SignUpCommand.class);
        verify(adminService).adminSetup(captor.capture());

        SignUpCommand command = captor.getValue();
        assertThat(command.username()).isEqualTo("admin01");
        assertThat(command.nickname()).isEqualTo("관리자");
        assertThat(command.password()).isEqualTo("password123!");
        assertThat(command.confirmPassword()).isEqualTo("password123!");
        assertThat(command.email()).isEqualTo("admin@test.com");
        assertThat(command.phoneNumber()).isEqualTo("01012345678");

        verifyNoMoreInteractions(adminService);
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
        given(adminService.get(any(AdminUserListQuery.class)))
                .willReturn(response);
        mockMvc.perform(
                        get("/admin/users")
                                .requestAttr("username", username)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "userId,desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공적으로 관리자 목록을 불러왔습니다."))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content[0]").exists());

        ArgumentCaptor<AdminUserListQuery> queryCaptor =
                ArgumentCaptor.forClass(AdminUserListQuery.class);

        verify(adminService).get(queryCaptor.capture());
        verifyNoMoreInteractions(adminService);

        AdminUserListQuery query = queryCaptor.getValue();

        assertThat(query.role()).isEqualTo(UserRole.ADMIN);
        assertThat(query.username()).isEqualTo(username);

        Pageable pageable = query.pageable();

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(10);

        Sort.Order order = pageable.getSort().getOrderFor("userId");

        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("관리자권한_부여")
    void updateAdmin() throws Exception {
        Long userId = 1L;
        String username = "username";
        UserRole role = UserRole.ADMIN;

        AdminPatchDto result = new AdminPatchDto(userId, role, username);

        given(adminService.promote(any(AdminPatchCommand.class)))
                .willReturn(result);

        mockMvc.perform(
                        patch("/admin/users/{id}/role", userId)
                                .requestAttr("username", username)
                                .param("role", role.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 권한이 변경되었습니다."))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("username"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
        ArgumentCaptor<AdminPatchCommand> commandCaptor = ArgumentCaptor.forClass(AdminPatchCommand.class);

        verify(adminService).promote(commandCaptor.capture());

        AdminPatchCommand command = commandCaptor.getValue();

        assertThat(command.id()).isEqualTo(userId);
        assertThat(command.role()).isEqualTo(UserRole.ADMIN);
    }
}