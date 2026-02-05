package com.porest.hr.user.domain;

import com.porest.hr.common.converter.CompanyTypeConverter;
import com.porest.hr.common.domain.AuditingFieldsWithIp;
import com.porest.core.type.CountryCode;
import com.porest.hr.common.type.CompanyType;
import com.porest.hr.department.domain.UserDepartment;
import com.porest.hr.permission.domain.Role;
import com.porest.hr.permission.domain.UserRole;
import com.porest.core.type.YNType;
import com.porest.hr.user.type.StatusType;
import com.porest.hr.vacation.domain.UserVacationPlan;
import com.porest.hr.vacation.domain.VacationApproval;
import com.porest.hr.vacation.domain.VacationGrant;
import com.porest.hr.vacation.domain.VacationPlan;
import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.domain.VacationUsage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "users")
public class User extends AuditingFieldsWithIp {
    /**
     * 유저 행 아이디<br>
     * HR 내부 관리용 PK (auto increment)
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id")
    private Long rowId;

    /**
     * SSO 사용자 행 아이디<br>
     * SSO에서 발급한 row_id<br>
     * SSO와 HR 간 사용자 연결 키 (nullable - SSO 연동 전 사용자 존재 가능)
     */
    @Column(name = "sso_user_row_id", unique = true)
    private Long ssoUserRowId;

    /**
     * 사용자 아이디<br>
     * 비즈니스 식별자 (고유값)
     */
    @Column(name = "user_id", length = 20, unique = true, nullable = false)
    private String id;

    /**
     * 사용자명<br>
     * 사용자의 실명
     */
    @Column(name = "user_name", nullable = false, length = 20)
    private String name;

    /**
     * 사용자 이메일<br>
     * 사용자의 이메일 주소
     */
    @Column(name = "user_email", nullable = false, length = 100)
    private String email;

    /**
     * 사용자 생일<br>
     * 사용자의 생년월일
     */
    @Column(name = "user_birth")
    private LocalDate birth;

    /**
     * 사용자 근무시간<br>
     * 사용자의 근무 시간대 (예: "8 ~ 17", "9 ~ 18", "10 ~ 19", "13 ~ 21")
     */
    @Column(name = "user_work_time", length = 10)
    private String workTime;

    /**
     * 사용자 입사 일자<br>
     * 사용자의 회사 입사 날짜
     */
    @Column(name = "join_date")
    private LocalDate joinDate;

    /**
     * 사용자 원소속 회사<br>
     * 사용자가 원래 속한 회사 정보
     */
    @Convert(converter = CompanyTypeConverter.class)
    @Column(name = "user_origin_company", nullable = false, length = 20)
    private CompanyType company;

    /**
     * 프로필 파일명<br>
     * 사용자의 프로필 이미지 원본 파일명
     */
    @Column(name = "profile_name", length = 50)
    private String profileName;

    /**
     * 프로필 파일 고유 UUID<br>
     * 사용자의 프로필 이미지 고유 식별자
     */
    @Column(name = "profile_uuid", length = 36)
    private String profileUUID;

    /**
     * 생일 음력 여부<br>
     * 생일의 음력/양력 여부
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "lunar_yn", nullable = false, length = 1)
    private YNType lunarYN;

    /**
     * 국가 코드<br>
     * 사용자의 국가 정보
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "country_code", nullable = false, length = 2)
    private CountryCode countryCode;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1)
    private YNType isDeleted;

    /**
     * 초대 상태<br>
     * PENDING: 초대 후 아직 회원가입하지 않은 상태<br>
     * ACTIVE: 회원가입 완료한 상태<br>
     * EXPIRED: 초대 링크가 만료된 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status", length = 10)
    private StatusType invitationStatus;

    /**
     * 초대 토큰<br>
     * SSO에서 발급한 초대 토큰 (회원가입 완료 후 null)
     */
    @Column(name = "invitation_token", length = 36)
    private String invitationToken;

    /**
     * 초대 발송 일시<br>
     * 초대 이메일 발송 시간
     */
    @Column(name = "invitation_sent_at")
    private LocalDateTime invitationSentAt;

    /**
     * 초대 만료 일시<br>
     * 초대 토큰 만료 시간 (기본 48시간)
     */
    @Column(name = "invitation_expires_at")
    private LocalDateTime invitationExpiresAt;

    /**
     * 회원가입 완료 일시<br>
     * 사용자가 회원가입을 완료한 시간
     */
    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    /**
     * 대시보드 레이아웃 정보<br>
     * 사용자 맞춤 대시보드 설정 정보 (JSON 형식)
     */
    @Lob
    @Column(name = "dashboard", length = 65535)
    private String dashboard;

    /**
     * 유저 휴가 부여 목록<br>
     * 사용자에게 부여된 휴가 내역 목록
     */
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VacationGrant> vacationGrants = new ArrayList<>();

    /**
     * 유저 휴가 사용 목록<br>
     * 사용자가 사용한 휴가 내역 목록
     */
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VacationUsage> vacationUsages = new ArrayList<>();

    /**
     * 유저 휴가 플랜 목록<br>
     * 사용자에게 적용된 휴가 플랜 매핑 목록
     */
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserVacationPlan> userVacationPlans = new ArrayList<>();

    /**
     * 유저 부서 목록<br>
     * 사용자가 소속된 부서 매핑 목록
     */
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserDepartment> userDepartments = new ArrayList<>();

    /**
     * 유저 휴가 승인 목록<br>
     * 사용자가 승인자로 지정된 휴가 승인 내역 목록
     */
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "approver", cascade = CascadeType.ALL)
    private List<VacationApproval> vacationApprovals = new ArrayList<>();

    /**
     * 유저 역할 목록<br>
     * 사용자에게 부여된 역할 매핑 목록
     */
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserRole> userRoles = new ArrayList<>();

    /**
     * 유저 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 생성할 것
     *
     * @param ssoUserRowId SSO에서 발급한 row_id (nullable - SSO 연동 전 사용자)
     * @return User
     */
    public static User createUser(Long ssoUserRowId, String id, String name, String email, LocalDate birth,
                                  CompanyType company, String workTime, LocalDate joinDate,
                                  YNType lunarYN, String profileName, String profileUUID, CountryCode countryCode) {
        User user = new User();
        user.ssoUserRowId = ssoUserRowId;
        user.id = id;
        user.name = name;
        user.email = email;
        user.birth = birth;
        user.company = company;
        user.workTime = workTime;
        user.joinDate = joinDate;
        user.lunarYN = lunarYN;
        user.profileName = profileName;
        user.profileUUID = profileUUID;
        user.countryCode = countryCode;
        user.isDeleted = YNType.N;
        return user;
    }

    /**
     * SSO 이벤트 기반 유저 생성 (최소 정보)<br>
     * SSO에서 사용자 생성 이벤트 수신 시 사용
     *
     * @param ssoUserRowId SSO에서 발급한 row_id
     * @param id 사용자 아이디
     * @param name 사용자명
     * @param email 이메일
     * @return User
     */
    public static User createUserFromSso(Long ssoUserRowId, String id, String name, String email) {
        User user = new User();
        user.ssoUserRowId = ssoUserRowId;
        user.id = id;
        user.name = name;
        user.email = email;
        user.isDeleted = YNType.N;
        return user;
    }

    /**
     * SSO 연동 - ssoUserRowId 설정<br>
     * 기존 사용자에 SSO 연결
     *
     * @param ssoUserRowId SSO에서 발급한 row_id
     */
    public void linkToSso(Long ssoUserRowId) {
        this.ssoUserRowId = ssoUserRowId;
    }

    /**
     * 유저 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 수정할 것
     */
    public void updateUser(String name, String email, List<Role> roles, LocalDate birth,
                           CompanyType company, String workTime,
                           YNType lunarYN, String profileName, String profileUUID, String dashboard, CountryCode countryCode) {
        if (!Objects.isNull(name)) { this.name = name; }
        if (!Objects.isNull(email)) { this.email = email; }
        if (!Objects.isNull(roles)) {
            // 새로운 역할 코드 Set 생성 (중복 제거)
            Set<String> newRoleCodes = roles.stream()
                    .map(Role::getCode)
                    .collect(Collectors.toSet());

            // 1. 기존 활성화된 역할 중 새로운 목록에 없는 것은 soft delete
            this.userRoles.stream()
                    .filter(ur -> YNType.isN(ur.getIsDeleted()))
                    .filter(ur -> !newRoleCodes.contains(ur.getRole().getCode()))
                    .forEach(UserRole::deleteUserRole);

            // 2. 새로운 역할들을 추가 (중복 방지)
            for (Role role : roles) {
                // 이미 활성화된 역할인지 확인 (soft delete된 것은 무시)
                boolean exists = this.userRoles.stream()
                        .anyMatch(ur -> ur.getRole().getCode().equals(role.getCode())
                                && YNType.isN(ur.getIsDeleted()));

                // 활성화된 역할이 없으면 새로 생성
                if (!exists) {
                    UserRole userRole = UserRole.createUserRole(this, role);
                    this.userRoles.add(userRole);
                }
            }
        }
        if (!Objects.isNull(birth)) { this.birth = birth; }
        if (!Objects.isNull(company)) { this.company = company; }
        if (!Objects.isNull(workTime)) { this.workTime = workTime; }
        if (!Objects.isNull(lunarYN)) { this.lunarYN = lunarYN; }
        if (!Objects.isNull(profileName)) { this.profileName = profileName; }
        if (!Objects.isNull(profileUUID)) { this.profileUUID = profileUUID; }
        if (!Objects.isNull(dashboard)) { this.dashboard = dashboard; }
        if (!Objects.isNull(countryCode)) { this.countryCode = countryCode; }
    }

    /**
     * 유저 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 삭제할 것
     */
    public void deleteUser() {
        this.isDeleted = YNType.Y;
    }

    /**
     * 대시보드 데이터 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 대시보드 수정할 것
     */
    public void updateDashboard(String dashboard) {
        this.dashboard = dashboard;
    }

    /* 비즈니스 편의 메소드 */
    /**
     * 사용자의 workTime에 맞춰<br>
     * start, end가 담긴 배열을 반환하는 함수
     *
     * @return [startTime, endTime] array
     */
    public List<LocalTime> convertWorkTimeToLocalTime() {
        LocalTime start = LocalTime.of(0,0);
        LocalTime end = LocalTime.of(0,0);

        switch (getWorkTime()) {
            case "8 ~ 17":
                start = LocalTime.of(8,0);
                end = LocalTime.of(17,0);
                break;
            case "9 ~ 18":
                start = LocalTime.of(9,0);
                end = LocalTime.of(18,0);
                break;
            case "10 ~ 19":
                start = LocalTime.of(10,0);
                end = LocalTime.of(19,0);
                break;
            case "13 ~ 21":
                start = LocalTime.of(13,0);
                end = LocalTime.of(21,0);
                break;
        }

        return List.of(start, end);
    }

    /**
     * 날짜가 유저의 유연근무제에 맞춰<br>
     * 정상적으로 설정되어 있는지 확인하는 함수
     *
     * @param startTime 검사 대상 시작 시간
     * @param endTime 검사 대항 종료 시간
     * @return true, false 반환
     */
    public boolean isBetweenWorkTime(LocalTime startTime, LocalTime endTime) {
        List<LocalTime> workTimes = convertWorkTimeToLocalTime();
        return ((startTime.isAfter(workTimes.get(0)) || startTime.equals(workTimes.get(0))) && startTime.isBefore(workTimes.get(1))) &&
                (endTime.isAfter(workTimes.get(0)) && (endTime.isBefore(workTimes.get(1)) || endTime.equals(workTimes.get(1))));
    }

    /* 역할 관리 편의 메소드 */

    /**
     * 역할 목록 조회<br>
     * UserRole에서 Role만 추출하여 반환
     *
     * @return 역할 리스트
     */
    public List<Role> getRoles() {
        return this.userRoles.stream()
                .filter(ur -> YNType.isN(ur.getIsDeleted()))
                .map(UserRole::getRole)
                .toList();
    }

    /**
     * 역할 추가<br>
     * 사용자에게 새로운 역할을 추가
     *
     * @param role 추가할 역할
     */
    public void addRole(Role role) {
        boolean exists = this.userRoles.stream()
                .anyMatch(ur -> ur.getRole().getCode().equals(role.getCode())
                        && YNType.isN(ur.getIsDeleted()));

        if (!exists) {
            UserRole userRole = UserRole.createUserRole(this, role);
            this.userRoles.add(userRole);
        }
    }

    /**
     * 역할 제거<br>
     * 사용자에서 특정 역할을 제거 (Soft Delete)
     *
     * @param role 제거할 역할
     */
    public void removeRole(Role role) {
        this.userRoles.stream()
                .filter(ur -> ur.getRole().getCode().equals(role.getCode())
                        && YNType.isN(ur.getIsDeleted()))
                .forEach(UserRole::deleteUserRole);
    }

    /**
     * 모든 역할 제거<br>
     * 사용자의 모든 역할을 제거
     */
    public void clearRoles() {
        this.userRoles.clear();
    }

    /**
     * 특정 역할 보유 여부 확인<br>
     * 사용자가 특정 역할을 가지고 있는지 확인
     *
     * @param roleCode 확인할 역할 코드
     * @return 역할 보유 여부
     */
    public boolean hasRole(String roleCode) {
        return this.userRoles.stream()
                .filter(ur -> YNType.isN(ur.getIsDeleted()))
                .anyMatch(ur -> ur.getRole().getCode().equals(roleCode));
    }

    /**
     * 모든 권한 코드 조회 (Spring Security용)<br>
     * 사용자의 역할(Role)과 해당 역할의 모든 권한(Permission)을 포함하여 반환
     *
     * @return 권한 코드 리스트 (예: ["ADMIN", "USER:READ", "USER:EDIT", ...])
     */
    public List<String> getAllAuthorities() {
        return this.userRoles.stream()
                .filter(ur -> YNType.isN(ur.getIsDeleted()))
                .flatMap(ur -> {
                    List<String> authorities = new ArrayList<>();
                    // 1. 역할 코드 추가 (예: "ADMIN", "MANAGER", "USER")
                    authorities.add(ur.getRole().getCode());

                    // 2. 역할의 모든 권한 코드 추가 (예: "USER:READ", "USER:EDIT", ...)
                    ur.getRole().getPermissions().stream()
                            .map(permission -> permission.getCode())
                            .forEach(authorities::add);

                    return authorities.stream();
                })
                .distinct()
                .toList();
    }

    /* 휴가 플랜 관리 편의 메소드 */

    /**
     * 휴가 플랜 목록 조회<br>
     * UserVacationPlan에서 VacationPlan만 추출하여 반환
     *
     * @return 휴가 플랜 리스트
     */
    public List<VacationPlan> getVacationPlans() {
        return this.userVacationPlans.stream()
                .filter(uvp -> YNType.isN(uvp.getIsDeleted()))
                .map(UserVacationPlan::getVacationPlan)
                .filter(plan -> YNType.isN(plan.getIsDeleted()))
                .toList();
    }

    /**
     * 모든 휴가 정책 조회<br>
     * 사용자에게 할당된 모든 플랜들의 정책을 중복 제거하여 반환
     *
     * @return 휴가 정책 리스트
     */
    public List<VacationPolicy> getAllVacationPolicies() {
        return this.userVacationPlans.stream()
                .filter(uvp -> YNType.isN(uvp.getIsDeleted()))
                .map(UserVacationPlan::getVacationPlan)
                .filter(plan -> YNType.isN(plan.getIsDeleted()))
                .flatMap(plan -> plan.getPolicies().stream())
                .distinct()
                .toList();
    }

    /**
     * 특정 휴가 플랜 보유 여부 확인<br>
     * 사용자가 특정 플랜을 가지고 있는지 확인
     *
     * @param planCode 확인할 플랜 코드
     * @return 플랜 보유 여부
     */
    public boolean hasVacationPlan(String planCode) {
        return this.userVacationPlans.stream()
                .filter(uvp -> YNType.isN(uvp.getIsDeleted()))
                .map(UserVacationPlan::getVacationPlan)
                .filter(plan -> YNType.isN(plan.getIsDeleted()))
                .anyMatch(plan -> plan.getCode().equals(planCode));
    }

    /* 초대 관련 메서드 */

    /**
     * 초대된 사용자 생성 팩토리 메서드<br>
     * SSO에서 초대 결과를 받아 HR에 사용자 생성
     *
     * @param ssoUserRowId SSO에서 발급한 row_id
     * @param id 사용자 ID
     * @param name 사용자명
     * @param email 이메일
     * @param company 회사
     * @param workTime 근무시간
     * @param joinDate 입사일
     * @param countryCode 국가코드
     * @param invitationToken 초대 토큰
     * @return 초대된 User 엔티티
     */
    public static User createInvitedUser(Long ssoUserRowId, String id, String name, String email,
                                         CompanyType company, String workTime, LocalDate joinDate,
                                         CountryCode countryCode, String invitationToken) {
        User user = new User();
        user.ssoUserRowId = ssoUserRowId;
        user.id = id;
        user.name = name;
        user.email = email;
        user.company = company;
        user.workTime = workTime;
        user.joinDate = joinDate;
        user.countryCode = countryCode;
        user.lunarYN = YNType.N;
        user.isDeleted = YNType.N;
        user.invitationStatus = StatusType.PENDING;
        user.invitationToken = invitationToken;
        user.invitationSentAt = LocalDateTime.now();
        user.invitationExpiresAt = LocalDateTime.now().plusHours(48);
        return user;
    }

    /**
     * 초대 정보 설정<br>
     * SSO 초대 결과를 바탕으로 초대 정보 설정
     *
     * @param token 초대 토큰
     * @param sentAt 발송 시간
     * @param expiresAt 만료 시간
     * @param status 초대 상태
     */
    public void setInvitationInfo(String token, LocalDateTime sentAt, LocalDateTime expiresAt, StatusType status) {
        this.invitationToken = token;
        this.invitationSentAt = sentAt;
        this.invitationExpiresAt = expiresAt;
        this.invitationStatus = status;
    }

    /**
     * 회원가입 완료 처리<br>
     * SSO에서 USER_UPDATED 이벤트 수신 시 호출
     *
     * @param newId 변경된 사용자 ID (null이면 유지)
     * @param newEmail 변경된 이메일 (null이면 유지)
     */
    public void completeRegistration(String newId, String newEmail) {
        if (newId != null && !newId.equals(this.id)) {
            this.id = newId;
        }
        if (newEmail != null && !newEmail.equals(this.email)) {
            this.email = newEmail;
        }
        this.invitationStatus = StatusType.ACTIVE;
        this.registeredAt = LocalDateTime.now();
        this.invitationToken = null;
    }

    /**
     * 초대 토큰 갱신<br>
     * 초대 재전송 시 토큰 및 만료 시간 갱신
     *
     * @param newToken 새 초대 토큰
     */
    public void renewInvitationToken(String newToken) {
        this.invitationToken = newToken;
        this.invitationSentAt = LocalDateTime.now();
        this.invitationExpiresAt = LocalDateTime.now().plusHours(48);
        this.invitationStatus = StatusType.PENDING;
    }

    /**
     * 초대 대기 상태 여부 확인
     *
     * @return PENDING 상태면 true
     */
    public boolean isPendingInvitation() {
        return this.invitationStatus == StatusType.PENDING;
    }

    /**
     * SSO 연동된 기존 사용자 연결 처리<br>
     * 이미 SSO에 등록된 사용자가 HR에 연결되는 경우
     *
     * @param ssoUserRowId SSO에서 발급한 row_id
     */
    public void linkExistingSsoUser(Long ssoUserRowId) {
        this.ssoUserRowId = ssoUserRowId;
        this.invitationStatus = StatusType.ACTIVE;
        this.registeredAt = LocalDateTime.now();
    }
}
