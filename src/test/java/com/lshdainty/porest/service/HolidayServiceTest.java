package com.lshdainty.porest.service;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.holiday.repository.HolidayRepositoryImpl;
import com.lshdainty.porest.holiday.service.HolidayService;
import com.lshdainty.porest.holiday.service.dto.HolidayServiceDto;
import com.lshdainty.porest.holiday.type.HolidayType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Nested
    @DisplayName("공휴일 등록")
    class RegistHoliday {
        @Test
        @DisplayName("성공 - 공휴일이 정상적으로 저장된다")
        void registHolidaySuccess() {
            // given
            HolidayServiceDto data = HolidayServiceDto.builder()
                    .name("설날")
                    .date("20250129")
                    .type(HolidayType.PUBLIC)
                    .countryCode(CountryCode.KR)
                    .lunarYN(YNType.Y)
                    .lunarDate("0101")
                    .isRecurring(YNType.Y)
                    .icon("🎉")
                    .build();
            willDoNothing().given(holidayRepositoryImpl).save(any(Holiday.class));

            // when
            holidayService.registHoliday(data);

            // then
            then(holidayRepositoryImpl).should().save(any(Holiday.class));
        }
    }

    @Nested
    @DisplayName("공휴일 단건 조회")
    class FindById {
        @Test
        @DisplayName("성공 - 존재하는 공휴일을 반환한다")
        void findByIdSuccess() {
            // given
            Long seq = 1L;
            Holiday holiday = Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "0101", YNType.Y, "🎉");
            setHolidaySeq(holiday, seq);
            given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.of(holiday));

            // when
            Holiday result = holidayService.findById(seq);

            // then
            then(holidayRepositoryImpl).should().findById(seq);
            assertThat(result.getName()).isEqualTo("설날");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공휴일이면 예외가 발생한다")
        void findByIdFailNotFound() {
            // given
            Long seq = 999L;
            given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.empty());
            given(ms.getMessage(eq("error.notfound.holiday"), any(), any())).willReturn("공휴일을 찾을 수 없습니다");

            // when & then
            assertThatThrownBy(() -> holidayService.findById(seq))
                    .isInstanceOf(IllegalArgumentException.class);
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
                    Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "0101", YNType.Y, "🎉"),
                    Holiday.createHoliday("추석", "20251006", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "0815", YNType.Y, "🌕")
            );
            given(holidayRepositoryImpl.findHolidays(countryCode)).willReturn(holidays);

            // when
            List<Holiday> result = holidayService.findHolidays(countryCode);

            // then
            then(holidayRepositoryImpl).should().findHolidays(countryCode);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("성공 - 공휴일이 없을 경우 빈 리스트가 반환된다")
        void findHolidaysEmptyList() {
            // given
            CountryCode countryCode = CountryCode.US;
            given(holidayRepositoryImpl.findHolidays(countryCode)).willReturn(List.of());

            // when
            List<Holiday> result = holidayService.findHolidays(countryCode);

            // then
            then(holidayRepositoryImpl).should().findHolidays(countryCode);
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
            String startDate = "20250101";
            String endDate = "20251231";
            CountryCode countryCode = CountryCode.KR;
            List<Holiday> holidays = List.of(
                    Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "0101", YNType.Y, "🎉")
            );
            given(holidayRepositoryImpl.findHolidaysByStartEndDate(startDate, endDate, countryCode)).willReturn(holidays);

            // when
            List<Holiday> result = holidayService.searchHolidaysByStartEndDate(startDate, endDate, countryCode);

            // then
            then(holidayRepositoryImpl).should().findHolidaysByStartEndDate(startDate, endDate, countryCode);
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
                    Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "0101", YNType.Y, "🎉")
            );
            given(holidayRepositoryImpl.findHolidaysByType(type)).willReturn(holidays);

            // when
            List<Holiday> result = holidayService.searchHolidaysByType(type);

            // then
            then(holidayRepositoryImpl).should().findHolidaysByType(type);
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
            Long seq = 1L;
            Holiday holiday = Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "0101", YNType.Y, "🎉");
            setHolidaySeq(holiday, seq);
            given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.of(holiday));

            HolidayServiceDto data = HolidayServiceDto.builder()
                    .seq(seq)
                    .name("설날 연휴")
                    .date("20250130")
                    .build();

            // when
            holidayService.editHoliday(data);

            // then
            then(holidayRepositoryImpl).should().findById(seq);
            assertThat(holiday.getName()).isEqualTo("설날 연휴");
            assertThat(holiday.getDate()).isEqualTo("20250130");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공휴일을 수정하려 하면 예외가 발생한다")
        void editHolidayFailNotFound() {
            // given
            Long seq = 999L;
            HolidayServiceDto data = HolidayServiceDto.builder().seq(seq).build();
            given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.empty());
            given(ms.getMessage(eq("error.notfound.holiday"), any(), any())).willReturn("공휴일을 찾을 수 없습니다");

            // when & then
            assertThatThrownBy(() -> holidayService.editHoliday(data))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("공휴일 삭제")
    class DeleteHoliday {
        @Test
        @DisplayName("성공 - 공휴일이 삭제된다")
        void deleteHolidaySuccess() {
            // given
            Long seq = 1L;
            Holiday holiday = Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "0101", YNType.Y, "🎉");
            given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.of(holiday));
            willDoNothing().given(holidayRepositoryImpl).delete(holiday);

            // when
            holidayService.deleteHoliday(seq);

            // then
            then(holidayRepositoryImpl).should().findById(seq);
            then(holidayRepositoryImpl).should().delete(holiday);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공휴일을 삭제하려 하면 예외가 발생한다")
        void deleteHolidayFailNotFound() {
            // given
            Long seq = 999L;
            given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.empty());
            given(ms.getMessage(eq("error.notfound.holiday"), any(), any())).willReturn("공휴일을 찾을 수 없습니다");

            // when & then
            assertThatThrownBy(() -> holidayService.deleteHoliday(seq))
                    .isInstanceOf(IllegalArgumentException.class);
            then(holidayRepositoryImpl).should(never()).delete(any(Holiday.class));
        }
    }

    @Nested
    @DisplayName("공휴일 존재 확인")
    class CheckHolidayExist {
        @Test
        @DisplayName("성공 - 존재하는 공휴일을 반환한다")
        void checkHolidayExistSuccess() {
            // given
            Long seq = 1L;
            Holiday holiday = Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC, CountryCode.KR, YNType.Y, "0101", YNType.Y, "🎉");
            given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.of(holiday));

            // when
            Holiday result = holidayService.checkHolidayExist(seq);

            // then
            assertThat(result).isEqualTo(holiday);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공휴일이면 예외가 발생한다")
        void checkHolidayExistFailNotFound() {
            // given
            Long seq = 999L;
            given(holidayRepositoryImpl.findById(seq)).willReturn(Optional.empty());
            given(ms.getMessage(eq("error.notfound.holiday"), any(), any())).willReturn("공휴일을 찾을 수 없습니다");

            // when & then
            assertThatThrownBy(() -> holidayService.checkHolidayExist(seq))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // 테스트 헬퍼 메서드
    private void setHolidaySeq(Holiday holiday, Long seq) {
        try {
            java.lang.reflect.Field field = Holiday.class.getDeclaredField("seq");
            field.setAccessible(true);
            field.set(holiday, seq);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
