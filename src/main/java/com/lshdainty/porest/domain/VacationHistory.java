package com.lshdainty.porest.domain;

import com.lshdainty.porest.type.VacationTimeType;
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
     * 휴가 등록 내역 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 등록 내역 생성할 것
     *
     * @return VacationHistory
     */
    public static VacationHistory createRegistVacationHistory(Vacation vacation, String desc, BigDecimal grantTime, String crtUserId, String clientIP) {
        VacationHistory vacationHistory = new VacationHistory();
        vacationHistory.vacation = vacation;
        vacationHistory.desc = desc;
        vacationHistory.grantTime = grantTime;
        vacationHistory.delYN = "N";
        vacationHistory.setCreated(crtUserId, clientIP);
        return vacationHistory;
    }

    /**
     * 휴가 사용 내역 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 사용 내역 생성할 것
     *
     * @return VacationHistory
     */
    public static VacationHistory createUseVacationHistory(Vacation vacation, String desc, VacationTimeType type, LocalDateTime usedDateTime, String crtUserId, String clientIP) {
        VacationHistory vacationHistory = new VacationHistory();
        vacationHistory.vacation = vacation;
        vacationHistory.desc = desc;
        vacationHistory.type = type;
        vacationHistory.usedDateTime = usedDateTime;
        vacationHistory.delYN = "N";
        vacationHistory.setCreated(crtUserId, clientIP);
        return vacationHistory;
    }

    /**
     * 휴가 추가 내역 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 추가 내역 삭제할 것
     */
    public void deleteRegistVacationHistory(Vacation vacation, String mdfUserId, String clientIP) {
        vacation.deductedVacation(getGrantTime(), mdfUserId, clientIP);
        this.delYN = "Y";
        this.setModified(LocalDateTime.now(), mdfUserId, clientIP);
    }

    /**
     * 휴가 사용 내역 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 사용 내역 삭제할 것
     */
    public void deleteUseVacationHistory(Vacation vacation,String mdfUserId, String clientIP) {
        vacation.addVacation(getType().convertToValue(1), mdfUserId, clientIP);
        this.delYN = "Y";
        this.setModified(LocalDateTime.now(), mdfUserId, clientIP);
    }
}
