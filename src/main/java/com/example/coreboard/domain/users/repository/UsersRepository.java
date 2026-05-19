package com.example.coreboard.domain.users.repository;

import com.example.coreboard.domain.users.dto.query.UserNicknameProjection;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<Users> findByRole(UserRole role, Pageable pageable);

    long countByRole(UserRole role);

    @Query("""
                    select u.userId as userId,
                    u.nickname as nickname
                    from Users u 
                    where u.userId in :userIds
            """)
    List<UserNicknameProjection> findNicknamesByUserIds(
            @Param("userIds") List<Long> userIds
    );
}