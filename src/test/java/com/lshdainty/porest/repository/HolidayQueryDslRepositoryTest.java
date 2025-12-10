package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.holiday.repository.HolidayQueryDslRepository;
import com.lshdainty.porest.holiday.type.HolidayType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({HolidayQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 공휴일 레포지토리 테스트")
class HolidayQueryDslRepositoryTest {
    @Autowired
    private HolidayQueryDslRepository holidayRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("공휴일 저장 및 단건 조회")
    void save() {
        // given
        Holiday holiday = Holiday.createHoliday(
                "설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC,
                CountryCode.KR, YNType.Y, LocalDate.of(2025, 1, 1),
                YNType.Y, "🎆"
        );

        // when
        holidayRepository.save(holiday);
        em.flush();
        em.clear();

        // then
        Optional<Holiday> findHoliday = holidayRepository.findById(holiday.getId());
        assertThat(findHoliday.isPresent()).isTrue();
        assertThat(findHoliday.get().getName()).isEqualTo("설날");
        assertThat(findHoliday.get().getType()).isEqualTo(HolidayType.PUBLIC);
    }

    @Test
    @DisplayName("단건 조회 시 공휴일이 없으면 빈 Optional 반환")
    void findByIdEmpty() {
        // when
        Optional<Holiday> findHoliday = holidayRepository.findById(999L);

        // then
        assertThat(findHoliday.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("국가코드로 전체 공휴일 조회")
    void findHolidays() {
        // given
        holidayRepository.save(Holiday.createHoliday(
                "설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC,
                CountryCode.KR, YNType.Y, null, YNType.Y, null
        ));
        holidayRepository.save(Holiday.createHoliday(
                "추석", LocalDate.of(2025, 10, 6), HolidayType.PUBLIC,
                CountryCode.KR, YNType.Y, null, YNType.Y, null
        ));
        em.flush();
        em.clear();

        // when
        List<Holiday> holidays = holidayRepository.findHolidays(CountryCode.KR);

        // then
        assertThat(holidays).hasSize(2);
        assertThat(holidays).extracting("name").containsExactly("설날", "추석");
    }

    @Test
    @DisplayName("전체 공휴일 조회 시 공휴일이 없으면 빈 리스트 반환")
    void findHolidaysEmpty() {
        // when
        List<Holiday> holidays = holidayRepository.findHolidays(CountryCode.KR);

        // then
        assertThat(holidays).isEmpty();
    }

    @Test
    @DisplayName("기간으로 공휴일 조회")
    void findHolidaysByStartEndDate() {
        // given
        holidayRepository.save(Holiday.createHoliday(
                "설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC,
                CountryCode.KR, YNType.Y, null, YNType.Y, null
        ));
        holidayRepository.save(Holiday.createHoliday(
                "추석", LocalDate.of(2025, 10, 6), HolidayType.PUBLIC,
                CountryCode.KR, YNType.Y, null, YNType.Y, null
        ));
        em.flush();
        em.clear();

        // when
        List<Holiday> holidays = holidayRepository.findHolidaysByStartEndDate(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30),
                CountryCode.KR
        );

        // then
        assertThat(holidays).hasSize(1);
        assertThat(holidays.get(0).getName()).isEqualTo("설날");
    }

    @Test
    @DisplayName("타입으로 공휴일 조회")
    void findHolidaysByType() {
        // given
        holidayRepository.save(Holiday.createHoliday(
                "설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC,
                CountryCode.KR, YNType.Y, null, YNType.Y, null
        ));
        holidayRepository.save(Holiday.createHoliday(
                "대체공휴일", LocalDate.of(2025, 1, 30), HolidayType.SUBSTITUTE,
                CountryCode.KR, YNType.N, null, YNType.N, null
        ));
        em.flush();
        em.clear();

        // when
        List<Holiday> holidays = holidayRepository.findHolidaysByType(HolidayType.PUBLIC);

        // then
        assertThat(holidays).hasSize(1);
        assertThat(holidays.get(0).getName()).isEqualTo("설날");
    }

    @Test
    @DisplayName("기간과 타입으로 공휴일 조회")
    void findHolidaysByStartEndDateWithType() {
        // given
        holidayRepository.save(Holiday.createHoliday(
                "설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC,
                CountryCode.KR, YNType.Y, null, YNType.Y, null
        ));
        holidayRepository.save(Holiday.createHoliday(
                "대체공휴일", LocalDate.of(2025, 1, 30), HolidayType.SUBSTITUTE,
                CountryCode.KR, YNType.N, null, YNType.N, null
        ));
        em.flush();
        em.clear();

        // when
        List<Holiday> holidays = holidayRepository.findHolidaysByStartEndDateWithType(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                HolidayType.PUBLIC,
                CountryCode.KR
        );

        // then
        assertThat(holidays).hasSize(1);
        assertThat(holidays.get(0).getName()).isEqualTo("설날");
    }

    @Test
    @DisplayName("공휴일 삭제")
    void delete() {
        // given
        Holiday holiday = Holiday.createHoliday(
                "삭제할 공휴일", LocalDate.of(2025, 1, 1), HolidayType.ETC,
                CountryCode.KR, YNType.N, null, YNType.N, null
        );
        holidayRepository.save(holiday);
        em.flush();
        em.clear();

        // when
        Holiday foundHoliday = holidayRepository.findById(holiday.getId()).orElseThrow();
        holidayRepository.delete(foundHoliday);
        em.flush();
        em.clear();

        // then
        Optional<Holiday> deletedHoliday = holidayRepository.findById(holiday.getId());
        assertThat(deletedHoliday.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("공휴일 수정")
    void updateHoliday() {
        // given
        Holiday holiday = Holiday.createHoliday(
                "원래 이름", LocalDate.of(2025, 1, 1), HolidayType.PUBLIC,
                CountryCode.KR, YNType.N, null, YNType.N, null
        );
        holidayRepository.save(holiday);
        em.flush();
        em.clear();

        // when
        Holiday foundHoliday = holidayRepository.findById(holiday.getId()).orElseThrow();
        foundHoliday.updateHoliday("수정된 이름", LocalDate.of(2025, 1, 2),
                HolidayType.SUBSTITUTE, CountryCode.KR, null, null, null, null);
        em.flush();
        em.clear();

        // then
        Holiday updatedHoliday = holidayRepository.findById(holiday.getId()).orElseThrow();
        assertThat(updatedHoliday.getName()).isEqualTo("수정된 이름");
        assertThat(updatedHoliday.getDate()).isEqualTo(LocalDate.of(2025, 1, 2));
        assertThat(updatedHoliday.getType()).isEqualTo(HolidayType.SUBSTITUTE);
    }
}
