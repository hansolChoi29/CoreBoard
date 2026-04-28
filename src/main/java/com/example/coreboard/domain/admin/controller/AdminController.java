package com.example.coreboard.domain.admin.controller;

import com.example.coreboard.domain.admin.service.AdminService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /* TODO
    POST /admin/users
    → 관리자 계정 생성

    GET /admin/users
    → 사용자/관리자 목록 조회, 선택사항

    PATCH /admin/users/{userId}/role
    → 권한 변경, 지금은 보류 추천
    */
}
