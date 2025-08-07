package com.lshdainty.myhr.domain;

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
    private Long seq;

    @Column(name = "holiday_name")
    private String name;

    @Column(name = "holiday_date")
    @NotNull
    private String date;

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_type")
    @NotNull
    private HolidayType type;

    /**
     * 공휴일 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 공휴일 생성할 것
     *
     * @return Dues
     */
    public static Holiday createHoliday(String name, String date, HolidayType type) {
        Holiday holiday = new Holiday();
        holiday.name = name;
        holiday.date = date;
        holiday.type = type;
        return holiday;
    }

    /**
     * 공휴일 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 공휴일 수정할 것
     */
    public void updateHoliday(String name, String date, HolidayType type) {
        if (!Objects.isNull(name)) { this.name = name; }
        if (!Objects.isNull(date)) { this.date = date; }
        if (!Objects.isNull(type)) { this.type = type; }
    }
}
