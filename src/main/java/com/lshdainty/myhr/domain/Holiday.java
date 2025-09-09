package com.lshdainty.myhr.domain;

import com.lshdainty.myhr.type.HolidayType;
import com.lshdainty.myhr.type.YNType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "holiday")
public class Holiday {
    @Id @GeneratedValue
    @Column(name = "holiday_seq")
    private Long seq;           // 공휴일 아이디

    @Column(name = "holiday_date")
    @NotNull
    private String date;        // 공휴일 날짜

    @Column(name = "holiday_name")
    private String name;        // 공휴일 이름

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_type")
    @NotNull
    private HolidayType type;   // 공휴일 타입

    @Column(name = "country_code")
    @NotNull
    private String countryCode; // 국가 코드

    @Enumerated(EnumType.STRING)
    @Column(name = "lunar_yn")
    @NotNull
    private YNType lunarYN;     // 음력 여부

    @Enumerated(EnumType.STRING)
    @Column(name = "is_recurring")
    @NotNull
    private YNType isRecurring; // 반복 여부

    /**
     * 공휴일 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 공휴일 생성할 것
     *
     * @return Dues
     */
    public static Holiday createHoliday(String name, String date, HolidayType type, String countryCode, YNType lunarYN, YNType isRecurring) {
        Holiday holiday = new Holiday();
        holiday.name = name;
        holiday.date = date;
        holiday.type = type;
        holiday.countryCode = countryCode;
        holiday.lunarYN = lunarYN;
        holiday.isRecurring = isRecurring;
        return holiday;
    }

    /**
     * 공휴일 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 공휴일 수정할 것
     */
    public void updateHoliday(String name, String date, HolidayType type, String countryCode, YNType lunarYN, YNType isRecurring) {
        if (!Objects.isNull(name)) { this.name = name; }
        if (!Objects.isNull(date)) { this.date = date; }
        if (!Objects.isNull(type)) { this.type = type; }
        if (!Objects.isNull(countryCode)) { this.countryCode = countryCode; }
        if (!Objects.isNull(lunarYN)) { this.lunarYN = lunarYN; }
        if (!Objects.isNull(isRecurring)) { this.isRecurring = isRecurring; }
    }
}
