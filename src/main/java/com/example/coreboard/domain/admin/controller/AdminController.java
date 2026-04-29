package com.example.coreboard.domain.admin.controller;

import com.example.coreboard.domain.admin.dto.AdminPatchDto;
import com.example.coreboard.domain.admin.dto.command.AdminPatchCommand;
import com.example.coreboard.domain.admin.dto.query.AdminUserListQuery;
import com.example.coreboard.domain.admin.dto.response.AdminGetResponse;
import com.example.coreboard.domain.admin.dto.response.AdminPatchResponse;
import com.example.coreboard.domain.admin.service.AdminService;
import com.example.coreboard.domain.auth.dto.SignUpDto;
import com.example.coreboard.domain.auth.dto.command.SignUpCommand;
import com.example.coreboard.domain.auth.dto.request.SignUpRequest;
import com.example.coreboard.domain.auth.dto.response.SignUpResponse;
import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.common.validation.AuthValidation;
import com.example.coreboard.domain.users.entity.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Admin", description = "관리자 권한관련 API")
@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

//    @Profile({"local", "dev"})
    @Operation(summary = "AMDIN 회원가입")
    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<SignUpResponse>> setup(
            @RequestBody SignUpRequest request
    ) {
        AuthValidation.signUpValidation(request);
        SignUpCommand users = new SignUpCommand(
                request.username(),
                request.nickname(),
                request.password(),
                request.confirmPassword(),
                request.email(),
                request.phoneNumber());
        SignUpDto out = adminService.adminSetup(users);
        SignUpResponse response = new SignUpResponse(
                out.username(),
                out.role()
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "회원가입 성공"));
    }

    @Operation(summary = "ADMIN 목록 조회")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<OffsetPageResponse<AdminGetResponse>>> getAdmins(
            @RequestParam(defaultValue = "ADMIN") UserRole role,
            @PageableDefault(size = 20, sort = "userId", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestAttribute("username") String username
    ) {
        AdminUserListQuery query = new AdminUserListQuery(role, pageable, username);
        OffsetPageResponse<AdminGetResponse> response = adminService.getAdmins(query);
        return ResponseEntity.ok(ApiResponse.ok(response, "성공적으로 관리자 목록을 불러왔습니다."));
    }
    @Operation(summary = "ADMIN/USER 전환 API", description = "- `role=ADMIN` : 해당 사용자를 관리자로 승급\n" +
            "- `role=USER` : 해당 사용자를 일반 유저로 강등")
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<AdminPatchResponse>> promoteToAdmin(
            @PathVariable Long id,
            @RequestParam(defaultValue = "ADMIN") UserRole role
    ) {
        AdminPatchCommand command = new AdminPatchCommand(id, role);
        AdminPatchDto out = adminService.promoteToAdmin(command);
        AdminPatchResponse response = new AdminPatchResponse(out.id(), out.username(), out.role());
        return ResponseEntity.ok(ApiResponse.ok(response, "사용자 권한이 변경되었습니다."));
    }
}