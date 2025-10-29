package com.lshdainty.porest.vacation.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.type.GrantMethod;
import com.lshdainty.porest.vacation.type.GrantTiming;
import com.lshdainty.porest.vacation.type.RepeatUnit;
import com.lshdainty.porest.vacation.type.VacationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ToString
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "vacation_policy")
public class VacationPolicy extends AuditingFields {
    @Id @GeneratedValue
    @Column(name = "vacation_policy_id")
    private Long id;                        // 휴가 정책 아이디

    @Column(name = "vacation_policy_name")
    private String name;                    // 휴가 정책 이름

    @Column(name = "vacation_policy_desc")
    private String desc;                    // 휴가 정책 설명

    @Enumerated(EnumType.STRING)
    @Column(name = "vacation_type")
    private VacationType  vacationType;     // 휴가 타입

    @Enumerated(EnumType.STRING)
    @Column(name = "grant_method")
    private GrantMethod grantMethod;        // 부여 방법
    
    @Column(name = "grant_time")
    private BigDecimal grantTime;           // 부여 시간

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_unit")
    private RepeatUnit repeatUnit;          // 반복 단위
    
    @Column(name = "repeat_Interval")
    private Integer repeatInterval;         // 반복 간격

    @Enumerated(EnumType.STRING)
    @Column(name = "grant_timing")
    private GrantTiming grantTiming;        // 부여 시점 지정 방식

    @Column(name = "specific_months")
    private Integer specificMonths;         // 특정 월 지정

    @Column(name = "specific_days")
    private Integer specificDays;           // 특정 일 지정

    @Column(name = "first_grant_date")
    private LocalDateTime firstGrantDate;   // 첫 부여 시점 (반복 부여의 기준일, 스케줄러가 이 날짜 기준으로 다음 부여일 계산)

    @Enumerated(EnumType.STRING)
    @Column(name = "can_deleted")
    private YNType canDeleted;              // 삭제 가능 여부

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted")
    private YNType isDeleted;               // 삭제 여부

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "vacationPolicy", cascade = CascadeType.ALL)
    private List<UserVacationPolicy> userVacationPolicies = new ArrayList<>();

    // 개발용
    public void updateCantDeleted() {
        this.canDeleted = YNType.N;
        this.isDeleted = YNType.N;
    }

    // 개발용
    public void updateCanDeleted() {
        this.canDeleted = YNType.Y;
        this.isDeleted = YNType.N;
    }

    /**
     * 휴가 정책 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 생성할 것
     *
     * @return VacationPolicy
     */
    public static VacationPolicy createVacationPolicy(String name, String desc, VacationType vacationType, GrantMethod grantMethod, BigDecimal grantTime, RepeatUnit repeatUnit, Integer repeatInterval, GrantTiming grantTiming, Integer specificMonths, Integer specificDays, LocalDateTime firstGrantDate) {
        VacationPolicy vacationPolicy = new VacationPolicy();
        vacationPolicy.name = name;
        vacationPolicy.desc = desc;
        vacationPolicy.vacationType = vacationType;
        vacationPolicy.grantMethod = grantMethod;
        vacationPolicy.grantTime = grantTime;
        vacationPolicy.repeatUnit = repeatUnit;
        vacationPolicy.repeatInterval = repeatInterval;
        vacationPolicy.grantTiming = grantTiming;
        vacationPolicy.specificMonths = specificMonths;
        vacationPolicy.specificDays = specificDays;
        vacationPolicy.firstGrantDate = firstGrantDate;
        vacationPolicy.canDeleted = YNType.Y;
        vacationPolicy.isDeleted = YNType.N;
        return vacationPolicy;
    }

    /**
     * 휴가 정책 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 정책 삭제할 것
     */
    public void deleteVacationPolicy() {
        this.isDeleted = YNType.Y;
    }
}
