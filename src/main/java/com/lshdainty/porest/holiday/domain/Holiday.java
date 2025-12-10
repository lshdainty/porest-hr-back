package com.lshdainty.porest.holiday.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.type.HolidayType;
import com.lshdainty.porest.common.type.YNType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "holiday")
public class Holiday extends AuditingFields {
    /**
     * 공휴일 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holiday_id")
    private Long id;

    /**
     * 공휴일 일자<br>
     * 공휴일이 해당하는 날짜 (양력 기준)
     */
    @NotNull
    @Column(name = "holiday_date", nullable = false)
    private LocalDate date;

    /**
     * 공휴일 이름<br>
     * 공휴일의 명칭 (예: 설날, 추석, 광복절 등)
     */
    @NotNull
    @Column(name = "holiday_name", nullable = false, length = 20)
    private String name;

    /**
     * 공휴일 타입<br>
     * 공휴일의 종류를 구분하기 위한 타입
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "holiday_type", nullable = false, length = 15)
    private HolidayType type;

    /**
     * 국가 코드<br>
     * 공휴일이 적용되는 국가 정보
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "country_code", nullable = false, length = 2)
    private CountryCode countryCode;

    /**
     * 음력 여부<br>
     * 공휴일 날짜가 음력인지 여부
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "lunar_yn", nullable = false, length = 1)
    private YNType lunarYN;

    /**
     * 음력 일자<br>
     * 음력 기준 공휴일 날짜 (음력 여부가 Y일 경우 사용)
     */
    @Column(name = "lunar_date")
    private LocalDate lunarDate;

    /**
     * 반복 여부<br>
     * 매년 반복되는 공휴일인지 여부
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "is_recurring", nullable = false, length = 1)
    private YNType isRecurring;

    /**
     * 아이콘<br>
     * UI에서 표시할 아이콘 정보
     */
    @Column(name = "holiday_icon", length = 5)
    private String icon;

    /**
     * 공휴일 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 공휴일 생성할 것
     *
     * @return Holiday
     */
    public static Holiday createHoliday(String name, LocalDate date, HolidayType type, CountryCode countryCode, YNType lunarYN, LocalDate lunarDate, YNType isRecurring, String icon) {
        Holiday holiday = new Holiday();
        holiday.name = name;
        holiday.date = date;
        holiday.type = type;
        holiday.countryCode = countryCode;
        holiday.lunarYN = lunarYN;
        holiday.lunarDate = lunarDate;
        holiday.isRecurring = isRecurring;
        holiday.icon = icon;
        return holiday;
    }

    /**
     * 공휴일 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 공휴일 수정할 것
     */
    public void updateHoliday(String name, LocalDate date, HolidayType type, CountryCode countryCode, YNType lunarYN, LocalDate lunarDate, YNType isRecurring, String icon) {
        if (!Objects.isNull(name)) { this.name = name; }
        if (!Objects.isNull(date)) { this.date = date; }
        if (!Objects.isNull(type)) { this.type = type; }
        if (!Objects.isNull(countryCode)) { this.countryCode = countryCode; }
        if (!Objects.isNull(lunarYN)) { this.lunarYN = lunarYN; }
        if (!Objects.isNull(lunarDate)) { this.lunarDate = lunarDate; }
        if (!Objects.isNull(isRecurring)) { this.isRecurring = isRecurring; }
        if (!Objects.isNull(icon)) { this.icon = icon; }
    }
}
