package com.lshdainty.porest.service;

import com.lshdainty.porest.common.exception.BusinessRuleViolationException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ForbiddenException;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.department.repository.DepartmentRepository;
import com.lshdainty.porest.holiday.repository.HolidayRepository;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.vacation.domain.*;
import com.lshdainty.porest.vacation.repository.*;
import com.lshdainty.porest.vacation.service.VacationService;
import com.lshdainty.porest.vacation.service.VacationServiceImpl;
import com.lshdainty.porest.vacation.service.VacationTimeFormatter;
import com.lshdainty.porest.vacation.service.dto.VacationApprovalServiceDto;
import com.lshdainty.porest.vacation.service.dto.VacationPolicyServiceDto;
import com.lshdainty.porest.vacation.service.dto.VacationServiceDto;
import com.lshdainty.porest.vacation.service.policy.ManualGrant;
import com.lshdainty.porest.vacation.service.policy.description.RepeatGrantDescriptionFactory;
import com.lshdainty.porest.vacation.service.policy.factory.VacationPolicyStrategyFactory;
import com.lshdainty.porest.vacation.type.*;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("휴가 서비스 테스트")
class VacationServiceTest {

    @Mock
    private VacationPolicyRepository vacationPolicyRepository;

    @Mock
    private UserVacationPolicyRepository userVacationPolicyRepository;

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private UserService userService;

    @Mock
    private VacationPolicyStrategyFactory vacationPolicyStrategyFactory;

    @Mock
    private VacationGrantRepository vacationGrantRepository;

    @Mock
    private VacationUsageRepository vacationUsageRepository;

    @Mock
    private VacationUsageDeductionRepository vacationUsageDeductionRepository;

    @Mock
    private VacationApprovalRepository vacationApprovalRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RepeatGrantDescriptionFactory repeatGrantDescriptionFactory;

    @Mock
    private VacationTimeFormatter vacationTimeFormatter;

    @InjectMocks
    private VacationServiceImpl vacationService;

    @Nested
    @DisplayName("유저 휴가 내역 조회")
    class GetUserVacationHistory {
        @Test
        @DisplayName("성공 - 유저의 휴가 부여 및 사용 내역을 반환한다")
        void getUserVacationHistorySuccess() {
            // given
            String userId = "user1";
            int year = 2024;
            User user = createTestUser(userId);

            VacationGrant grant = createTestGrant(user);
            VacationUsage usage = createTestUsage(user);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationGrantRepository.findByUserIdAndYear(userId, year)).willReturn(List.of(grant));
            given(vacationUsageRepository.findByUserIdAndYear(userId, year)).willReturn(List.of(usage));

            // when
            VacationServiceDto result = vacationService.getUserVacationHistory(userId, year);

            // then
            assertThat(result.getGrants()).hasSize(1);
            assertThat(result.getUsages()).hasSize(1);
            then(userService).should().checkUserExist(userId);
        }

        @Test
        @DisplayName("성공 - 부여 및 사용 내역이 없으면 빈 리스트를 반환한다")
        void getUserVacationHistoryEmpty() {
            // given
            String userId = "user1";
            int year = 2024;
            User user = createTestUser(userId);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationGrantRepository.findByUserIdAndYear(userId, year)).willReturn(List.of());
            given(vacationUsageRepository.findByUserIdAndYear(userId, year)).willReturn(List.of());

            // when
            VacationServiceDto result = vacationService.getUserVacationHistory(userId, year);

            // then
            assertThat(result.getGrants()).isEmpty();
            assertThat(result.getUsages()).isEmpty();
        }
    }

    @Nested
    @DisplayName("모든 유저 휴가 내역 조회")
    class GetAllUsersVacationHistory {
        @Test
        @DisplayName("성공 - 모든 유저의 휴가 내역을 반환한다")
        void getAllUsersVacationHistorySuccess() {
            // given
            User user1 = createTestUser("user1");
            User user2 = createTestUser("user2");

            VacationGrant grant1 = createTestGrant(user1);
            VacationGrant grant2 = createTestGrant(user2);

            given(vacationGrantRepository.findAllWithUser()).willReturn(List.of(grant1, grant2));
            given(vacationUsageRepository.findAllWithUser()).willReturn(List.of());

            // when
            List<VacationServiceDto> result = vacationService.getAllUsersVacationHistory();

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("성공 - 내역이 없으면 빈 리스트를 반환한다")
        void getAllUsersVacationHistoryEmpty() {
            // given
            given(vacationGrantRepository.findAllWithUser()).willReturn(List.of());
            given(vacationUsageRepository.findAllWithUser()).willReturn(List.of());

            // when
            List<VacationServiceDto> result = vacationService.getAllUsersVacationHistory();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("사용 가능한 휴가 조회")
    class GetAvailableVacations {
        @Test
        @DisplayName("성공 - 시작일 기준 사용 가능한 휴가를 VacationType별로 그룹화하여 반환한다")
        void getAvailableVacationsSuccess() {
            // given
            String userId = "user1";
            LocalDateTime startDate = LocalDateTime.of(2025, 6, 1, 0, 0);
            User user = createTestUser(userId);

            VacationGrant grant1 = createTestGrant(user, VacationType.ANNUAL, new BigDecimal("10.0000"));
            VacationGrant grant2 = createTestGrant(user, VacationType.ANNUAL, new BigDecimal("5.0000"));

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationGrantRepository.findAvailableGrantsByUserIdAndDate(userId, startDate))
                    .willReturn(List.of(grant1, grant2));

            // when
            List<VacationServiceDto> result = vacationService.getAvailableVacations(userId, startDate);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo(VacationType.ANNUAL);
            assertThat(result.get(0).getRemainTime()).isEqualByComparingTo(new BigDecimal("15.0000"));
        }

        @Test
        @DisplayName("성공 - 사용 가능한 휴가가 없으면 빈 리스트를 반환한다")
        void getAvailableVacationsEmpty() {
            // given
            String userId = "user1";
            LocalDateTime startDate = LocalDateTime.of(2025, 6, 1, 0, 0);
            User user = createTestUser(userId);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationGrantRepository.findAvailableGrantsByUserIdAndDate(userId, startDate))
                    .willReturn(List.of());

            // when
            List<VacationServiceDto> result = vacationService.getAvailableVacations(userId, startDate);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("휴가 사용 취소")
    class CancelVacationUsage {
        @Test
        @DisplayName("성공 - 휴가 사용 내역이 취소되고 VacationGrant에 시간이 복구된다")
        void cancelVacationUsageSuccess() {
            // given
            Long usageId = 1L;
            User user = createTestUser("user1");
            VacationUsage usage = createTestUsage(user);
            ReflectionTestUtils.setField(usage, "id", usageId);
            ReflectionTestUtils.setField(usage, "startDate", LocalDateTime.now().plusDays(1));

            VacationGrant grant = createTestGrant(user);
            ReflectionTestUtils.setField(grant, "id", 1L);

            VacationUsageDeduction deduction = VacationUsageDeduction.createVacationUsageDeduction(
                    usage, grant, new BigDecimal("1.0000"));

            given(vacationUsageRepository.findById(usageId)).willReturn(Optional.of(usage));
            given(vacationUsageDeductionRepository.findByUsageId(usageId)).willReturn(List.of(deduction));

            // when
            vacationService.cancelVacationUsage(usageId);

            // then
            assertThat(usage.getIsDeleted()).isEqualTo(YNType.Y);
            then(vacationUsageRepository).should().findById(usageId);
            then(vacationUsageDeductionRepository).should().findByUsageId(usageId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 휴가 사용 내역이면 예외가 발생한다")
        void cancelVacationUsageFailNotFound() {
            // given
            Long usageId = 999L;
            given(vacationUsageRepository.findById(usageId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationService.cancelVacationUsage(usageId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 이미 삭제된 휴가 사용 내역이면 예외가 발생한다")
        void cancelVacationUsageFailAlreadyDeleted() {
            // given
            Long usageId = 1L;
            User user = createTestUser("user1");
            VacationUsage usage = createTestUsage(user);
            ReflectionTestUtils.setField(usage, "id", usageId);
            usage.deleteVacationUsage();

            given(vacationUsageRepository.findById(usageId)).willReturn(Optional.of(usage));

            // when & then
            assertThatThrownBy(() -> vacationService.cancelVacationUsage(usageId))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("휴가 정책 조회")
    class GetVacationPolicy {
        @Test
        @DisplayName("성공 - 휴가 정책을 반환한다")
        void getVacationPolicySuccess() {
            // given
            Long policyId = 1L;
            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", policyId);

            given(vacationPolicyRepository.findVacationPolicyById(policyId))
                    .willReturn(Optional.of(policy));

            // when
            VacationPolicyServiceDto result = vacationService.getVacationPolicy(policyId);

            // then
            assertThat(result.getId()).isEqualTo(policyId);
            assertThat(result.getName()).isEqualTo("연차");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 휴가 정책이면 예외가 발생한다")
        void getVacationPolicyFailNotFound() {
            // given
            Long policyId = 999L;
            given(vacationPolicyRepository.findVacationPolicyById(policyId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationService.getVacationPolicy(policyId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("휴가 정책 목록 조회")
    class GetVacationPolicies {
        @Test
        @DisplayName("성공 - 휴가 정책 목록을 반환한다")
        void getVacationPoliciesSuccess() {
            // given
            VacationPolicy policy1 = createTestPolicy();
            VacationPolicy policy2 = createTestPolicy();
            ReflectionTestUtils.setField(policy1, "id", 1L);
            ReflectionTestUtils.setField(policy2, "id", 2L);

            given(vacationPolicyRepository.findVacationPolicies())
                    .willReturn(List.of(policy1, policy2));

            // when
            List<VacationPolicyServiceDto> result = vacationService.getVacationPolicies();

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("성공 - 정책이 없으면 빈 리스트를 반환한다")
        void getVacationPoliciesEmpty() {
            // given
            given(vacationPolicyRepository.findVacationPolicies()).willReturn(List.of());

            // when
            List<VacationPolicyServiceDto> result = vacationService.getVacationPolicies();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("휴가 정책 삭제")
    class DeleteVacationPolicy {
        @Test
        @DisplayName("성공 - 휴가 정책이 삭제된다")
        void deleteVacationPolicySuccess() {
            // given
            Long policyId = 1L;
            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", policyId);

            given(vacationPolicyRepository.findVacationPolicyById(policyId))
                    .willReturn(Optional.of(policy));
            given(userVacationPolicyRepository.findByVacationPolicyId(policyId))
                    .willReturn(List.of());
            given(vacationGrantRepository.findByPolicyId(policyId))
                    .willReturn(List.of());

            // when
            Long result = vacationService.deleteVacationPolicy(policyId);

            // then
            assertThat(result).isEqualTo(policyId);
            assertThat(policy.getIsDeleted()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("실패 - 이미 삭제된 정책이면 예외가 발생한다")
        void deleteVacationPolicyFailAlreadyDeleted() {
            // given
            Long policyId = 1L;
            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", policyId);
            policy.deleteVacationPolicy();

            given(vacationPolicyRepository.findVacationPolicyById(policyId))
                    .willReturn(Optional.of(policy));

            // when & then
            assertThatThrownBy(() -> vacationService.deleteVacationPolicy(policyId))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test
        @DisplayName("실패 - 삭제 불가능한 정책이면 예외가 발생한다")
        void deleteVacationPolicyFailCannotDelete() {
            // given
            Long policyId = 1L;
            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", policyId);
            ReflectionTestUtils.setField(policy, "canDeleted", YNType.N);

            given(vacationPolicyRepository.findVacationPolicyById(policyId))
                    .willReturn(Optional.of(policy));

            // when & then
            assertThatThrownBy(() -> vacationService.deleteVacationPolicy(policyId))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("유저에게 휴가 정책 할당")
    class AssignVacationPoliciesToUser {
        @Test
        @DisplayName("성공 - 유저에게 휴가 정책이 할당된다")
        void assignVacationPoliciesToUserSuccess() {
            // given
            String userId = "user1";
            Long policyId = 1L;
            User user = createTestUser(userId);
            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", policyId);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationPolicyRepository.findVacationPolicyById(policyId))
                    .willReturn(Optional.of(policy));
            given(userVacationPolicyRepository.existsByUserIdAndVacationPolicyId(userId, policyId))
                    .willReturn(false);
            willDoNothing().given(userVacationPolicyRepository).saveAll(anyList());

            // when
            List<Long> result = vacationService.assignVacationPoliciesToUser(userId, List.of(policyId));

            // then
            assertThat(result).contains(policyId);
            then(userVacationPolicyRepository).should().saveAll(anyList());
        }

        @Test
        @DisplayName("성공 - 이미 할당된 정책은 스킵된다")
        void assignVacationPoliciesToUserSkipAlreadyAssigned() {
            // given
            String userId = "user1";
            Long policyId = 1L;
            User user = createTestUser(userId);
            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", policyId);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationPolicyRepository.findVacationPolicyById(policyId))
                    .willReturn(Optional.of(policy));
            given(userVacationPolicyRepository.existsByUserIdAndVacationPolicyId(userId, policyId))
                    .willReturn(true);

            // when
            List<Long> result = vacationService.assignVacationPoliciesToUser(userId, List.of(policyId));

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("유저에게 할당된 휴가 정책 조회")
    class GetUserAssignedVacationPolicies {
        @Test
        @DisplayName("성공 - 유저에게 할당된 휴가 정책 목록을 반환한다")
        void getUserAssignedVacationPoliciesSuccess() {
            // given
            String userId = "user1";
            User user = createTestUser(userId);
            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", 1L);

            UserVacationPolicy uvp = UserVacationPolicy.createUserVacationPolicy(user, policy);
            ReflectionTestUtils.setField(uvp, "id", 1L);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(userVacationPolicyRepository.findByUserId(userId)).willReturn(List.of(uvp));

            // when
            List<VacationPolicyServiceDto> result = vacationService.getUserAssignedVacationPolicies(userId, null);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("연차");
        }

        @Test
        @DisplayName("성공 - 할당된 정책이 없으면 빈 리스트를 반환한다")
        void getUserAssignedVacationPoliciesEmpty() {
            // given
            String userId = "user1";
            User user = createTestUser(userId);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(userVacationPolicyRepository.findByUserId(userId)).willReturn(List.of());

            // when
            List<VacationPolicyServiceDto> result = vacationService.getUserAssignedVacationPolicies(userId, null);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("유저의 휴가 정책 회수")
    class RevokeVacationPolicyFromUser {
        @Test
        @DisplayName("성공 - 유저의 휴가 정책이 회수된다")
        void revokeVacationPolicyFromUserSuccess() {
            // given
            String userId = "user1";
            Long policyId = 1L;
            User user = createTestUser(userId);
            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", policyId);

            UserVacationPolicy uvp = UserVacationPolicy.createUserVacationPolicy(user, policy);
            ReflectionTestUtils.setField(uvp, "id", 1L);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationPolicyRepository.findVacationPolicyById(policyId))
                    .willReturn(Optional.of(policy));
            given(userVacationPolicyRepository.findByUserIdAndVacationPolicyId(userId, policyId))
                    .willReturn(Optional.of(uvp));
            given(vacationGrantRepository.findByUserId(userId)).willReturn(List.of());

            // when
            Long result = vacationService.revokeVacationPolicyFromUser(userId, policyId);

            // then
            assertThat(result).isEqualTo(1L);
            assertThat(uvp.getIsDeleted()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("실패 - 할당되지 않은 정책이면 예외가 발생한다")
        void revokeVacationPolicyFromUserFailNotAssigned() {
            // given
            String userId = "user1";
            Long policyId = 1L;
            User user = createTestUser(userId);
            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", policyId);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationPolicyRepository.findVacationPolicyById(policyId))
                    .willReturn(Optional.of(policy));
            given(userVacationPolicyRepository.findByUserIdAndVacationPolicyId(userId, policyId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationService.revokeVacationPolicyFromUser(userId, policyId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("휴가 부여 회수")
    class RevokeVacationGrant {
        @Test
        @DisplayName("성공 - 휴가 부여가 회수된다")
        void revokeVacationGrantSuccess() {
            // given
            Long grantId = 1L;
            User user = createTestUser("user1");
            VacationGrant grant = createTestGrant(user);
            ReflectionTestUtils.setField(grant, "id", grantId);

            given(vacationGrantRepository.findById(grantId)).willReturn(Optional.of(grant));

            // when
            VacationGrant result = vacationService.revokeVacationGrant(grantId);

            // then
            assertThat(result.getStatus()).isEqualTo(GrantStatus.REVOKED);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 휴가 부여면 예외가 발생한다")
        void revokeVacationGrantFailNotFound() {
            // given
            Long grantId = 999L;
            given(vacationGrantRepository.findById(grantId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationService.revokeVacationGrant(grantId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - ACTIVE 상태가 아니면 예외가 발생한다")
        void revokeVacationGrantFailNotActive() {
            // given
            Long grantId = 1L;
            User user = createTestUser("user1");
            VacationGrant grant = createTestGrant(user);
            ReflectionTestUtils.setField(grant, "id", grantId);
            grant.revoke(); // REVOKED 상태로 변경

            given(vacationGrantRepository.findById(grantId)).willReturn(Optional.of(grant));

            // when & then
            assertThatThrownBy(() -> vacationService.revokeVacationGrant(grantId))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test
        @DisplayName("실패 - 일부 사용된 경우 예외가 발생한다")
        void revokeVacationGrantFailPartiallyUsed() {
            // given
            Long grantId = 1L;
            User user = createTestUser("user1");
            VacationGrant grant = createTestGrant(user);
            ReflectionTestUtils.setField(grant, "id", grantId);
            grant.deduct(new BigDecimal("1.0000")); // 일부 사용

            given(vacationGrantRepository.findById(grantId)).willReturn(Optional.of(grant));

            // when & then
            assertThatThrownBy(() -> vacationService.revokeVacationGrant(grantId))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("휴가 신청 취소")
    class CancelVacationRequest {
        @Test
        @DisplayName("성공 - 휴가 신청이 취소된다")
        void cancelVacationRequestSuccess() {
            // given
            Long grantId = 1L;
            String userId = "user1";
            User user = createTestUser(userId);

            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", 1L);
            ReflectionTestUtils.setField(policy, "grantMethod", GrantMethod.ON_REQUEST);

            VacationGrant grant = VacationGrant.createPendingVacationGrant(
                    user, policy, "신청 사유", VacationType.ANNUAL, new BigDecimal("1.0000"),
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), "상세 사유"
            );
            ReflectionTestUtils.setField(grant, "id", grantId);

            given(vacationGrantRepository.findById(grantId)).willReturn(Optional.of(grant));

            // when
            Long result = vacationService.cancelVacationRequest(grantId, userId);

            // then
            assertThat(result).isEqualTo(grantId);
            assertThat(grant.getStatus()).isEqualTo(GrantStatus.CANCELED);
        }

        @Test
        @DisplayName("실패 - 신청자가 아니면 예외가 발생한다")
        void cancelVacationRequestFailNotAuthorized() {
            // given
            Long grantId = 1L;
            String userId = "user1";
            String anotherUserId = "user2";
            User user = createTestUser(userId);

            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", 1L);
            ReflectionTestUtils.setField(policy, "grantMethod", GrantMethod.ON_REQUEST);

            VacationGrant grant = VacationGrant.createPendingVacationGrant(
                    user, policy, "신청 사유", VacationType.ANNUAL, new BigDecimal("1.0000"),
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), "상세 사유"
            );
            ReflectionTestUtils.setField(grant, "id", grantId);

            given(vacationGrantRepository.findById(grantId)).willReturn(Optional.of(grant));

            // when & then
            assertThatThrownBy(() -> vacationService.cancelVacationRequest(grantId, anotherUserId))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("기간별 휴가 사용 내역 조회")
    class GetVacationUsagesByPeriod {
        @Test
        @DisplayName("성공 - 기간 내 휴가 사용 내역을 반환한다")
        void getVacationUsagesByPeriodSuccess() {
            // given
            LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59);

            User user = createTestUser("user1");
            VacationUsage usage = createTestUsage(user);
            ReflectionTestUtils.setField(usage, "id", 1L);

            given(vacationUsageRepository.findByPeriodWithUser(startDate, endDate))
                    .willReturn(List.of(usage));
            given(vacationUsageDeductionRepository.findByUsageId(1L)).willReturn(List.of());

            // when
            List<VacationServiceDto> result = vacationService.getVacationUsagesByPeriod(startDate, endDate);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 기간 내 내역이 없으면 빈 리스트를 반환한다")
        void getVacationUsagesByPeriodEmpty() {
            // given
            LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59);

            given(vacationUsageRepository.findByPeriodWithUser(startDate, endDate))
                    .willReturn(List.of());

            // when
            List<VacationServiceDto> result = vacationService.getVacationUsagesByPeriod(startDate, endDate);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("유저별 기간별 휴가 사용 내역 조회")
    class GetUserVacationUsagesByPeriod {
        @Test
        @DisplayName("성공 - 유저의 기간 내 휴가 사용 내역을 반환한다")
        void getUserVacationUsagesByPeriodSuccess() {
            // given
            String userId = "user1";
            LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59);

            User user = createTestUser(userId);
            VacationUsage usage = createTestUsage(user);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationUsageRepository.findByUserIdAndPeriodWithUser(userId, startDate, endDate))
                    .willReturn(List.of(usage));

            // when
            List<VacationServiceDto> result = vacationService.getUserVacationUsagesByPeriod(userId, startDate, endDate);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("유저 월별 휴가 사용 통계 조회")
    class GetUserMonthlyVacationStats {
        @Test
        @DisplayName("성공 - 12개월 통계를 반환한다")
        void getUserMonthlyVacationStatsSuccess() {
            // given
            String userId = "user1";
            String year = "2025";
            User user = createTestUser(userId);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationUsageRepository.findByUserIdAndPeriodWithUser(eq(userId), any(), any()))
                    .willReturn(List.of());

            // when
            List<VacationServiceDto> result = vacationService.getUserMonthlyVacationStats(userId, year);

            // then
            assertThat(result).hasSize(12);
            assertThat(result.get(0).getMonth()).isEqualTo(1);
            assertThat(result.get(11).getMonth()).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("승인자별 휴가 신청 목록 조회")
    class GetAllVacationsByApprover {
        @Test
        @DisplayName("성공 - 승인자가 처리해야 할 휴가 목록을 반환한다")
        void getAllVacationsByApproverSuccess() {
            // given
            String approverId = "approver1";
            Integer year = 2025;
            User approver = createTestUser(approverId);

            given(userService.checkUserExist(approverId)).willReturn(approver);
            given(vacationApprovalRepository.findAllVacationGrantIdsByApproverIdAndYear(approverId, year))
                    .willReturn(List.of());

            // when
            List<VacationServiceDto> result = vacationService.getAllVacationsByApprover(approverId, year, null);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("유저별 신청 휴가 목록 조회")
    class GetAllRequestedVacationsByUserId {
        @Test
        @DisplayName("성공 - 유저가 신청한 휴가 목록을 반환한다")
        void getAllRequestedVacationsByUserIdSuccess() {
            // given
            String userId = "user1";
            Integer year = 2025;
            User user = createTestUser(userId);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationGrantRepository.findAllRequestedVacationsByUserIdAndYear(userId, year))
                    .willReturn(List.of());

            // when
            List<VacationServiceDto> result = vacationService.getAllRequestedVacationsByUserId(userId, year);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("유저 휴가 통계 조회")
    class GetUserVacationStats {
        @Test
        @DisplayName("성공 - 유저의 휴가 통계를 반환한다")
        void getUserVacationStatsSuccess() {
            // given
            String userId = "user1";
            User user = createTestUser(userId);
            LocalDateTime baseTime = LocalDateTime.now();

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationGrantRepository.findValidGrantsByUserIdAndBaseTime(eq(userId), any()))
                    .willReturn(List.of());
            given(vacationUsageRepository.findUsedByUserIdAndBaseTime(eq(userId), any()))
                    .willReturn(List.of());
            given(vacationUsageRepository.findExpectedByUserIdAndBaseTime(eq(userId), any()))
                    .willReturn(List.of());

            // when
            VacationServiceDto result = vacationService.getUserVacationStats(userId, baseTime);

            // then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("다수 유저 휴가 정책 회수")
    class RevokeVacationPoliciesFromUser {
        @Test
        @DisplayName("성공 - 다수의 정책을 회수한다")
        void revokeVacationPoliciesFromUserSuccess() {
            // given
            String userId = "user1";
            User user = createTestUser(userId);
            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", 1L);

            UserVacationPolicy uvp = UserVacationPolicy.createUserVacationPolicy(user, policy);
            ReflectionTestUtils.setField(uvp, "id", 1L);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationPolicyRepository.findVacationPolicyById(1L))
                    .willReturn(Optional.of(policy));
            given(userVacationPolicyRepository.findByUserIdAndVacationPolicyId(userId, 1L))
                    .willReturn(Optional.of(uvp));
            given(vacationGrantRepository.findByUserId(userId)).willReturn(List.of());

            // when
            List<Long> result = vacationService.revokeVacationPoliciesFromUser(userId, List.of(1L));

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("유저별 신청 휴가 통계 조회")
    class GetRequestedVacationStatsByUserId {
        @Test
        @DisplayName("성공 - 신청 휴가 상태별 통계를 반환한다")
        void getRequestedVacationStatsByUserIdSuccess() {
            // given
            String userId = "user1";
            Integer year = 2025;
            User user = createTestUser(userId);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationGrantRepository.findAllRequestedVacationsByUserIdAndYear(userId, year))
                    .willReturn(List.of());

            // when
            VacationServiceDto result = vacationService.getRequestedVacationStatsByUserId(userId, year);

            // then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("필터 조건으로 유저 할당 정책 조회")
    class GetUserAssignedVacationPoliciesWithFilters {
        @Test
        @DisplayName("성공 - 필터 조건으로 정책을 조회한다")
        void getUserAssignedVacationPoliciesWithFiltersSuccess() {
            // given
            String userId = "user1";
            User user = createTestUser(userId);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(userVacationPolicyRepository.findByUserIdWithFilters(userId, VacationType.ANNUAL, GrantMethod.MANUAL_GRANT))
                    .willReturn(List.of());

            // when
            List<VacationPolicyServiceDto> result = vacationService.getUserAssignedVacationPoliciesWithFilters(
                    userId, VacationType.ANNUAL, GrantMethod.MANUAL_GRANT
            );

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("사용 가능한 휴가 조회")
    class GetAvailableVacationsTest {
        @Test
        @DisplayName("성공 - 사용 가능한 휴가를 조회한다")
        void getAvailableVacationsSuccess() {
            // given
            String userId = "user1";
            User user = createTestUser(userId);
            VacationGrant grant = createTestGrant(user);
            ReflectionTestUtils.setField(grant, "id", 1L);
            LocalDateTime startDate = LocalDateTime.now();

            given(vacationGrantRepository.findAvailableGrantsByUserIdAndDate(userId, startDate))
                    .willReturn(List.of(grant));

            // when
            List<VacationServiceDto> result = vacationService.getAvailableVacations(userId, startDate);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 사용 가능한 휴가가 없으면 빈 목록을 반환한다")
        void getAvailableVacationsEmpty() {
            // given
            String userId = "user1";
            LocalDateTime startDate = LocalDateTime.now();

            given(vacationGrantRepository.findAvailableGrantsByUserIdAndDate(userId, startDate))
                    .willReturn(List.of());

            // when
            List<VacationServiceDto> result = vacationService.getAvailableVacations(userId, startDate);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("유저 휴가 사용 내역 조회")
    class GetUserVacationUsages {
        @Test
        @DisplayName("성공 - 기간별 휴가 사용 내역을 조회한다")
        void getUserVacationUsagesByPeriodSuccess() {
            // given
            String userId = "user1";
            LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 23, 59);

            given(vacationUsageRepository.findByUserIdAndPeriodWithUser(userId, startDate, endDate))
                    .willReturn(List.of());

            // when
            List<VacationServiceDto> result = vacationService.getUserVacationUsagesByPeriod(userId, startDate, endDate);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("정책에 유저 할당")
    class AssignVacationPoliciesToUserTest {
        @Test
        @DisplayName("실패 - 존재하지 않는 정책이면 예외가 발생한다")
        void assignVacationPoliciesFailPolicyNotFound() {
            // given
            String userId = "user1";
            User user = createTestUser(userId);
            List<Long> policyIds = List.of(999L);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationPolicyRepository.findVacationPolicyById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationService.assignVacationPoliciesToUser(userId, policyIds))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("휴가 승인")
    class ApproveVacation {
        @Test
        @DisplayName("실패 - 존재하지 않는 승인이면 예외가 발생한다")
        void approveVacationFailNotFound() {
            // given
            Long approvalId = 999L;
            String approverId = "approver1";

            given(vacationApprovalRepository.findByIdWithVacationGrantAndUser(approvalId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> vacationService.approveVacation(approvalId, approverId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("휴가 거부")
    class RejectVacation {
        @Test
        @DisplayName("실패 - 존재하지 않는 승인이면 예외가 발생한다")
        void rejectVacationFailNotFound() {
            // given
            Long approvalId = 999L;
            String approverId = "approver1";

            given(vacationApprovalRepository.findByIdWithVacationGrantAndUser(approvalId))
                    .willReturn(Optional.empty());

            VacationApprovalServiceDto data = VacationApprovalServiceDto.builder()
                    .rejectionReason("거부 사유")
                    .build();

            // when & then
            assertThatThrownBy(() -> vacationService.rejectVacation(approvalId, approverId, data))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("휴가 정책 생성")
    class CreateVacationPolicy {
        @Test
        @DisplayName("성공 - 휴가 정책을 생성한다")
        void createVacationPolicySuccess() {
            // given
            VacationPolicyServiceDto dto = VacationPolicyServiceDto.builder()
                    .name("테스트정책")
                    .desc("테스트 설명")
                    .vacationType(VacationType.ANNUAL)
                    .grantMethod(GrantMethod.MANUAL_GRANT)
                    .grantTime(new BigDecimal("15.0000"))
                    .isFlexibleGrant(YNType.N)
                    .minuteGrantYn(YNType.N)
                    .effectiveType(EffectiveType.IMMEDIATELY)
                    .expirationType(ExpirationType.END_OF_YEAR)
                    .build();

            ManualGrant mockStrategy = mock(ManualGrant.class);

            given(vacationPolicyStrategyFactory.getStrategy(GrantMethod.MANUAL_GRANT))
                    .willReturn(mockStrategy);
            given(mockStrategy.registVacationPolicy(dto)).willReturn(1L);

            // when
            Long result = vacationService.createVacationPolicy(dto);

            // then
            assertThat(result).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("정책에서 유저 제거")
    class RevokeVacationPoliciesFromUserTest {
        @Test
        @DisplayName("성공 - 정책에 할당되지 않은 유저면 스킵한다")
        void revokeVacationPoliciesSkipNotAssigned() {
            // given
            String userId = "user1";
            User user = createTestUser(userId);
            Long policyId = 1L;
            VacationPolicy policy = createTestPolicy();
            ReflectionTestUtils.setField(policy, "id", policyId);

            given(userService.checkUserExist(userId)).willReturn(user);
            given(vacationPolicyRepository.findVacationPolicyById(policyId)).willReturn(Optional.of(policy));
            given(userVacationPolicyRepository.findByUserIdAndVacationPolicyId(userId, policyId))
                    .willReturn(Optional.empty());

            // when
            List<Long> result = vacationService.revokeVacationPoliciesFromUser(userId, List.of(policyId));

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("유저 할당 정책 조회 (필터)")
    class GetUserAssignedVacationPoliciesWithFiltersTest {
        @Test
        @DisplayName("성공 - 필터 조건으로 유저 할당 정책을 조회한다")
        void getUserAssignedVacationPoliciesWithFiltersSuccess() {
            // given
            String userId = "user1";
            VacationType vacationType = VacationType.ANNUAL;
            GrantMethod grantMethod = GrantMethod.MANUAL_GRANT;

            given(userVacationPolicyRepository.findByUserIdWithFilters(userId, vacationType, grantMethod))
                    .willReturn(List.of());

            // when
            List<VacationPolicyServiceDto> result = vacationService
                    .getUserAssignedVacationPoliciesWithFilters(userId, vacationType, grantMethod);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("유저별 모든 신청 휴가 조회")
    class GetAllRequestedVacationsByUserIdTest {
        @Test
        @DisplayName("성공 - 유저별 신청 휴가 목록을 조회한다")
        void getAllRequestedVacationsByUserIdSuccess() {
            // given
            String userId = "user1";
            Integer year = 2025;

            given(vacationGrantRepository.findAllRequestedVacationsByUserIdAndYear(userId, year)).willReturn(List.of());

            // when
            List<VacationServiceDto> result = vacationService.getAllRequestedVacationsByUserId(userId, year);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("승인자별 모든 휴가 조회")
    class GetAllVacationsByApproverTest {
        @Test
        @DisplayName("성공 - 승인자별 모든 휴가 목록을 조회한다")
        void getAllVacationsByApproverSuccess() {
            // given
            String approverId = "approver1";
            Integer year = 2025;
            GrantStatus status = GrantStatus.PENDING;

            given(vacationApprovalRepository.findAllVacationGrantIdsByApproverIdAndYear(approverId, year))
                    .willReturn(List.of());

            // when
            List<VacationServiceDto> result = vacationService.getAllVacationsByApprover(approverId, year, status);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("휴가 사용 기록 조회")
    class GetVacationUsagesTest {
        @Test
        @DisplayName("성공 - 휴가 사용 기록을 조회한다")
        void getVacationUsagesSuccess() {
            // given
            String userId = "user1";
            LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);
            User user = createTestUser(userId);
            VacationUsage usage = createTestUsage(user);
            ReflectionTestUtils.setField(usage, "id", 1L);

            given(vacationUsageRepository.findByUserIdAndPeriodWithUser(userId, start, end))
                    .willReturn(List.of(usage));

            // when
            List<VacationServiceDto> result = vacationService.getUserVacationUsagesByPeriod(userId, start, end);

            // then
            assertThat(result).hasSize(1);
        }
    }

    // 테스트 헬퍼 메서드들
    private User createTestUser(String userId) {
        return User.createUser(userId, "password", "테스트유저", "test@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
    }

    private VacationGrant createTestGrant(User user) {
        return createTestGrant(user, VacationType.ANNUAL, new BigDecimal("15.0000"));
    }

    private VacationGrant createTestGrant(User user, VacationType type, BigDecimal remainTime) {
        VacationPolicy policy = createTestPolicy();
        ReflectionTestUtils.setField(policy, "id", 1L);

        return VacationGrant.createVacationGrant(
                user, policy, "휴가 부여", type, remainTime,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 12, 31, 23, 59)
        );
    }

    private VacationUsage createTestUsage(User user) {
        return VacationUsage.createVacationUsage(
                user, "연차 사용", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 1, 9, 0),
                LocalDateTime.of(2025, 6, 1, 18, 0),
                new BigDecimal("1.0000")
        );
    }

    private VacationPolicy createTestPolicy() {
        VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                "연차", "연차 휴가", VacationType.ANNUAL,
                new BigDecimal("15.0000"), YNType.N, YNType.N,
                EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        return policy;
    }
}
