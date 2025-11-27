package com.lshdainty.porest.work.service;

import com.lshdainty.porest.work.domain.WorkSystemLog;
import com.lshdainty.porest.work.repository.WorkSystemLogRepository;
import com.lshdainty.porest.work.type.SystemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 업무 시스템 로그 서비스<br>
 * 시스템 체크 관련 비즈니스 로직 처리<br>
 * - 사용자 정보는 AuditingFields의 createBy에서 자동으로 설정됨<br>
 * - 체크 시간은 AuditingFields의 createDate에서 자동으로 설정됨<br>
 * - 누가 체크했는지는 중요하지 않고, 시스템이 오늘 체크됐는지만 확인
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WorkSystemLogService {
    private final WorkSystemLogRepository workSystemLogRepository;

    /**
     * 시스템 체크 토글<br>
     * 오늘 날짜로 이미 체크된 로그가 있으면 삭제, 없으면 생성<br>
     * 누가 체크했는지는 무관하게 시스템 전체 체크 여부만 관리
     *
     * @param code 시스템 코드
     * @return true: 생성됨, false: 삭제됨
     */
    @Transactional
    public boolean toggleSystemCheck(SystemType code) {
        LocalDate today = LocalDate.now();

        Optional<WorkSystemLog> existingLog = workSystemLogRepository
                .findTodayLogByCode(today, code);

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

    /**
     * 오늘 날짜 특정 시스템 체크 여부 확인<br>
     * 누가 체크했는지는 무관
     *
     * @param code 시스템 코드
     * @return true: 체크됨, false: 체크 안됨
     */
    public boolean isCheckedToday(SystemType code) {
        LocalDate today = LocalDate.now();
        return workSystemLogRepository
                .findTodayLogByCode(today, code)
                .isPresent();
    }
}
