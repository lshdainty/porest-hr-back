package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.Holiday;
import com.lshdainty.porest.type.CountryCode;
import com.lshdainty.porest.type.HolidayType;
import com.lshdainty.porest.type.YNType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(HolidayRepositoryImpl.class)
@Transactional
@DisplayName("JPA 공휴일 레포지토리 테스트")
class HolidayRepositoryImplTest {

    @Autowired
    private HolidayRepositoryImpl holidayRepositoryImpl;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("공휴일 저장 및 단건 조회")
    void save() {
        // given
        String name = "신정";
        String date = "20250101";
        HolidayType type = HolidayType.PUBLIC;
        CountryCode countryCode = CountryCode.KR;
        YNType lunarYN = YNType.N;
        String lunarDate = null;
        YNType isRecurring = YNType.Y;
        String icon = "🎊";

        Holiday holiday = Holiday.createHoliday(name, date, type, countryCode, lunarYN, lunarDate, isRecurring, icon);

        // when
        holidayRepositoryImpl.save(holiday);
        em.flush();
        em.clear();

        // then
        Optional<Holiday> findHoliday = holidayRepositoryImpl.findById(holiday.getSeq());
        assertThat(findHoliday.isPresent()).isTrue();
        assertThat(findHoliday.get().getName()).isEqualTo(name);
        assertThat(findHoliday.get().getDate()).isEqualTo(date);
        assertThat(findHoliday.get().getType()).isEqualTo(type);
        assertThat(findHoliday.get().getCountryCode()).isEqualTo(countryCode);
        assertThat(findHoliday.get().getLunarYN()).isEqualTo(lunarYN);
        assertThat(findHoliday.get().getIsRecurring()).isEqualTo(isRecurring);
        assertThat(findHoliday.get().getIcon()).isEqualTo(icon);
    }

    @Test
    @DisplayName("단건 조회 시 공휴일이 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given
        Long holidayId = 999L;

        // when
        Optional<Holiday> findHoliday = holidayRepositoryImpl.findById(holidayId);

        // then
        assertThat(findHoliday.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("국가별 공휴일 목록을 조회한다.")
    void getHolidays() {
        // given
        CountryCode countryCode = CountryCode.KR;
        String[] names = {"신정", "어린이날", "크리스마스"};
        String[] dates = {"20250101", "20250505", "20251225"};
        HolidayType[] types = {HolidayType.PUBLIC, HolidayType.PUBLIC, HolidayType.ETC};

        for (int i = 0; i < names.length; i++) {
            Holiday holiday = Holiday.createHoliday(
                    names[i], dates[i], types[i], countryCode,
                    YNType.N, null, YNType.Y, "🎊"
            );
            holidayRepositoryImpl.save(holiday);
        }

        // when
        List<Holiday> holidays = holidayRepositoryImpl.findHolidays(countryCode);

        // then
        assertThat(holidays.size()).isEqualTo(names.length);
        // 쿼리에서 날짜 기준으로 정렬하므로 순서까지 맞아야함
        assertThat(holidays).extracting("name").containsExactly(names);
        assertThat(holidays).extracting("date").containsExactly(dates);
        assertThat(holidays).extracting("type").containsExactly(types);
    }

    @Test
    @DisplayName("국가별 공휴일 목록이 없더라도 Null이 반환되면 안된다.")
    void getHolidaysEmpty() {
        // given & when
        List<Holiday> holidays = holidayRepositoryImpl.findHolidays(CountryCode.KR);

        // then
        assertThat(holidays.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("기간에 해당하는 휴일만 나오는지 조회한다.")
    void getHolidaysByDate() {
        // given
        CountryCode countryCode = CountryCode.KR;
        String[] names = {"신정", "어린이날", "크리스마스"};
        String[] dates = {"20250101", "20250505", "20251225"};
        HolidayType[] types = {HolidayType.PUBLIC, HolidayType.PUBLIC, HolidayType.PUBLIC};

        for (int i = 0; i < names.length; i++) {
            Holiday holiday = Holiday.createHoliday(
                    names[i], dates[i], types[i], countryCode,
                    YNType.N, null, YNType.Y, "🎊"
            );
            holidayRepositoryImpl.save(holiday);
        }

        // when
        List<Holiday> holidays = holidayRepositoryImpl.findHolidaysByStartEndDate("20241201", "20250131", countryCode);

        // then
        assertThat(holidays.size()).isEqualTo(1);
        assertThat(holidays.get(0).getName()).isEqualTo("신정");
        assertThat(holidays.get(0).getDate()).isEqualTo("20250101");
        assertThat(holidays.get(0).getType()).isEqualTo(HolidayType.PUBLIC);
    }

    @Test
    @DisplayName("기간에 해당하는 휴일만 나오는지 조회한다. (경계값 케이스)")
    void getHolidaysByDateBoundary() {
        // given
        CountryCode countryCode = CountryCode.KR;
        String[] names = {"신정", "어린이날", "크리스마스"};
        String[] dates = {"20250101", "20250505", "20251225"};
        HolidayType[] types = {HolidayType.PUBLIC, HolidayType.PUBLIC, HolidayType.PUBLIC};

        for (int i = 0; i < names.length; i++) {
            Holiday holiday = Holiday.createHoliday(
                    names[i], dates[i], types[i], countryCode,
                    YNType.N, null, YNType.Y, "🎊"
            );
            holidayRepositoryImpl.save(holiday);
        }

        // when
        List<Holiday> holidayLeft = holidayRepositoryImpl.findHolidaysByStartEndDate("20250101", "20250504", countryCode);
        List<Holiday> holidayRight = holidayRepositoryImpl.findHolidaysByStartEndDate("20250102", "20250505", countryCode);
        List<Holiday> holidayNo = holidayRepositoryImpl.findHolidaysByStartEndDate("20250102", "20250504", countryCode);

        // then
        assertThat(holidayLeft.size()).isEqualTo(1);
        assertThat(holidayLeft.get(0).getName()).isEqualTo("신정");
        assertThat(holidayLeft.get(0).getDate()).isEqualTo("20250101");
        assertThat(holidayLeft.get(0).getType()).isEqualTo(HolidayType.PUBLIC);

        assertThat(holidayRight.size()).isEqualTo(1);
        assertThat(holidayRight.get(0).getName()).isEqualTo("어린이날");
        assertThat(holidayRight.get(0).getDate()).isEqualTo("20250505");
        assertThat(holidayRight.get(0).getType()).isEqualTo(HolidayType.PUBLIC);

        assertThat(holidayNo.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("기간에 해당하는 휴일이 없더라도 Null이 반환되면 안된다.")
    void getHolidaysByDateEmpty() {
        // given & when
        List<Holiday> holidays = holidayRepositoryImpl.findHolidaysByStartEndDate("20250101", "20250504", CountryCode.KR);

        // then
        assertThat(holidays.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("타입에 해당하는 휴일만 나오는지 조회한다.")
    void getHolidaysByType() {
        // given
        CountryCode countryCode = CountryCode.KR;
        String[] names = {"신정", "권장휴가", "크리스마스"};
        String[] dates = {"20250101", "20250404", "20251225"};
        HolidayType[] types = {HolidayType.PUBLIC, HolidayType.ETC, HolidayType.PUBLIC};

        for (int i = 0; i < names.length; i++) {
            Holiday holiday = Holiday.createHoliday(
                    names[i], dates[i], types[i], countryCode,
                    YNType.N, null, YNType.Y, "🎊"
            );
            holidayRepositoryImpl.save(holiday);
        }

        // when
        List<Holiday> publics = holidayRepositoryImpl.findHolidaysByType(HolidayType.PUBLIC);
        List<Holiday> etcHolidays = holidayRepositoryImpl.findHolidaysByType(HolidayType.ETC);

        // then
        assertThat(publics.size()).isEqualTo(2);
        assertThat(etcHolidays.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("타입에 해당하는 휴일이 없더라도 Null이 반환되면 안된다.")
    void getHolidaysByTypeEmpty() {
        // given & when
        List<Holiday> holidays = holidayRepositoryImpl.findHolidaysByType(HolidayType.SUBSTITUTE);

        // then
        assertThat(holidays.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("기간과 타입에 해당하는 휴일만 나오는지 조회한다.")
    void findHolidaysByStartEndDateWithType() {
        // given
        CountryCode countryCode = CountryCode.KR;
        String[] names = {"신정", "권장휴가", "크리스마스"};
        String[] dates = {"20240101", "20250404", "20251225"};
        HolidayType[] types = {HolidayType.PUBLIC, HolidayType.ETC, HolidayType.PUBLIC};

        for (int i = 0; i < names.length; i++) {
            Holiday holiday = Holiday.createHoliday(
                    names[i], dates[i], types[i], countryCode,
                    YNType.N, null, YNType.Y, "🎊"
            );
            holidayRepositoryImpl.save(holiday);
        }

        // when
        List<Holiday> publics = holidayRepositoryImpl.findHolidaysByStartEndDateWithType("20250101", "20251231", HolidayType.PUBLIC);
        List<Holiday> etcHolidays = holidayRepositoryImpl.findHolidaysByStartEndDateWithType("20250101", "20251231", HolidayType.ETC);

        // then
        assertThat(publics.size()).isEqualTo(1);
        assertThat(etcHolidays.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("기간과 타입에 해당하는 휴일이 없더라도 Null이 반환되면 안된다.")
    void findHolidaysByStartEndDateWithTypeEmpty() {
        // given & when
        List<Holiday> holidays = holidayRepositoryImpl.findHolidaysByStartEndDateWithType("20250101", "20250504", HolidayType.PUBLIC);

        // then
        assertThat(holidays.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("휴일 삭제")
    void deleteHoliday() {
        // given
        String name = "신정";
        String date = "20250101";
        HolidayType type = HolidayType.PUBLIC;
        CountryCode countryCode = CountryCode.KR;

        Holiday holiday = Holiday.createHoliday(
                name, date, type, countryCode,
                YNType.N, null, YNType.Y, "🎊"
        );
        holidayRepositoryImpl.save(holiday);

        // when
        holidayRepositoryImpl.delete(holiday);
        em.flush();
        em.clear();
        Optional<Holiday> findHoliday = holidayRepositoryImpl.findById(holiday.getSeq());

        // then
        assertThat(findHoliday.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("공휴일 수정 기능 테스트")
    void updateHoliday() {
        // given
        String originalName = "신정";
        String updatedName = "새해 첫날";
        Holiday holiday = Holiday.createHoliday(
                originalName, "20250101", HolidayType.PUBLIC, CountryCode.KR,
                YNType.N, null, YNType.Y, "🎊"
        );
        holidayRepositoryImpl.save(holiday);
        em.flush();
        em.clear();

        // when
        Holiday foundHoliday = holidayRepositoryImpl.findById(holiday.getSeq()).orElseThrow();
        foundHoliday.updateHoliday(updatedName, null, null, null, null, null, null, null);
        em.flush();
        em.clear();

        // then
        Holiday updatedHoliday = holidayRepositoryImpl.findById(holiday.getSeq()).orElseThrow();
        assertThat(updatedHoliday.getName()).isEqualTo(updatedName);
    }
}