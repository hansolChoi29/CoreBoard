package com.example.coreboard.domain.auth.repository;

import com.example.coreboard.domain.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Users, Long> {
    //DB에서 username 찾아서 반환
    // 옵셔널: 값이 없으면 널 대신 빈 반환 NPE방지

    Optional<Users> findByUsername(String username);

    boolean existsByUsername(String username);
}
