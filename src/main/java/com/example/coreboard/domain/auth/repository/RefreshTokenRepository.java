package com.example.coreboard.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository <RefreshTokenRepository, Long>{

    Optional<RefreshTokenRepository> findByUserId(Long userId);
}
