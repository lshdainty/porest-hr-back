package com.lshdainty.porest.user.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.department.domain.UserDepartment;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.domain.UserRole;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.type.StatusType;
import com.lshdainty.porest.vacation.domain.UserVacationPolicy;
import com.lshdainty.porest.vacation.domain.VacationApproval;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.lshdainty.porest.vacation.domain.VacationUsage;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "users")
public class User extends AuditingFields {
    @Id
    @Column(name = "user_id")
    private String id; // 유저 아이디

    @Column(name = "user_pwd")
    private String pwd; // 유저 비번

    @Column(name = "user_name")
    private String name; // 유저 이름

    @Column(name = "user_email")
    private String email; // 유저 이메일

    @Column(name = "user_birth")
    private LocalDate birth; // 유저 생일

    @Column(name = "user_work_time")
    private String workTime; // 유연근무제

    @Column(name = "join_date")
    private LocalDate joinDate; // 입사일

    @Enumerated(EnumType.STRING)
    @Column(name = "user_origin_company")
    private OriginCompanyType company; // 유저 원소속 회사

    @Column(name = "profile_name")
    private String profileName;

    @Column(name = "profile_uuid")
    private String profileUUID;

    @Enumerated(EnumType.STRING)
    @Column(name = "lunar_yn")
    private YNType lunarYN; // 음력여부

    @Column(name = "invitation_token")
    private String invitationToken; // 초대 토큰

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status")
    private StatusType invitationStatus; // 초대 상태

    @Column(name = "invitation_sent_at")
    private LocalDateTime invitationSentAt; // 초대 발송 시간

    @Column(name = "invitation_expires_at")
    private LocalDateTime invitationExpiresAt; // 초대 만료 시간

    @Column(name = "registered_at")
    private LocalDateTime registeredAt; // 실제 회원가입 완료 시간

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted")
    private YNType isDeleted; // 삭제여부

    @Column(name = "dashboard", columnDefinition = "json")
    private String dashboard; // 대시보드 데이터

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)   // JPA에서는 mappedBy는 읽기 전용
    private List<UserProvider> providers = new ArrayList<>();

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VacationGrant> vacationGrants = new ArrayList<>();

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VacationUsage> vacationUsages = new ArrayList<>();

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserVacationPolicy> userVacationPolicies = new ArrayList<>();

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserDepartment> userDepartments = new ArrayList<>();

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "approver", cascade = CascadeType.ALL)
    private List<VacationApproval> vacationApprovals = new ArrayList<>();

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
                                  OriginCompanyType company, String workTime,
                                  YNType lunarYN, String profileName, String profileUUID) {
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
     * 관리자가 초대용 사용자 생성<br>
     * 초대 토큰과 만료 시간을 자동으로 생성하여 PENDING 상태의 사용자 생성
     *
     * @return User
     */
    public static User createInvitedUser(String id, String name, String email,
                                       OriginCompanyType company, String workTime, LocalDate joinDate) {
        User user = new User();
        user.id = id;
        user.name = name;
        user.email = email;
        user.company = company;
        user.workTime = workTime;
        user.joinDate = joinDate;
        user.invitationStatus = StatusType.PENDING; // 초대 상태로 설정
        user.isDeleted = YNType.N;

        // 초대 토큰 생성 (48시간 유효)
        user.invitationToken = UUID.randomUUID().toString();
        user.invitationSentAt = LocalDateTime.now();
        user.invitationExpiresAt = LocalDateTime.now().plusHours(48);

        return user;
    }

    /**
     * 초대 이메일 재전송을 위한 토큰 갱신
     */
    public void renewInvitationToken() {
        this.invitationToken = UUID.randomUUID().toString();
        this.invitationSentAt = LocalDateTime.now();
        this.invitationExpiresAt = LocalDateTime.now().plusHours(48);
        this.invitationStatus = StatusType.PENDING;
    }

    /**
     * 초대된 사용자 정보 수정<br>
     * PENDING 상태인 사용자만 수정 가능
     */
    public void updateInvitedUser(String name, String email, OriginCompanyType company, String workTime, LocalDate joinDate) {
        if (!Objects.isNull(name)) { this.name = name; }
        if (!Objects.isNull(email)) { this.email = email; }
        if (!Objects.isNull(company)) { this.company = company; }
        if (!Objects.isNull(workTime)) { this.workTime = workTime; }
        if (!Objects.isNull(joinDate)) { this.joinDate = joinDate; }
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
     * 회원가입 완료 처리
     */
    public void completeRegistration(LocalDate birth, YNType lunarYN) {
        this.birth = birth;
        this.lunarYN = lunarYN;
        this.invitationStatus = StatusType.ACTIVE;
        this.registeredAt = LocalDateTime.now();
        this.invitationSentAt = null;
        this.invitationExpiresAt = null;
        this.invitationToken = null; // 토큰 제거
    }

    /**
     * 유저 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 수정할 것
     */
    public void updateUser(String name, String email, List<Role> roles, LocalDate birth,
                           OriginCompanyType company, String workTime,
                           YNType lunarYN, String profileName, String profileUUID, String dashboard) {
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
            case "8 ~ 5":
                start = LocalTime.of(8,0);
                end = LocalTime.of(17,0);
                break;
            case "9 ~ 6":
                start = LocalTime.of(9,0);
                end = LocalTime.of(18,0);
                break;
            case "10 ~ 7":
                start = LocalTime.of(10,0);
                end = LocalTime.of(19,0);
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
}
