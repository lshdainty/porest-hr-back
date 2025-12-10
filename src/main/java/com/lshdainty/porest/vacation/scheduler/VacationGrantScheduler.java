package com.lshdainty.porest.vacation.scheduler;

import com.lshdainty.porest.vacation.domain.UserVacationPolicy;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.UserVacationPolicyRepository;
import com.lshdainty.porest.vacation.repository.VacationGrantRepository;
import com.lshdainty.porest.vacation.service.policy.RepeatGrant;
import com.lshdainty.porest.vacation.service.policy.factory.VacationPolicyStrategyFactory;
import com.lshdainty.porest.vacation.type.GrantMethod;
import com.lshdainty.porest.vacation.type.VacationType;
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
 * 매일 12시에 실행되어 오늘 부여해야 할 휴가를 자동으로 부여함
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VacationGrantScheduler {
    private final UserVacationPolicyRepository userVacationPolicyRepository;
    private final VacationGrantRepository vacationGrantRepository;
    private final VacationPolicyStrategyFactory strategyFactory;

    /**
     * 만료된 휴가 자동 처리 스케줄러<br>
     * 매일 자정(00:00)에 실행<br>
     * cron: "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 0 * * *")
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
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void grantVacationsDaily() {
        LocalDate today = LocalDate.now();
        log.info("========== 휴가 자동 부여 스케줄러 시작 ========== [{}]", today);

        try {
            // RepeatGrant 전략 인스턴스 가져오기
            RepeatGrant repeatGrantService = (RepeatGrant) strategyFactory.getStrategy(GrantMethod.REPEAT_GRANT);

            // 1. 오늘 부여 대상인 UserVacationPolicy 조회
            List<UserVacationPolicy> targets = userVacationPolicyRepository.findRepeatGrantTargetsForToday(today);
            log.info("부여 대상 정책 수: {}", targets.size());

            if (targets.isEmpty()) {
                log.info("오늘 부여할 휴가가 없습니다.");
                return;
            }

            // 2. 각 정책에 대해 휴가 부여 처리
            List<VacationGrant> grantsToSave = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (UserVacationPolicy uvp : targets) {
                try {
                    VacationPolicy policy = uvp.getVacationPolicy();
                    VacationType vacationType = policy.getVacationType();

                    // 효력 발생일과 만료일 계산
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime startDate = policy.getEffectiveType().calculateDate(now);
                    LocalDateTime expiryDate = policy.getExpirationType().calculateDate(startDate);

                    // VacationGrant 생성
                    String desc = policy.getName() + "에 의한 휴가 부여";
                    VacationGrant vacationGrant = VacationGrant.createVacationGrant(
                            uvp.getUser(),
                            policy,
                            desc,
                            vacationType,
                            policy.getGrantTime(),
                            startDate,
                            expiryDate
                    );

                    grantsToSave.add(vacationGrant);

                    // 다음 부여일 갱신 (현재 부여일 기준으로 재계산)
                    LocalDate newNextGrantDate = repeatGrantService.calculateNextGrantDate(policy, today);
                    uvp.updateGrantHistory(startDate, newNextGrantDate);

                    successCount++;
                    log.info("휴가 부여 완료 - User: {}, Policy: {}, VacationType: {}, GrantTime: {}, StartDate: {}, ExpiryDate: {}, NextGrantDate: {}",
                            uvp.getUser().getId(),
                            policy.getName(),
                            vacationType.name(),
                            policy.getGrantTime(),
                            startDate,
                            expiryDate,
                            newNextGrantDate);

                } catch (Exception e) {
                    log.error("휴가 부여 실패 - UserVacationPolicy ID: {}, Error: {}",
                            uvp.getId(), e.getMessage(), e);
                    failCount++;
                    // 개별 실패는 스킵하고 계속 진행
                }
            }

            // 3. 일괄 저장
            if (!grantsToSave.isEmpty()) {
                vacationGrantRepository.saveAll(grantsToSave);
                log.info("VacationGrant {} 건 저장 완료", grantsToSave.size());
            }

            log.info("========== 휴가 자동 부여 스케줄러 완료 ========== 성공: {}, 실패: {}, 총: {}",
                    successCount, failCount, targets.size());

        } catch (Exception e) {
            log.error("휴가 자동 부여 스케줄러 실행 중 오류 발생", e);
            throw e;
        }
    }
}