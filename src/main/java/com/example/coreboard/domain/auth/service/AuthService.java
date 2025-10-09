package com.example.coreboard.domain.auth.service;

import com.example.coreboard.domain.auth.dto.SignUpRequest;
import com.example.coreboard.domain.auth.dto.SignUpResponse;
import com.example.coreboard.domain.common.config.PasswordEncode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.stereotype.Service;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.*;


@Service
public class AuthService {
    private final UsersRepository usersRepository;
    private final PasswordEncode passwordEncoder;

    public AuthService(PasswordEncode passwordEncoder, UsersRepository usersRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usersRepository = usersRepository;
    }
//
//    // 회원가입
//    public SignUpResponse signup(SignUpRequest signUpRequest) {
//        // 1) 비밀번호 확인
//        if (signUpRequest.getPassword() == null ||
//                !signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
//            throw new AuthErrorException(PASSWORD_CONFIRM_MISMATCH); // 비밀번호 확인 불일치
//        }
//
//        // 2) 아이디 중복 체크
//        if (usersRepository.existsByUsername(signUpRequest.getUsername())) {
//            throw new AuthErrorException(CONFLICT); // 409: 이미 존재
//        }
//
//        // 3) salt 생성 + 해시
//        byte[] salt = passwordEncoder.generateSalt();
//        String hashBase64 = passwordEncoder.encrypt(signUpRequest.getPassword(), salt);
//        String saltBase64 = passwordEncoder.toBase64(salt);
//
//        // 4) 저장
//        Users users = Users.createUsers(
//                signUpRequest.getUsername(),
//                hashBase64,
//                signUpRequest.getEmail(),
//                signUpRequest.getPhoneNumber(),
//                saltBase64
//        );
//        usersRepository.save(users);
//
//        // 5) 응답
//        return new SignUpResponse(users.getUsername(), "회원가입이 완료되었습니다.");
//    }


}
