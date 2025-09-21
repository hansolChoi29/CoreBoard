package com.example.coreboard.domain.users.repository;

import com.example.coreboard.domain.users.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository <UsersEntity,Long>{
}
