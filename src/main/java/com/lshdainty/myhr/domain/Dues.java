package com.lshdainty.myhr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private int amount;

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

    // 회비 생성자  (setter말고 해당 메소드 사용할 것)
    public static Dues createDues(String userName, int amount, DuesType type, DuesCalcType calc, String date, String detail) {
        Dues dues = new Dues();
        dues.userName = userName;
        dues.amount = amount;
        dues.type = type;
        dues.calc = calc;
        dues.date = date;
        dues.detail = detail;
        return dues;
    }
}
