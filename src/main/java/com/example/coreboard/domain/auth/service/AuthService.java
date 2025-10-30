package com.example.coreboard.domain.auth.service;

import com.example.coreboard.domain.auth.dto.*;
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

    // 회원가입
    public SignUpDto signUp(SignUpCommand signUpCommand) { // AuthResponse :  객체 형태로 반환한다는 의미
        // 1) 비밀번호 확인
        if (signUpCommand.getPassword() == null || !signUpCommand.getPassword().equals(signUpCommand.getConfirmPassword())) {
            throw new AuthErrorException(PASSWORD_CONFIRM_MISMATCH); // 비밀번호 확인 불일치
        }

        // 2) 아이디 중복 체크
        if (usersRepository.existsByUsername(signUpCommand.getUsername())) {
            throw new AuthErrorException(CONFLICT); // 409: 이미 존재
        }

        // 3) 암호화
        String encodedPassword = passwordEncoder.encrypt(signUpCommand.getPassword());
        String encryptedEmail = emailPhoneNumberEncode.encrypt(signUpCommand.getEmail());
        String encryptPhoneNubmer = emailPhoneNumberEncode.encrypt(signUpCommand.getPhoneNumber());

        // 4) 저장 - 트러블 : createUsers와 순서 맞춰야 함
        Users users = Users.createUsers(
                signUpCommand.getUsername(), // 사용자의 아이디,
                encodedPassword,             // 사용자의 암호화된 비밀번호를
                encryptedEmail,              // 사용자의 암호화된 이메일
                encryptPhoneNubmer           // 사용자의 암호화된 전화번호
        );
        Users user = usersRepository.save(users); // DB에 저장 및 응답
        return new SignUpDto(
                user.getUsername()

        );
    }

    public TokenDto signIn(SignInCommand authSignInCommand) {
        // 사용자 조회
        Users users =
                usersRepository.findByUsername(authSignInCommand.getUsername()).orElseThrow(() -> new AuthErrorException(NOT_FOUND));  // Optional 객체에서 값을 꺼내 오는 메서드(값이 존재하는 경우 해당 값
        // 반환, 없는 경우 예외 발생)
        // 비밀번호 검증
        if (!passwordEncoder.matches(authSignInCommand.getPassword(), users.getPassword())) {
            throw new AuthErrorException(UNAUTHORIZED);
        }
        // 토큰 발급
        String accessToken = JwtUtil.createAccessToken(users.getUserId(), users.getUsername());
        String refreshToken = JwtUtil.createRefreshToken(users.getUserId(), users.getUsername());

        // 토큰 반환
        return new TokenDto(accessToken, refreshToken);
    }
}
