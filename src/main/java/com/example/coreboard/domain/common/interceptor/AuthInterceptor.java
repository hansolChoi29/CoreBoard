package com.example.coreboard.domain.common.interceptor;

import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

// 실제 인터셉터 동작 로직
// 핵심 기능
// 1) 요청 헤더에 Authorization 가져오기
// 2) JWT 토큰 검증
// 3) 토큰에서 username 추출 후 request 저장
// 4) 유효하지 않으면 예외 발생 (컨트롤러 실행 안 되게)
public class AuthInterceptor implements HandlerInterceptor {
    // 트러블 - 토큰 검증 실패!: JWT strings must contain exactly 2 period characters
    // 매 컨트롤러/서비스 마다 토큰 검증하는 대신 인터셉터 이용하기로 함
    // 왜  Filter 사용 안 했는지? -

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 클라이언트가 보낸 Authorization 헤더 읽기
        // JWT는 일반적으로 Authorization: Bearer토큰 형태로 실어 보내기 때문
        String authorization = request.getHeader("Authorization");

        // 헤더가 없으면 인증 자체가 안 됨 -> 401
        // 인증이 안 된 요청을 뒤로 넘기면 실제 비즈니스 로직에서 더 큰 보안 리스크가 생김
        if (authorization == null) {
            throw new AuthErrorException(AuthErrorCode.UNAUTHORIZED);
        }

        // (?i) : 대소문자 무시(Bearer/bearer 모두 허용)
        // ^Bearer\s : 문자열 시작에서 Bearer 다음의 한 칸 이상 공백 제거
        // replaceAll("\\s+", "") : 토큰 앞뒤/ 중간에 섞여 들어온 개행/스페이스 방지
        String accessToken = authorization
                .replaceFirst("(?i)^Bearer\\s+", "")
                .replaceAll("\\s+", "");

        // 토큰 유효성 검사 
        if (!JwtUtil.validationToken(accessToken)) {
            throw new AuthErrorException(AuthErrorCode.UNAUTHORIZED);
        }

        // 토큰에서 식별자 추출 - 권한/소유권 체크를 하기 위함 (board 컨트롤러에서 쓸 예정)
        String username = JwtUtil.getUsername(accessToken);

        // @RequestAttribute("username") 쓰기 위함 - 현재 사용자 전달하는 역할
        request.setAttribute("username", username);

        // true 반환해야 컨트롤러 요청 가능
        return true;
    }
}
