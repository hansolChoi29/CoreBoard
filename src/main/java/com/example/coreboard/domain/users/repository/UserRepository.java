package com.example.coreboard.domain.users.repository;

import com.example.coreboard.domain.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository {
    Optional<Users> findByUsername(String username);
}
