package com.example.coreboard.domain.admin.service;

import com.example.coreboard.domain.admin.dto.query.AdminUserListQuery;
import com.example.coreboard.domain.admin.dto.response.AdminGetResponse;
import com.example.coreboard.domain.auth.dto.SignUpDto;
import com.example.coreboard.domain.auth.dto.command.SignUpCommand;
import com.example.coreboard.domain.common.config.EmailPhoneNumberManager;
import com.example.coreboard.domain.common.config.PasswordManager;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock
    UsersRepository usersRepository;

    @InjectMocks
    AdminService adminService;

    @Mock
    PasswordManager passwordManager;

    @Mock
    EmailPhoneNumberManager emailPhoneNumberManager;

    @Test
    @DisplayName("관리자_setup_성공")
    void adminSetup_success() {
        SignUpCommand command = new SignUpCommand(
                "admin01",
                "관리자",
                "password123!",
                "password123!",
                "admin@test.com",
                "01012345678"
        );

        given(usersRepository.existsByUsername("admin01")).willReturn(false);
        given(passwordManager.encrypt("password123!")).willReturn("encodedPassword");
        given(emailPhoneNumberManager.encrypt("admin@test.com")).willReturn("encryptedEmail");
        given(emailPhoneNumberManager.encrypt("01012345678")).willReturn("encryptedPhoneNumber");

        SignUpDto result = adminService.adminSetup(command);

        assertThat(result.username()).isEqualTo("admin01");
        assertThat(result.role()).isEqualTo(UserRole.ADMIN);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(usersRepository).save(captor.capture());

        Users savedUser = captor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("admin01");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.ADMIN);

        verify(usersRepository).existsByUsername("admin01");
        verify(passwordManager).encrypt("password123!");
        verify(emailPhoneNumberManager).encrypt("admin@test.com");
        verify(emailPhoneNumberManager).encrypt("01012345678");
        verifyNoMoreInteractions(usersRepository, passwordManager, emailPhoneNumberManager);
    }

    @Test
    @DisplayName("관리자_setup_실패_username_중복")
    void adminSetup_fail_duplicateUsername() {
        SignUpCommand command = new SignUpCommand(
                "admin01",
                "관리자",
                "password123!",
                "password123!",
                "admin@test.com",
                "01012345678"
        );

        given(usersRepository.existsByUsername("admin01")).willReturn(true);

        assertThatThrownBy(() -> adminService.adminSetup(command))
                .isInstanceOf(AuthErrorException.class);

        verify(usersRepository).existsByUsername("admin01");
        verifyNoMoreInteractions(usersRepository, passwordManager, emailPhoneNumberManager);
    }


    @Test
    @DisplayName("관리자_전체_조회")
    void createAdmin() {
        String username = "admin";

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Direction.DESC, "id")
        );
        AdminUserListQuery query = new AdminUserListQuery(
                UserRole.ADMIN,
                pageable,
                username
        );
        Users loginAdmin = new Users(
                "admin",
                "password",
                "password",
                "qwe@qwe.com",
                "1234123412",
                UserRole.ADMIN
        );

        Users adminUser = new Users(
                "admin",
                "password",
                "password",
                "qwe@qwe.com",
                "1234123412",
                UserRole.ADMIN
        );

        Page<Users> adminPage = new PageImpl<>(
                List.of(adminUser),
                pageable,
                11
        );

        given(usersRepository.findByUsername(username))
                .willReturn(Optional.of(loginAdmin));

        given(usersRepository.findByRole(UserRole.ADMIN, pageable))
                .willReturn(adminPage);

        OffsetPageResponse<AdminGetResponse> response =
                adminService.getAdmins(query);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).username()).isEqualTo("admin");
        assertThat(response.getContent().get(0).role()).isEqualTo(UserRole.ADMIN);

        assertThat(response.getPageInfo().getPage()).isEqualTo(0);
        assertThat(response.getPageInfo().getSize()).isEqualTo(10);
        assertThat(response.getPageInfo().getTotalElements()).isEqualTo(11);
        assertThat(response.getPageInfo().getTotalPages()).isEqualTo(2);

        verify(usersRepository).findByUsername(username);
        verify(usersRepository).findByRole(UserRole.ADMIN, pageable);
    }

    // TODO: 사용자 권한 변경 ADMIN으로
    // TODO : 요청자 없음
    // TODO : 요청자가 ADMIN이 아님
    // TODO : 이미 ADMIN인 경우
    @Test
    @DisplayName("")
    void updateAdmin(){

    }

}