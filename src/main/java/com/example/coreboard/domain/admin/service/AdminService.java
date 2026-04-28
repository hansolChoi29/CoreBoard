package com.example.coreboard.domain.admin.service;

import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final UsersRepository usersRepository;

    public AdminService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }
}
