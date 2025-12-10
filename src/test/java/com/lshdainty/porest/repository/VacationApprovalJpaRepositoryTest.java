package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.domain.VacationApproval;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.VacationApprovalJpaRepository;
import com.lshdainty.porest.vacation.type.EffectiveType;
import com.lshdainty.porest.vacation.type.ExpirationType;
import com.lshdainty.porest.vacation.type.VacationType;
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
@Import({VacationApprovalJpaRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPA 휴가승인 레포지토리 테스트")
class VacationApprovalJpaRepositoryTest {
    @Autowired
    private VacationApprovalJpaRepository vacationApprovalRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private User approver;
    private VacationPolicy policy;
    private VacationGrant grant;

    @BeforeEach
    void setUp() {
        user = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        em.persist(user);

        approver = User.createUser(
                "approver1", "password", "결재자1", "approver1@test.com",
                LocalDate.of(1985, 5, 5), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
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
    @DisplayName("승인 저장")
    void save() {
        // given
        VacationApproval approval = VacationApproval.createVacationApproval(grant, approver, 1);

        // when
        vacationApprovalRepository.save(approval);
        em.flush();
        em.clear();

        // then
        Optional<VacationApproval> result = vacationApprovalRepository.findById(approval.getId());
        assertThat(result.isPresent()).isTrue();
    }

    @Test
    @DisplayName("승인 다건 저장")
    void saveAll() {
        // given
        User approver2 = User.createUser(
                "approver2", "password", "결재자2", "approver2@test.com",
                LocalDate.of(1986, 6, 6), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
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
        vacationApprovalRepository.save(VacationApproval.createVacationApproval(grant, approver, 1));
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
        User approver2 = User.createUser(
                "approver2", "password", "결재자2", "approver2@test.com",
                LocalDate.of(1986, 6, 6), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        em.persist(approver2);

        VacationApproval activeApproval = VacationApproval.createVacationApproval(grant, approver, 1);
        VacationApproval deletedApproval = VacationApproval.createVacationApproval(grant, approver2, 2);

        vacationApprovalRepository.save(activeApproval);
        vacationApprovalRepository.save(deletedApproval);

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
    @DisplayName("승인자 ID로 부여 ID 목록 조회")
    void findAllVacationGrantIdsByApproverId() {
        // given
        vacationApprovalRepository.save(VacationApproval.createVacationApproval(grant, approver, 1));
        em.flush();
        em.clear();

        // when
        List<Long> result = vacationApprovalRepository.findAllVacationGrantIdsByApproverId("approver1");

        // then
        assertThat(result).hasSize(1);
        assertThat(result).contains(grant.getId());
    }

    @Test
    @DisplayName("승인자 ID로 조회 시 중복 제거")
    void findAllVacationGrantIdsByApproverIdDistinct() {
        // given
        VacationGrant grant2 = VacationGrant.createPendingVacationGrant(
                user, policy, "두번째 신청", VacationType.ANNUAL, new BigDecimal("1.0"),
                LocalDateTime.of(2025, 6, 2, 9, 0), LocalDateTime.of(2025, 6, 2, 18, 0), "두번째 사유"
        );
        em.persist(grant2);

        vacationApprovalRepository.save(VacationApproval.createVacationApproval(grant, approver, 1));
        vacationApprovalRepository.save(VacationApproval.createVacationApproval(grant2, approver, 1));
        em.flush();
        em.clear();

        // when
        List<Long> result = vacationApprovalRepository.findAllVacationGrantIdsByApproverId("approver1");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(grant.getId(), grant2.getId());
    }

    @Test
    @DisplayName("승인자 ID로 조회 시 없으면 빈 리스트 반환")
    void findAllVacationGrantIdsByApproverIdEmpty() {
        // when
        List<Long> result = vacationApprovalRepository.findAllVacationGrantIdsByApproverId("nonexistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ID로 승인 조회")
    void findById() {
        // given
        VacationApproval approval = VacationApproval.createVacationApproval(grant, approver, 1);
        vacationApprovalRepository.save(approval);
        em.flush();
        em.clear();

        // when
        Optional<VacationApproval> result = vacationApprovalRepository.findById(approval.getId());

        // then
        assertThat(result.isPresent()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 빈 Optional 반환")
    void findByIdEmpty() {
        // when
        Optional<VacationApproval> result = vacationApprovalRepository.findById(999L);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("ID로 승인 조회 시 부여와 유저 fetch join")
    void findByIdWithVacationGrantAndUser() {
        // given
        VacationApproval approval = VacationApproval.createVacationApproval(grant, approver, 1);
        vacationApprovalRepository.save(approval);
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
        User approver2 = User.createUser(
                "approver2", "password", "결재자2", "approver2@test.com",
                LocalDate.of(1986, 6, 6), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        User approver3 = User.createUser(
                "approver3", "password", "결재자3", "approver3@test.com",
                LocalDate.of(1987, 7, 7), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        em.persist(approver2);
        em.persist(approver3);

        vacationApprovalRepository.save(VacationApproval.createVacationApproval(grant, approver, 1));
        vacationApprovalRepository.save(VacationApproval.createVacationApproval(grant, approver2, 2));
        vacationApprovalRepository.save(VacationApproval.createVacationApproval(grant, approver3, 3));
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
        vacationApprovalRepository.save(VacationApproval.createVacationApproval(grant, approver, 1));
        em.flush();
        em.clear();

        // when
        List<Long> result = vacationApprovalRepository.findAllVacationGrantIdsByApproverIdAndYear("approver1", 2025);

        // then
        assertThat(result).hasSize(1);
        assertThat(result).contains(grant.getId());
    }

    @Test
    @DisplayName("승인자 ID와 연도로 조회 시 다른 연도 제외")
    void findAllVacationGrantIdsByApproverIdAndYearExcludesOtherYear() {
        // given
        vacationApprovalRepository.save(VacationApproval.createVacationApproval(grant, approver, 1));
        em.flush();
        em.clear();

        // when - 2024년으로 조회
        List<Long> result = vacationApprovalRepository.findAllVacationGrantIdsByApproverIdAndYear("approver1", 2024);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("승인자 ID와 연도로 조회 시 없으면 빈 리스트 반환")
    void findAllVacationGrantIdsByApproverIdAndYearEmpty() {
        // when
        List<Long> result = vacationApprovalRepository.findAllVacationGrantIdsByApproverIdAndYear("nonexistent", 2025);

        // then
        assertThat(result).isEmpty();
    }
}
