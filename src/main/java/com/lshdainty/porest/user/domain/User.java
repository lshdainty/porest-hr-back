package com.lshdainty.porest.user.domain;

import com.lshdainty.porest.common.converter.CompanyTypeConverter;
import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.CompanyType;
import com.lshdainty.porest.department.domain.UserDepartment;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.domain.UserRole;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.type.StatusType;
import com.lshdainty.porest.vacation.domain.UserVacationPlan;
import com.lshdainty.porest.vacation.domain.VacationApproval;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.lshdainty.porest.vacation.domain.VacationPlan;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.domain.VacationUsage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.security.SecureRandom;
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
public class User extends AuditingFields {
    /**
     * 사용자 순번<br>
     * 테이블 관리용 Primary Key (자동 생성)
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    private Long no;

    /**
     * 사용자 아이디<br>
     * 비즈니스 식별자 (고유값)
     */
    @Column(name = "user_id", length = 20, unique = true, nullable = false)
    private String id;

    /**
     * 사용자 비밀번호<br>
     * 암호화된 비밀번호 저장
     */
    @Column(name = "user_pwd", length = 100)
    private String pwd;

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
     * 회원가입 초대 토큰/코드<br>
     * 회원가입 초대 시 사용되는 토큰 (초대 코드의 경우 8자리 영숫자)
     */
    @Column(name = "invitation_token", length = 36)
    private String invitationToken;

    /**
     * 회원가입 상태<br>
     * 초대 진행 상태 (PENDING, ACTIVE 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status", length = 10)
    private StatusType invitationStatus;

    /**
     * 초대 메일 전송 일시<br>
     * 초대 이메일이 발송된 일시
     */
    @Column(name = "invitation_sent_at")
    private LocalDateTime invitationSentAt;

    /**
     * 초대 메일 만료 일시<br>
     * 초대 토큰이 만료되는 일시
     */
    @Column(name = "invitation_expires_at")
    private LocalDateTime invitationExpiresAt;

    /**
     * 회원가입 일시<br>
     * 사용자가 회원가입을 완료한 일시
     */
    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1)
    private YNType isDeleted;

    /**
     * 비밀번호 변경 필요 여부<br>
     * 임시 비밀번호로 초대된 사용자의 첫 로그인 시 비밀번호 변경 강제
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "pwd_change_required", nullable = false, length = 1)
    private YNType passwordChangeRequired = YNType.N;

    /**
     * 대시보드 레이아웃 정보<br>
     * 사용자 맞춤 대시보드 설정 정보 (JSON 형식)
     */
    @Lob
    @Column(name = "dashboard", length = 65535)
    private String dashboard;

    /**
     * 유저 OAuth 제공자 목록<br>
     * 사용자의 소셜 로그인 연동 정보 목록
     */
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserProvider> providers = new ArrayList<>();

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
     * 관리자가 초대용 사용자 생성<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 생성할 것
     *
     * @return User
     */
    public static User createUser(String id, String pwd, String name, String email, LocalDate birth,
                                  CompanyType company, String workTime,
                                  YNType lunarYN, String profileName, String profileUUID, CountryCode countryCode) {
        User user = new User();
        user.id = id;
        user.pwd = pwd;
        user.name = name;
        user.email = email;
        user.birth = birth;
        user.company = company;
        user.workTime = workTime;
        user.lunarYN = lunarYN;
        user.profileName = profileName;
        user.profileUUID = profileUUID;
        user.countryCode = countryCode;
        user.isDeleted = YNType.N;
        return user;
    }

    public static User createUser(String id) {
        User user = new User();
        user.id = id;
        user.isDeleted = YNType.N;
        return user;
    }

    /**
     * 8자리 초대 코드 생성 (숫자 + 대문자)<br>
     * 사용자가 직접 입력해야 하므로 짧고 입력하기 쉬운 형태로 생성
     *
     * @return 8자리 영숫자 코드 (예: A3B7K9X2)
     */
    private static String generateInvitationCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 관리자가 초대용 사용자 생성<br>
     * 8자리 초대 코드와 만료 시간을 자동으로 생성하여 PENDING 상태의 사용자 생성
     *
     * @return User
     */
    public static User createInvitedUser(String id, String name, String email,
                                       CompanyType company, String workTime, LocalDate joinDate, CountryCode countryCode) {
        User user = new User();
        user.id = id;
        user.name = name;
        user.email = email;
        user.company = company;
        user.workTime = workTime;
        user.joinDate = joinDate;
        user.countryCode = countryCode;
        user.invitationStatus = StatusType.PENDING; // 초대 상태로 설정
        user.lunarYN = YNType.N;
        user.isDeleted = YNType.N;

        // 8자리 초대 코드 생성 (48시간 유효)
        user.invitationToken = generateInvitationCode();
        user.invitationSentAt = LocalDateTime.now();
        user.invitationExpiresAt = LocalDateTime.now().plusHours(48);

        return user;
    }

    /**
     * 초대 이메일 재전송을 위한 코드 갱신
     */
    public void renewInvitationToken() {
        this.invitationToken = generateInvitationCode();
        this.invitationSentAt = LocalDateTime.now();
        this.invitationExpiresAt = LocalDateTime.now().plusHours(48);
        this.invitationStatus = StatusType.PENDING;
    }

    /**
     * 초대된 사용자 정보 수정<br>
     * PENDING 상태인 사용자만 수정 가능
     */
    public void updateInvitedUser(String name, String email, CompanyType company, String workTime, LocalDate joinDate, CountryCode countryCode) {
        if (!Objects.isNull(name)) { this.name = name; }
        if (!Objects.isNull(email)) { this.email = email; }
        if (!Objects.isNull(company)) { this.company = company; }
        if (!Objects.isNull(workTime)) { this.workTime = workTime; }
        if (!Objects.isNull(joinDate)) { this.joinDate = joinDate; }
        if (!Objects.isNull(countryCode)) { this.countryCode = countryCode; }
    }

    /**
     * 초대 유효성 검사
     */
    public boolean isInvitationValid() {
        return this.invitationStatus == StatusType.PENDING &&
               this.invitationExpiresAt != null &&
               this.invitationExpiresAt.isAfter(LocalDateTime.now()) &&
               this.invitationToken != null;
    }

    /**
     * 회원가입 완료 처리<br>
     * 초대받은 사용자가 회원가입을 완료할 때 호출<br>
     * 새 ID/PW로 변경하고 추가 정보를 입력하여 회원가입 완료
     *
     * @param newId 새로운 사용자 ID
     * @param newEmail 새로운 이메일 (null이면 변경하지 않음)
     * @param newEncodedPassword 암호화된 새 비밀번호
     * @param birth 생년월일
     * @param lunarYN 음력 여부
     */
    public void completeRegistration(String newId, String newEmail, String newEncodedPassword, LocalDate birth, YNType lunarYN) {
        this.id = newId;
        if (newEmail != null && !newEmail.isBlank()) {
            this.email = newEmail;
        }
        this.pwd = newEncodedPassword;
        this.birth = birth;
        this.lunarYN = lunarYN;
        this.invitationStatus = StatusType.ACTIVE;
        this.registeredAt = LocalDateTime.now();
        // 초대 관련 필드 정리
        this.invitationToken = null;
        this.invitationSentAt = null;
        this.invitationExpiresAt = null;
    }

    /**
     * OAuth 기반 회원가입 완료
     * OAuth 연동 후 생년월일과 음력 여부만 설정하여 회원가입 완료
     * (ID/PW는 OAuth로 대체되므로 변경하지 않음)
     */
    public void completeOAuthRegistration(LocalDate birth, YNType lunarYN) {
        this.birth = birth;
        this.lunarYN = lunarYN;
        this.invitationStatus = StatusType.ACTIVE;
        this.registeredAt = LocalDateTime.now();
        // 초대 관련 필드 정리
        this.invitationToken = null;
        this.invitationSentAt = null;
        this.invitationExpiresAt = null;
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

    /**
     * 비밀번호 변경 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 비밀번호 변경할 것
     *
     * @param encodedPassword 암호화된 비밀번호
     */
    public void updatePassword(String encodedPassword) {
        this.pwd = encodedPassword;
    }

    /**
     * 비밀번호 변경 필요 플래그 설정<br>
     * 임시 비밀번호 발급 시 호출
     */
    public void requirePasswordChange() {
        this.passwordChangeRequired = YNType.Y;
    }

    /**
     * 비밀번호 변경 완료 후 플래그 해제<br>
     * 사용자가 비밀번호 변경 완료 시 호출
     */
    public void clearPasswordChangeRequired() {
        this.passwordChangeRequired = YNType.N;
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
}
