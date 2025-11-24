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

    public SignUpDto signUp(SignUpCommand signUpCommand) {
        if (signUpCommand.getPassword() == null || !signUpCommand.getPassword().equals(signUpCommand.getConfirmPassword())) {
            throw new AuthErrorException(PASSWORD_CONFIRM_MISMATCH);
        }

        if (usersRepository.existsByUsername(signUpCommand.getUsername())) {
            throw new AuthErrorException(CONFLICT);
        }

        String encodedPassword = passwordEncoder.encrypt(signUpCommand.getPassword());
        String encryptedEmail = emailPhoneNumberEncode.encrypt(signUpCommand.getEmail());
        String encryptPhoneNubmer = emailPhoneNumberEncode.encrypt(signUpCommand.getPhoneNumber());

        Users users = Users.createUsers(
                signUpCommand.getUsername(),
                encodedPassword,
                encryptedEmail,
                encryptPhoneNubmer
        );
        Users user = usersRepository.save(users);
        return new SignUpDto(
                user.getUsername()

        );
    }

    public TokenDto signIn(SignInCommand authSignInCommand) {
        Users users =
                usersRepository.findByUsername(authSignInCommand.getUsername()).orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        if (!passwordEncoder.matches(authSignInCommand.getPassword(), users.getPassword())) {
            throw new AuthErrorException(UNAUTHORIZED);
        }
        String accessToken = JwtUtil.createAccessToken(users.getUserId(), users.getUsername());
        String refreshToken = JwtUtil.createRefreshToken(users.getUserId(), users.getUsername());

        return new TokenDto(accessToken, refreshToken);
    }
}
