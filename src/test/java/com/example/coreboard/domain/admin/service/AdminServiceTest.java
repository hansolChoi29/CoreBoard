package com.example.coreboard.domain.admin.service;

import com.example.coreboard.domain.admin.dto.AdminPatchDto;
import com.example.coreboard.domain.admin.dto.command.AdminPatchCommand;
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
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


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

        given(usersRepository.countByRole(UserRole.ADMIN)).willReturn(0L);
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

        verify(usersRepository).countByRole(UserRole.ADMIN);
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

        given(usersRepository.countByRole(UserRole.ADMIN)).willReturn(0L);
        given(usersRepository.existsByUsername("admin01")).willReturn(true);

        assertThatThrownBy(() -> adminService.adminSetup(command))
                .isInstanceOf(AuthErrorException.class);

        verify(usersRepository).countByRole(UserRole.ADMIN);
        verify(usersRepository).existsByUsername("admin01");
        verify(usersRepository, never()).save(any(Users.class));
        verifyNoMoreInteractions(usersRepository, passwordManager, emailPhoneNumberManager);
    }

    @Test
    @DisplayName("관리자_전체_조회_성공")
    void getAdmin() {
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
                adminService.get(query);

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

    @Test
    @DisplayName("사용자_권한_변경_성공")
    void promoteToAdmin() {
        Long userId = 1L;
        Users user = new Users(
                "username",
                "nickname",
                "password",
                "qwr@qwe.com",
                "01012341234",
                UserRole.USER
        );

        given(usersRepository.findById(userId))
                .willReturn(Optional.of(user));

        AdminPatchDto result = adminService.promote(new AdminPatchCommand(userId, UserRole.ADMIN));
        assertEquals(UserRole.ADMIN, result.role());
        verify(usersRepository).findById(userId);
    }

    @Test
    @DisplayName("관리자가_아니면_관리자_목록_조회에_실패한다")
    void getAdmin_forbidden() {
        Users user = new Users(
                "username",
                "nickname",
                "password",
                "user@test.com",
                "01012341234",
                UserRole.USER
        );

        Pageable pageable = PageRequest.of(0, 20);

        AdminUserListQuery query = new AdminUserListQuery(
                UserRole.ADMIN,
                pageable,
                "username"
        );

        given(usersRepository.findByUsername("username"))
                .willReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.get(query))
                .isInstanceOf(AuthErrorException.class);

        verify(usersRepository).findByUsername("username");
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("마지막_ADMIN은_USER로_전환할_수_없다")
    void promoteToAdmin_no_last_user() {
        Users admin = new Users(
                "admin01",
                "관리자",
                "password",
                "admin@test.com",
                "01012345678",
                UserRole.ADMIN
        );

        AdminPatchCommand command = new AdminPatchCommand(1L, UserRole.USER);

        given(usersRepository.findById(1L)).willReturn(Optional.of(admin));

        given(usersRepository.countByRole(UserRole.ADMIN)).willReturn(1L);
        AuthErrorException exception = assertThrows(AuthErrorException.class,
                () -> adminService.promote(command));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(usersRepository).findById(1L);
        verify(usersRepository).countByRole(UserRole.ADMIN);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("ADMIN이_2명_이상이면_ADMIN을_USER로_전환할_수_있다")
    void promoteToAdmin_2admin_no() {
        Users admin = new Users(
                "admin01",
                "관리자",
                "password",
                "admin@test.com",
                "01012345678",
                UserRole.ADMIN
        );

        AdminPatchCommand command = new AdminPatchCommand(1L, UserRole.USER);

        given(usersRepository.findById(1L)).willReturn(Optional.of(admin));
        given(usersRepository.countByRole(UserRole.ADMIN)).willReturn(2L);

        AdminPatchDto result = adminService.promote(command);

        assertEquals(UserRole.USER, result.role());

        verify(usersRepository).findById(1L);
        verify(usersRepository).countByRole(UserRole.ADMIN);
        verify(usersRepository).save(admin);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("요청_role이_ADMIN이면_마지막_ADMIN_검사를_하지_않는다")
    void promoteToAdmin_no_error() {
        Users admin = new Users(
                "admin01",
                "관리자",
                "password",
                "admin@test.com",
                "01012345678",
                UserRole.ADMIN
        );

        AdminPatchCommand command = new AdminPatchCommand(1L, UserRole.ADMIN);

        given(usersRepository.findById(1L)).willReturn(Optional.of(admin));

        AdminPatchDto result = adminService.promote(command);

        assertEquals(UserRole.ADMIN, result.role());

        verify(usersRepository).findById(1L);
        verify(usersRepository, never()).countByRole(UserRole.ADMIN);
        verify(usersRepository).save(admin);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("대상_사용자가_USER이면_마지막_ADMIN_검사를_하지_않는다")
    void changeUserRole_doesNotCheckLastAdminWhenTargetUserIsUser() {
        Users user = new Users(
                "user01",
                "일반사용자",
                "password",
                "user@test.com",
                "01012345678",
                UserRole.USER
        );

        AdminPatchCommand command = new AdminPatchCommand(1L, UserRole.USER);

        given(usersRepository.findById(1L)).willReturn(Optional.of(user));

        AdminPatchDto result = adminService.promote(command);

        assertEquals(UserRole.USER, result.role());

        verify(usersRepository).findById(1L);
        verify(usersRepository, never()).countByRole(UserRole.ADMIN);
        verify(usersRepository).save(user);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("ADMIN이_이미_존재하면_setup에_실패한다")
    void adminSetup_fail_adminAlreadyExists() {
        SignUpCommand command = new SignUpCommand(
                "admin02",
                "관리자2",
                "password123!",
                "password123!",
                "admin2@test.com",
                "01098765432"
        );

        given(usersRepository.countByRole(UserRole.ADMIN)).willReturn(1L);

        AuthErrorException exception = assertThrows(AuthErrorException.class,
                () -> adminService.adminSetup(command));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(usersRepository).countByRole(UserRole.ADMIN);
        verify(usersRepository, never()).existsByUsername(anyString());
        verify(usersRepository, never()).save(any(Users.class));
        verifyNoMoreInteractions(usersRepository, passwordManager, emailPhoneNumberManager);
    }
}