package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.Holiday;
import com.lshdainty.myhr.domain.HolidayType;
import com.lshdainty.myhr.repository.HolidayRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("공휴일 서비스 테스트")
class HolidayServiceTest {
    // 삭제하지 말 것 (NullpointException 발생)
    @Mock
    private MessageSource ms;
    @Mock
    private HolidayRepositoryImpl holidayRepositoryImpl;

    @InjectMocks
    private HolidayService holidayService;

    @Test
    @DisplayName("공휴일 저장 테스트 - 성공")
    void saveHolidaySuccessTest() {
        // Given
        String name = "신정";
        String date = "20250101";
        HolidayType type = HolidayType.PUBLIC;
        willDoNothing().given(holidayRepositoryImpl).save(any(Holiday.class));

        // When
        holidayService.save(name, date, type);

        // Then
        then(holidayRepositoryImpl).should().save(any(Holiday.class));
    }

    @Test
    @DisplayName("단건 공휴일 조회 테스트 - 성공")
    void findByIdSuccessTest() {
        // Given
        Long seq = 1L;
        String name = "신정";
        String date = "20250101";
        HolidayType type = HolidayType.PUBLIC;
        Holiday holiday = Holiday.createHoliday(name, date, type);

        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.of(holiday));

        // When
        Holiday findHoliday = holidayService.findById(seq);

        // Then
        then(holidayRepositoryImpl).should().findById(seq);
        assertThat(findHoliday).isNotNull();
        assertThat(findHoliday.getName()).isEqualTo(name);
        assertThat(findHoliday.getDate()).isEqualTo(date);
        assertThat(findHoliday.getType()).isEqualTo(type);
    }

    @Test
    @DisplayName("단일 공휴일 조회 테스트 - 실패 (공휴일 없음)")
    void findByIdFailTestNotFoundHoliday() {
        // Given
        Long seq = 900L;
        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> holidayService.findById(seq));
        then(holidayRepositoryImpl).should().findById(seq);
    }

    @Test
    @DisplayName("전체 공휴일 조회 테스트 - 성공 (시간 정렬)")
    void findHolidaysSuccessTest() {
        // Given
        given(holidayRepositoryImpl.findHolidays()).willReturn(List.of(
                Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC),
                Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC),
                Holiday.createHoliday("권장휴가", "20250131", HolidayType.RECOMMEND)
        ));

        // When
        List<Holiday> holidays = holidayService.findHolidays();

        // Then
        then(holidayRepositoryImpl).should().findHolidays();
        assertThat(holidays).hasSize(3);
        // date 기준 order by이므로 순서 중요
        assertThat(holidays)
                .extracting("name")
                .containsExactly("신정", "설날", "권장휴가");
    }

    @Test
    @DisplayName("공휴일 기간별 조회 테스트 - 성공")
    void findHolidaysByStartEndDateSuccessTest() {
        // Given
        String start = "20250101";
        String end = "20251231";
        given(holidayRepositoryImpl.findHolidaysByStartEndDate(start, end)).willReturn(List.of(
                Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC),
                Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC),
                Holiday.createHoliday("권장휴가", "20250131", HolidayType.RECOMMEND),
                Holiday.createHoliday("권장휴가", "20251231", HolidayType.RECOMMEND)
        ));

        // When
        List<Holiday> holidays = holidayService.findHolidaysByStartEndDate(start, end);

        // Then
        then(holidayRepositoryImpl).should().findHolidaysByStartEndDate(start, end);
        assertThat(holidays).hasSize(4);
        assertThat(holidays)
                .extracting("name")
                .containsExactly("신정", "설날", "권장휴가", "권장휴가");
    }

    @Test
    @DisplayName("공휴일 타입별 조회 테스트 - 성공")
    void findHolidaysByTypeSuccessTest() {
        // Given
        HolidayType type = HolidayType.RECOMMEND;
        given(holidayRepositoryImpl.findHolidaysByType(type)).willReturn(List.of(
                Holiday.createHoliday("권장휴가", "20250131", HolidayType.RECOMMEND)
        ));

        // When
        List<Holiday> holidays = holidayService.findHolidaysByType(type);

        // Then
        then(holidayRepositoryImpl).should().findHolidaysByType(type);
        assertThat(holidays).hasSize(1);
        assertThat(holidays).extracting("type").containsOnly(type);
    }

    @Test
    @DisplayName("공휴일 수정 테스트 - 성공")
    void editHolidaySuccessTest() {
        // Given
        Long seq = 1L;
        String name = "신정";
        String date = "20250101";
        HolidayType type = HolidayType.PUBLIC;
        Holiday holiday = Holiday.createHoliday(name, date, type);

        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.of(holiday));

        // When
        name = "임시공휴일";
        date = "20250131";
        holidayService.editHoliday(seq, name, date, null);

        // Then
        then(holidayRepositoryImpl).should().findById(seq);
        assertThat(holiday.getName()).isEqualTo(name);
        assertThat(holiday.getDate()).isEqualTo(date);
        assertThat(holiday.getType()).isEqualTo(type);
    }

    @Test
    @DisplayName("공휴일 수정 테스트 - 실패 (공휴일 없음)")
    void editHolidayFailTestNotFoundHoliday() {
        // Given
        Long seq = 900L;
        String name = "신정";
        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> holidayService.editHoliday(seq, name, null, null));
        then(holidayRepositoryImpl).should().findById(seq);
    }

    @Test
    @DisplayName("공휴일 삭제 테스트 - 성공")
    void deleteHolidaySuccessTest() {
        // Given
        Long seq = 1L;
        String name = "신정";
        String date = "20250101";
        HolidayType type = HolidayType.PUBLIC;
        Holiday holiday = Holiday.createHoliday(name, date, type);

        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.of(holiday));
        willDoNothing().given(holidayRepositoryImpl).delete(holiday);

        // When
        holidayService.deleteHoliday(seq);

        // Then
        then(holidayRepositoryImpl).should().findById(seq);
        then(holidayRepositoryImpl).should().delete(holiday);
    }

    @Test
    @DisplayName("공휴일 삭제 테스트 - 실패 (공휴일 없음)")
    void deleteHolidayFailTestNotFoundHoliday() {
        // Given
        Long seq = 900L;
        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> holidayService.deleteHoliday(seq));
        then(holidayRepositoryImpl).should().findById(seq);
        then(holidayRepositoryImpl).should(never()).delete(any(Holiday.class));
    }
}
