# Project Overview
- Language: Java 17 [LTS 사용] [web:26]
- Framework: Spring Boot 3.4.x (Spring Framework 6.x) [web:26]
- Build: Gradle [web:26]
- Packaging: Jar 배포 [web:26]
- API 문서: springdoc-openapi (Swagger UI) [web:26]

## Modules & Structure
- modules: core(domain), api(web), infra(persistence, external-clients) [web:27]
- Hexagonal 스타일: domain은 framework 의존 금지, adapter는 의존 허용 [web:26]
- 패키지 규칙: com.lshdainty.porest.{domain}.{controller, service, repository} [web:26]

## Coding Standards
- Controller: DTO 변환 철저, 엔티티 노출 금지 [web:26]
- Service: 트랜잭션 경계 @Transactional, 읽기 전용 readOnly=true [web:26]
- Repository: QueryDSL [web:26]
- 예외: 전역 @RestControllerAdvice로 에러 응답 표준화 [web:26]
- 로깅: slf4j 사용 [web:26]
- Import 규칙:
  - 클래스는 반드시 파일 상단에 import 문으로 선언 (풀경로 인라인 사용 금지)
  - 사용하지 않는 import는 즉시 삭제
  - 와일드카드 import(`*`) 사용 금지, 개별 클래스 명시

## i18n & Message 관리
- 메시지 키: `MessageKey` enum으로 중앙 관리 (`common/message/MessageKey.java`)
- 메시지 조회: `MessageResolver` 사용 (`common/util/MessageResolver.java`)
- 메시지 파일: `messages.properties` (한국어), `messages_en.properties` (영어)

### 새 메시지 추가 절차
1. `MessageKey` enum에 키 추가:
   ```java
   NOT_FOUND_USER("error.notfound.user"),
   VALIDATE_DUPLICATE_USER_ID("error.validate.duplicate.userId"),
   ```
2. `messages.properties`에 메시지 추가:
   ```properties
   error.notfound.user=사용자를 찾을 수 없습니다.
   error.validate.duplicate.userId=이미 존재하는 사용자 ID입니다.
   ```
3. `messages_en.properties`에 영어 메시지 추가
4. 서비스에서 사용:
   ```java
   throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.NOT_FOUND_USER));
   // 파라미터 포함 시
   throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.SOME_KEY, param1, param2));
   ```

### MessageKey 카테고리
- `NOT_FOUND_*`: 조회 실패
- `VALIDATE_*`: 검증 오류
- `VACATION_*`: 휴가 관련
- `VACATION_POLICY_*`: 휴가 정책
- `FILE_*`: 파일 관련
- `COMMON_*`: 공통

## API Guidelines
- Base path: /api/v1 [web:27]
- 에러 코드: 도메인별 접두어(USER_*, ORDER_*), HTTP 상태와 매핑 [web:26]

## Persistence Rules
- Soft delete: 공통 Audit + soft delete 필드 사용 (is_deleted) [web:27]
- N+1 방지: fetch join 또는 EntityGraph, 필요 시 read-only projection [web:26]
- 트랜잭션: 서비스 계층에서만 시작, 중첩 금지 [web:26]

## Testing Strategy
- Unit: JUnit 5 + Mockito/MockK, 빠른 실행 [web:26]
- Integration: @SpringBootTest + Testcontainers(Postgres) [web:26]
- Web: @WebMvcTest로 컨트롤러 슬라이스 테스트 [web:26]
- 커버리지 목표: line 80%+, critical path 90%+ [web:26]
- 테스트 데이터: Fixture/Factory 패턴, 임의 값은 Instancio/JavaFaker [web:26]

## Build & Run
- Gradle
    - build: ./gradlew clean build -x test [web:26]
    - test: ./gradlew test integrationTest [web:26]
    - run: ./gradlew bootRun -Dspring.profiles.active=local [web:26]

## Observability
- Metrics: Micrometer + Prometheus, 핵심 비즈 지표 계측 [web:26]
- Tracing: OpenTelemetry 자동 계측 + TraceId 로깅 상관관계 [web:26]
- Health: /actuator/health, readiness/liveness 분리 [web:26]

## Do Not
- 엔티티 직렬화로 API 응답 반환 금지 [web:26]
- EAGER 남발 금지, 양방향 연관 복잡화 금지 [web:26]
- 비밀값/토큰/키를 코드나 로그에 노출 금지 [web:26]
