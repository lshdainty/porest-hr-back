package com.lshdainty.porest.domain;

import com.lshdainty.porest.type.vacation.GrantMethod;
import com.lshdainty.porest.type.vacation.GrantTiming;
import com.lshdainty.porest.type.vacation.RepeatUnit;
import com.lshdainty.porest.type.vacation.VacationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ToString
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "vacation")
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
    
    @Column(name = "grant_amount")
    private BigDecimal grantAmount;         // 부여 수량

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_unit")
    private RepeatUnit repeatUnit;          // 반복 단위
    
    @Column(name = "repeat_Interval")
    private Integer repeatInterval;         // 반복 간격

    @Enumerated(EnumType.STRING)
    @Column(name = "grant_timing")
    private GrantTiming grantTiming;        // 부여 시점 지정 방식

    @Column(name = "specific_months")
    private Integer specificMonths;          // 특정 월 지정

    @Column(name = "specific_days")
    private Integer specificDays;            // 특정 일 지정

//    /**
//     * 휴가 정책 생성 함수<br>
//     * Entity의 경우 Setter없이 Getter만 사용<br>
//     * 해당 메소드를 통해 휴가 생성할 것
//     *
//     * @return VacationPolicy
//     */
//    public static VacationPolicy createVacationPolicy(String name, String desc, VacationType vacationType, GrantMethod grantMethod, BigDecimal grantAmount, RepeatUnit repeatUnit, LocalDateTime grantBaseDate) {
//        VacationPolicy vacationPolicy = new VacationPolicy();
//        vacationPolicy.name = name;
//        vacationPolicy.desc = desc;
//        vacationPolicy.vacationType = vacationType;
//        vacationPolicy.grantMethod = grantMethod;
//        vacationPolicy.grantAmount = grantAmount;
//        vacationPolicy.repeatUnit = repeatUnit;
//        vacationPolicy.grantBaseDate = grantBaseDate;
//        return vacationPolicy;
//    }
}
