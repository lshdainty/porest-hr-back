package com.lshdainty.porest.vacation.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.type.VacationTimeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "vacation_usage")
public class VacationUsage extends AuditingFields {
    /**
     * 휴가 사용 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vacation_usage_id", columnDefinition = "bigint(20) COMMENT '휴가 사용 아이디'")
    private Long id;

    /**
     * 유저 객체<br>
     * 테이블 컬럼은 user_id<br>
     * 어떤 유저가 사용했는지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private User user;

    /**
     * 휴가 내역 설명<br>
     * 사용자에게 부여한 혹은 사용내역에 대한 설명
     */
    @Column(name = "vacation_usage_desc", length = 1000, columnDefinition = "varchar(1000) COMMENT '휴가 사용 사유'")
    private String desc;

    /**
     * 휴가 사용 시간 타입<br>
     * 사용자가 사용한 휴가 시간 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "vacation_time_type", nullable = false, length = 20, columnDefinition = "varchar(20) NOT NULL COMMENT '휴가 시간 타입'")
    private VacationTimeType type;

    /**
     * 휴가 사용 시작일<br>
     * 사용자가 사용한 휴가의 시작 일자와 시간
     */
    @Column(name = "start_date", nullable = false, columnDefinition = "datetime(6) NOT NULL COMMENT '휴가 사용 시작 일시'")
    private LocalDateTime startDate;

    /**
     * 휴가 사용 종료일<br>
     * 사용자가 사용한 휴가의 종료 일자와 시간
     */
    @Column(name = "end_date", nullable = false, columnDefinition = "datetime(6) NOT NULL COMMENT '휴가 사용 종료 일시'")
    private LocalDateTime endDate;

    /**
     * 실제 사용 일수 (또는 시간)<br>
     * 주말/공휴일 제외한 실제 사용한 시간<br>
     * 부분 취소 시 이 값을 기준으로 재계산
     */
    @Column(name = "used_time", nullable = false, precision = 7, scale = 4, columnDefinition = "decimal(7,4) NOT NULL COMMENT '휴가 총 사용 시간'")
    private BigDecimal usedTime;

    /**
     * 삭제 여부
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1, columnDefinition = "varchar(1) DEFAULT 'N' NOT NULL COMMENT '삭제 여부'")
    private YNType isDeleted;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "usage", cascade = CascadeType.ALL)
    private List<VacationUsageDeduction> deductions = new ArrayList<>();

    // user 추가 연관관계 편의 메소드
    public void addUser(User user) {
        this.user = user;
        user.getVacationUsages().add(this);
    }

    /**
     * 휴가 사용 내역 생성 함수 (기간 통합)<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 사용 내역 생성할 것
     *
     * @param user 사용자
     * @param desc 설명
     * @param type 휴가 시간 타입
     * @param startDate 시작일
     * @param endDate 종료일
     * @param usedTime 실제 사용 시간 (주말/공휴일 제외)
     * @return VacationUsage
     */
    public static VacationUsage createVacationUsage(
            User user,
            String desc,
            VacationTimeType type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal usedTime) {
        VacationUsage vu = new VacationUsage();
        vu.addUser(user);
        vu.desc = desc;
        vu.type = type;
        vu.startDate = startDate;
        vu.endDate = endDate;
        vu.usedTime = usedTime;
        vu.isDeleted = YNType.N;
        return vu;
    }


    /**
     * 삭제 처리 (소프트 삭제)
     */
    public void deleteVacationUsage() {
        this.isDeleted = YNType.Y;
    }
}
