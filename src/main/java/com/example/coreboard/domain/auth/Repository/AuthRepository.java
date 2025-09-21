package com.example.coreboard.domain.auth.Repository;

import com.example.coreboard.domain.auth.Entity.SignIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository {
    Optional<SignIn> findByUsername(String username);
}
