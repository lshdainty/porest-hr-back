package com.porest.hr.service;

import com.porest.hr.work.domain.WorkSystemLog;
import com.porest.hr.work.repository.WorkSystemLogRepository;
import com.porest.hr.work.service.WorkSystemLogServiceImpl;
import com.porest.hr.common.type.SystemType;
import com.porest.hr.work.type.TestSystemType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("업무 시스템 로그 서비스 테스트")
class WorkSystemLogServiceTest {

    @Mock
    private WorkSystemLogRepository workSystemLogRepository;

    @InjectMocks
    private WorkSystemLogServiceImpl workSystemLogService;

    @Nested
    @DisplayName("시스템 체크 토글")
    class ToggleSystemCheck {
        @Test
        @DisplayName("성공 - 체크가 없으면 새로 생성하고 true를 반환한다")
        void toggleSystemCheckCreate() {
            // given
            TestSystemType code = TestSystemType.ERP;
            given(workSystemLogRepository.findByPeriodAndCode(any(), any(), eq(code)))
                    .willReturn(Optional.empty());
            willDoNothing().given(workSystemLogRepository).save(any(WorkSystemLog.class));

            // when
            boolean result = workSystemLogService.toggleSystemCheck(code);

            // then
            assertThat(result).isTrue();
            then(workSystemLogRepository).should().save(any(WorkSystemLog.class));
        }

        @Test
        @DisplayName("성공 - 체크가 있으면 삭제하고 false를 반환한다")
        void toggleSystemCheckDelete() {
            // given
            TestSystemType code = TestSystemType.ERP;
            WorkSystemLog existingLog = WorkSystemLog.of(code);
            given(workSystemLogRepository.findByPeriodAndCode(any(), any(), eq(code)))
                    .willReturn(Optional.of(existingLog));
            willDoNothing().given(workSystemLogRepository).delete(existingLog);

            // when
            boolean result = workSystemLogService.toggleSystemCheck(code);

            // then
            assertThat(result).isFalse();
            then(workSystemLogRepository).should().delete(existingLog);
        }
    }

    @Nested
    @DisplayName("오늘 체크 여부 확인")
    class IsCheckedToday {
        @Test
        @DisplayName("성공 - 체크가 있으면 true를 반환한다")
        void isCheckedTodayTrue() {
            // given
            TestSystemType code = TestSystemType.ERP;
            WorkSystemLog log = WorkSystemLog.of(code);
            given(workSystemLogRepository.findByPeriodAndCode(any(), any(), eq(code)))
                    .willReturn(Optional.of(log));

            // when
            boolean result = workSystemLogService.isCheckedToday(code);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 체크가 없으면 false를 반환한다")
        void isCheckedTodayFalse() {
            // given
            TestSystemType code = TestSystemType.ERP;
            given(workSystemLogRepository.findByPeriodAndCode(any(), any(), eq(code)))
                    .willReturn(Optional.empty());

            // when
            boolean result = workSystemLogService.isCheckedToday(code);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("시스템 상태 배치 체크")
    class CheckSystemStatusBatch {
        @Test
        @DisplayName("성공 - 여러 시스템의 상태를 배치로 확인한다")
        void checkSystemStatusBatchSuccess() {
            // given
            List<SystemType> codes = List.of(TestSystemType.ERP, TestSystemType.MES, TestSystemType.WMS);
            List<SystemType> checkedCodes = List.of(TestSystemType.ERP, TestSystemType.WMS);

            given(workSystemLogRepository.findCodesByPeriodAndCodes(any(), any(), eq(codes)))
                    .willReturn(checkedCodes);

            // when
            Map<SystemType, Boolean> result = workSystemLogService.checkSystemStatusBatch(codes);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(TestSystemType.ERP)).isTrue();
            assertThat(result.get(TestSystemType.MES)).isFalse();
            assertThat(result.get(TestSystemType.WMS)).isTrue();
        }

        @Test
        @DisplayName("성공 - 모든 시스템이 체크되지 않았으면 모두 false를 반환한다")
        void checkSystemStatusBatchAllFalse() {
            // given
            List<SystemType> codes = List.of(TestSystemType.ERP, TestSystemType.MES);
            given(workSystemLogRepository.findCodesByPeriodAndCodes(any(), any(), eq(codes)))
                    .willReturn(List.of());

            // when
            Map<SystemType, Boolean> result = workSystemLogService.checkSystemStatusBatch(codes);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(TestSystemType.ERP)).isFalse();
            assertThat(result.get(TestSystemType.MES)).isFalse();
        }

        @Test
        @DisplayName("성공 - 모든 시스템이 체크되었으면 모두 true를 반환한다")
        void checkSystemStatusBatchAllTrue() {
            // given
            List<SystemType> codes = List.of(TestSystemType.ERP, TestSystemType.MES);
            given(workSystemLogRepository.findCodesByPeriodAndCodes(any(), any(), eq(codes)))
                    .willReturn(codes);

            // when
            Map<SystemType, Boolean> result = workSystemLogService.checkSystemStatusBatch(codes);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(TestSystemType.ERP)).isTrue();
            assertThat(result.get(TestSystemType.MES)).isTrue();
        }

        @Test
        @DisplayName("성공 - 빈 리스트를 입력하면 빈 맵을 반환한다")
        void checkSystemStatusBatchEmpty() {
            // given
            List<SystemType> codes = List.of();
            given(workSystemLogRepository.findCodesByPeriodAndCodes(any(), any(), eq(codes)))
                    .willReturn(List.of());

            // when
            Map<SystemType, Boolean> result = workSystemLogService.checkSystemStatusBatch(codes);

            // then
            assertThat(result).isEmpty();
        }
    }
}
