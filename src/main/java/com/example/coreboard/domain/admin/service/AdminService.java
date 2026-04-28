package com.example.coreboard.domain.admin.service;

import com.example.coreboard.domain.admin.dto.response.AdminGetResponse;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.common.response.PageInfo;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AdminService {
    private final UsersRepository usersRepository;

    public AdminService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Transactional(readOnly = true)
    public OffsetPageResponse<AdminGetResponse> get(
            Pageable pageable,
            UserRole role,
            String username
    ) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.NOT_FOUND));


        if (user.getRole() != UserRole.ADMIN) {
            throw new AuthErrorException(AuthErrorCode.FORBIDDEN);
        }
        Page<Users> admin = usersRepository.findByRole(role, pageable);

        List<AdminGetResponse> contents = admin.getContent().stream()
                .map(users -> new AdminGetResponse(
                        users.getUserId(),
                        users.getUsername(),
                        users.getRole()
                )).toList();

        PageInfo pageInfo = new PageInfo(
                admin.getNumber(),
                admin.getSize(),
                admin.getTotalElements(),
                admin.getTotalPages()
        );
        return new OffsetPageResponse<>(contents, pageInfo);
    }
}
