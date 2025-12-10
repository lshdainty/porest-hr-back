package com.lshdainty.porest.work.service;

import com.lshdainty.porest.work.domain.WorkSystemLog;
import com.lshdainty.porest.work.repository.WorkSystemLogRepository;
import com.lshdainty.porest.work.type.SystemType;
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
    public boolean toggleSystemCheck(SystemType code) {
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
    public boolean isCheckedToday(SystemType code) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        return workSystemLogRepository
                .findByPeriodAndCode(startOfDay, endOfDay, code)
                .isPresent();
    }

    @Override
    public Map<SystemType, Boolean> checkSystemStatusBatch(List<SystemType> codes) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<SystemType> checkedCodes = workSystemLogRepository
                .findCodesByPeriodAndCodes(startOfDay, endOfDay, codes);

        Map<SystemType, Boolean> result = codes.stream()
                .collect(Collectors.toMap(
                        code -> code,
                        checkedCodes::contains
                ));

        log.info("Batch system status checked - codes: {}, result: {}", codes, result);
        return result;
    }
}
