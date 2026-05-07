# ADR-0009 : RefreshToken 노출 범위를 줄이기 위해 HttpOnly 쿠키를 선택한 이유

## Date
2026.04.30 (목)

## Context
CoreBoard는 JWT 기반 인증 방식을 사용한다.

AccessToken은 API 요청 인증에 사용되고, RefreshToken은 AccessToken 재발급에 사용된다.

RefreshToken은 AccessToken보다 유효기간이 길기 때문에 탈취될 경우 위험이 더 크다.

따라서 RefreshToken을 클라이언트 저장소에 보관할지, 브라우저 쿠키에 저장할지 결정해야 했다.

특히 localStorage처럼 JavaScript에서 접근 가능한 저장소에 RefreshToken을 보관하면 XSS 공격 상황에서 토큰이 탈취될 위험이 커진다.

반면 쿠키에 저장하면 브라우저가 자동으로 전송해주는 장점이 있지만, 쿠키 기반 전송 특성상 CSRF와 쿠키 속성 설정을 함께 고려해야 한다.

따라서 RefreshToken의 JavaScript 접근을 막고, 전송 범위를 제한하는 방향으로 저장 방식을 설계할 필요가 있었다.

## Decision
RefreshToken은 `HttpOnly` 쿠키에 저장하기로 했다.

RefreshToken 쿠키에는 다음 속성을 적용한다.

- `HttpOnly`
- `Secure`
- `SameSite=Strict`
- `Path=/auth/refresh`

`HttpOnly`를 사용해 JavaScript에서 RefreshToken에 접근하지 못하게 한다.

운영 환경에서는 `Secure` 속성을 적용해 HTTPS 요청에서만 RefreshToken 쿠키가 전송되도록 한다.

`SameSite=Strict`를 사용해 cross-site 요청에 쿠키가 함께 전송될 가능성을 줄인다.

`Path=/auth/refresh`를 설정해 RefreshToken이 모든 API 요청에 자동으로 첨부되지 않고, 토큰 재발급 API에만 사용되도록 전송 범위를 제한한다.

이 결정의 핵심 이유는 RefreshToken의 저장 위치뿐 아니라, JavaScript 접근 가능성과 쿠키 전송 범위를 함께 제한하기 위함이다.

## Options Considered

### 1. RefreshToken을 응답 body로 내려 클라이언트 저장소에 보관
- 장점
  - 구현이 단순하다.
  - 프론트엔드에서 토큰 값을 직접 제어하기 쉽다.
  - API 테스트 시 토큰 값을 확인하기 쉽다.

- 단점
  - localStorage 등에 저장하면 JavaScript에서 접근할 수 있다.
  - XSS 공격 상황에서 RefreshToken이 탈취될 위험이 커진다.
  - RefreshToken은 유효기간이 길어 탈취 시 피해가 크다.
  - 클라이언트 저장 위치에 따라 보안 수준이 달라진다.

### 2. RefreshToken을 일반 쿠키에 저장
- 장점
  - 브라우저가 쿠키 저장과 전송을 자동으로 처리한다.
  - 재발급 요청 시 RefreshToken을 직접 body에 담지 않아도 된다.

- 단점
  - HttpOnly가 없으면 JavaScript에서 쿠키 값에 접근할 수 있다.
  - Path 제한이 없으면 불필요한 API 요청에도 쿠키가 전송될 수 있다.
  - 쿠키 기반 전송 특성상 CSRF 대응을 함께 고려해야 한다.

### 3. RefreshToken을 HttpOnly 쿠키에 저장하고 전송 범위를 제한 (선택)
- 장점
  - JavaScript에서 RefreshToken에 접근할 수 없어 XSS 상황에서 탈취 위험을 낮출 수 있다.
  - Path를 `/auth/refresh`로 제한해 RefreshToken이 재발급 API에만 전송되도록 할 수 있다.
  - AccessToken과 RefreshToken의 역할과 저장 위치를 분리할 수 있다.
  - SameSite와 Secure 속성을 통해 쿠키 전송 조건을 제한할 수 있다.

- 단점
  - 쿠키 기반 인증 특성상 CSRF 정책을 함께 고려해야 한다.
  - 프론트엔드와 백엔드의 쿠키 설정, CORS 설정을 함께 맞춰야 한다.
  - 로컬 개발 환경과 운영 환경의 Secure 설정 차이를 관리해야 한다.
  - 브라우저 정책에 영향을 받으므로 배포 환경에서 쿠키 동작을 별도로 확인해야 한다.

## Consequences
- 좋은 점
  - RefreshToken의 JavaScript 접근을 차단할 수 있다.
  - RefreshToken이 모든 API에 자동으로 전송되지 않도록 범위를 제한할 수 있다.
  - AccessToken과 RefreshToken의 역할과 저장 위치를 분리할 수 있다.
  - XSS 상황에서 RefreshToken 탈취 위험을 낮출 수 있다.

- 나쁜 점 (트레이드오프)
  - 쿠키 속성 설정을 신경 써야 한다.
  - CSRF 위험을 함께 고려해야 한다.
  - 프론트엔드 연동 시 credentials 설정이 필요할 수 있다.
  - 운영 환경에서는 HTTPS 적용이 전제되어야 한다.
  - 로컬 환경과 운영 환경의 쿠키 설정 차이를 관리해야 한다.

## Evidence (선택)
- RefreshToken은 `HttpOnly` 쿠키로 저장
- RefreshToken 쿠키의 전송 경로를 `/auth/refresh`로 제한
- 운영 환경에서는 `Secure` 속성을 통해 HTTPS 전송을 전제로 설정
- `SameSite=Strict`로 cross-site 요청에서 쿠키 전송 가능성을 제한

## References
- MDN Web Docs - Set-Cookie
- MDN Web Docs - Secure cookie configuration
- OWASP Cheat Sheet - Cross-Site Request Forgery Prevention