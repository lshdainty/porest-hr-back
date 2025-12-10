package com.lshdainty.porest.vacation.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "vacation_usage_deduction")
public class VacationUsageDeduction extends AuditingFields {
    /**
     * 휴가 사용 차감 내역 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vacation_usage_deduction_id")
    private Long id;

    /**
     * 휴가 사용 객체<br>
     * 사용자가 사용한 휴가 내역을 알기위해 사용 (어디서 사용했는지)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_usage_id", nullable = false)
    private VacationUsage usage;

    /**
     * 휴가 부여 객체<br>
     * 사용자가 부여받은 휴가를 알기위해 사용 (어디서 차감했는지)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_grant_id", nullable = false)
    private VacationGrant grant;

    /**
     * 휴가 차감 시간<br>
     * 부여받은 객체에서 어느정도 차감했는지를 관리하는 컬럼<br>
     */
    @Column(name = "deducted_time", nullable = false, precision = 7, scale = 4)
    private BigDecimal deductedTime;

    /**
     * 휴가 사용 차감 내역 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 차감 내역 생성할 것
     *
     * @return VacationUsageDeduction
     */
    public static VacationUsageDeduction createVacationUsageDeduction(VacationUsage usage, VacationGrant grant, BigDecimal deductedTime) {
        VacationUsageDeduction vud = new VacationUsageDeduction();
        vud.usage = usage;
        vud.grant = grant;
        vud.deductedTime = deductedTime;
        return vud;
    }
}
