package com.porest.hr.work.service;

import com.porest.hr.work.domain.WorkSystemLog;
import com.porest.hr.work.repository.WorkSystemLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WorkSystemLogServiceImpl implements WorkSystemLogService {
    private final WorkSystemLogRepository workSystemLogRepository;

    @Override
    @Transactional
    public boolean toggleSystemCheck(String code) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        Optional<WorkSystemLog> existingLog = workSystemLogRepository
                .findByPeriodAndCode(startOfDay, endOfDay, code);

        if (existingLog.isPresent()) {
            // 이미 체크된 로그가 있으면 삭제
            workSystemLogRepository.delete(existingLog.get());
            log.info("System log deleted - code: {}", code);
            return false;
        } else {
            // 체크된 로그가 없으면 생성 (userId는 AuditingFields에서 자동 설정)
            WorkSystemLog newLog = WorkSystemLog.of(code);
            workSystemLogRepository.save(newLog);
            log.info("System log created - code: {}", code);
            return true;
        }
    }

    @Override
    public boolean isCheckedToday(String code) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        return workSystemLogRepository
                .findByPeriodAndCode(startOfDay, endOfDay, code)
                .isPresent();
    }

    @Override
    public Map<String, Boolean> checkSystemStatusBatch(List<String> codes) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<String> checkedCodes = workSystemLogRepository
                .findCodesByPeriodAndCodes(startOfDay, endOfDay, codes);

        Map<String, Boolean> result = codes.stream()
                .collect(Collectors.toMap(
                        code -> code,
                        checkedCodes::contains
                ));

        log.info("Batch system status checked - codes: {}, result: {}", codes, result);
        return result;
    }
}
