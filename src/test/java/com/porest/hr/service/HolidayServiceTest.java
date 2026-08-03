package com.porest.hr.service;

import com.porest.core.exception.DuplicateException;
import com.porest.core.exception.EntityNotFoundException;
import com.porest.core.type.CountryCode;
import com.porest.core.type.YNType;
import com.porest.hr.holiday.domain.Holiday;
import com.porest.hr.holiday.repository.HolidayRepository;
import com.porest.hr.holiday.service.HolidayServiceImpl;
import com.porest.hr.holiday.service.dto.HolidayServiceDto;
import com.porest.hr.holiday.type.HolidayType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("공휴일 서비스 테스트")
class HolidayServiceTest {
    @Mock
    private HolidayRepository holidayRepository;

    @InjectMocks
    private HolidayServiceImpl holidayService;

    @Nested
    @DisplayName("공휴일 등록")
    class RegistHoliday {
        @Test
        @DisplayName("성공 - 공휴일이 정상적으로 저장된다")
        void registHolidaySuccess() {
            // given
            HolidayServiceDto data = HolidayServiceDto.builder()
                    .name("설날")
                    .date(LocalDate.of(2025, 1, 29))
                    .type(HolidayType.PUBLIC)
                    .countryCode(CountryCode.KR)
                    .lunarYN(YNType.Y)
                    .lunarDate(LocalDate.of(2025, 1, 1))
                    .isRecurring(YNType.Y)
                    .icon("🎉")
                    .build();
            willDoNothing().given(holidayRepository).save(any(Holiday.class));

            // when
            holidayService.registHoliday(data);

            // then
            then(holidayRepository).should().save(any(Holiday.class));
        }
    }

    @Nested
    @DisplayName("공휴일 단건 조회")
    class FindById {
        @Test
        @DisplayName("성공 - 존재하는 공휴일을 반환한다")
        void findByIdSuccess() {
            // given
            Long id = 1L;
            Holiday holiday = Holiday.createHoliday("설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 1, 1), YNType.Y, "🎉");
            setHolidayId(holiday, id);
            given(holidayRepository.findByRowId(id)).willReturn(Optional.of(holiday));

            // when
            Holiday result = holidayService.findByRowId(id);

            // then
            then(holidayRepository).should().findByRowId(id);
            assertThat(result.getName()).isEqualTo("설날");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공휴일이면 예외가 발생한다")
        void findByIdFailNotFound() {
            // given
            Long id = 999L;
            given(holidayRepository.findByRowId(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> holidayService.findByRowId(id))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("국가별 공휴일 조회")
    class FindHolidays {
        @Test
        @DisplayName("성공 - 국가 코드로 공휴일 목록을 조회한다")
        void findHolidaysSuccess() {
            // given
            CountryCode countryCode = CountryCode.KR;
            List<Holiday> holidays = List.of(
                    Holiday.createHoliday("설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 1, 1), YNType.Y, "🎉"),
                    Holiday.createHoliday("추석", LocalDate.of(2025, 10, 6), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 8, 15), YNType.Y, "🌕")
            );
            given(holidayRepository.findHolidays(countryCode)).willReturn(holidays);

            // when
            List<Holiday> result = holidayService.findHolidays(countryCode);

            // then
            then(holidayRepository).should().findHolidays(countryCode);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("성공 - 공휴일이 없을 경우 빈 리스트가 반환된다")
        void findHolidaysEmptyList() {
            // given
            CountryCode countryCode = CountryCode.US;
            given(holidayRepository.findHolidays(countryCode)).willReturn(List.of());

            // when
            List<Holiday> result = holidayService.findHolidays(countryCode);

            // then
            then(holidayRepository).should().findHolidays(countryCode);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("기간별 공휴일 조회")
    class SearchHolidaysByStartEndDate {
        @Test
        @DisplayName("성공 - 시작일과 종료일 사이의 공휴일을 조회한다")
        void searchHolidaysByStartEndDateSuccess() {
            // given
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);
            CountryCode countryCode = CountryCode.KR;
            List<Holiday> holidays = List.of(
                    Holiday.createHoliday("설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 1, 1), YNType.Y, "🎉")
            );
            given(holidayRepository.findHolidaysByStartEndDate(startDate, endDate, countryCode)).willReturn(holidays);

            // when
            List<Holiday> result = holidayService.searchHolidaysByStartEndDate(startDate, endDate, countryCode);

            // then
            then(holidayRepository).should().findHolidaysByStartEndDate(startDate, endDate, countryCode);
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("타입별 공휴일 조회")
    class SearchHolidaysByType {
        @Test
        @DisplayName("성공 - 공휴일 타입으로 목록을 조회한다")
        void searchHolidaysByTypeSuccess() {
            // given
            HolidayType type = HolidayType.PUBLIC;
            List<Holiday> holidays = List.of(
                    Holiday.createHoliday("설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 1, 1), YNType.Y, "🎉")
            );
            given(holidayRepository.findHolidaysByType(type)).willReturn(holidays);

            // when
            List<Holiday> result = holidayService.searchHolidaysByType(type);

            // then
            then(holidayRepository).should().findHolidaysByType(type);
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("공휴일 수정")
    class EditHoliday {
        @Test
        @DisplayName("성공 - 공휴일 정보가 수정된다")
        void editHolidaySuccess() {
            // given
            Long id = 1L;
            Holiday holiday = Holiday.createHoliday("설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 1, 1), YNType.Y, "🎉");
            setHolidayId(holiday, id);
            given(holidayRepository.findByRowId(id)).willReturn(Optional.of(holiday));

            HolidayServiceDto data = HolidayServiceDto.builder()
                    .id(id)
                    .name("설날 연휴")
                    .date(LocalDate.of(2025, 1, 30))
                    .build();

            // when
            holidayService.editHoliday(data);

            // then
            then(holidayRepository).should().findByRowId(id);
            assertThat(holiday.getName()).isEqualTo("설날 연휴");
            assertThat(holiday.getDate()).isEqualTo(LocalDate.of(2025, 1, 30));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공휴일을 수정하려 하면 예외가 발생한다")
        void editHolidayFailNotFound() {
            // given
            Long id = 999L;
            HolidayServiceDto data = HolidayServiceDto.builder().id(id).build();
            given(holidayRepository.findByRowId(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> holidayService.editHoliday(data))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("공휴일 삭제")
    class DeleteHoliday {
        @Test
        @DisplayName("성공 - 공휴일이 삭제된다")
        void deleteHolidaySuccess() {
            // given
            Long id = 1L;
            Holiday holiday = Holiday.createHoliday("설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 1, 1), YNType.Y, "🎉");
            given(holidayRepository.findByRowId(id)).willReturn(Optional.of(holiday));
            willDoNothing().given(holidayRepository).delete(holiday);

            // when
            holidayService.deleteHoliday(id);

            // then
            then(holidayRepository).should().findByRowId(id);
            then(holidayRepository).should().delete(holiday);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공휴일을 삭제하려 하면 예외가 발생한다")
        void deleteHolidayFailNotFound() {
            // given
            Long id = 999L;
            given(holidayRepository.findByRowId(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> holidayService.deleteHoliday(id))
                    .isInstanceOf(EntityNotFoundException.class);
            then(holidayRepository).should(never()).delete(any(Holiday.class));
        }
    }

    @Nested
    @DisplayName("공휴일 존재 확인")
    class CheckHolidayExist {
        @Test
        @DisplayName("성공 - 존재하는 공휴일을 반환한다")
        void checkHolidayExistSuccess() {
            // given
            Long id = 1L;
            Holiday holiday = Holiday.createHoliday("설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC, CountryCode.KR, YNType.Y, LocalDate.of(2025, 1, 1), YNType.Y, "🎉");
            given(holidayRepository.findByRowId(id)).willReturn(Optional.of(holiday));

            // when
            Holiday result = holidayService.checkHolidayExist(id);

            // then
            assertThat(result).isEqualTo(holiday);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공휴일이면 예외가 발생한다")
        void checkHolidayExistFailNotFound() {
            // given
            Long id = 999L;
            given(holidayRepository.findByRowId(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> holidayService.checkHolidayExist(id))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("반복 공휴일 프리뷰 조회")
    class GetRecurringHolidaysPreview {
        @Test
        @DisplayName("성공 - 양력 반복 공휴일 프리뷰")
        void getRecurringHolidaysPreviewSolarSuccess() {
            // given
            int targetYear = 2026;
            CountryCode countryCode = CountryCode.KR;
            Holiday solarHoliday = Holiday.createHoliday(
                    "광복절", LocalDate.of(2025, 8, 15), HolidayType.PUBLIC,
                    CountryCode.KR, YNType.N, null, YNType.Y, null
            );
            given(holidayRepository.findByIsRecurring(YNType.Y, countryCode))
                    .willReturn(List.of(solarHoliday));

            // when
            List<HolidayServiceDto> result = holidayService.getRecurringHolidaysPreview(targetYear, countryCode);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 8, 15));
            assertThat(result.get(0).getIsRecurring()).isEqualTo(YNType.N);
        }

        @Test
        @DisplayName("성공 - 음력 반복 공휴일 프리뷰 (양력/음력 년도가 같은 경우)")
        void getRecurringHolidaysPreviewLunarSuccess() {
            // given
            int targetYear = 2026;
            CountryCode countryCode = CountryCode.KR;
            // 설날: 양력 2025-01-29, 음력 2025-01-01 (년도 차이 0)
            Holiday lunarHoliday = Holiday.createHoliday(
                    "설날", LocalDate.of(2025, 1, 29), HolidayType.PUBLIC,
                    CountryCode.KR, YNType.Y, LocalDate.of(2025, 1, 1), YNType.Y, null
            );
            given(holidayRepository.findByIsRecurring(YNType.Y, countryCode))
                    .willReturn(List.of(lunarHoliday));

            // when
            List<HolidayServiceDto> result = holidayService.getRecurringHolidaysPreview(targetYear, countryCode);

            // then
            assertThat(result).hasSize(1);
            // yearOffset = 2025 - 2025 = 0, targetLunarYear = 2026 - 0 = 2026
            assertThat(result.get(0).getLunarDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            // 양력 날짜는 음력 변환 결과 (2026년 음력 1월 1일 -> 양력 2026-02-17)
            assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 2, 17));
            assertThat(result.get(0).getIsRecurring()).isEqualTo(YNType.N);
        }

        @Test
        @DisplayName("성공 - 음력 반복 공휴일 프리뷰 (양력/음력 년도가 다른 경우 - 설날 전날)")
        void getRecurringHolidaysPreviewLunarWithYearOffsetSuccess() {
            // given
            int targetYear = 2026;
            CountryCode countryCode = CountryCode.KR;
            // 설날 전날: 양력 2025-01-28, 음력 2024-12-29 (년도 차이 1)
            Holiday lunarHoliday = Holiday.createHoliday(
                    "설날연휴", LocalDate.of(2025, 1, 28), HolidayType.PUBLIC,
                    CountryCode.KR, YNType.Y, LocalDate.of(2024, 12, 29), YNType.Y, null
            );
            given(holidayRepository.findByIsRecurring(YNType.Y, countryCode))
                    .willReturn(List.of(lunarHoliday));

            // when
            List<HolidayServiceDto> result = holidayService.getRecurringHolidaysPreview(targetYear, countryCode);

            // then
            assertThat(result).hasSize(1);
            // yearOffset = 2025 - 2024 = 1, targetLunarYear = 2026 - 1 = 2025
            assertThat(result.get(0).getLunarDate()).isEqualTo(LocalDate.of(2025, 12, 29));
            // 양력 날짜는 음력 변환 결과 (2025년 음력 12월 29일 -> 양력 2026-02-16)
            assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 2, 16));
            assertThat(result.get(0).getIsRecurring()).isEqualTo(YNType.N);
        }

        @Test
        @DisplayName("성공 - 반복 공휴일이 없으면 빈 리스트")
        void getRecurringHolidaysPreviewEmpty() {
            // given
            given(holidayRepository.findByIsRecurring(YNType.Y, CountryCode.KR))
                    .willReturn(List.of());

            // when
            List<HolidayServiceDto> result = holidayService.getRecurringHolidaysPreview(2026, CountryCode.KR);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - 음력 공휴일이지만 lunarDate가 null인 경우 양력으로 처리")
        void getRecurringHolidaysPreviewLunarWithNullLunarDate() {
            // given
            int targetYear = 2026;
            CountryCode countryCode = CountryCode.KR;
            // 음력 플래그는 Y이지만 lunarDate가 null인 경우
            Holiday lunarHolidayWithNullDate = Holiday.createHoliday(
                    "테스트공휴일", LocalDate.of(2025, 5, 5), HolidayType.PUBLIC,
                    CountryCode.KR, YNType.Y, null, YNType.Y, null
            );
            given(holidayRepository.findByIsRecurring(YNType.Y, countryCode))
                    .willReturn(List.of(lunarHolidayWithNullDate));

            // when
            List<HolidayServiceDto> result = holidayService.getRecurringHolidaysPreview(targetYear, countryCode);

            // then
            assertThat(result).hasSize(1);
            // lunarDate가 null이므로 양력으로 처리되어 년도만 변경
            assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 5, 5));
            assertThat(result.get(0).getLunarDate()).isNull();
        }

        @Test
        @DisplayName("성공 - 음력 공휴일이지만 lunarDate 년도가 0 이하인 경우 양력으로 처리")
        void getRecurringHolidaysPreviewLunarWithInvalidYear() {
            // given
            int targetYear = 2026;
            CountryCode countryCode = CountryCode.KR;
            // lunarDate 년도가 0인 경우 (유효하지 않은 음력 날짜)
            Holiday lunarHolidayWithInvalidYear = Holiday.createHoliday(
                    "테스트공휴일", LocalDate.of(2025, 5, 5), HolidayType.PUBLIC,
                    CountryCode.KR, YNType.Y, LocalDate.of(0, 4, 4), YNType.Y, null
            );
            given(holidayRepository.findByIsRecurring(YNType.Y, countryCode))
                    .willReturn(List.of(lunarHolidayWithInvalidYear));

            // when
            List<HolidayServiceDto> result = holidayService.getRecurringHolidaysPreview(targetYear, countryCode);

            // then
            assertThat(result).hasSize(1);
            // lunarDate가 유효하지 않으므로 양력으로 처리되어 년도만 변경
            assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 5, 5));
            assertThat(result.get(0).getLunarDate()).isNull();
        }

        @Test
        @DisplayName("성공 - 음력 변환 실패 시 양력 날짜로 폴백")
        void getRecurringHolidaysPreviewLunarConversionFailFallback() {
            // given
            int targetYear = 2026;
            CountryCode countryCode = CountryCode.KR;
            // 음력 달력은 29일 또는 30일까지만 존재하므로 음력 1월 31일은 유효하지 않음
            // 이 경우 음력 변환이 실패하고 양력 날짜로 폴백해야 함
            Holiday lunarHolidayWithInvalidLunarDate = Holiday.createHoliday(
                    "테스트공휴일", LocalDate.of(2025, 5, 5), HolidayType.PUBLIC,
                    CountryCode.KR, YNType.Y, LocalDate.of(2025, 1, 31), YNType.Y, null
            );
            given(holidayRepository.findByIsRecurring(YNType.Y, countryCode))
                    .willReturn(List.of(lunarHolidayWithInvalidLunarDate));

            // when
            List<HolidayServiceDto> result = holidayService.getRecurringHolidaysPreview(targetYear, countryCode);

            // then
            assertThat(result).hasSize(1);
            // 음력 1월 31일은 존재하지 않으므로 변환 실패 -> 양력 날짜로 폴백
            assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 5, 5));
            // 음력 날짜는 null로 무효화
            assertThat(result.get(0).getLunarDate()).isNull();
        }
    }

    @Nested
    @DisplayName("공휴일 일괄 저장")
    class BulkSaveHolidays {
        @Test
        @DisplayName("성공 - 공휴일 일괄 저장")
        void bulkSaveHolidaysSuccess() {
            // given
            List<HolidayServiceDto> holidays = List.of(
                    HolidayServiceDto.builder()
                            .name("설날")
                            .date(LocalDate.of(2026, 2, 17))
                            .type(HolidayType.PUBLIC)
                            .countryCode(CountryCode.KR)
                            .lunarYN(YNType.Y)
                            .lunarDate(LocalDate.of(2026, 1, 1))
                            .isRecurring(YNType.N)
                            .build()
            );
            given(holidayRepository.existsByDateAndNameAndCountryCode(any(), any(), any()))
                    .willReturn(false);
            willDoNothing().given(holidayRepository).saveAll(anyList());

            // when
            int result = holidayService.bulkSaveHolidays(holidays);

            // then
            assertThat(result).isEqualTo(1);
            then(holidayRepository).should().saveAll(anyList());
        }

        @Test
        @DisplayName("실패 - 중복 공휴일 존재 시 예외 발생")
        void bulkSaveHolidaysFailDuplicate() {
            // given
            List<HolidayServiceDto> holidays = List.of(
                    HolidayServiceDto.builder()
                            .name("설날")
                            .date(LocalDate.of(2026, 2, 17))
                            .type(HolidayType.PUBLIC)
                            .countryCode(CountryCode.KR)
                            .lunarYN(YNType.Y)
                            .isRecurring(YNType.N)
                            .build()
            );
            given(holidayRepository.existsByDateAndNameAndCountryCode(
                    LocalDate.of(2026, 2, 17), "설날", CountryCode.KR))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> holidayService.bulkSaveHolidays(holidays))
                    .isInstanceOf(DuplicateException.class);
            then(holidayRepository).should(never()).saveAll(anyList());
        }

        @Test
        @DisplayName("성공 - 빈 목록 저장 시 0 반환")
        void bulkSaveHolidaysEmptyList() {
            // given
            List<HolidayServiceDto> holidays = List.of();

            // when
            int result = holidayService.bulkSaveHolidays(holidays);

            // then
            assertThat(result).isEqualTo(0);
            then(holidayRepository).should(never()).saveAll(anyList());
        }
    }

    // 테스트 헬퍼 메서드
    private void setHolidayId(Holiday holiday, Long id) {
        try {
            java.lang.reflect.Field field = Holiday.class.getDeclaredField("rowId");
            field.setAccessible(true);
            field.set(holiday, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
