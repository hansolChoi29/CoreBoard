package com.example.coreboard.domain.admin.service;

import com.example.coreboard.domain.admin.dto.AdminPatchDto;
import com.example.coreboard.domain.admin.dto.command.AdminPatchCommand;
import com.example.coreboard.domain.admin.dto.query.AdminUserListQuery;
import com.example.coreboard.domain.admin.dto.response.AdminGetResponse;
import com.example.coreboard.domain.auth.dto.SignUpDto;
import com.example.coreboard.domain.auth.dto.command.SignUpCommand;
import com.example.coreboard.domain.common.config.EmailPhoneNumberManager;
import com.example.coreboard.domain.common.config.PasswordManager;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.common.response.PageInfo;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final UsersRepository usersRepository;
    private final PasswordManager passwordEncoder;
    private final EmailPhoneNumberManager emailPhoneNumberEncode;

    public AdminService(
            PasswordManager passwordEncoder,
            UsersRepository usersRepository,
            EmailPhoneNumberManager emailPhoneNumberEncode
    ) {
        this.passwordEncoder = passwordEncoder;
        this.usersRepository = usersRepository;
        this.emailPhoneNumberEncode = emailPhoneNumberEncode;
    }

    @Transactional
    public SignUpDto adminSetup(SignUpCommand command) {
        if (usersRepository.countByRole(UserRole.ADMIN) > 0) {
            throw new AuthErrorException(AuthErrorCode.ADMIN_ALREADY_EXISTS);
        }
        
        if (usersRepository.existsByUsername(command.username())) {
            throw new AuthErrorException(AuthErrorCode.CONFLICT);
        }

        String encodedPassword = passwordEncoder.encrypt(command.password());
        String encryptedEmail = emailPhoneNumberEncode.encrypt(command.email());
        String encryptPhoneNubmer = emailPhoneNumberEncode.encrypt(command.phoneNumber());

        Users user = Users.createAdmin(
                command.username(),
                command.nickname(),
                encodedPassword,
                encryptedEmail,
                encryptPhoneNubmer
        );
        usersRepository.save(user);
        return new SignUpDto(user.getUsername(), user.getRole());
    }

    @Transactional(readOnly = true)
    public OffsetPageResponse<AdminGetResponse> getAdmins(AdminUserListQuery query) {
        Users user = usersRepository.findByUsername(query.username())
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.ADMIN_REQUESTER_NOT_FOUND));

        if (user.getRole() != UserRole.ADMIN) {
            throw new AuthErrorException(AuthErrorCode.ADMIN_PERMISSION_REQUIRED);
        }
        Page<Users> admin = usersRepository.findByRole(query.role(), query.pageable());

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

    @Transactional
    public AdminPatchDto promoteToAdmin(AdminPatchCommand command) {

        Users user = (usersRepository.findById(command.id()))
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.TARGET_USER_NOT_FOUND));

        if (user.getRole() == UserRole.ADMIN &&
                command.role() == UserRole.USER &&
                usersRepository.countByRole(UserRole.ADMIN) <= 1) {
            throw new AuthErrorException(AuthErrorCode.LAST_ADMIN_CANNOT_BE_DEMOTED);
        }

        user.promoteToAdmin(command.role());
        usersRepository.save(user);
        return new AdminPatchDto(user.getUserId(), user.getRole(), user.getUsername());
    }
}
