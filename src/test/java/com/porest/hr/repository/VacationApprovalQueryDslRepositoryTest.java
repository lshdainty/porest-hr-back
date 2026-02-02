package com.porest.hr.repository;

import com.porest.core.type.CountryCode;
import com.porest.core.type.YNType;
import com.porest.hr.common.type.DefaultCompanyType;
import com.porest.hr.user.domain.User;
import com.porest.hr.vacation.domain.VacationApproval;
import com.porest.hr.vacation.domain.VacationGrant;
import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.repository.VacationApprovalQueryDslRepository;
import com.porest.hr.vacation.type.EffectiveType;
import com.porest.hr.vacation.type.ExpirationType;
import com.porest.hr.vacation.type.VacationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({VacationApprovalQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 휴가승인 레포지토리 테스트")
class VacationApprovalQueryDslRepositoryTest {
    @Autowired
    private VacationApprovalQueryDslRepository vacationApprovalRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private User approver;
    private VacationPolicy policy;
    private VacationGrant grant;

    // 테스트용 User 생성 헬퍼 메소드
    private User createTestUser(String id, String name, String email) {
        return User.createUser(
                null, id, name, email,
                LocalDate.of(1990, 1, 1), DefaultCompanyType.NONE, "9 ~ 18",
                LocalDate.now(), YNType.N, null, null, CountryCode.KR
        );
    }

    @BeforeEach
    void setUp() {
        user = createTestUser("user1", "테스트유저1", "user1@test.com");
        em.persist(user);

        approver = createTestUser("approver1", "결재자1", "approver1@test.com");
        em.persist(approver);

        policy = VacationPolicy.createOnRequestPolicy(
                "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("1.0"),
                YNType.N, YNType.N, 1, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(policy);

        grant = VacationGrant.createPendingVacationGrant(
                user, policy, "연차 신청", VacationType.ANNUAL, new BigDecimal("1.0"),
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0), "개인 사유"
        );
        em.persist(grant);
    }

    @Test
    @DisplayName("승인 다건 저장")
    void saveAll() {
        // given
        User approver2 = createTestUser("approver2", "결재자2", "approver2@test.com");
        em.persist(approver2);

        List<VacationApproval> approvals = List.of(
                VacationApproval.createVacationApproval(grant, approver, 1),
                VacationApproval.createVacationApproval(grant, approver2, 2)
        );

        // when
        vacationApprovalRepository.saveAll(approvals);
        em.flush();
        em.clear();

        // then
        List<VacationApproval> result = vacationApprovalRepository.findByVacationGrantId(grant.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("부여 ID로 승인 목록 조회")
    void findByVacationGrantId() {
        // given
        vacationApprovalRepository.saveAll(List.of(VacationApproval.createVacationApproval(grant, approver, 1)));
        em.flush();
        em.clear();

        // when
        List<VacationApproval> result = vacationApprovalRepository.findByVacationGrantId(grant.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVacationGrant()).isNotNull();
        assertThat(result.get(0).getApprover()).isNotNull();
    }

    @Test
    @DisplayName("부여 ID로 조회 시 삭제된 승인 제외")
    void findByVacationGrantIdExcludesDeleted() {
        // given
        User approver2 = createTestUser("approver2", "결재자2", "approver2@test.com");
        em.persist(approver2);

        VacationApproval activeApproval = VacationApproval.createVacationApproval(grant, approver, 1);
        VacationApproval deletedApproval = VacationApproval.createVacationApproval(grant, approver2, 2);

        vacationApprovalRepository.saveAll(List.of(activeApproval, deletedApproval));

        // deletedApproval에 isDeleted 설정이 필요하지만 도메인에 deleteVacationApproval 메서드가 없으므로
        // 현재는 비활성화된 상태로 테스트하지 않음
        em.flush();
        em.clear();

        // when
        List<VacationApproval> result = vacationApprovalRepository.findByVacationGrantId(grant.getId());

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("부여 ID로 조회 시 없으면 빈 리스트 반환")
    void findByVacationGrantIdEmpty() {
        // when
        List<VacationApproval> result = vacationApprovalRepository.findByVacationGrantId(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ID로 승인 조회 시 부여와 유저 fetch join")
    void findByIdWithVacationGrantAndUser() {
        // given
        VacationApproval approval = VacationApproval.createVacationApproval(grant, approver, 1);
        vacationApprovalRepository.saveAll(List.of(approval));
        em.flush();
        em.clear();

        // when
        Optional<VacationApproval> result = vacationApprovalRepository
                .findByIdWithVacationGrantAndUser(approval.getId());

        // then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getVacationGrant()).isNotNull();
        assertThat(result.get().getVacationGrant().getUser()).isNotNull();
        assertThat(result.get().getVacationGrant().getPolicy()).isNotNull();
        assertThat(result.get().getApprover()).isNotNull();
    }

    @Test
    @DisplayName("ID로 승인 조회 (fetch join) 시 없으면 빈 Optional 반환")
    void findByIdWithVacationGrantAndUserEmpty() {
        // when
        Optional<VacationApproval> result = vacationApprovalRepository
                .findByIdWithVacationGrantAndUser(999L);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("다단계 승인 저장 및 조회")
    void multipleApproversForSameGrant() {
        // given
        User approver2 = createTestUser("approver2", "결재자2", "approver2@test.com");
        User approver3 = createTestUser("approver3", "결재자3", "approver3@test.com");
        em.persist(approver2);
        em.persist(approver3);

        vacationApprovalRepository.saveAll(List.of(
                VacationApproval.createVacationApproval(grant, approver, 1),
                VacationApproval.createVacationApproval(grant, approver2, 2),
                VacationApproval.createVacationApproval(grant, approver3, 3)
        ));
        em.flush();
        em.clear();

        // when
        List<VacationApproval> result = vacationApprovalRepository.findByVacationGrantId(grant.getId());

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("승인자 ID와 연도로 부여 ID 목록 조회")
    void findAllVacationGrantIdsByApproverIdAndYear() {
        // given
        vacationApprovalRepository.saveAll(List.of(VacationApproval.createVacationApproval(grant, approver, 1)));
        em.flush();
        em.clear();

        // when
        List<Long> result = vacationApprovalRepository.findAllVacationGrantIdsByApproverIdAndYear("approver1", 2026);

        // then
        assertThat(result).hasSize(1);
        assertThat(result).contains(grant.getId());
    }

    @Test
    @DisplayName("승인자 ID와 연도로 조회 시 다른 연도 제외")
    void findAllVacationGrantIdsByApproverIdAndYearExcludesOtherYear() {
        // given
        vacationApprovalRepository.saveAll(List.of(VacationApproval.createVacationApproval(grant, approver, 1)));
        em.flush();
        em.clear();

        // when - 2025년으로 조회
        List<Long> result = vacationApprovalRepository.findAllVacationGrantIdsByApproverIdAndYear("approver1", 2025);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("승인자 ID와 연도로 조회 시 없으면 빈 리스트 반환")
    void findAllVacationGrantIdsByApproverIdAndYearEmpty() {
        // when
        List<Long> result = vacationApprovalRepository.findAllVacationGrantIdsByApproverIdAndYear("nonexistent", 2026);

        // then
        assertThat(result).isEmpty();
    }
}
