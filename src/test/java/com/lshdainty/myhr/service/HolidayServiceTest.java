//package com.lshdainty.myhr.service;
//
//import com.lshdainty.myhr.domain.Holiday;
//import com.lshdainty.myhr.domain.HolidayType;
//import com.lshdainty.myhr.repository.HolidayRepositoryImpl;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.context.MessageSource;
//
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.*;
//
//@Slf4j
//@ExtendWith(MockitoExtension.class)
//@DisplayName("공휴일 서비스 테스트")
//class HolidayServiceTest {
//    @Mock
//    private MessageSource ms;
//
//    @Mock
//    private HolidayRepositoryImpl holidayRepositoryImpl;
//
//    @InjectMocks
//    private HolidayService holidayService;
//
//    @Test
//    @DisplayName("휴일 저장 테스트 - 성공")
//    void saveHolidaySuccessTest() {
//        // Given
//        String name = "신정";
//        String date = "20250101";
//        HolidayType type = HolidayType.PUBLIC;
//        willDoNothing().given(holidayRepositoryImpl).save(any(Holiday.class));
//
//        // When
//        Long savedSeq = holidayService.save(name, date, type);
//
//        // Then
//        then(holidayRepositoryImpl).should().save(any(Holiday.class));
//    }
//
//    @Test
//    @DisplayName("단건 휴일 조회 테스트 - 성공")
//    void findByIdSuccessTest() {
//        // Given
//        Long seq = 1L;
//        String name = "신정";
//        String date = "20250101";
//        HolidayType type = HolidayType.PUBLIC;
//        Holiday holiday = Holiday.createHoliday(name, date, type);
//
//        // Reflection을 사용하여 seq 설정 (테스트용)
//        try {
//            java.lang.reflect.Field field = Holiday.class.getDeclaredField("seq");
//            field.setAccessible(true);
//            field.set(holiday, seq);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        given(holidayRepositoryImpl.findHoliday(seq)).willReturn(holiday);
//
//        // When
//        Holiday findHoliday = holidayService.findById(seq);
//
//        // Then
//        then(holidayRepositoryImpl).should().findHoliday(seq);
//        assertThat(findHoliday).isNotNull();
//        assertThat(findHoliday.getSeq()).isEqualTo(seq);
//        assertThat(findHoliday.getName()).isEqualTo(name);
//        assertThat(findHoliday.getDate()).isEqualTo(date);
//        assertThat(findHoliday.getType()).isEqualTo(type);
//    }
//
//    @Test
//    @DisplayName("단일 휴일 조회 테스트 - 실패 (휴일 없음)")
//    void findByIdFailTest() {
//        // Given
//        Long seq = 900L;
//        given(holidayRepositoryImpl.findHoliday(seq)).willReturn(null);
//
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> holidayService.findById(seq));
//        then(holidayRepositoryImpl).should().findHoliday(seq);
//    }
//
//    @Test
//    @DisplayName("전체 휴일 조회 테스트 - 성공 (시간 정렬)")
//    void findHolidaysSuccessTest() {
//        // Given
//        given(holidayRepositoryImpl.findHolidays()).willReturn(List.of(
//                Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC),
//                Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC),
//                Holiday.createHoliday("권장휴가", "20250131", HolidayType.RECOMMEND)
//        ));
//
//        // When
//        List<Holiday> holidays = holidayService.findHolidays();
//
//        // Then
//        then(holidayRepositoryImpl).should().findHolidays();
//        assertThat(holidays).hasSize(3);
//        // date 기준 order by이므로 순서 중요
//        assertThat(holidays)
//                .extracting("name")
//                .containsExactly("신정", "설날", "권장휴가");
//    }
//
//    @Test
//    @DisplayName("휴일 기간별 조회 테스트 - 성공")
//    void findHolidaysByStartEndDateSuccessTest() {
//        // Given
//        String start = "";
//        String end = "";
//        given(holidayRepositoryImpl.findHolidaysByStartEndDate(start, end)).willReturn(List.of(
//                Holiday.createHoliday("신정", "20250101", HolidayType.PUBLIC),
//                Holiday.createHoliday("설날", "20250129", HolidayType.PUBLIC),
//                Holiday.createHoliday("권장휴가", "20250131", HolidayType.RECOMMEND),
//                Holiday.createHoliday("권장휴가", "20251231", HolidayType.RECOMMEND)
//        ));
//
//        // When
//        List<Holiday> holidays = holidayService.findHolidaysByStartEndDate(start, end);
//
//        // Then
//        then(holidayRepositoryImpl).should().findHolidaysByStartEndDate(start, end);
//        assertThat(holidays).hasSize(4);
//        assertThat(holidays)
//                .extracting("name")
//                .containsExactly("신정", "설날", "권장휴가", "권장휴가");
//    }
//
//    @Test
//    @DisplayName("휴일 타입별 조회 테스트 - 성공")
//    void findHolidaysByTypeSuccessTest() {
//        // Given
//        HolidayType type = HolidayType.RECOMMEND;
//        given(holidayRepositoryImpl.findHolidaysByType(type)).willReturn(List.of(
//                Holiday.createHoliday("권장휴가", "20250131", HolidayType.RECOMMEND)
//        ));
//
//        // When
//        List<Holiday> holidays = holidayService.findHolidaysByType(type);
//
//        // Then
//        then(holidayRepositoryImpl).should().findHolidaysByType(type);
//        assertThat(holidays).hasSize(1);
//        assertThat(holidays).extracting("type").containsOnly(type);
//    }
//
//    @Test
//    @DisplayName("휴일 수정 테스트 - 성공")
//    void editHolidaySuccessTest() {
//        // Given
//        Long seq = 1L;
//        String name = "신정";
//        String date = "20250101";
//        HolidayType type = HolidayType.PUBLIC;
//        Holiday holiday = Holiday.createHoliday(name, date, type);
//
//        // Reflection을 사용하여 seq 설정 (테스트용)
//        try {
//            java.lang.reflect.Field field = Holiday.class.getDeclaredField("seq");
//            field.setAccessible(true);
//            field.set(holiday, seq);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        given(holidayRepositoryImpl.findHoliday(seq)).willReturn(holiday);
//
//        // When
//        name = "임시공휴일";
//        date = "20250131";
//        holidayService.editHoliday(seq, name, date, null);
//
//        // Then
//        then(holidayRepositoryImpl).should().findHoliday(seq);
//        assertThat(holiday.getSeq()).isEqualTo(seq);
//        assertThat(holiday.getName()).isEqualTo(name);
//        assertThat(holiday.getDate()).isEqualTo(date);
//        assertThat(holiday.getType()).isEqualTo(type);
//    }
//
//    @Test
//    @DisplayName("휴일 수정 테스트 - 실패 (휴일 없음)")
//    void editHolidayFailTest() {
//        // Given
//        Long seq = 900L;
//        String name = "신정";
//        given(holidayRepositoryImpl.findHoliday(seq)).willReturn(null);
//
//        // When & Then
//        assertThrows(IllegalArgumentException.class,
//                () -> holidayService.editHoliday(seq, name, null, null));
//        then(holidayRepositoryImpl).should().findHoliday(seq);
//    }
//
//    @Test
//    @DisplayName("휴일 삭제 테스트 - 성공")
//    void deleteHolidaySuccessTest() {
//        // Given
//        Long seq = 1L;
//        String name = "신정";
//        String date = "20250101";
//        HolidayType type = HolidayType.PUBLIC;
//        Holiday holiday = Holiday.createHoliday(name, date, type);
//
//        // Reflection을 사용하여 seq 설정 (테스트용)
//        try {
//            java.lang.reflect.Field field = Holiday.class.getDeclaredField("seq");
//            field.setAccessible(true);
//            field.set(holiday, seq);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        given(holidayRepositoryImpl.findHoliday(seq)).willReturn(holiday);
//        willDoNothing().given(holidayRepositoryImpl).delete(holiday);
//
//        // When
//        holidayService.deleteHoliday(seq);
//
//        // Then
//        then(holidayRepositoryImpl).should().findHoliday(seq);
//        then(holidayRepositoryImpl).should().delete(holiday);
//    }
//
//    @Test
//    @DisplayName("휴일 삭제 테스트 - 실패 (휴일 없음)")
//    void deleteHolidayFailTest() {
//        // Given
//        Long seq = 900L;
//        given(holidayRepositoryImpl.findHoliday(seq)).willReturn(null);
//
//        // When & Then
//        assertThrows(IllegalArgumentException.class,
//                () -> holidayService.deleteHoliday(seq));
//        then(holidayRepositoryImpl).should().findHoliday(seq);
//        then(holidayRepositoryImpl).should(never()).delete(any(Holiday.class));
//    }
//}
