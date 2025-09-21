package com.example.coreboard.domain.auth.service;

import com.example.coreboard.domain.auth.repository.AuthRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AuthService {
    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository){
        this.authRepository=authRepository;
    }
    public void findByUsername(String username){
        // void는 if 조건 쓸 수 없음
//        if(username!=null){
//             authRepository.findByUsername(username);
//        }
//        else{
//            throw new IllegalArgumentException();
//        }

    }

    //중복확인
    public boolean existsByUsername(String username){
        return authRepository.existsByUsername(username);
    }
}
