# 🔐 따숨 Security Flow

> 인증/인가가 어떻게 동작하는지, 그리고 **각 도메인 개발자가 컨트롤러에서 로그인한 회원 정보를 어떻게 꺼내 쓰는지** 안내하는 문서입니다.
>
> ⚠️ **현재 구현 상태**: 이메일 인증 회원가입까지 구현 완료. JWT 로그인/로그아웃, SNS 로그인은 **설계 확정 후 구현 예정**이며, 아래 [예정] 표시된 부분은 구현 시점에 갱신됩니다. 시그니처가 바뀔 수 있으니 큰 흐름 위주로 참고해 주세요.
>
> 🔑 **토큰 저장 방식 결정 (2026-07)**: Access Token / Refresh Token **모두 HttpOnly 쿠키**로 관리합니다. 프론트엔드는 토큰을 직접 다루지 않습니다. (결정 배경은 하단 부록 참고)

---

## 1. 권한(Role) 체계

| Role | 대상 | 접근 범위 |
|------|------|-----------|
| `GUEST` | SNS 가입 후 **추가정보 미입력** 회원 | 추가정보 입력 API만 접근 가능 |
| `USER` | 일반 가입 완료 회원, SNS 가입 + 추가정보 입력 완료 회원 | 일반 서비스 전체 |
| `ADMIN` | 초기 데이터로 생성되는 관리자 | `/api/admin/**` 포함 전체 |

- 일반 회원가입은 즉시 `USER`로 생성됩니다.
- SNS 가입자는 `GUEST`로 생성 → 추가정보(닉네임/실명/전화번호) 입력 완료 시 `USER`로 승급됩니다. (`member.updateExtraInfo()`)
- **ADMIN은 회원가입 API로 만들 수 없습니다.** 초기 시드 데이터(SQL)로만 생성합니다.

---

## 2. 전체 인증 흐름 요약

```
[일반 회원가입 - 구현 완료]
이메일 입력 → POST /api/auth/email/send   (인증코드 메일 발송, Redis authCode:{email} TTL 3분)
코드 입력   → POST /api/auth/email/verify  (검증 성공 시 verified:{email} TTL 30분)
정보 입력   → POST /api/auth/signup        (verified 확인 → BCrypt 인코딩 → DB 저장 → 201)

[로그인 - 예정]
POST /api/auth/login
  → 이메일/비밀번호 검증 (BCrypt matches)
  → Set-Cookie 로 AT / RT 두 개의 HttpOnly 쿠키 발급
  → RT는 Redis(refresh:{memberId})에도 저장 (서버 측 대조용)
  → 응답 body에는 토큰을 담지 않고 회원 기본 정보만 반환

[인증이 필요한 API 호출 - 예정]
브라우저가 AT 쿠키를 자동 전송
  → JwtAuthenticationFilter가 "쿠키에서" AT를 추출해 검증   ★ 헤더 방식 아님
  → SecurityContext에 인증 정보(Authentication) 등록
  → 컨트롤러에서 회원 정보 추출 가능 (아래 4번 참고)

[토큰 재발급 - 예정]
POST /api/auth/reissue
  → 쿠키의 RT를 Redis(refresh:{memberId})와 대조
  → 일치하면 새 AT 쿠키 발급 (Set-Cookie)
  → 불일치/만료 시 401 → 프론트는 로그인 페이지로 이동

[로그아웃 - 예정]
POST /api/auth/logout (인증 필요)
  → Redis의 refresh:{memberId} 삭제
  → AT 남은 유효시간만큼 블랙리스트 등록 (Redis)
  → AT / RT 쿠키 삭제 (Max-Age=0 Set-Cookie)
```

---

## 3. 엔드포인트 접근 정책

`SecurityConfig` + `SecurityConstants.PUBLIC_URIS`에서 관리합니다.

| 경로 | 접근 정책 | 상태 |
|------|-----------|------|
| `/api/auth/email/send`, `/email/verify`, `/signup` | permitAll | 구현 완료 |
| `/api/auth/login`, `/api/auth/reissue` | permitAll | 예정 |
| `/api/auth/logout` | **authenticated** (⚠️ auth 하위지만 토큰 필요) | 예정 |
| `/api/members/me/signup-complete` (SNS 추가정보) | hasRole("GUEST") | 예정 |
| `/api/members/**` | hasAnyRole("USER", "ADMIN") | 예정 |
| `/api/admin/**` | hasRole("ADMIN") | 구성됨 |
| 그 외 | authenticated | 구성됨 |

> ⚠️ **현재는 개발 초기라 `PUBLIC_URIS = "/api/**"` 로 전부 열려 있습니다.**
> 기능 구현이 진행되면 위 표대로 좁혀갑니다. 즉 **지금 인증 없이 되는 API도 나중에 토큰이 필요해질 수 있으니**, 프론트 연동 시 이 표를 기준으로 개발해 주세요.
>
> ⚠️ `requestMatchers`는 **선언 순서대로** 매칭됩니다. 구체적 경로(logout, GUEST 규칙)를 넓은 경로보다 먼저 선언해야 합니다. SecurityConfig 주석의 번호 순서를 지켜주세요.

---

## 4. [팀원 필독] 컨트롤러에서 로그인 회원 정보 꺼내기 [예정 — 시그니처 확정 후 갱신]

로그인 구현 완료 후, 인증이 필요한 API에서는 아래 방법으로 "지금 요청한 회원"을 식별합니다.

### 설계 방침

- JWT의 `subject`에는 **회원 PK(memberId)** 를 담습니다. (이메일이 아닌 PK — 이메일 변경 기능이 생겨도 토큰이 무효화되지 않도록)
- 토큰 검증 후 `SecurityContext`에는 `CustomUserDetails`(memberId, email, role 보유)가 등록됩니다.

### 방법 A — `@AuthenticationPrincipal` (권장)

```java
@GetMapping("/me")
public ResponseEntity<ApiResponse<MemberResponse>> getMyInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long memberId = userDetails.getMemberId();   // ← 회원 PK
    MemberResponse response = memberService.getMyInfo(memberId);
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

### 방법 B — `SecurityUtil` (서비스 계층 등 파라미터 전달이 번거로운 곳)

```java
Long memberId = SecurityUtil.getMemberId();   // SecurityContext에서 직접 추출
```

### 사용 규칙

1. **컨트롤러에서는 방법 A를 기본**으로 사용합니다. 파라미터로 명시되어 테스트와 가독성에 유리합니다.
2. 서비스 메서드는 `memberId`를 **파라미터로 받도록** 작성합니다. 서비스가 SecurityContext에 직접 의존하면 테스트가 어려워집니다.
   ```java
   // ✅ 권장
   public MemberResponse getMyInfo(Long memberId) { ... }
   // ❌ 지양 — 서비스 내부에서 SecurityUtil 호출
   ```
3. 회원 엔티티가 필요하면 서비스에서 조회합니다.
   ```java
   Member member = memberRepository.findById(memberId)
           .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
   ```
4. **다른 사람의 리소스를 조작하는 API가 아니라면, 요청 body나 쿼리 파라미터로 memberId를 받지 마세요.**
   항상 토큰에서 추출한 memberId를 사용합니다. (body의 memberId를 믿으면 타인 정보 조작이 가능해집니다)

---

## 5. 토큰 정책 [예정 — 구현 시 확정]

| 항목 | Access Token | Refresh Token |
|------|--------------|---------------|
| 클라이언트 저장 | **HttpOnly 쿠키** | **HttpOnly 쿠키** |
| 서버 저장 | 없음 (블랙리스트만 예외) | **Redis** (`refresh:{memberId}`) — reissue 시 대조 |
| 쿠키 Path | `/` (모든 API에 전송) | `/api/auth` (reissue/logout에만 전송) |
| 유효 기간 | 30분 (예정) | 14일 (예정) |
| 담는 정보 | memberId(sub), role | memberId(sub) |
| 만료 시 | `POST /api/auth/reissue`로 재발급 | 재로그인 |

### 쿠키 공통 속성

| 속성 | 값 | 이유 |
|------|-----|------|
| `HttpOnly` | true | JS에서 접근 불가 → XSS로 토큰 반출 차단 |
| `SameSite` | `Lax` | 타 사이트발 POST에 쿠키 미전송 → CSRF 방어 |
| `Secure` | 운영 true / 로컬 false | HTTPS에서만 전송 (프로파일로 분기) |

> ⚠️ **배포 전제 조건**: SameSite=Lax가 성립하려면 **프론트와 백엔드가 same-site**(예: `ddasoom.com` + `api.ddasoom.com`)로 배포되어야 합니다. 도메인이 완전히 갈리면(`xxx.vercel.app` + `yyy.cloudtype.app` 등) `SameSite=None` + 별도 CSRF 방어가 필요해지므로, **배포 인프라 결정 시 이 문서 기준으로 함께 검토해 주세요.** (로컬 개발은 `localhost` 포트가 달라도 same-site로 취급되어 문제없음)

### RT 회전(rotation) 정책

- 이번 구현 범위에서는 **RT 회전을 적용하지 않습니다.** (reissue 시 RT는 그대로, AT만 재발급)
- 회전을 도입하면 멀티탭 동시 reissue 경합에 대비한 grace period 처리가 함께 필요해져 복잡도가 크게 오릅니다. 데모 규모에서는 Redis 대조 + 짧은 AT 만료로 충분하다고 판단했습니다.
- 보안 강화가 필요해지는 시점에 회전 + 재사용 탐지를 별도 작업으로 도입합니다.

### 팀 결정 대기 항목

| 항목 | 선택지 | 잠정 기본값 |
|------|--------|-------------|
| 다중 기기 로그인 | 단일 세션(`refresh:{memberId}`) vs 기기별 세션(`refresh:{memberId}:{jti}`) | **단일 세션** — 새 로그인 시 기존 기기 세션 만료 |
| RT TTL 방식 | 고정 14일 vs reissue마다 갱신(슬라이딩) | **고정 14일** — 14일마다 재로그인 |

---

## 6. 프론트엔드 연동 가이드 [예정]

방식 B(전부 쿠키)에서는 **프론트가 토큰을 저장·전송·파싱할 일이 없습니다.** 아래만 구현하면 됩니다.

### 필수 설정

```js
// axios 등 HTTP 클라이언트에 쿠키 전송 옵션 필수
axios.defaults.withCredentials = true;
```

### 앱 시작 시 로그인 판별 (새 탭 / 새로고침 포함)

```
앱 마운트 → GET /api/members/me
  ├─ 200 → 로그인 상태 (회원 정보까지 한 번에 획득)
  └─ 401 → 아래 재발급 흐름으로
```

쿠키는 브라우저가 자동 전송하므로 **탭마다 별도 부트스트랩이나 토큰 복원 로직이 필요 없습니다.**

### 401 인터셉터 (AT 만료 처리)

```
API 401 응답 → POST /api/auth/reissue
  ├─ 200 (새 AT 쿠키 자동 설정) → 원요청 1회 재시도
  └─ 401 (RT 만료/불일치) → 스토어 초기화 후 로그인 페이지로
```

- reissue 실패 시 **무한 재시도 금지** (재시도 1회 제한 플래그 필수)
- 로그아웃은 `POST /api/auth/logout` 호출 후 스토어 초기화. 쿠키 삭제는 서버가 수행하므로 프론트에서 쿠키를 건드리지 않습니다.
- 탭 간 UI 동기화(다른 탭 로그아웃 반영 등)는 쿠키가 공유되므로 서버 상태는 자동 일치합니다. 화면 상태 동기화가 필요하면 BroadcastChannel 등을 프론트에서 선택적으로 적용합니다.

---

## 7. Redis 키 설계 (인증 관련 전체)

| 키 | 값 | TTL | 용도 | 상태 |
|----|----|-----|------|------|
| `authCode:{email}` | 6자리 코드 | 3분 | 이메일 인증 코드 | 구현 완료 |
| `verified:{email}` | "true" | 30분 | 인증 완료 ~ 가입 사이 유예 | 구현 완료 |
| `refresh:{memberId}` | Refresh Token 문자열 | 14일 | reissue 시 쿠키 RT와 대조 | 예정 |
| `blacklist:{accessToken}` | "logout" | AT 남은 시간 | 로그아웃된 AT 차단 | 예정 |

- TTL은 **RedisConfig가 아니라 서비스 계층에서** 지정합니다.
- 인증 코드 재발송 시 기존 `verified:{email}`이 삭제됩니다. (프론트의 "재인증 시 되돌리기"와 서버 상태 일치)
- 로그아웃/강제 로그아웃은 `refresh:{memberId}` 삭제로 처리합니다. 삭제 즉시 해당 회원의 reissue가 차단됩니다.

---

## 8. 인증 관련 에러 응답

모든 에러는 공통 규격 `{ code, message, data: null }`로 내려갑니다. ([컨벤션 문서](./CONVENTIONS.md) 참고)

| code | HTTP | 상황 |
|------|------|------|
| `AUTH_001` | 409 | 이미 가입된 이메일 (인증코드 발송/가입 시) |
| `AUTH_002` | 409 | 닉네임 중복 |
| `AUTH_003` | 400 | 이메일 인증 미완료 상태로 가입 시도 (30분 초과 포함) |
| `AUTH_004` | 400 | 인증 코드 불일치 또는 만료 |
| `AUTH_005` | 500 | 메일 전송 실패 |
| `MEMBER_001` | 404 | 회원 없음 |
| 401 응답 | 401 | AT 없음/만료/위조, RT 재발급 실패 [예정 — EntryPoint 구현 시 코드 확정] |
| 403 응답 | 403 | 권한 부족 (GUEST의 USER API 접근 등) [예정] |

> [예정] 401/403은 Spring Security 필터 레벨에서 발생하므로 GlobalExceptionHandler가 잡지 못합니다.
> `AuthenticationEntryPoint` / `AccessDeniedHandler`를 구현해 동일한 `ApiResponse` 포맷으로 내려줄 예정입니다.

---

## 부록. 토큰 저장 방식 결정 배경 (방식 B 채택)

검토한 선택지:

| 방식 | 구성 | 미채택/채택 사유 |
|------|------|------|
| A. AT 메모리 + RT 쿠키 | 글로벌 SPA 권장 표준 | 새 탭/새로고침마다 reissue 부트스트랩, 토큰 상태관리, 탭 동기화 등 프론트 부담이 커서 미채택 |
| **B. AT/RT 모두 HttpOnly 쿠키** | **채택** | 프론트가 토큰을 다루지 않아 연동 단순. 멀티탭/새로고침 자동 처리. CSRF는 SameSite=Lax로 방어 (same-site 배포 전제) |
| C. AT 쿠키 + 세션만 Redis | JWT+세션 하이브리드 | 클라이언트 자격증명이 하나로 줄지만, 만료 AT를 갱신 자격증명으로 쓰는 커스텀 파싱이 필요. 표준 패턴(B) 대비 구현 특이점이 많아 미채택 |

- B의 대가인 CSRF는 SameSite=Lax + same-site 배포로 방어하며, RT 쿠키의 Path를 `/api/auth`로 좁혀 노출 면적을 줄입니다.
- 서버 측 세션 통제(강제 로그아웃)는 Redis의 `refresh:{memberId}` 삭제로 확보합니다.
