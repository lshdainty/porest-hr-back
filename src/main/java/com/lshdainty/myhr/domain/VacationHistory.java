package com.lshdainty.myhr.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@ToString
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "deptop_vacation_history")
public class VacationHistory extends AuditingFields {
    @Id @GeneratedValue
    @Column(name = "vacation_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_id")
    @Setter
    private Vacation vacation;

    @Column(name = "vacation_history_desc")
    private String desc;

    // grantTime만 있으면 휴가 추가 내역
    @Column(name = "grant_time", precision = 7, scale = 4)
    private BigDecimal grantTime;

    // grantTime을 제외한 type, usedDateTime 있으면 휴가 사용 내역
    @Enumerated(EnumType.STRING)
    @Column(name = "vacation_time_type")
    private VacationTimeType type;

    @Column(name = "used_date_time")
    private LocalDateTime usedDateTime;

    @Column(name = "del_yn")
    private String delYN;

    /**
     * 휴가 등록 내역 생성자
     * 휴가 등록에 따른 History 내역을 생성해주는 생성자
     * Setter를 사용하지 말고 해당 생성자를 통해 생성 및 사용할 것
     */
    public static VacationHistory createRegistVacationHistory(Vacation vacation, String desc, BigDecimal grantTime, Long userNo, String clientIP) {
        VacationHistory vacationHistory = new VacationHistory();
        vacationHistory.vacation = vacation;
        vacationHistory.desc = desc;
        vacationHistory.grantTime = grantTime;
        vacationHistory.delYN = "N";
        vacationHistory.setCreated(userNo, clientIP);
        return vacationHistory;
    }

    /**
     * 휴가 사용 내역 생성자
     * 휴가 사용에 따른 History 내역을 생성해주는 생성자
     * Setter를 사용하지 말고 해당 생성자를 통해 생성 및 사용할 것
     */
    public static VacationHistory createUseVacationHistory(Vacation vacation, String desc, VacationTimeType type, LocalDateTime usedDateTime, Long userNo, String clientIP) {
        VacationHistory vacationHistory = new VacationHistory();
        vacationHistory.vacation = vacation;
        vacationHistory.desc = desc;
        vacationHistory.type = type;
        vacationHistory.usedDateTime = usedDateTime;
        vacationHistory.delYN = "N";
        vacationHistory.setCreated(userNo, clientIP);
        return vacationHistory;
    }

    /**
     * 휴가 내역 삭제
     * Setter를 사용하지 말고 해당 메소드를 통해 설정할 것
     */
    public void deleteVacationHistory(Long userNo, String clientIP) {
        this.delYN = "Y";
        this.setmodified(LocalDateTime.now(), userNo, clientIP);
    }
}
