<p align="center">
  <img src="https://img.shields.io/badge/POREST-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="POREST" />
</p>

<h1 align="center">POREST HR Backend</h1>

<p align="center">
  <strong>사업장 근로자를 위한 일정관리 및 휴가관리 서비스</strong>
</p>

<p align="center">
  <a href="https://github.com/lshdainty/porest-hr-back/actions/workflows/ci-main.yml">
    <img src="https://github.com/lshdainty/porest-hr-back/actions/workflows/ci-main.yml/badge.svg" alt="CI/CD" />
  </a>
  <a href="https://codecov.io/gh/lshdainty/porest-hr-back">
    <img src="https://codecov.io/gh/lshdainty/porest-hr-back/branch/main/graph/badge.svg" alt="codecov" />
  </a>
  <img src="https://img.shields.io/badge/Java-25-007396?logo=openjdk&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot" />
</p>

---

## 소개

**POREST HR Backend**는 [POREST](https://github.com/lshdainty/POREST) 서비스의 HR(인사관리) 백엔드입니다.

Golang 사용자가 Java Spring Boot를 공부하고자 시작했으며, 기존 사용하던 Legacy 프로그램을 **JPA**, **QueryDSL** 등 실무에서 많이 사용중인 라이브러리를 사용하여 개선하고자 합니다.

---

## 기술 스택

| Category | Technology |
|----------|------------|
| **Language** | ![Java](https://img.shields.io/badge/Java_25-007396?style=flat-square&logo=openjdk&logoColor=white) |
| **Framework** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot_4.0-6DB33F?style=flat-square&logo=springboot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white) |
| **ORM** | ![JPA](https://img.shields.io/badge/JPA-59666C?style=flat-square&logo=hibernate&logoColor=white) ![QueryDSL](https://img.shields.io/badge/QueryDSL_7.1-0769AD?style=flat-square) |
| **Database** | ![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=flat-square&logo=mariadb&logoColor=white) |
| **Authentication** | ![OAuth2](https://img.shields.io/badge/OAuth2-EB5424?style=flat-square&logo=auth0&logoColor=white) Session-Based |
| **API Documentation** | ![Swagger](https://img.shields.io/badge/Swagger_UI-85EA2D?style=flat-square&logo=swagger&logoColor=black) |
| **Testing** | ![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=flat-square&logo=junit5&logoColor=white) ![Mockito](https://img.shields.io/badge/Mockito-C5D9C8?style=flat-square) |
| **Build** | ![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white) |

---

## 프로젝트 아키텍처

### Hexagonal Architecture

- **Domain Layer**: 프레임워크 독립적인 비즈니스 로직
- **API Layer**: REST 컨트롤러, DTO 변환
- **Infrastructure Layer**: 데이터베이스, 외부 서비스 연동

### 모듈 구조

```
porest-hr-back
├── porest-core           # 공통 라이브러리 (Exception, i18n, Utility 등)
└── porest-back-{company} # 회사별 커스텀 모듈 (선택적 확장)
```

- **porest-core**: 예외 처리, 국제화(i18n), API 응답 포맷, 공통 유틸리티 등 프로젝트 전반에서 사용되는 컴포넌트 제공
- **회사별 커스텀 모듈**: 각 회사별로 필요한 타입(회사 타입, 시스템 타입 등)을 정의하여 플러그인 방식으로 확장 가능

---

## 도메인 모듈

```
src/main/java/com/porest/hr/
├── user/                  # 사용자 관리
├── vacation/              # 휴가 관리 (핵심 도메인)
├── work/                  # 근무 이력 관리
├── schedule/              # 일정 관리
├── holiday/               # 공휴일 관리
├── department/            # 부서 관리
├── company/               # 회사 관리
├── permission/            # 권한 관리
├── dues/                  # 회비 관리
├── notice/                # 공지사항
├── calendar/              # 캘린더 API
├── security/              # 보안 설정
└── common/                # 공통 유틸리티
```

### 각 도메인 패키지 구조

```
{domain}/
├── domain/           # JPA 엔티티
├── controller/       # REST API 엔드포인트
├── service/          # 비즈니스 로직 (@Transactional)
├── repository/       # 데이터 접근 (QueryDSL + JPQL)
└── type/             # 도메인별 Enum 타입
```

### Repository 패턴

각 도메인의 Repository는 3개 파일로 구성됩니다.

```java
// 1. Interface
public interface UserRepository { ... }

// 2. QueryDSL Implementation (@Primary)
@Repository @Primary
public class UserQueryDslRepository implements UserRepository { ... }

// 3. JPQL Fallback Implementation
@Repository("userJpaRepository")
public class UserJpaRepository implements UserRepository { ... }
```

---

## 시작하기

### 요구사항

- **Java**: 25+
- **Gradle**: 8.x
- **MariaDB**: 10.x+

### 빌드 및 실행

```bash
# 빌드 (테스트 제외)
./gradlew clean build -x test

# 로컬 실행
./gradlew bootRun -Dspring.profiles.active=local
```

> 참고: 현재 `build.gradle`에서 `test` 태스크가 `enabled = false`로 설정되어 있습니다.

---

## 국제화 (i18n)

```
src/main/resources/message/
├── messages.properties      # 기본 (영어)
├── messages_en.properties   # 영어
└── messages_ko.properties   # 한국어
```

---

## 관련 저장소

| Repository | Description |
|------------|-------------|
| [POREST](https://github.com/lshdainty/POREST) | 통합 레포지토리 (서비스 소개) |
| [porest-hr-front](https://github.com/lshdainty/porest-hr-front) | HR 프론트엔드 |
| [porest-core](https://github.com/lshdainty/porest-core) | 공통 라이브러리 |
| [porest-sso-back](https://github.com/lshdainty/porest-sso-back) | SSO 백엔드 |
| [porest-sso-front](https://github.com/lshdainty/porest-sso-front) | SSO 프론트엔드 |

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/lshdainty">lshdainty</a>
</p>
