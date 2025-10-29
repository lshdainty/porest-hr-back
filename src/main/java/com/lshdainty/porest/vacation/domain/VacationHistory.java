package com.lshdainty.porest.vacation.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.vacation.type.VacationTimeType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "vacation_history")
public class VacationHistory extends AuditingFields {
    /**
     * 휴가 내역 아이디
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue
    @Column(name = "vacation_history_id")
    private Long id;

    /**
     * 휴가 아이디
     * 휴가 타입이 같은 휴가인 경우 잔여 시간을 합하여 관리
     * 예) 25년도 연차 휴가
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_id")
    @Setter
    private Vacation vacation;

    /**
     * 휴가 내역 설명
     * 사용자에게 부여한 혹은 사용내역에 대한 설명
     */
    @Column(name = "vacation_history_desc")
    private String desc;

    /**
     * 휴가 부여 시간
     * 휴가 정책에 따른 휴가 부여 내역
     * (grantTime만 있다면 휴가 부여 내역으로 간주)
     */
    @Column(name = "grant_time", precision = 7, scale = 4)
    private BigDecimal grantTime;

    /**
     * 휴가 사용 시간 타입
     * 사용자가 사용한 휴가 시간 타입
     * (type, usedDateTime이 있다면 휴가 사용 내역으로 간주)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "vacation_time_type")
    private VacationTimeType type;

    /**
     * 휴가 사용 시간
     * 사용자가 사용한 휴가 시간
     * (type, usedDateTime이 있다면 휴가 사용 내역으로 간주)
     */
    @Column(name = "used_date_time")
    private LocalDateTime usedDateTime;

    /* 휴가 내역 삭제 여부 */
    @Column(name = "is_deleted")
    private String isDeleted;

    @Override
    public String toString() {
        return "VacationHistory{" +
                "id=" + id +
                ", desc='" + desc + '\'' +
                ", grantTime=" + grantTime +
                ", type=" + type +
                ", usedDateTime=" + usedDateTime +
                ", isDeleted='" + isDeleted + '\'' +
                '}';
    }

    /**
     * 휴가 등록 내역 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 등록 내역 생성할 것
     *
     * @return VacationHistory
     */
    public static VacationHistory createRegistVacationHistory(Vacation vacation, String desc, BigDecimal grantTime) {
        VacationHistory vacationHistory = new VacationHistory();
        vacationHistory.vacation = vacation;
        vacationHistory.desc = desc;
        vacationHistory.grantTime = grantTime;
        vacationHistory.isDeleted = "N";
        return vacationHistory;
    }

    /**
     * 휴가 사용 내역 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 사용 내역 생성할 것
     *
     * @return VacationHistory
     */
    public static VacationHistory createUseVacationHistory(Vacation vacation, String desc, VacationTimeType type, LocalDateTime usedDateTime) {
        VacationHistory vacationHistory = new VacationHistory();
        vacationHistory.vacation = vacation;
        vacationHistory.desc = desc;
        vacationHistory.type = type;
        vacationHistory.usedDateTime = usedDateTime;
        vacationHistory.isDeleted = "N";
        return vacationHistory;
    }

    /**
     * 휴가 추가 내역 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 추가 내역 삭제할 것
     */
    public void deleteRegistVacationHistory(Vacation vacation) {
        vacation.deductedVacation(getGrantTime());
        this.isDeleted = "Y";
    }

    /**
     * 휴가 사용 내역 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 사용 내역 삭제할 것
     */
    public void deleteUseVacationHistory(Vacation vacation) {
        vacation.addVacation(getType().convertToValue(1));
        this.isDeleted = "Y";
    }
}
