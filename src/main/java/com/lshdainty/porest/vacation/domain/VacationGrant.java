package com.lshdainty.porest.vacation.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.type.VacationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "vacation_grant")
public class VacationGrant extends AuditingFields {
    /**
     * 휴가 부여 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue
    @Column(name = "vacation_grant_id")
    private Long id;

    /**
     * 유저 객체<br>
     * 테이블 컬럼은 user_id<br>
     * 어떤 유저에게 부여했는지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Setter
    private User user;

    /**
     * 휴가 정책 객체<br>
     * 테이블 컬럼은 vacation_policy_id
     * 어떤 휴가 정책에 의해 부여 받았는지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_policy_id")
    private VacationPolicy policy;

    /**
     * 휴가 부여 사유 설명<br>
     * 휴가 부여 사유를 작성, 관리하는 컬럼
     */
    @Column(name = "vacation_grant_desc")
    private String desc;

    /**
     * 휴가 타입<br>
     * 휴가 타입으로 그룹핑하여 휴가 사용일수 관리함
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "vacation_type")
    private VacationType type;

    /**
     * 휴가 사용 가능기간 시작 일자<br>
     * 사용자가 휴가 정책을 통해 휴가를 부여받아
     * 사용할 수 있는 기간의 시작 일자를 의미<br>
     * 시스템에서 추가하는 createAt하고 grantDate는 다름
     */
    @Column(name = "grant_date")
    private LocalDateTime grantDate;

    /**
     * 휴가 사용 가능기간 만료 일자<br>
     * 사용자가 휴가 정책을 통해 휴가를 부여받아
     * 사용할 수 있는 기간의 만료 일자를 의미
     */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    /**
     * 휴가 부여 시간<br>
     * 휴가 정책에 따른 휴가 부여 내역<br>
     * (grantTime만 있다면 휴가 부여 내역으로 간주)
     */
    @Column(name = "grant_time", precision = 7, scale = 4)
    private BigDecimal grantTime;

    /**
     * 휴가 잔여 시간<br>
     * 사용자가 부여받은 휴가 중에서 만료 기간이 짧은 것을 기준으로<br>
     * 부여받은 시간에서 사용한 시간만큼 차감하여 남아있는 시간을 기록
     */
    @Column(name = "remain_time", precision = 7, scale = 4)
    private BigDecimal remainTime;

    /**
     * 삭제 여부
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted")
    private YNType isDeleted;

    // user 추가 연관관계 편의 메소드
    public void addUser(User user) {
        this.user = user;
        user.getVacationGrants().add(this);
    }

    // policy 추가 연관관계 편의 메소드
    public void addPolicy(VacationPolicy policy) {
        this.policy = policy;
        policy.getVacationGrants().add(this);
    }

    /**
     * 휴가 부여 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 부여할 것
     *
     * @return VacationGrant
     */
    public static VacationGrant createVacationGrant(User user, VacationPolicy policy, String desc, VacationType type, BigDecimal grantTime, LocalDateTime grantDate, LocalDateTime expiryDate) {
        VacationGrant vg = new VacationGrant();
        vg.addUser(user);
        vg.addPolicy(policy);
        vg.desc = desc;
        vg.type = type;
        vg.grantDate = grantDate;
        vg.expiryDate = expiryDate;
        vg.grantTime = grantTime;
        vg.remainTime = grantTime;
        vg.isDeleted = YNType.N;
        return vg;
    }

    /* 비즈니스 편의 메소드 */
    /**
     * 휴가 추가 메소드<br>
     * remainTime(잔여시간)에 grantTime(추가시간)을 더함
     */
    public void addVacation(BigDecimal grantTime) {
        this.remainTime = getRemainTime().add(grantTime);
    }

    /**
     * 휴가 차감 메소드<br>
     * remainTime(잔여시간)에서 deductTime을(사용시간)을 뺌
     */
    public void deductedVacation(BigDecimal deductTime) {
        this.remainTime = getRemainTime().subtract(deductTime);
    }

    /**
     * grantDate, expireDate를 비교하여<br>
     * 부여일자가 만료일자 이전인지 확인
     *
     * @return true, false
     */
    public boolean isBeforeGrant() {
        return !getGrantDate().isBefore(getExpiryDate());
    }
}
