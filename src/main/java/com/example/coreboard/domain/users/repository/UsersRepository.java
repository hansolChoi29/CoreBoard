package com.example.coreboard.domain.users.repository;

import com.example.coreboard.domain.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUsername(String username); //사용자 엔티티 자체가 필요할 때 쓰는 조회
    boolean existsByUsername(String username); // 존재 여부만 확인할 때 쓰는 체크
}
