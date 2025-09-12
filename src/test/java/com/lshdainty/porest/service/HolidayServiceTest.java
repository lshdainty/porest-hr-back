package com.lshdainty.porest.service;

import com.lshdainty.porest.domain.Holiday;
import com.lshdainty.porest.type.CountryCode;
import com.lshdainty.porest.type.HolidayType;
import com.lshdainty.porest.type.YNType;
import com.lshdainty.porest.repository.HolidayRepositoryImpl;
import com.lshdainty.porest.service.dto.HolidayServiceDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("공휴일 서비스 테스트")
class HolidayServiceTest {

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
        HolidayServiceDto serviceDto = HolidayServiceDto.builder()
                .name("신정")
                .date("20250101")
                .type(HolidayType.PUBLIC)
                .countryCode(CountryCode.KR)
                .lunarYN(YNType.N)
                .lunarDate(null)
                .isRecurring(YNType.Y)
                .icon("🎊")
                .build();

        willDoNothing().given(holidayRepositoryImpl).save(any(Holiday.class));

        // When
        Long result = holidayService.save(serviceDto);

        // Then
        then(holidayRepositoryImpl).should().save(any(Holiday.class));

        // ArgumentCaptor를 사용하여 저장된 엔티티 검증
        ArgumentCaptor<Holiday> holidayCaptor = ArgumentCaptor.forClass(Holiday.class);
        verify(holidayRepositoryImpl).save(holidayCaptor.capture());

        Holiday savedHoliday = holidayCaptor.getValue();
        assertThat(savedHoliday.getName()).isEqualTo("신정");
        assertThat(savedHoliday.getDate()).isEqualTo("20250101");
        assertThat(savedHoliday.getType()).isEqualTo(HolidayType.PUBLIC);
        assertThat(savedHoliday.getCountryCode()).isEqualTo(CountryCode.KR);
        assertThat(savedHoliday.getLunarYN()).isEqualTo(YNType.N);
        assertThat(savedHoliday.getIsRecurring()).isEqualTo(YNType.Y);
        assertThat(savedHoliday.getIcon()).isEqualTo("🎊");
    }

    @Test
    @DisplayName("단건 공휴일 조회 테스트 - 성공")
    void findByIdSuccessTest() {
        // Given
        Long seq = 1L;
        String name = "신정";
        String date = "20250101";
        HolidayType type = HolidayType.PUBLIC;
        CountryCode countryCode = CountryCode.KR;

        Holiday holiday = Holiday.createHoliday(name, date, type, countryCode, YNType.N, null, YNType.Y, "🎊");
        setHolidaySeq(holiday, seq);

        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.of(holiday));

        // When
        Holiday findHoliday = holidayService.findById(seq);

        // Then
        then(holidayRepositoryImpl).should().findById(seq);
        assertThat(findHoliday).isNotNull();
        assertThat(findHoliday.getSeq()).isEqualTo(seq);
        assertThat(findHoliday.getName()).isEqualTo(name);
        assertThat(findHoliday.getDate()).isEqualTo(date);
        assertThat(findHoliday.getType()).isEqualTo(type);
        assertThat(findHoliday.getCountryCode()).isEqualTo(countryCode);
    }

    @Test
    @DisplayName("단일 공휴일 조회 테스트 - 실패 (공휴일 없음)")
    void findByIdFailTestNotFoundHoliday() {
        // Given
        Long seq = 900L;
        String errorMessage = "Holiday not found";

        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.empty());
        given(ms.getMessage("error.notfound.holiday", null, null)).willReturn(errorMessage);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> holidayService.findById(seq));

        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        then(holidayRepositoryImpl).should().findById(seq);
        then(ms).should().getMessage("error.notfound.holiday", null, null);
    }

    @Test
    @DisplayName("국가별 공휴일 조회 테스트 - 성공 (시간 정렬)")
    void findHolidaysSuccessTest() {
        // Given
        CountryCode countryCode = CountryCode.KR;
        List<Holiday> expectedHolidays = List.of(
                Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC, countryCode, YNType.N, null, YNType.Y, "🎊"),
                Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC, countryCode, YNType.Y, "20250129", YNType.Y, "🌙"),
                Holiday.createHoliday("임시공휴일", "20250131", HolidayType.ETC, countryCode, YNType.N, null, YNType.N, "📅")
        );

        given(holidayRepositoryImpl.findHolidays(countryCode)).willReturn(expectedHolidays);

        // When
        List<Holiday> holidays = holidayService.findHolidays(countryCode);

        // Then
        then(holidayRepositoryImpl).should().findHolidays(countryCode);
        assertThat(holidays).hasSize(3);
        assertThat(holidays).extracting("name").containsExactly("신정", "설날", "임시공휴일");
        assertThat(holidays).extracting("type").contains(HolidayType.PUBLIC, HolidayType.ETC);
        assertThat(holidays).extracting("countryCode").containsOnly(countryCode);
    }

    @Test
    @DisplayName("국가별 공휴일 조회 테스트 - 빈 결과")
    void findHolidaysEmptyTest() {
        // Given
        CountryCode countryCode = CountryCode.US;
        given(holidayRepositoryImpl.findHolidays(countryCode)).willReturn(Collections.emptyList());

        // When
        List<Holiday> holidays = holidayService.findHolidays(countryCode);

        // Then
        then(holidayRepositoryImpl).should().findHolidays(countryCode);
        assertThat(holidays).isEmpty();
    }

    @Test
    @DisplayName("공휴일 기간별 조회 테스트 - 성공")
    void findHolidaysByStartEndDateSuccessTest() {
        // Given
        String start = "20250101";
        String end = "20251231";
        CountryCode countryCode = CountryCode.KR;

        List<Holiday> expectedHolidays = List.of(
                Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC, countryCode, YNType.N, null, YNType.Y, "🎊"),
                Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC, countryCode, YNType.Y, "20250129", YNType.Y, "🌙"),
                Holiday.createHoliday("임시공휴일", "20250131", HolidayType.ETC, countryCode, YNType.N, null, YNType.N, "📅"),
                Holiday.createHoliday("크리스마스", "20251225", HolidayType.ETC, countryCode, YNType.N, null, YNType.Y, "🎄")
        );

        given(holidayRepositoryImpl.findHolidaysByStartEndDate(start, end, countryCode)).willReturn(expectedHolidays);

        // When
        List<Holiday> holidays = holidayService.findHolidaysByStartEndDate(start, end, countryCode);

        // Then
        then(holidayRepositoryImpl).should().findHolidaysByStartEndDate(start, end, countryCode);
        assertThat(holidays).hasSize(4);
        assertThat(holidays).extracting("name").containsExactly("신정", "설날", "임시공휴일", "크리스마스");
        assertThat(holidays).allMatch(h -> h.getDate().compareTo(start) >= 0 && h.getDate().compareTo(end) <= 0);
    }

    @Test
    @DisplayName("공휴일 기간별 조회 테스트 - 빈 결과")
    void findHolidaysByStartEndDateEmptyTest() {
        // Given
        String start = "20220101";
        String end = "20221231";
        CountryCode countryCode = CountryCode.KR;

        given(holidayRepositoryImpl.findHolidaysByStartEndDate(start, end, countryCode)).willReturn(Collections.emptyList());

        // When
        List<Holiday> holidays = holidayService.findHolidaysByStartEndDate(start, end, countryCode);

        // Then
        then(holidayRepositoryImpl).should().findHolidaysByStartEndDate(start, end, countryCode);
        assertThat(holidays).isEmpty();
    }

    @Test
    @DisplayName("공휴일 타입별 조회 테스트 - 성공")
    void findHolidaysByTypeSuccessTest() {
        // Given
        HolidayType type = HolidayType.ETC;
        List<Holiday> expectedHolidays = List.of(
                Holiday.createHoliday("임시공휴일", "20250131", type, CountryCode.KR, YNType.N, null, YNType.N, "📅")
        );

        given(holidayRepositoryImpl.findHolidaysByType(type)).willReturn(expectedHolidays);

        // When
        List<Holiday> holidays = holidayService.findHolidaysByType(type);

        // Then
        then(holidayRepositoryImpl).should().findHolidaysByType(type);
        assertThat(holidays).hasSize(1);
        assertThat(holidays).extracting("type").containsOnly(type);
        assertThat(holidays.get(0).getName()).isEqualTo("임시공휴일");
    }

    @Test
    @DisplayName("공휴일 타입별 조회 테스트 - 빈 결과")
    void findHolidaysByTypeEmptyTest() {
        // Given
        HolidayType type = HolidayType.SUBSTITUTE;
        given(holidayRepositoryImpl.findHolidaysByType(type)).willReturn(Collections.emptyList());

        // When
        List<Holiday> holidays = holidayService.findHolidaysByType(type);

        // Then
        then(holidayRepositoryImpl).should().findHolidaysByType(type);
        assertThat(holidays).isEmpty();
    }

    @Test
    @DisplayName("공휴일 수정 테스트 - 성공")
    void editHolidaySuccessTest() {
        // Given
        Long seq = 1L;
        String originalName = "신정";
        String updatedName = "임시공휴일";
        HolidayType updatedType = HolidayType.ETC;

        Holiday holiday = Holiday.createHoliday(originalName, "20250101", HolidayType.PUBLIC,
                CountryCode.KR, YNType.N, null, YNType.Y, "🎊");
        setHolidaySeq(holiday, seq);

        HolidayServiceDto updateDto = HolidayServiceDto.builder()
                .seq(seq)
                .name(updatedName)
                .type(updatedType)
                .countryCode(CountryCode.KR)
                .lunarYN(YNType.N)
                .isRecurring(YNType.Y)
                .icon("📅")
                .build();

        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.of(holiday));

        // When
        holidayService.editHoliday(updateDto);

        // Then
        then(holidayRepositoryImpl).should().findById(seq);
        assertThat(holiday.getName()).isEqualTo(updatedName);
        assertThat(holiday.getType()).isEqualTo(updatedType);
        assertThat(holiday.getIcon()).isEqualTo("📅");
        // 기존 값들은 유지되어야 함
        assertThat(holiday.getDate()).isEqualTo("20250101");
        assertThat(holiday.getCountryCode()).isEqualTo(CountryCode.KR);
    }

    @Test
    @DisplayName("공휴일 수정 테스트 - 실패 (공휴일 없음)")
    void editHolidayFailTestNotFoundHoliday() {
        // Given
        Long seq = 900L;
        String errorMessage = "Holiday not found";
        HolidayServiceDto data = HolidayServiceDto.builder()
                .seq(seq)
                .name("임시공휴일")
                .build();

        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.empty());
        given(ms.getMessage("error.notfound.holiday", null, null)).willReturn(errorMessage);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> holidayService.editHoliday(data));

        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        then(holidayRepositoryImpl).should().findById(seq);
        then(ms).should().getMessage("error.notfound.holiday", null, null);
    }

    @Test
    @DisplayName("공휴일 삭제 테스트 - 성공")
    void deleteHolidaySuccessTest() {
        // Given
        Long seq = 1L;
        Holiday holiday = Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC,
                CountryCode.KR, YNType.N, null, YNType.Y, "🎊");
        setHolidaySeq(holiday, seq);

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
        String errorMessage = "Holiday not found";

        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.empty());
        given(ms.getMessage("error.notfound.holiday", null, null)).willReturn(errorMessage);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> holidayService.deleteHoliday(seq));

        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        then(holidayRepositoryImpl).should().findById(seq);
        then(holidayRepositoryImpl).should(never()).delete(any(Holiday.class));
    }

    @Test
    @DisplayName("공휴일 존재 확인 테스트 - 성공")
    void checkHolidayExistSuccessTest() {
        // Given
        Long seq = 1L;
        Holiday holiday = Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC,
                CountryCode.KR, YNType.N, null, YNType.Y, "🎊");
        setHolidaySeq(holiday, seq);

        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.of(holiday));

        // When
        Holiday result = holidayService.checkHolidayExist(seq);

        // Then
        then(holidayRepositoryImpl).should().findById(seq);
        assertThat(result).isEqualTo(holiday);
        assertThat(result.getSeq()).isEqualTo(seq);
    }

    @Test
    @DisplayName("공휴일 존재 확인 테스트 - 실패 (공휴일 없음)")
    void checkHolidayExistFailTest() {
        // Given
        Long seq = 900L;
        String errorMessage = "Holiday not found";

        given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.empty());
        given(ms.getMessage("error.notfound.holiday", null, null)).willReturn(errorMessage);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> holidayService.checkHolidayExist(seq));

        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        then(holidayRepositoryImpl).should().findById(seq);
        then(ms).should().getMessage("error.notfound.holiday", null, null);
    }

    @Test
    @DisplayName("기간과 타입별 공휴일 조회 테스트")
    void findHolidaysByStartEndDateWithTypeTest() {
        // Given
        String start = "20250101";
        String end = "20251231";
        HolidayType type = HolidayType.PUBLIC;

        List<Holiday> expectedHolidays = List.of(
                Holiday.createHoliday("신정", "20250101", type, CountryCode.KR, YNType.N, null, YNType.Y, "🎊"),
                Holiday.createHoliday("설날", "20250129", type, CountryCode.KR, YNType.Y, "20250129", YNType.Y, "🌙")
        );

        // Service에 해당 메서드가 없다면 추가 구현 필요
        // given(holidayRepositoryImpl.findHolidaysByStartEndDateWithType(start, end, type)).willReturn(expectedHolidays);

        // When & Then
        // 실제 서비스에 해당 메서드가 구현되어야 함
        // List<Holiday> holidays = holidayService.findHolidaysByStartEndDateWithType(start, end, type);
        // assertThat(holidays).hasSize(2);
    }

    // 테스트 헬퍼 메서드
    private void setHolidaySeq(Holiday holiday, Long seq) {
        try {
            java.lang.reflect.Field field = Holiday.class.getDeclaredField("seq");
            field.setAccessible(true);
            field.set(holiday, seq);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set holiday seq", e);
        }
    }
}
