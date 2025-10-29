package com.lshdainty.porest.vacation.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.type.GrantMethod;
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
    /**
     * 휴가 정책 아이디
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue
    @Column(name = "vacation_policy_id")
    private Long id;

    /**
     * 휴가 정책 이름
     * 정책을 구분할 수 있는 이름
     */
    @Column(name = "vacation_policy_name")
    private String name;

    /**
     * 휴가 정책 설명
     * 사용자가 휴가를 신청하거나 할때 확인할 수 있는 정책 내용
     */
    @Column(name = "vacation_policy_desc")
    private String desc;

    /**
     * 휴가 타입
     * vacation 테이블에서 그룹화하여 휴가 일수를 관리하기 위한 휴가 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "vacation_type")
    private VacationType  vacationType;

    /**
     * 휴가 부여 방법
     * 타입에 따라서 휴가가 부여되는 방식이 결정된다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "grant_method")
    private GrantMethod grantMethod;

    /**
     * 휴가 부여 기준 시간
     * 정책에 설정된 휴가 부여 시간
     */
    @Column(name = "grant_time")
    private BigDecimal grantTime;

    /**
     * 휴가 부여 반복 단위
     * 반복 부여일 경우에 사용되는 값
     * 해당 단위에 따라 스케줄링되어 휴가가 자동 부여됨
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_unit")
    private RepeatUnit repeatUnit;

    /**
     * 휴가 부여 반복 간격
     * 반복 부여일 경우에 사용되는 값
     * 해당 값에 따라 스케줄링되어 휴가가 자동 부여됨
     * 예) 2년 간격
     */
    @Column(name = "repeat_Interval")
    private Integer repeatInterval;

    /**
     * 특정 월 지정
     * 반복 부여일 경우에 사용되는 값
     * 해당 값에 따라 스케줄링되어 휴가가 자동 부여됨
     * 예) 매년 10월 16일
     */
    @Column(name = "specific_months")
    private Integer specificMonths;

    /**
     * 특정 일 지정
     * 반복 부여일 경우에 사용되는 값
     * 해당 값에 따라 스케줄링되어 휴가가 자동 부여됨
     * 예) 매년 10월 16일
     */
    @Column(name = "specific_days")
    private Integer specificDays;

    /**
     * 첫 부여 시점
     * 정책 생성 시점과는 다른 값
     * 정책은 25년에 생성했지만 정책 실행은 26년도부터 가능하도록 할 때 사용
     */
    @Column(name = "first_grant_date")
    private LocalDateTime firstGrantDate;

    /**
     * 반복 여부
     * Y: 계속 반복 부여 (매년, 매월 등)
     * N: 1회만 부여 (N년 후 1회 부여)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_recurring")
    private YNType isRecurring;

    /**
     * 최대 부여 횟수
     * isRecurring = N일 때 사용
     * null이면 제한 없음, 1이면 1회만 부여
     * 예: "7년 후 1회 부여" → maxGrantCount = 1
     */
    @Column(name = "max_grant_count")
    private Integer maxGrantCount;

    /* 삭제 가능 여부 */
    @Enumerated(EnumType.STRING)
    @Column(name = "can_deleted")
    private YNType canDeleted;

    /* 삭제 여부 */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted")
    private YNType isDeleted;

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
    public static VacationPolicy createVacationPolicy(String name, String desc, VacationType vacationType, GrantMethod grantMethod, BigDecimal grantTime, RepeatUnit repeatUnit, Integer repeatInterval, Integer specificMonths, Integer specificDays, LocalDateTime firstGrantDate, YNType isRecurring, Integer maxGrantCount) {
        VacationPolicy vacationPolicy = new VacationPolicy();
        vacationPolicy.name = name;
        vacationPolicy.desc = desc;
        vacationPolicy.vacationType = vacationType;
        vacationPolicy.grantMethod = grantMethod;
        vacationPolicy.grantTime = grantTime;
        vacationPolicy.repeatUnit = repeatUnit;
        vacationPolicy.repeatInterval = repeatInterval;
        vacationPolicy.specificMonths = specificMonths;
        vacationPolicy.specificDays = specificDays;
        vacationPolicy.firstGrantDate = firstGrantDate;
        vacationPolicy.isRecurring = isRecurring;
        vacationPolicy.maxGrantCount = maxGrantCount;
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
        if (this.canDeleted.equals(YNType.N)) {
            return;
        }
        this.isDeleted = YNType.Y;
    }
}
