package com.lshdainty.porest.dues.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.dues.type.DuesCalcType;
import com.lshdainty.porest.dues.type.DuesType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "dues")
public class Dues extends AuditingFields {
    /**
     * 회비 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dues_id", columnDefinition = "bigint(20) COMMENT '회비 아이디'")
    private Long id;

    /**
     * 회원 이름<br>
     * 회비를 납부하거나 사용한 사용자의 이름
     */
    @Column(name = "dues_user_name", length = 20, columnDefinition = "varchar(20) COMMENT '회원 이름'")
    private String userName;

    /**
     * 회비 금액<br>
     * 납부 또는 사용된 회비 금액
     */
    @Column(name = "dues_amount", nullable = false, columnDefinition = "bigint(20) NOT NULL COMMENT '회비 금액'")
    private Long amount;

    /**
     * 회비 타입<br>
     * 회비의 종류를 구분하기 위한 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dues_type", nullable = false, length = 15, columnDefinition = "varchar(15) NOT NULL COMMENT '회비 타입'")
    private DuesType type;

    /**
     * 회비 계산 연산자<br>
     * 회비 금액 계산 방식을 구분하기 위한 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dues_calc", nullable = false, length = 10, columnDefinition = "varchar(10) NOT NULL COMMENT '회비 계산 연산자'")
    private DuesCalcType calc;

    /**
     * 회비 날짜<br>
     * 회비 납부 또는 사용 날짜
     */
    @Column(name = "dues_date", nullable = false, columnDefinition = "date NOT NULL COMMENT '회비 날짜'")
    private LocalDate date;

    /**
     * 회비 설명<br>
     * 회비 사용 내역에 대한 상세 설명
     */
    @Column(name = "dues_detail", length = 1000, columnDefinition = "varchar(1000) COMMENT '회비 설명'")
    private String detail;

    /**
     * 회비 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 회비 생성할 것
     *
     * @return Dues
     */
    public static Dues createDues(String userName, Long amount, DuesType type, DuesCalcType calc, LocalDate date, String detail) {
        Dues dues = new Dues();
        dues.userName = userName;
        dues.amount = amount;
        dues.type = type;
        dues.calc = calc;
        dues.date = date;
        dues.detail = detail;
        return dues;
    }

    /**
     * 회비 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 회비 수정할 것
     */
    public void updateDues(String userName, Long amount, DuesType type, DuesCalcType calc, LocalDate date, String detail) {
        if (!Objects.isNull(userName)) { this.userName = userName; }
        if (!Objects.isNull(amount)) { this.amount = amount; }
        if (!Objects.isNull(type)) { this.type = type; }
        if (!Objects.isNull(calc)) { this.calc = calc; }
        if (!Objects.isNull(date)) { this.date = date; }
        if (!Objects.isNull(detail)) { this.detail = detail; }
    }
}
