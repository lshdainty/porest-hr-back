# porest-hr-back

POREST HR 백엔드 서비스입니다. 사용자/권한/근무/휴가/공지/부서/회비 등 인사 도메인을 제공합니다.

## Tech Stack
- Java 25
- Spring Boot 4.0.0
- Spring Security + JWT + OAuth2 Client
- Spring Data JPA + QueryDSL 7.1
- MariaDB, Redis
- Gradle + JaCoCo

## 주요 도메인
- `user`, `permission`, `department`, `company`
- `work`, `schedule`, `holiday`, `vacation`, `notice`, `dues`
- `security`, `calendar`, `client`, `common`

## 실행

### 요구사항
- JDK 25
- MariaDB
- Redis

### 환경 변수 파일
앱은 아래 파일을 자동 로드합니다.

- `.env.local.properties` (기본)
- `.env.{profile}.properties`

필수 키(핵심):
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `REDIS_DATABASE`
- `JWT_SECRET`, `JWT_HR_SECRET`
- `SSO_API_URL`, `FRONTEND_BASE_URL`
- `FILE_ROOT_PATH`
- `MAIL_USERNAME`, `MAIL_PASSWORD`

### 로컬 실행

```bash
./gradlew clean build -x test
./gradlew bootRun -Dspring.profiles.active=local
```

기본 포트: `8000`

## API 문서/모니터링
- Swagger UI: `http://localhost:8000/swagger-ui.html`
- OpenAPI: `http://localhost:8000/v3/api-docs`
- Actuator: `http://localhost:8000/actuator`

## 테스트 참고
`build.gradle`에서 `test` 태스크가 현재 `enabled = false`로 설정되어 있습니다.

## Docker

```bash
docker build -t porest-hr-back .
docker run --rm -p 8000:8000 --env-file .env.local.properties porest-hr-back
```
