package com.lshdainty.porest.repository;

import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.repository.HolidayRepositoryImpl;
import com.lshdainty.porest.holiday.type.HolidayType;
import com.lshdainty.porest.common.type.YNType;
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
@Import({HolidayRepositoryImpl.class, TestQuerydslConfig.class})
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
        Holiday holiday = Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊");

        // when
        holidayRepositoryImpl.save(holiday);
        em.flush();
        em.clear();

        // then
        Optional<Holiday> findHoliday = holidayRepositoryImpl.findById(holiday.getSeq());
        assertThat(findHoliday.isPresent()).isTrue();
        assertThat(findHoliday.get().getName()).isEqualTo("신정");
        assertThat(findHoliday.get().getDate()).isEqualTo("20250101");
    }

    @Test
    @DisplayName("단건 조회 시 공휴일이 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given & when
        Optional<Holiday> findHoliday = holidayRepositoryImpl.findById(999L);

        // then
        assertThat(findHoliday.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("국가별 공휴일 목록을 조회한다.")
    void getHolidays() {
        // given
        holidayRepositoryImpl.save(Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));
        holidayRepositoryImpl.save(Holiday.createHoliday("어린이날", "20250505", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));
        holidayRepositoryImpl.save(Holiday.createHoliday("크리스마스", "20251225", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));

        // when
        List<Holiday> holidays = holidayRepositoryImpl.findHolidays(CountryCode.KR);

        // then
        assertThat(holidays.size()).isEqualTo(3);
        assertThat(holidays).extracting("name").containsExactly("신정", "어린이날", "크리스마스");
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
        holidayRepositoryImpl.save(Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));
        holidayRepositoryImpl.save(Holiday.createHoliday("어린이날", "20250505", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));
        holidayRepositoryImpl.save(Holiday.createHoliday("크리스마스", "20251225", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));

        // when
        List<Holiday> holidays = holidayRepositoryImpl.findHolidaysByStartEndDate("20241201", "20250131", CountryCode.KR);

        // then
        assertThat(holidays.size()).isEqualTo(1);
        assertThat(holidays.get(0).getName()).isEqualTo("신정");
    }

    @Test
    @DisplayName("기간에 해당하는 휴일만 나오는지 조회한다. (경계값 케이스)")
    void getHolidaysByDateBoundary() {
        // given
        holidayRepositoryImpl.save(Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));
        holidayRepositoryImpl.save(Holiday.createHoliday("어린이날", "20250505", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));
        holidayRepositoryImpl.save(Holiday.createHoliday("크리스마스", "20251225", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));

        // when
        List<Holiday> holidayLeft = holidayRepositoryImpl.findHolidaysByStartEndDate("20250101", "20250504", CountryCode.KR);
        List<Holiday> holidayRight = holidayRepositoryImpl.findHolidaysByStartEndDate("20250102", "20250505", CountryCode.KR);
        List<Holiday> holidayNo = holidayRepositoryImpl.findHolidaysByStartEndDate("20250102", "20250504", CountryCode.KR);

        // then
        assertThat(holidayLeft.size()).isEqualTo(1);
        assertThat(holidayLeft.get(0).getName()).isEqualTo("신정");

        assertThat(holidayRight.size()).isEqualTo(1);
        assertThat(holidayRight.get(0).getName()).isEqualTo("어린이날");

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
        holidayRepositoryImpl.save(Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));
        holidayRepositoryImpl.save(Holiday.createHoliday("권장휴가", "20250404", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));
        holidayRepositoryImpl.save(Holiday.createHoliday("크리스마스", "20251225", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));

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
        holidayRepositoryImpl.save(Holiday.createHoliday("신정", "20240101", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));
        holidayRepositoryImpl.save(Holiday.createHoliday("권장휴가", "20250404", HolidayType.ETC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));
        holidayRepositoryImpl.save(Holiday.createHoliday("크리스마스", "20251225", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"));

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
        Holiday holiday = Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊");
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
        Holiday holiday = Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC, CountryCode.KR, YNType.N, null, YNType.Y, "🎊");
        holidayRepositoryImpl.save(holiday);
        em.flush();
        em.clear();

        // when
        Holiday foundHoliday = holidayRepositoryImpl.findById(holiday.getSeq()).orElseThrow();
        foundHoliday.updateHoliday("새해 첫날", null, null, null, null, null, null, null);
        em.flush();
        em.clear();

        // then
        Holiday updatedHoliday = holidayRepositoryImpl.findById(holiday.getSeq()).orElseThrow();
        assertThat(updatedHoliday.getName()).isEqualTo("새해 첫날");
    }
}
