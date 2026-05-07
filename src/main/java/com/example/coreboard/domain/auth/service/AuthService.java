package com.example.coreboard.domain.auth.service;

import com.example.coreboard.domain.auth.dto.*;
import com.example.coreboard.domain.auth.dto.command.SignInCommand;
import com.example.coreboard.domain.auth.dto.command.SignUpCommand;
import com.example.coreboard.domain.common.config.EmailPhoneNumberManager;
import com.example.coreboard.domain.common.config.PasswordManager;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.util.JwtUtil;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.*;


@Service
public class AuthService {
    private final UsersRepository usersRepository;
    private final PasswordManager passwordEncoder;
    private final EmailPhoneNumberManager emailPhoneNumberEncode;

    public AuthService(
            PasswordManager passwordEncoder,
            UsersRepository usersRepository,
            EmailPhoneNumberManager emailPhoneNumberEncode
    ) {
        this.passwordEncoder = passwordEncoder;
        this.usersRepository = usersRepository;
        this.emailPhoneNumberEncode = emailPhoneNumberEncode;
    }

    @Transactional
    public SignUpDto signUp(SignUpCommand command) {
        if (usersRepository.existsByUsername(command.username())) {
            throw new AuthErrorException(CONFLICT);
        }

        String encodedPassword = passwordEncoder.encrypt(command.password());
        String encryptedEmail = emailPhoneNumberEncode.encrypt(command.email());
        String encryptPhoneNubmer = emailPhoneNumberEncode.encrypt(command.phoneNumber());

        Users users = Users.createUsers(
                command.username(),
                command.nickname(),
                encodedPassword,
                encryptedEmail,
                encryptPhoneNubmer
        );
        usersRepository.save(users);

        return new SignUpDto(users.getUsername(), users.getRole());
    }

    @Transactional(readOnly = true)
    public TokenDto signIn(SignInCommand command) {
        Users users = usersRepository.findByUsername(command.username())
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        if (!passwordEncoder.matches(command.password(), users.getPassword())) {
            throw new AuthErrorException(UNAUTHORIZED);
        }

        String accessToken = JwtUtil.createAccessToken(
                users.getUserId(),
                users.getUsername(),
                users.getRole());
        String refreshToken = JwtUtil.createRefreshToken(
                users.getUserId(),
                users.getUsername(),
                users.getRole());

        return new TokenDto(accessToken, refreshToken);
    }
}
