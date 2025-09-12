package com.lshdainty.porest.domain;

import com.lshdainty.porest.type.DuesCalcType;
import com.lshdainty.porest.type.DuesType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "dues")
public class Dues {
    @Id @GeneratedValue
    @Column(name = "dues_seq")
    private Long seq;

    @Column(name = "dues_user_name")
    private String userName;

    @Column(name = "dues_amount")
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "dues_type")
    private DuesType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "dues_calc")
    private DuesCalcType calc;

    @Column(name = "dues_date")
    private String date;

    @Column(name = "dues_detail")
    private String detail;

    /**
     * 회비 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 회비 생성할 것
     *
     * @return Dues
     */
    public static Dues createDues(String userName, Long amount, DuesType type, DuesCalcType calc, String date, String detail) {
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
    public void updateDues(String userName, Long amount, DuesType type, DuesCalcType calc, String date, String detail) {
        if (!Objects.isNull(userName)) { this.userName = userName; }
        if (!Objects.isNull(amount)) { this.amount = amount; }
        if (!Objects.isNull(type)) { this.type = type; }
        if (!Objects.isNull(calc)) { this.calc = calc; }
        if (!Objects.isNull(date)) { this.date = date; }
        if (!Objects.isNull(detail)) { this.detail = detail; }
    }
}
