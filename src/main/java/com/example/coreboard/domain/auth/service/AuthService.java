package com.example.coreboard.domain.auth.service;

import com.example.coreboard.domain.auth.dto.SignInRequest;
import com.example.coreboard.domain.auth.dto.SignUpRequest;
import com.example.coreboard.domain.auth.dto.SignUpResponse;
import com.example.coreboard.domain.auth.dto.TokenResponse;
import com.example.coreboard.domain.auth.repository.AuthRepository;
import com.example.coreboard.domain.common.config.PasswordEncode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.*;


@Service
public class AuthService {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final PasswordEncode passwordEncode;

    public AuthService(AuthRepository authRepository, PasswordEncode passwordEncode,UserRepository userRepository){
        this.authRepository=authRepository;
        this.passwordEncode=passwordEncode;
        this.userRepository=userRepository;
    }


   @Transactional
    public SignUpResponse signup(SignUpRequest signupRequest){
        // 트러블: getter인데 파라미터를 넣어서 에러났었음
       //signupRequest.getUsername(username)
        if(authRepository.existsByUsername(signupRequest.getUsername())){
            throw new AuthErrorException(CONFLICT);
        }
        String encodePassword = passwordEncode.encrypt(signupRequest.getPassword());

        Users newUser=new Users(
                signupRequest.getUsername(),
                encodePassword
        );
        authRepository.save(newUser);

        return new SignUpResponse(newUser.getUsername(),"가입 성공!");
   }

   @Transactional
    public ResponseEntity<TokenResponse> signin(@RequestBody SignInRequest signinRequest){
       String username = signinRequest.getUsername();
       String password = signinRequest.getPassword();
//
//        Users user=userRepository.findByUsername(signinRequest.getUsername()).orElseThrow(()-> new AuthErrorException("회원가입을 먼저 해주세요!"));

//       TokenResponse tokenResponse = new TokenResponse(accessToken,refreshToken);
//       return ResponseEntity.ok(tokenResponse);
   }

}
