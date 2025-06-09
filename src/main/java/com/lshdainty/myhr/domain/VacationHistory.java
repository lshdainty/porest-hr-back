package com.lshdainty.myhr.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "vacation_history")
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

    @Override
    public String toString() {
        return "VacationHistory{" +
                "id=" + id +
                ", desc='" + desc + '\'' +
                ", grantTime=" + grantTime +
                ", type=" + type +
                ", usedDateTime=" + usedDateTime +
                ", delYN='" + delYN + '\'' +
                '}';
    }

    /**
     * 휴가 등록 내역 생성자
     * 휴가 등록에 따른 History 내역을 생성해주는 생성자
     * Setter를 사용하지 말고 해당 생성자를 통해 생성 및 사용할 것
     */
    public static VacationHistory createRegistVacationHistory(Vacation vacation, String desc, BigDecimal grantTime, Long crtUserNo, String clientIP) {
        VacationHistory vacationHistory = new VacationHistory();
        vacationHistory.vacation = vacation;
        vacationHistory.desc = desc;
        vacationHistory.grantTime = grantTime;
        vacationHistory.delYN = "N";
        vacationHistory.setCreated(crtUserNo, clientIP);
        return vacationHistory;
    }

    /**
     * 휴가 사용 내역 생성자
     * 휴가 사용에 따른 History 내역을 생성해주는 생성자
     * Setter를 사용하지 말고 해당 생성자를 통해 생성 및 사용할 것
     */
    public static VacationHistory createUseVacationHistory(Vacation vacation, String desc, VacationTimeType type, LocalDateTime usedDateTime, Long crtUserNo, String clientIP) {
        VacationHistory vacationHistory = new VacationHistory();
        vacationHistory.vacation = vacation;
        vacationHistory.desc = desc;
        vacationHistory.type = type;
        vacationHistory.usedDateTime = usedDateTime;
        vacationHistory.delYN = "N";
        vacationHistory.setCreated(crtUserNo, clientIP);
        return vacationHistory;
    }

    /**
     * 휴가 추가 내역 삭제
     * Setter를 사용하지 말고 해당 메소드를 통해 설정할 것
     */
    public void deleteRegistVacationHistory(Vacation vacation, Long mdfUserNo, String clientIP) {
        vacation.deductedVacation(getGrantTime(), mdfUserNo, clientIP);
        this.delYN = "Y";
        this.setModified(LocalDateTime.now(), mdfUserNo, clientIP);
    }

    /**
     * 휴가 사용 내역 삭제
     * Setter를 사용하지 말고 해당 메소드를 통해 설정할 것
     */
    public void deleteUseVacationHistory(Vacation vacation,Long mdfUserNo, String clientIP) {
        vacation.addVacation(getType().convertToValue(1), mdfUserNo, clientIP);
        this.delYN = "Y";
        this.setModified(LocalDateTime.now(), mdfUserNo, clientIP);
    }
}
