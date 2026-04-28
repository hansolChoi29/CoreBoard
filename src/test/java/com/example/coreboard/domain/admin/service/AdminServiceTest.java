package com.example.coreboard.domain.admin.service;

import com.example.coreboard.domain.admin.dto.response.AdminGetResponse;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock
    UsersRepository usersRepository;

    @InjectMocks
    AdminService adminService;

    @Test
    @DisplayName("관리자_전체_조회")
    void adminCreate() {
        String username = "admin";

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Direction.DESC, "id")
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
                adminService.get(pageable, UserRole.ADMIN, username);

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

}