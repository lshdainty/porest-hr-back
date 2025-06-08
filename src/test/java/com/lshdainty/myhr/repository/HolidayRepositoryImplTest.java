package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.Holiday;
import com.lshdainty.myhr.domain.HolidayType;
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

        Holiday holiday = Holiday.createHoliday(name, date, type);

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
    }

    @Test
    @DisplayName("단건 조회 시 공휴일이 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given
        Long vacationId = 999L;

        // when
        Optional<Holiday> findHoliday = holidayRepositoryImpl.findById(vacationId);

        // then
        assertThat(findHoliday.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("공휴일 목록을 조회한다.")
    void getHolidays() {
        // given
        String[] names = {"신정", "어린이날", "크리스마스"};
        String[] dates = {"20250101", "20250505", "20251226"};
        HolidayType[] types = {HolidayType.PUBLIC, HolidayType.PUBLIC, HolidayType.RECOMMEND};

        for (int i = 0; i < names.length; i++) {
            Holiday holiday = Holiday.createHoliday(names[i], dates[i], types[i]);
            holidayRepositoryImpl.save(holiday);
        }

        // when
        List<Holiday> holidays = holidayRepositoryImpl.findHolidays();

        // then
        assertThat(holidays.size()).isEqualTo(names.length);
        // 쿼리에서 날짜 기준으로 정렬하므로 순서까지 맞아야함
        assertThat(holidays).extracting("name").containsExactly(names);
        assertThat(holidays).extracting("date").containsExactly(dates);
        assertThat(holidays).extracting("type").containsExactly(types);
    }

    @Test
    @DisplayName("공휴일 목록이 없더라도 Null이 반환되면 안된다.")
    void getHolidaysEmpty() {
        // given & when
        List<Holiday> holidays = holidayRepositoryImpl.findHolidays();

        // then
        assertThat(holidays.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("기간에 해당하는 휴일만 나오는지 조회한다.")
    void getHolidaysByDate() {
        // given
        String[] names = {"신정", "어린이날", "크리스마스"};
        String[] dates = {"20250101", "20250505", "20251225"};
        HolidayType[] types = {HolidayType.PUBLIC, HolidayType.PUBLIC, HolidayType.PUBLIC};

        for (int i = 0; i < names.length; i++) {
            Holiday holiday = Holiday.createHoliday(names[i], dates[i], types[i]);
            holidayRepositoryImpl.save(holiday);
        }

        // when
        List<Holiday> holidays = holidayRepositoryImpl.findHolidaysByStartEndDate("20241201", "20250131");

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
        String[] names = {"신정", "어린이날", "크리스마스"};
        String[] dates = {"20250101", "20250505", "20251225"};
        HolidayType[] types = {HolidayType.PUBLIC, HolidayType.PUBLIC, HolidayType.PUBLIC};

        for (int i = 0; i < names.length; i++) {
            Holiday holiday = Holiday.createHoliday(names[i], dates[i], types[i]);
            holidayRepositoryImpl.save(holiday);
        }

        // when
        List<Holiday> holidayLeft = holidayRepositoryImpl.findHolidaysByStartEndDate("20250101", "20250504");
        List<Holiday> holidayRight = holidayRepositoryImpl.findHolidaysByStartEndDate("20250102", "20250505");
        List<Holiday> holidayNo = holidayRepositoryImpl.findHolidaysByStartEndDate("20250102", "20250504");

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
        List<Holiday> holidays = holidayRepositoryImpl.findHolidaysByStartEndDate("20250101", "20250504");

        // then
        assertThat(holidays.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("타입에 해당하는 휴일만 나오는지 조회한다.")
    void getHolidaysByType() {
        // given
        String[] names = {"신정", "권장휴가", "크리스마스"};
        String[] dates = {"20250101", "20250404", "20251225"};
        HolidayType[] types = {HolidayType.PUBLIC, HolidayType.RECOMMEND, HolidayType.PUBLIC};

        for (int i = 0; i < names.length; i++) {
            Holiday holiday = Holiday.createHoliday(names[i], dates[i], types[i]);
            holidayRepositoryImpl.save(holiday);
        }

        // when
        List<Holiday> publics = holidayRepositoryImpl.findHolidaysByType(HolidayType.PUBLIC);
        List<Holiday> recommends = holidayRepositoryImpl.findHolidaysByType(HolidayType.RECOMMEND);

        // then
        assertThat(publics.size()).isEqualTo(2);
        assertThat(recommends.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("타입에 해당하는 휴일이 없더라도 Null이 반환되면 안된다.")
    void getHolidaysByTypeEmpty() {
        // given & when
        List<Holiday> holidays = holidayRepositoryImpl.findHolidaysByType(HolidayType.PUBLIC);

        // then
        assertThat(holidays.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("기간과 타입에 해당하는 휴일만 나오는지 조회한다.")
    void findHolidaysByStartEndDateWithType() {
        // given
        String[] names = {"신정", "권장휴가", "크리스마스"};
        String[] dates = {"20240101", "20250404", "20251225"};
        HolidayType[] types = {HolidayType.PUBLIC, HolidayType.RECOMMEND, HolidayType.PUBLIC};

        for (int i = 0; i < names.length; i++) {
            Holiday holiday = Holiday.createHoliday(names[i], dates[i], types[i]);
            holidayRepositoryImpl.save(holiday);
        }

        // when
        List<Holiday> publics = holidayRepositoryImpl.findHolidaysByStartEndDateWithType("20250101", "20251231", HolidayType.PUBLIC);
        List<Holiday> recommends = holidayRepositoryImpl.findHolidaysByStartEndDateWithType("20250101", "20251231", HolidayType.RECOMMEND);

        // then
        assertThat(publics.size()).isEqualTo(1);
        assertThat(recommends.size()).isEqualTo(1);
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

        Holiday holiday = Holiday.createHoliday(name, date, type);
        holidayRepositoryImpl.save(holiday);

        // when
        holidayRepositoryImpl.delete(holiday);
        em.flush();
        em.clear();
        Optional<Holiday> findHoliday = holidayRepositoryImpl.findById(holiday.getSeq());

        // then
        assertThat(findHoliday.isEmpty()).isTrue();
    }
}