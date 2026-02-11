package com.porest.hr.vacation.service;

import com.porest.core.exception.DuplicateException;
import com.porest.core.exception.EntityNotFoundException;
import com.porest.hr.common.exception.HrErrorCode;
import com.porest.core.type.YNType;
import com.porest.hr.user.domain.User;
import com.porest.hr.user.service.UserService;
import com.porest.hr.vacation.domain.UserVacationPlan;
import com.porest.hr.vacation.domain.VacationGrantSchedule;
import com.porest.hr.vacation.domain.VacationPlan;
import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.repository.UserVacationPlanRepository;
import com.porest.hr.vacation.repository.VacationGrantScheduleRepository;
import com.porest.hr.vacation.repository.VacationPlanRepository;
import com.porest.hr.vacation.repository.VacationPolicyRepository;
import com.porest.hr.vacation.service.dto.VacationPlanServiceDto;
import com.porest.hr.vacation.type.GrantMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VacationPlanServiceImpl implements VacationPlanService {
    private final VacationPlanRepository vacationPlanRepository;
    private final UserVacationPlanRepository userVacationPlanRepository;
    private final VacationPolicyRepository vacationPolicyRepository;
    private final VacationGrantScheduleRepository vacationGrantScheduleRepository;
    private final UserService userService;

    // ========================================
    // Plan CRUD
    // ========================================

    @Transactional
    @Override
    public VacationPlanServiceDto createPlan(String code, String name, String desc) {
        log.debug("휴가 플랜 생성 시작: code={}, name={}", code, name);

        // 코드 중복 체크
        if (vacationPlanRepository.existsByCode(code)) {
            log.warn("휴가 플랜 생성 실패 - 코드 중복: code={}", code);
            throw new DuplicateException(HrErrorCode.VACATION_PLAN_ALREADY_EXISTS);
        }

        VacationPlan plan = VacationPlan.createPlan(code, name, desc);
        vacationPlanRepository.save(plan);

        log.info("휴가 플랜 생성 완료: planId={}, code={}", plan.getRowId(), code);
        return VacationPlanServiceDto.from(plan);
    }

    @Transactional
    @Override
    public VacationPlanServiceDto createPlanWithPolicies(String code, String name, String desc, List<Long> policyIds) {
        log.debug("휴가 플랜 생성 시작 (정책 포함): code={}, name={}, policyCount={}", code, name, policyIds.size());

        // 코드 중복 체크
        if (vacationPlanRepository.existsByCode(code)) {
            log.warn("휴가 플랜 생성 실패 - 코드 중복: code={}", code);
            throw new DuplicateException(HrErrorCode.VACATION_PLAN_ALREADY_EXISTS);
        }

        // 정책 조회 및 검증
        List<VacationPolicy> policies = new ArrayList<>();
        for (Long policyId : policyIds) {
            VacationPolicy policy = vacationPolicyRepository.findByRowId(policyId)
                    .orElseThrow(() -> {
                        log.warn("휴가 플랜 생성 실패 - 정책 없음: policyId={}", policyId);
                        return new EntityNotFoundException(HrErrorCode.VACATION_POLICY_NOT_FOUND);
                    });
            policies.add(policy);
        }

        VacationPlan plan = VacationPlan.createPlanWithPolicies(code, name, desc, policies);
        vacationPlanRepository.save(plan);

        log.info("휴가 플랜 생성 완료: planId={}, code={}, policyCount={}", plan.getRowId(), code, policies.size());
        return VacationPlanServiceDto.fromWithPolicies(plan);
    }

    @Override
    public VacationPlanServiceDto getPlan(String code) {
        log.debug("휴가 플랜 조회: code={}", code);

        VacationPlan plan = vacationPlanRepository.findByCodeWithPolicies(code)
                .orElseThrow(() -> {
                    log.warn("휴가 플랜 조회 실패 - 존재하지 않음: code={}", code);
                    return new EntityNotFoundException(HrErrorCode.VACATION_PLAN_NOT_FOUND);
                });

        return VacationPlanServiceDto.fromWithPolicies(plan);
    }

    @Override
    public VacationPlanServiceDto getPlanById(Long planId) {
        log.debug("휴가 플랜 조회: planId={}", planId);

        VacationPlan plan = vacationPlanRepository.findByIdWithPolicies(planId)
                .orElseThrow(() -> {
                    log.warn("휴가 플랜 조회 실패 - 존재하지 않음: planId={}", planId);
                    return new EntityNotFoundException(HrErrorCode.VACATION_PLAN_NOT_FOUND);
                });

        return VacationPlanServiceDto.fromWithPolicies(plan);
    }

    @Override
    public List<VacationPlanServiceDto> getAllPlans() {
        log.debug("전체 휴가 플랜 조회");

        List<VacationPlan> plans = vacationPlanRepository.findAllWithPolicies();
        return plans.stream()
                .map(VacationPlanServiceDto::fromWithPolicies)
                .toList();
    }

    @Transactional
    @Override
    public VacationPlanServiceDto updatePlan(String code, String name, String desc) {
        log.debug("휴가 플랜 수정: code={}, name={}", code, name);

        VacationPlan plan = vacationPlanRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("휴가 플랜 수정 실패 - 존재하지 않음: code={}", code);
                    return new EntityNotFoundException(HrErrorCode.VACATION_PLAN_NOT_FOUND);
                });

        plan.updatePlan(name, desc);

        log.info("휴가 플랜 수정 완료: planId={}, code={}", plan.getRowId(), code);
        return VacationPlanServiceDto.from(plan);
    }

    @Transactional
    @Override
    public void deletePlan(String code) {
        log.debug("휴가 플랜 삭제: code={}", code);

        VacationPlan plan = vacationPlanRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("휴가 플랜 삭제 실패 - 존재하지 않음: code={}", code);
                    return new EntityNotFoundException(HrErrorCode.VACATION_PLAN_NOT_FOUND);
                });

        plan.deletePlan();

        log.info("휴가 플랜 삭제 완료: planId={}, code={}", plan.getRowId(), code);
    }

    // ========================================
    // Plan-Policy 관리
    // ========================================

    @Transactional
    @Override
    public void addPolicyToPlan(String planCode, Long policyId) {
        log.debug("플랜에 정책 추가: planCode={}, policyId={}", planCode, policyId);

        VacationPlan plan = vacationPlanRepository.findByCodeWithPolicies(planCode)
                .orElseThrow(() -> {
                    log.warn("정책 추가 실패 - 플랜 없음: planCode={}", planCode);
                    return new EntityNotFoundException(HrErrorCode.VACATION_PLAN_NOT_FOUND);
                });

        VacationPolicy policy = vacationPolicyRepository.findByRowId(policyId)
                .orElseThrow(() -> {
                    log.warn("정책 추가 실패 - 정책 없음: policyId={}", policyId);
                    return new EntityNotFoundException(HrErrorCode.VACATION_POLICY_NOT_FOUND);
                });

        // 이미 존재하는지 확인
        if (plan.hasPolicy(policyId)) {
            log.warn("정책 추가 실패 - 이미 존재: planCode={}, policyId={}", planCode, policyId);
            throw new DuplicateException(HrErrorCode.VACATION_PLAN_POLICY_ALREADY_EXISTS);
        }

        plan.addPolicy(policy);

        log.info("플랜에 정책 추가 완료: planCode={}, policyId={}", planCode, policyId);
    }

    @Transactional
    @Override
    public void removePolicyFromPlan(String planCode, Long policyId) {
        log.debug("플랜에서 정책 제거: planCode={}, policyId={}", planCode, policyId);

        VacationPlan plan = vacationPlanRepository.findByCodeWithPolicies(planCode)
                .orElseThrow(() -> {
                    log.warn("정책 제거 실패 - 플랜 없음: planCode={}", planCode);
                    return new EntityNotFoundException(HrErrorCode.VACATION_PLAN_NOT_FOUND);
                });

        VacationPolicy policy = vacationPolicyRepository.findByRowId(policyId)
                .orElseThrow(() -> {
                    log.warn("정책 제거 실패 - 정책 없음: policyId={}", policyId);
                    return new EntityNotFoundException(HrErrorCode.VACATION_POLICY_NOT_FOUND);
                });

        plan.removePolicy(policy);

        log.info("플랜에서 정책 제거 완료: planCode={}, policyId={}", planCode, policyId);
    }

    @Transactional
    @Override
    public void updatePlanPolicies(String planCode, List<Long> policyIds) {
        log.debug("플랜 정책 전체 업데이트: planCode={}, policyCount={}", planCode, policyIds.size());

        VacationPlan plan = vacationPlanRepository.findByCodeWithPolicies(planCode)
                .orElseThrow(() -> {
                    log.warn("정책 업데이트 실패 - 플랜 없음: planCode={}", planCode);
                    return new EntityNotFoundException(HrErrorCode.VACATION_PLAN_NOT_FOUND);
                });

        // 기존 정책 모두 제거
        plan.clearPolicies();

        // 새 정책 추가
        for (Long policyId : policyIds) {
            VacationPolicy policy = vacationPolicyRepository.findByRowId(policyId)
                    .orElseThrow(() -> {
                        log.warn("정책 업데이트 실패 - 정책 없음: policyId={}", policyId);
                        return new EntityNotFoundException(HrErrorCode.VACATION_POLICY_NOT_FOUND);
                    });
            plan.addPolicy(policy);
        }

        log.info("플랜 정책 전체 업데이트 완료: planCode={}, policyCount={}", planCode, policyIds.size());
    }

    // ========================================
    // User-Plan 관리
    // ========================================

    @Transactional
    @Override
    public void assignPlanToUser(String userId, String planCode) {
        log.debug("사용자에게 플랜 할당: userId={}, planCode={}", userId, planCode);

        User user = userService.checkUserExist(userId);

        VacationPlan plan = vacationPlanRepository.findByCodeWithPolicies(planCode)
                .orElseThrow(() -> {
                    log.warn("플랜 할당 실패 - 플랜 없음: planCode={}", planCode);
                    return new EntityNotFoundException(HrErrorCode.VACATION_PLAN_NOT_FOUND);
                });

        // 이미 할당되어 있는지 확인
        if (userVacationPlanRepository.existsByUserIdAndPlanCode(userId, planCode)) {
            log.warn("플랜 할당 실패 - 이미 할당됨: userId={}, planCode={}", userId, planCode);
            throw new DuplicateException(HrErrorCode.USER_VACATION_PLAN_ALREADY_EXISTS);
        }

        // UserVacationPlan 생성
        UserVacationPlan userVacationPlan = UserVacationPlan.createUserVacationPlan(user, plan);
        userVacationPlanRepository.save(userVacationPlan);

        // REPEAT_GRANT 정책에 대한 VacationGrantSchedule 생성
        createSchedulesForUserPlan(user, plan);

        log.info("사용자에게 플랜 할당 완료: userId={}, planCode={}", userId, planCode);
    }

    @Transactional
    @Override
    public void assignPlansToUser(String userId, List<String> planCodes) {
        log.debug("사용자에게 여러 플랜 할당: userId={}, planCount={}", userId, planCodes.size());

        User user = userService.checkUserExist(userId);

        for (String planCode : planCodes) {
            VacationPlan plan = vacationPlanRepository.findByCodeWithPolicies(planCode)
                    .orElseThrow(() -> {
                        log.warn("플랜 할당 실패 - 플랜 없음: planCode={}", planCode);
                        return new EntityNotFoundException(HrErrorCode.VACATION_PLAN_NOT_FOUND);
                    });

            // 이미 할당되어 있으면 스킵
            if (userVacationPlanRepository.existsByUserIdAndPlanCode(userId, planCode)) {
                log.warn("플랜 이미 할당되어 있음, 스킵: userId={}, planCode={}", userId, planCode);
                continue;
            }

            // UserVacationPlan 생성
            UserVacationPlan userVacationPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userVacationPlan);

            // REPEAT_GRANT 정책에 대한 VacationGrantSchedule 생성
            createSchedulesForUserPlan(user, plan);
        }

        log.info("사용자에게 여러 플랜 할당 완료: userId={}, planCount={}", userId, planCodes.size());
    }

    @Transactional
    @Override
    public void revokePlanFromUser(String userId, String planCode) {
        log.debug("사용자에게서 플랜 회수: userId={}, planCode={}", userId, planCode);

        User user = userService.checkUserExist(userId);

        VacationPlan plan = vacationPlanRepository.findByCodeWithPolicies(planCode)
                .orElseThrow(() -> {
                    log.warn("플랜 회수 실패 - 플랜 없음: planCode={}", planCode);
                    return new EntityNotFoundException(HrErrorCode.VACATION_PLAN_NOT_FOUND);
                });

        UserVacationPlan userVacationPlan = userVacationPlanRepository.findByUserIdAndPlanCode(userId, planCode)
                .orElseThrow(() -> {
                    log.warn("플랜 회수 실패 - 사용자에게 할당되지 않음: userId={}, planCode={}", userId, planCode);
                    return new EntityNotFoundException(HrErrorCode.USER_VACATION_PLAN_NOT_FOUND);
                });

        // UserVacationPlan 소프트 삭제
        userVacationPlan.deleteUserVacationPlan();

        // VacationGrantSchedule 삭제 (다른 Plan에 없는 정책만)
        deleteSchedulesForUserPlan(user, plan);

        log.info("사용자에게서 플랜 회수 완료: userId={}, planCode={}", userId, planCode);
    }

    @Override
    public List<VacationPlanServiceDto> getUserPlans(String userId) {
        log.debug("사용자의 플랜 목록 조회: userId={}", userId);

        userService.checkUserExist(userId);

        List<UserVacationPlan> userVacationPlans = userVacationPlanRepository.findByUserIdWithPlanAndPolicies(userId);

        return userVacationPlans.stream()
                .filter(uvp -> YNType.isN(uvp.getIsDeleted()))
                .map(uvp -> VacationPlanServiceDto.fromWithPolicies(uvp.getVacationPlan()))
                .toList();
    }

    // ========================================
    // VacationGrantSchedule 관리 (내부 메서드)
    // ========================================

    /**
     * Plan 할당 시 REPEAT_GRANT 정책에 대한 Schedule 생성<br>
     * 이미 존재하는 (User, Policy) 조합은 스킵 (중복 방지)
     */
    private void createSchedulesForUserPlan(User user, VacationPlan plan) {
        List<VacationPolicy> repeatPolicies = plan.getRepeatGrantPolicies();

        for (VacationPolicy policy : repeatPolicies) {
            // 이미 존재하면 스킵 (다른 Plan에서 이미 생성됨)
            if (!vacationGrantScheduleRepository.existsByUserIdAndPolicyId(user.getId(), policy.getRowId())) {
                VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, policy);
                vacationGrantScheduleRepository.save(schedule);

                log.debug("VacationGrantSchedule 생성: userId={}, policyId={}", user.getId(), policy.getRowId());
            } else {
                log.debug("VacationGrantSchedule 이미 존재, 스킵: userId={}, policyId={}", user.getId(), policy.getRowId());
            }
        }
    }

    /**
     * Plan 회수 시 REPEAT_GRANT 정책에 대한 Schedule 삭제<br>
     * 다른 Plan에도 해당 Policy가 있으면 삭제하지 않음
     */
    private void deleteSchedulesForUserPlan(User user, VacationPlan plan) {
        // 회수할 Plan의 REPEAT_GRANT 정책들
        List<VacationPolicy> policiesToCheck = plan.getRepeatGrantPolicies();

        if (policiesToCheck.isEmpty()) {
            return;
        }

        // User의 다른 Plan들에 포함된 REPEAT_GRANT Policy ID 집합
        List<UserVacationPlan> userPlans = userVacationPlanRepository.findByUserIdWithPlanAndPolicies(user.getId());

        Set<Long> otherPlanPolicyIds = userPlans.stream()
                .filter(uvp -> YNType.isN(uvp.getIsDeleted()))
                .filter(uvp -> !uvp.getVacationPlan().getRowId().equals(plan.getRowId())) // 회수할 Plan 제외
                .flatMap(uvp -> uvp.getVacationPlan().getRepeatGrantPolicies().stream())
                .map(VacationPolicy::getRowId)
                .collect(Collectors.toSet());

        for (VacationPolicy policy : policiesToCheck) {
            // 다른 Plan에 없는 Policy만 Schedule 삭제
            if (!otherPlanPolicyIds.contains(policy.getRowId())) {
                vacationGrantScheduleRepository.findByUserIdAndPolicyId(user.getId(), policy.getRowId())
                        .ifPresent(schedule -> {
                            schedule.deleteSchedule();
                            log.debug("VacationGrantSchedule 삭제: userId={}, policyId={}", user.getId(), policy.getRowId());
                        });
            } else {
                log.debug("VacationGrantSchedule 삭제 스킵 (다른 Plan에 존재): userId={}, policyId={}", user.getId(), policy.getRowId());
            }
        }
    }
}
