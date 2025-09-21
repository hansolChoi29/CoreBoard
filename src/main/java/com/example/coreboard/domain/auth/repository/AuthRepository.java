package com.example.coreboard.domain.auth.repository;

import com.example.coreboard.domain.auth.entity.SignIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository <SignIn,Long>{
    Optional<SignIn> findByUsername(String username);

    boolean existsByUsername(String username);
}
