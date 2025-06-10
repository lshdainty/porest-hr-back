package com.lshdainty.myhr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "users")
public class User {
    @Id @GeneratedValue
    @Column(name = "user_no")
    private Long id;

    @Column(name = "user_name")
    private String name;

    @Column(name = "user_pwd")
    private String pwd;

    @Column(name = "user_ip")
    private String ip;

    @Column(name = "user_role")
    private int role;

    @Column(name = "user_birth")
    private String birth;

    @Column(name = "user_work_time")
    private String workTime;

    @Column(name = "user_employ")
    private String employ;

    @Column(name = "lunar_yn")
    private String lunarYN;

    @Column(name = "del_yn")
    private String delYN;

    @OneToMany(mappedBy = "user")   // JPA에서는 mappedBy는 읽기 전용
    private List<Vacation> vacations;

    // 유저 생성자 (setter말고 해당 메소드 사용할 것)
    public static User createUser(String name, String birth, String employ, String workTime, String lunarYN) {
        User user = new User();
        user.name = name;
        user.birth = birth;
        user.employ = employ;
        user.workTime = workTime;
        user.lunarYN = lunarYN;
        user.delYN = "N";
        user.vacations = new ArrayList<>();
        return user;
    }

    public static User createUser(Long id) {
        User user = new User();
        user.id = id;
        return user;
    }

    // 유저 수정 (setter말고 해당 메소드 사용할 것)
    public void updateUser(String name, String birth, String employ, String workTime, String lunarYN) {
        if (!Objects.isNull(name)) { this.name = name; }
        if (!Objects.isNull(birth)) { this.birth = birth; }
        if (!Objects.isNull(employ)) { this.employ = employ; }
        if (!Objects.isNull(workTime)) { this.workTime = workTime; }
        if (!Objects.isNull(lunarYN)) { this.lunarYN = lunarYN; }
    }

    // 유저 삭제 (setter말고 해당 메소드 사용할 것)
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
     * @Param stratTime
     * @Param endTime
     * @return true, false 반환
     */
    public boolean isBetweenWorkTime(LocalTime startTime, LocalTime endTime) {
        List<LocalTime> workTimes = convertWorkTimeToLocalTime();
        return ((startTime.isAfter(workTimes.get(0)) || startTime.equals(workTimes.get(0))) && startTime.isBefore(workTimes.get(1))) &&
                (endTime.isAfter(workTimes.get(0)) && (endTime.isBefore(workTimes.get(1)) || endTime.equals(workTimes.get(1))));
    }
}
