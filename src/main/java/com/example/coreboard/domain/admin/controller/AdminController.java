package com.example.coreboard.domain.admin.controller;

import com.example.coreboard.domain.admin.dto.response.AdminGetResponse;
import com.example.coreboard.domain.admin.service.AdminService;
import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.users.entity.UserRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<OffsetPageResponse<AdminGetResponse>>> get(
            @RequestParam(defaultValue = "ADMIN") UserRole role,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestAttribute("username") String username
    ) {
        OffsetPageResponse<AdminGetResponse> response = adminService.get(pageable, role, username);
        return ResponseEntity.ok(ApiResponse.ok(response, "성공적으로 관리자 목록을 불러왔습니다."));
    }
}