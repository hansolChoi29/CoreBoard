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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Admin", description = "관리자 권한 및 사용자 권한 관리 API")
@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    //    @Profile({"local", "dev"})
    @Operation(
            summary = "최초 ADMIN 계정 생성",
            description = "서비스 초기 설정용 ADMIN 계정을 생성합니다."
    )
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

    @Operation(
            summary = "사용자 목록 조회",
            description = "ADMIN 권한으로 사용자 목록을 조회합니다. role 파라미터로 ADMIN 또는 USER 목록을 필터링할 수 있습니다."
    )
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<OffsetPageResponse<AdminGetResponse>>> get(
            @RequestParam(defaultValue = "ADMIN") UserRole role,
            @PageableDefault(size = 20, sort = "userId", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestAttribute("username") String username
    ) {
        AdminUserListQuery query = new AdminUserListQuery(role, pageable, username);
        OffsetPageResponse<AdminGetResponse> response = adminService.get(query);
        return ResponseEntity.ok(ApiResponse.ok(response, "성공적으로 관리자 목록을 불러왔습니다."));
    }

    @Operation(
            summary = "사용자 권한 변경",
            description = """
                    ADMIN 권한으로 사용자의 권한을 변경합니다.
                    
                    - role=ADMIN : 해당 사용자를 관리자로 승급
                    - role=USER : 해당 사용자를 일반 사용자로 강등
                    """
    )
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<AdminPatchResponse>> promote(
            @PathVariable Long id,
            @RequestParam(defaultValue = "ADMIN") UserRole role
    ) {
        AdminPatchCommand command = new AdminPatchCommand(id, role);
        AdminPatchDto out = adminService.promote(command);
        AdminPatchResponse response = new AdminPatchResponse(out.id(), out.username(), out.role());
        return ResponseEntity.ok(ApiResponse.ok(response, "사용자 권한이 변경되었습니다."));
    }
}