package com.porest.hr.service;

import com.porest.core.exception.DuplicateException;
import com.porest.core.exception.EntityNotFoundException;
import com.porest.core.type.CountryCode;
import com.porest.core.type.YNType;
import com.porest.hr.common.type.DefaultCompanyType;
import com.porest.hr.user.domain.User;
import com.porest.hr.user.service.UserService;
import com.porest.hr.vacation.domain.UserVacationPlan;
import com.porest.hr.vacation.domain.VacationGrantSchedule;
import com.porest.hr.vacation.domain.VacationPlan;
import com.porest.hr.vacation.domain.VacationPlanPolicy;
import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.repository.UserVacationPlanRepository;
import com.porest.hr.vacation.repository.VacationGrantScheduleRepository;
import com.porest.hr.vacation.repository.VacationPlanRepository;
import com.porest.hr.vacation.repository.VacationPolicyRepository;
import com.porest.hr.vacation.service.VacationPlanServiceImpl;
import com.porest.hr.vacation.service.dto.VacationPlanServiceDto;
import com.porest.hr.vacation.type.EffectiveType;
import com.porest.hr.vacation.type.ExpirationType;
import com.porest.hr.vacation.type.RepeatUnit;
import com.porest.hr.vacation.type.VacationType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("휴가 플랜 서비스 테스트")
class VacationPlanServiceTest {

    @Mock
    private VacationPlanRepository vacationPlanRepository;

    @Mock
    private UserVacationPlanRepository userVacationPlanRepository;

    @Mock
    private VacationPolicyRepository vacationPolicyRepository;

    @Mock
    private VacationGrantScheduleRepository vacationGrantScheduleRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private VacationPlanServiceImpl vacationPlanService;

    private User user;
    private VacationPlan plan;
    private VacationPolicy policy;
    private VacationPolicy repeatPolicy;

    // 테스트용 User 생성 헬퍼 메소드
    private User createTestUser(String id) {
        return User.createUser(
                null, id, "테스트유저", "test@test.com",
                LocalDate.of(1990, 1, 1), DefaultCompanyType.NONE, "9 ~ 18",
                LocalDate.now(), YNType.N, null, null, CountryCode.KR
        );
    }

    @BeforeEach
    void setUp() {
        user = createTestUser("user1");

        plan = VacationPlan.createPlan("DEFAULT", "기본 플랜", "기본 휴가 플랜");
        ReflectionTestUtils.setField(plan, "id", 1L);

        policy = VacationPolicy.createManualGrantPolicy(
                "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("15.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        ReflectionTestUtils.setField(policy, "id", 1L);

        repeatPolicy = VacationPolicy.createRepeatGrantPolicy(
                "정기연차", "정기 연차 정책", VacationType.ANNUAL,
                new BigDecimal("15.0"), YNType.N, RepeatUnit.YEARLY, 1, null, null,
                LocalDateTime.of(2025, 1, 1, 0, 0), YNType.Y, null,
                EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        ReflectionTestUtils.setField(repeatPolicy, "id", 2L);
    }

    @Nested
    @DisplayName("createPlan")
    class CreatePlan {
        @Test
        @DisplayName("성공 - 휴가 플랜을 생성한다")
        void createPlanSuccess() {
            // given
            given(vacationPlanRepository.existsByCode("NEW_PLAN")).willReturn(false);
            willDoNothing().given(vacationPlanRepository).save(any(VacationPlan.class));

            // when
            VacationPlanServiceDto result = vacationPlanService.createPlan("NEW_PLAN", "새 플랜", "새로운 휴가 플랜");

            // then
            assertThat(result.getCode()).isEqualTo("NEW_PLAN");
            assertThat(result.getName()).isEqualTo("새 플랜");
            then(vacationPlanRepository).should().save(any(VacationPlan.class));
        }

        @Test
        @DisplayName("실패 - 코드 중복 시 예외 발생")
        void createPlanDuplicateCode() {
            // given
            given(vacationPlanRepository.existsByCode("DEFAULT")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> vacationPlanService.createPlan("DEFAULT", "기본 플랜", "설명"))
                    .isInstanceOf(DuplicateException.class);
        }
    }

    @Nested
    @DisplayName("createPlanWithPolicies")
    class CreatePlanWithPolicies {
        @Test
        @DisplayName("성공 - 정책과 함께 휴가 플랜을 생성한다")
        void createPlanWithPoliciesSuccess() {
            // given
            given(vacationPlanRepository.existsByCode("NEW_PLAN")).willReturn(false);
            given(vacationPolicyRepository.findByRowId(1L)).willReturn(Optional.of(policy));
            willDoNothing().given(vacationPlanRepository).save(any(VacationPlan.class));

            // when
            VacationPlanServiceDto result = vacationPlanService.createPlanWithPolicies(
                    "NEW_PLAN", "새 플랜", "설명", List.of(1L));

            // then
            assertThat(result.getCode()).isEqualTo("NEW_PLAN");
            then(vacationPlanRepository).should().save(any(VacationPlan.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 정책 ID로 예외 발생")
        void createPlanWithPoliciesNotFoundPolicy() {
            // given
            given(vacationPlanRepository.existsByCode("NEW_PLAN")).willReturn(false);
            given(vacationPolicyRepository.findByRowId(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.createPlanWithPolicies(
                    "NEW_PLAN", "새 플랜", "설명", List.of(999L)))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 코드 중복 시 예외 발생")
        void createPlanWithPoliciesDuplicateCode() {
            // given
            given(vacationPlanRepository.existsByCode("DEFAULT")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> vacationPlanService.createPlanWithPolicies(
                    "DEFAULT", "기본 플랜", "설명", List.of(1L)))
                    .isInstanceOf(DuplicateException.class);
        }
    }

    @Nested
    @DisplayName("getPlan")
    class GetPlan {
        @Test
        @DisplayName("성공 - 코드로 플랜을 조회한다")
        void getPlanSuccess() {
            // given
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, 1, YNType.N);
            ReflectionTestUtils.setField(planPolicy, "id", 1L);
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>(List.of(planPolicy)));
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));

            // when
            VacationPlanServiceDto result = vacationPlanService.getPlan("DEFAULT");

            // then
            assertThat(result.getCode()).isEqualTo("DEFAULT");
            assertThat(result.getName()).isEqualTo("기본 플랜");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 코드로 예외 발생")
        void getPlanNotFound() {
            // given
            given(vacationPlanRepository.findByCodeWithPolicies("NOT_EXISTS")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.getPlan("NOT_EXISTS"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getPlanById")
    class GetPlanById {
        @Test
        @DisplayName("성공 - ID로 플랜을 조회한다")
        void getPlanByIdSuccess() {
            // given
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>());
            given(vacationPlanRepository.findByIdWithPolicies(1L)).willReturn(Optional.of(plan));

            // when
            VacationPlanServiceDto result = vacationPlanService.getPlanById(1L);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCode()).isEqualTo("DEFAULT");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 ID로 예외 발생")
        void getPlanByIdNotFound() {
            // given
            given(vacationPlanRepository.findByIdWithPolicies(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.getPlanById(999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllPlans")
    class GetAllPlans {
        @Test
        @DisplayName("성공 - 전체 플랜 목록을 조회한다")
        void getAllPlansSuccess() {
            // given
            VacationPlan plan2 = VacationPlan.createPlan("SENIOR", "선임 플랜", "선임용");
            ReflectionTestUtils.setField(plan2, "id", 2L);
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>());
            ReflectionTestUtils.setField(plan2, "vacationPlanPolicies", new ArrayList<>());
            given(vacationPlanRepository.findAllWithPolicies()).willReturn(List.of(plan, plan2));

            // when
            List<VacationPlanServiceDto> result = vacationPlanService.getAllPlans();

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("성공 - 플랜이 없으면 빈 리스트를 반환한다")
        void getAllPlansEmpty() {
            // given
            given(vacationPlanRepository.findAllWithPolicies()).willReturn(List.of());

            // when
            List<VacationPlanServiceDto> result = vacationPlanService.getAllPlans();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updatePlan")
    class UpdatePlan {
        @Test
        @DisplayName("성공 - 플랜 정보를 수정한다")
        void updatePlanSuccess() {
            // given
            given(vacationPlanRepository.findByCode("DEFAULT")).willReturn(Optional.of(plan));

            // when
            VacationPlanServiceDto result = vacationPlanService.updatePlan("DEFAULT", "수정된 플랜", "수정된 설명");

            // then
            assertThat(result.getName()).isEqualTo("수정된 플랜");
            assertThat(result.getDesc()).isEqualTo("수정된 설명");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 플랜으로 예외 발생")
        void updatePlanNotFound() {
            // given
            given(vacationPlanRepository.findByCode("NOT_EXISTS")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.updatePlan("NOT_EXISTS", "수정", "설명"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deletePlan")
    class DeletePlan {
        @Test
        @DisplayName("성공 - 플랜을 삭제한다")
        void deletePlanSuccess() {
            // given
            given(vacationPlanRepository.findByCode("DEFAULT")).willReturn(Optional.of(plan));

            // when
            vacationPlanService.deletePlan("DEFAULT");

            // then
            assertThat(plan.getIsDeleted()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 플랜으로 예외 발생")
        void deletePlanNotFound() {
            // given
            given(vacationPlanRepository.findByCode("NOT_EXISTS")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.deletePlan("NOT_EXISTS"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("addPolicyToPlan")
    class AddPolicyToPlan {
        @Test
        @DisplayName("성공 - 플랜에 정책을 추가한다")
        void addPolicyToPlanSuccess() {
            // given
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>());
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(vacationPolicyRepository.findByRowId(1L)).willReturn(Optional.of(policy));

            // when
            vacationPlanService.addPolicyToPlan("DEFAULT", 1L);

            // then
            assertThat(plan.getVacationPlanPolicies()).hasSize(1);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 플랜으로 예외 발생")
        void addPolicyToPlanNotFoundPlan() {
            // given
            given(vacationPlanRepository.findByCodeWithPolicies("NOT_EXISTS")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.addPolicyToPlan("NOT_EXISTS", 1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 정책으로 예외 발생")
        void addPolicyToPlanNotFoundPolicy() {
            // given
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>());
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(vacationPolicyRepository.findByRowId(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.addPolicyToPlan("DEFAULT", 999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 이미 존재하는 정책으로 예외 발생")
        void addPolicyToPlanDuplicate() {
            // given
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, 1, YNType.N);
            ReflectionTestUtils.setField(planPolicy, "id", 1L);
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>(List.of(planPolicy)));
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(vacationPolicyRepository.findByRowId(1L)).willReturn(Optional.of(policy));

            // when & then
            assertThatThrownBy(() -> vacationPlanService.addPolicyToPlan("DEFAULT", 1L))
                    .isInstanceOf(DuplicateException.class);
        }
    }

    @Nested
    @DisplayName("removePolicyFromPlan")
    class RemovePolicyFromPlan {
        @Test
        @DisplayName("성공 - 플랜에서 정책을 제거한다")
        void removePolicyFromPlanSuccess() {
            // given
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, 1, YNType.N);
            ReflectionTestUtils.setField(planPolicy, "id", 1L);
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>(List.of(planPolicy)));
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(vacationPolicyRepository.findByRowId(1L)).willReturn(Optional.of(policy));

            // when
            vacationPlanService.removePolicyFromPlan("DEFAULT", 1L);

            // then
            assertThat(plan.getVacationPlanPolicies().get(0).getIsDeleted()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 플랜으로 예외 발생")
        void removePolicyFromPlanNotFoundPlan() {
            // given
            given(vacationPlanRepository.findByCodeWithPolicies("NOT_EXISTS")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.removePolicyFromPlan("NOT_EXISTS", 1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 정책으로 예외 발생")
        void removePolicyFromPlanNotFoundPolicy() {
            // given
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>());
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(vacationPolicyRepository.findByRowId(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.removePolicyFromPlan("DEFAULT", 999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updatePlanPolicies")
    class UpdatePlanPolicies {
        @Test
        @DisplayName("성공 - 플랜의 정책 목록을 업데이트한다")
        void updatePlanPoliciesSuccess() {
            // given
            VacationPlanPolicy oldPlanPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, 1, YNType.N);
            ReflectionTestUtils.setField(oldPlanPolicy, "id", 1L);
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>(List.of(oldPlanPolicy)));

            VacationPolicy newPolicy = VacationPolicy.createManualGrantPolicy(
                    "건강휴가", "건강휴가 정책", VacationType.HEALTH, new BigDecimal("3.0"),
                    YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
            );
            ReflectionTestUtils.setField(newPolicy, "id", 3L);

            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(vacationPolicyRepository.findByRowId(3L)).willReturn(Optional.of(newPolicy));

            // when
            vacationPlanService.updatePlanPolicies("DEFAULT", List.of(3L));

            // then
            assertThat(oldPlanPolicy.getIsDeleted()).isEqualTo(YNType.Y);
            assertThat(plan.getVacationPlanPolicies()).hasSize(2); // old + new
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 플랜으로 예외 발생")
        void updatePlanPoliciesNotFoundPlan() {
            // given
            given(vacationPlanRepository.findByCodeWithPolicies("NOT_EXISTS")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.updatePlanPolicies("NOT_EXISTS", List.of(1L)))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 정책으로 예외 발생")
        void updatePlanPoliciesNotFoundPolicy() {
            // given
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>());
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(vacationPolicyRepository.findByRowId(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.updatePlanPolicies("DEFAULT", List.of(999L)))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("assignPlanToUser")
    class AssignPlanToUser {
        @Test
        @DisplayName("성공 - 사용자에게 플랜을 할당한다")
        void assignPlanToUserSuccess() {
            // given
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>());
            given(userService.checkUserExist("user1")).willReturn(user);
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(userVacationPlanRepository.existsByUserIdAndPlanCode("user1", "DEFAULT")).willReturn(false);
            willDoNothing().given(userVacationPlanRepository).save(any(UserVacationPlan.class));

            // when
            vacationPlanService.assignPlanToUser("user1", "DEFAULT");

            // then
            then(userVacationPlanRepository).should().save(any(UserVacationPlan.class));
        }

        @Test
        @DisplayName("성공 - REPEAT_GRANT 정책에 대해 Schedule 생성")
        void assignPlanToUserWithRepeatPolicy() {
            // given
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, repeatPolicy, 1, YNType.N);
            ReflectionTestUtils.setField(planPolicy, "id", 1L);
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>(List.of(planPolicy)));

            given(userService.checkUserExist("user1")).willReturn(user);
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(userVacationPlanRepository.existsByUserIdAndPlanCode("user1", "DEFAULT")).willReturn(false);
            willDoNothing().given(userVacationPlanRepository).save(any(UserVacationPlan.class));
            given(vacationGrantScheduleRepository.existsByUserIdAndPolicyId("user1", 2L)).willReturn(false);
            willDoNothing().given(vacationGrantScheduleRepository).save(any(VacationGrantSchedule.class));

            // when
            vacationPlanService.assignPlanToUser("user1", "DEFAULT");

            // then
            then(vacationGrantScheduleRepository).should().save(any(VacationGrantSchedule.class));
        }

        @Test
        @DisplayName("실패 - 이미 할당된 플랜으로 예외 발생")
        void assignPlanToUserDuplicate() {
            // given
            given(userService.checkUserExist("user1")).willReturn(user);
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(userVacationPlanRepository.existsByUserIdAndPlanCode("user1", "DEFAULT")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> vacationPlanService.assignPlanToUser("user1", "DEFAULT"))
                    .isInstanceOf(DuplicateException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 플랜으로 예외 발생")
        void assignPlanToUserNotFoundPlan() {
            // given
            given(userService.checkUserExist("user1")).willReturn(user);
            given(vacationPlanRepository.findByCodeWithPolicies("NOT_EXISTS")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.assignPlanToUser("user1", "NOT_EXISTS"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("assignPlansToUser")
    class AssignPlansToUser {
        @Test
        @DisplayName("성공 - 사용자에게 여러 플랜을 할당한다")
        void assignPlansToUserSuccess() {
            // given
            VacationPlan plan2 = VacationPlan.createPlan("SENIOR", "선임 플랜", "선임용");
            ReflectionTestUtils.setField(plan2, "id", 2L);
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>());
            ReflectionTestUtils.setField(plan2, "vacationPlanPolicies", new ArrayList<>());

            given(userService.checkUserExist("user1")).willReturn(user);
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(vacationPlanRepository.findByCodeWithPolicies("SENIOR")).willReturn(Optional.of(plan2));
            given(userVacationPlanRepository.existsByUserIdAndPlanCode("user1", "DEFAULT")).willReturn(false);
            given(userVacationPlanRepository.existsByUserIdAndPlanCode("user1", "SENIOR")).willReturn(false);
            willDoNothing().given(userVacationPlanRepository).save(any(UserVacationPlan.class));

            // when
            vacationPlanService.assignPlansToUser("user1", List.of("DEFAULT", "SENIOR"));

            // then - 2개 플랜 할당하므로 save가 2번 호출됨
            then(userVacationPlanRepository).should(times(2)).save(any(UserVacationPlan.class));
        }

        @Test
        @DisplayName("성공 - 이미 할당된 플랜은 스킵한다")
        void assignPlansToUserSkipExisting() {
            // given
            VacationPlan plan2 = VacationPlan.createPlan("SENIOR", "선임 플랜", "선임용");
            ReflectionTestUtils.setField(plan2, "id", 2L);
            ReflectionTestUtils.setField(plan2, "vacationPlanPolicies", new ArrayList<>());

            given(userService.checkUserExist("user1")).willReturn(user);
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(vacationPlanRepository.findByCodeWithPolicies("SENIOR")).willReturn(Optional.of(plan2));
            given(userVacationPlanRepository.existsByUserIdAndPlanCode("user1", "DEFAULT")).willReturn(true); // 이미 할당됨
            given(userVacationPlanRepository.existsByUserIdAndPlanCode("user1", "SENIOR")).willReturn(false);
            willDoNothing().given(userVacationPlanRepository).save(any(UserVacationPlan.class));

            // when
            vacationPlanService.assignPlansToUser("user1", List.of("DEFAULT", "SENIOR"));

            // then - SENIOR만 저장됨
            then(userVacationPlanRepository).should().save(any(UserVacationPlan.class));
        }
    }

    @Nested
    @DisplayName("revokePlanFromUser")
    class RevokePlanFromUser {
        @Test
        @DisplayName("성공 - 사용자에게서 플랜을 회수한다")
        void revokePlanFromUserSuccess() {
            // given
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>());
            UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            ReflectionTestUtils.setField(userPlan, "id", 1L);

            given(userService.checkUserExist("user1")).willReturn(user);
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(userVacationPlanRepository.findByUserIdAndPlanCode("user1", "DEFAULT")).willReturn(Optional.of(userPlan));
            // Note: findByUserIdWithPlanAndPolicies is only called if there are REPEAT_GRANT policies

            // when
            vacationPlanService.revokePlanFromUser("user1", "DEFAULT");

            // then
            assertThat(userPlan.getIsDeleted()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("성공 - REPEAT_GRANT 정책에 대해 Schedule 삭제")
        void revokePlanFromUserWithRepeatPolicy() {
            // given
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, repeatPolicy, 1, YNType.N);
            ReflectionTestUtils.setField(planPolicy, "id", 1L);
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>(List.of(planPolicy)));

            UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            ReflectionTestUtils.setField(userPlan, "id", 1L);

            VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, repeatPolicy);
            ReflectionTestUtils.setField(schedule, "id", 1L);

            given(userService.checkUserExist("user1")).willReturn(user);
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(userVacationPlanRepository.findByUserIdAndPlanCode("user1", "DEFAULT")).willReturn(Optional.of(userPlan));
            given(userVacationPlanRepository.findByUserIdWithPlanAndPolicies("user1")).willReturn(List.of(userPlan));
            given(vacationGrantScheduleRepository.findByUserIdAndPolicyId("user1", 2L)).willReturn(Optional.of(schedule));

            // when
            vacationPlanService.revokePlanFromUser("user1", "DEFAULT");

            // then
            assertThat(schedule.getIsDeleted()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("실패 - 할당되지 않은 플랜으로 예외 발생")
        void revokePlanFromUserNotAssigned() {
            // given
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>());
            given(userService.checkUserExist("user1")).willReturn(user);
            given(vacationPlanRepository.findByCodeWithPolicies("DEFAULT")).willReturn(Optional.of(plan));
            given(userVacationPlanRepository.findByUserIdAndPlanCode("user1", "DEFAULT")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.revokePlanFromUser("user1", "DEFAULT"))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 플랜으로 예외 발생")
        void revokePlanFromUserNotFoundPlan() {
            // given
            given(userService.checkUserExist("user1")).willReturn(user);
            given(vacationPlanRepository.findByCodeWithPolicies("NOT_EXISTS")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationPlanService.revokePlanFromUser("user1", "NOT_EXISTS"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getUserPlans")
    class GetUserPlans {
        @Test
        @DisplayName("성공 - 사용자의 플랜 목록을 조회한다")
        void getUserPlansSuccess() {
            // given
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>());
            UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            ReflectionTestUtils.setField(userPlan, "id", 1L);

            given(userService.checkUserExist("user1")).willReturn(user);
            given(userVacationPlanRepository.findByUserIdWithPlanAndPolicies("user1")).willReturn(List.of(userPlan));

            // when
            List<VacationPlanServiceDto> result = vacationPlanService.getUserPlans("user1");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCode()).isEqualTo("DEFAULT");
        }

        @Test
        @DisplayName("성공 - 삭제된 플랜은 조회에서 제외한다")
        void getUserPlansExcludesDeleted() {
            // given
            ReflectionTestUtils.setField(plan, "vacationPlanPolicies", new ArrayList<>());
            UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            ReflectionTestUtils.setField(userPlan, "id", 1L);
            userPlan.deleteUserVacationPlan();

            given(userService.checkUserExist("user1")).willReturn(user);
            given(userVacationPlanRepository.findByUserIdWithPlanAndPolicies("user1")).willReturn(List.of(userPlan));

            // when
            List<VacationPlanServiceDto> result = vacationPlanService.getUserPlans("user1");

            // then
            assertThat(result).isEmpty();
        }
    }
}
