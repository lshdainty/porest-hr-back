package com.porest.hr.vacation.scheduler;

import com.porest.hr.vacation.domain.VacationGrant;
import com.porest.hr.vacation.domain.VacationGrantSchedule;
import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.repository.VacationGrantRepository;
import com.porest.hr.vacation.repository.VacationGrantScheduleRepository;
import com.porest.hr.vacation.service.policy.RepeatGrant;
import com.porest.hr.vacation.service.policy.factory.VacationPolicyStrategyFactory;
import com.porest.hr.vacation.type.GrantMethod;
import com.porest.hr.vacation.type.VacationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 반복 부여 휴가 정책 스케줄러<br>
 * 매일 자정(00:00)에 실행되어 오늘 부여해야 할 휴가를 자동으로 부여함
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VacationGrantScheduler {
    private final VacationGrantScheduleRepository vacationGrantScheduleRepository;
    private final VacationGrantRepository vacationGrantRepository;
    private final VacationPolicyStrategyFactory strategyFactory;

    /**
     * 만료된 휴가 자동 처리 스케줄러<br>
     * 매일 자정(00:00)에 실행<br>
     * cron: "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void expireVacationsDaily() {
        LocalDateTime now = LocalDateTime.now();
        log.info("========== 휴가 만료 처리 스케줄러 시작 ========== [{}]", now);

        try {
            // 1. 만료 대상 VacationGrant 조회 (status == ACTIVE && expiryDate < 현재)
            List<VacationGrant> expiredTargets = vacationGrantRepository.findExpiredTargets(now);
            log.info("만료 대상 휴가 수: {}", expiredTargets.size());

            if (expiredTargets.isEmpty()) {
                log.info("만료 처리할 휴가가 없습니다.");
                return;
            }

            // 2. 각 grant에 대해 만료 처리
            int successCount = 0;
            int failCount = 0;

            for (VacationGrant grant : expiredTargets) {
                try {
                    // 만료 처리 (status를 EXPIRED로 변경)
                    grant.expire();
                    successCount++;

                    log.info("휴가 만료 처리 완료 - Grant ID: {}, User: {}, VacationType: {}, RemainTime: {}, ExpiryDate: {}",
                            grant.getId(),
                            grant.getUser().getId(),
                            grant.getType().name(),
                            grant.getRemainTime(),
                            grant.getExpiryDate());

                } catch (Exception e) {
                    log.error("휴가 만료 처리 실패 - VacationGrant ID: {}, Error: {}",
                            grant.getId(), e.getMessage(), e);
                    failCount++;
                }
            }

            log.info("========== 휴가 만료 처리 스케줄러 완료 ========== 성공: {}, 실패: {}, 총: {}",
                    successCount, failCount, expiredTargets.size());

        } catch (Exception e) {
            log.error("휴가 만료 처리 스케줄러 실행 중 오류 발생", e);
            throw e;
        }
    }

    /**
     * 반복 부여 휴가 자동 부여 스케줄러<br>
     * 매일 자정(00:00)에 실행<br>
     * cron: "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void grantVacationsDaily() {
        LocalDate today = LocalDate.now();
        log.info("========== 휴가 자동 부여 스케줄러 시작 ========== [{}]", today);

        try {
            // RepeatGrant 전략 인스턴스 가져오기
            RepeatGrant repeatGrantService = (RepeatGrant) strategyFactory.getStrategy(GrantMethod.REPEAT_GRANT);

            // 1. 오늘 부여 대상인 VacationGrantSchedule 조회
            List<VacationGrantSchedule> targets = vacationGrantScheduleRepository.findRepeatGrantTargetsForToday(today);
            log.info("부여 대상 스케줄 수: {}", targets.size());

            if (targets.isEmpty()) {
                log.info("오늘 부여할 휴가가 없습니다.");
                return;
            }

            // 2. 각 스케줄에 대해 휴가 부여 처리
            List<VacationGrant> grantsToSave = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            int skipCount = 0;

            for (VacationGrantSchedule schedule : targets) {
                try {
                    VacationPolicy policy = schedule.getVacationPolicy();

                    // 오늘이 정책의 실제 부여 예정일인지 검증
                    LocalDate expectedGrantDate = repeatGrantService.calculateNextGrantDate(policy, today.minusDays(1));

                    if (expectedGrantDate == null || !today.equals(expectedGrantDate)) {
                        // 오늘 부여 대상이 아님 → nextGrantDate만 갱신하고 skip
                        LocalDate newNextGrantDate = repeatGrantService.calculateNextGrantDate(policy, today);
                        schedule.updateNextGrantDate(newNextGrantDate);
                        skipCount++;
                        log.info("휴가 부여 대상 아님 (skip) - User: {}, Policy: {}, ExpectedDate: {}, NextGrantDate: {}",
                                schedule.getUser().getId(),
                                policy.getName(),
                                expectedGrantDate,
                                newNextGrantDate);
                        continue;
                    }

                    VacationType vacationType = policy.getVacationType();

                    // 효력 발생일과 만료일 계산
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime startDate = policy.getEffectiveType().calculateDate(now);
                    LocalDateTime expiryDate = policy.getExpirationType().calculateDate(startDate);

                    // VacationGrant 생성
                    String desc = policy.getName() + "에 의한 휴가 부여";
                    VacationGrant vacationGrant = VacationGrant.createVacationGrant(
                            schedule.getUser(),
                            policy,
                            desc,
                            vacationType,
                            policy.getGrantTime(),
                            startDate,
                            expiryDate
                    );

                    grantsToSave.add(vacationGrant);

                    // 다음 부여일 갱신 (현재 부여일 기준으로 재계산)
                    // lastGrantedAt은 실제 부여 시점(today), startDate는 휴가 유효기간 시작일
                    LocalDate newNextGrantDate = repeatGrantService.calculateNextGrantDate(policy, today);
                    schedule.updateGrantHistory(today.atStartOfDay(), newNextGrantDate);

                    successCount++;
                    log.info("휴가 부여 완료 - User: {}, Policy: {}, VacationType: {}, GrantTime: {}, StartDate: {}, ExpiryDate: {}, NextGrantDate: {}",
                            schedule.getUser().getId(),
                            policy.getName(),
                            vacationType.name(),
                            policy.getGrantTime(),
                            startDate,
                            expiryDate,
                            newNextGrantDate);

                } catch (Exception e) {
                    log.error("휴가 부여 실패 - VacationGrantSchedule ID: {}, Error: {}",
                            schedule.getId(), e.getMessage(), e);
                    failCount++;
                    // 개별 실패는 스킵하고 계속 진행
                }
            }

            // 3. 일괄 저장
            if (!grantsToSave.isEmpty()) {
                vacationGrantRepository.saveAll(grantsToSave);
                log.info("VacationGrant {} 건 저장 완료", grantsToSave.size());
            }

            log.info("========== 휴가 자동 부여 스케줄러 완료 ========== 성공: {}, 실패: {}, 스킵: {}, 총: {}",
                    successCount, failCount, skipCount, targets.size());

        } catch (Exception e) {
            log.error("휴가 자동 부여 스케줄러 실행 중 오류 발생", e);
            throw e;
        }
    }
}