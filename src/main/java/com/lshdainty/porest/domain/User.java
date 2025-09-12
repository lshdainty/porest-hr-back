package com.lshdainty.porest.domain;

import com.lshdainty.porest.type.CompanyType;
import com.lshdainty.porest.type.DepartmentType;
import com.lshdainty.porest.type.RoleType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "users")
public class User {
    @Id
    @Column(name = "user_id")
    private String id; // 유저 아이디

    @Column(name = "user_pwd")
    private String pwd; // 유저 비번

    @Column(name = "user_name")
    private String name; // 유저 이름

    @Column(name = "user_email")
    private String email; // 유저 이메일

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private RoleType role; //

    @Column(name = "user_birth")
    private String birth; // 유저 생일

    @Column(name = "user_work_time")
    private String workTime; // 유연근무제

    @Enumerated(EnumType.STRING)
    @Column(name = "user_company")
    private CompanyType company; // 유저 소속 회사

    @Enumerated(EnumType.STRING)
    @Column(name = "user_department")
    private DepartmentType department; // 유저 소속 부서

    @Column(name = "profile_name")
    private String profileName;

    @Column(name = "profile_uuid")
    private String profileUUID;

    @Column(name = "lunar_yn")
    private String lunarYN; // 음력여부

    @Column(name = "del_yn")
    private String delYN; // 삭제여부

    @OneToMany(mappedBy = "user")   // JPA에서는 mappedBy는 읽기 전용
    private List<Vacation> vacations  =  new ArrayList<>();

    /**
     * 유저 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 생성할 것
     *
     * @return User
     */
    public static User createUser(String id, String pwd, String name, String email, String birth,
                                  CompanyType company, DepartmentType department, String workTime,
                                  String lunarYN, String profileName, String profileUUID) {
        User user = new User();
        user.id = id;
        user.pwd = pwd;
        user.name = name;
        user.email = email;
        user.role = RoleType.USER;
        user.birth = birth;
        user.company = company;
        user.department = department;
        user.workTime = workTime;
        user.lunarYN = lunarYN;
        user.profileName = profileName;
        user.profileUUID = profileUUID;
        user.delYN = "N";
        return user;
    }

    public static User createUser(String id, String pwd, String name, String email, String birth,
                                  CompanyType company, DepartmentType department, String workTime, String lunarYN) {
        return createUser(id, pwd, name, email, birth, company, department, workTime, lunarYN, null, null);
    }

    public static User createUser(String id) {
        User user = new User();
        user.id = id;
        user.delYN = "N";
        return user;
    }

    /**
     * 유저 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 수정할 것
     */
    public void updateUser(String name, String email, RoleType role, String birth,
                           CompanyType company, DepartmentType department, String workTime,
                           String lunarYN, String profileName, String profileUUID) {
        if (!Objects.isNull(name)) { this.name = name; }
        if (!Objects.isNull(email)) { this.email = email; }
        if (!Objects.isNull(role)) { this.role = role; }
        if (!Objects.isNull(birth)) { this.birth = birth; }
        if (!Objects.isNull(company)) { this.company = company; }
        if (!Objects.isNull(department)) { this.department = department; }
        if (!Objects.isNull(workTime)) { this.workTime = workTime; }
        if (!Objects.isNull(lunarYN)) { this.lunarYN = lunarYN; }
        if (!Objects.isNull(profileName)) { this.profileName = profileName; }
        if (!Objects.isNull(profileUUID)) { this.profileUUID = profileUUID; }
    }

    public void updateUser(String name, String email, RoleType role, String birth,
                           CompanyType company, DepartmentType department, String workTime, String lunarYN) {
        updateUser(name, email, role, birth, company, department, workTime, lunarYN, null, null);
    }

    /**
     * 유저 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 삭제할 것
     */
    public void deleteUser() {
        this.delYN = "Y";
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
}
