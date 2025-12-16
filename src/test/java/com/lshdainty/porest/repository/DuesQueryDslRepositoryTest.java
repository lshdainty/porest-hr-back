package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.dues.domain.Dues;
import com.lshdainty.porest.dues.repository.DuesQueryDslRepository;
import com.lshdainty.porest.dues.repository.dto.UsersMonthBirthDuesDto;
import com.lshdainty.porest.dues.type.DuesCalcType;
import com.lshdainty.porest.dues.type.DuesType;
import com.lshdainty.porest.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({DuesQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 회비 레포지토리 테스트")
class DuesQueryDslRepositoryTest {
    @Autowired
    private DuesQueryDslRepository duesRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("회비 저장 및 단건 조회")
    void save() {
        // given
        Dues dues = Dues.createDues(
                "홍길동", 50000L, DuesType.OPERATION, DuesCalcType.PLUS,
                LocalDate.of(2025, 1, 1), "1월 운영비"
        );

        // when
        duesRepository.save(dues);
        em.flush();
        em.clear();

        // then
        Optional<Dues> findDues = duesRepository.findById(dues.getId());
        assertThat(findDues.isPresent()).isTrue();
        assertThat(findDues.get().getUserName()).isEqualTo("홍길동");
        assertThat(findDues.get().getAmount()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("단건 조회 시 회비가 없으면 빈 Optional 반환")
    void findByIdEmpty() {
        // when
        Optional<Dues> findDues = duesRepository.findById(999L);

        // then
        assertThat(findDues.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("전체 회비 조회")
    void findDues() {
        // given
        duesRepository.save(Dues.createDues(
                "홍길동", 50000L, DuesType.OPERATION, DuesCalcType.PLUS,
                LocalDate.of(2025, 1, 1), "1월 운영비"
        ));
        duesRepository.save(Dues.createDues(
                "김철수", 50000L, DuesType.OPERATION, DuesCalcType.PLUS,
                LocalDate.of(2025, 2, 1), "2월 운영비"
        ));
        em.flush();
        em.clear();

        // when
        List<Dues> duesList = duesRepository.findDues();

        // then
        assertThat(duesList).hasSize(2);
    }

    @Test
    @DisplayName("연도별 회비 조회")
    void findDuesByYear() {
        // given
        duesRepository.save(Dues.createDues(
                "홍길동", 50000L, DuesType.OPERATION, DuesCalcType.PLUS,
                LocalDate.of(2025, 1, 1), "2025년 회비"
        ));
        duesRepository.save(Dues.createDues(
                "김철수", 50000L, DuesType.OPERATION, DuesCalcType.PLUS,
                LocalDate.of(2024, 1, 1), "2024년 회비"
        ));
        em.flush();
        em.clear();

        // when
        List<Dues> duesList = duesRepository.findDuesByYear(2025);

        // then
        assertThat(duesList).hasSize(1);
        assertThat(duesList.get(0).getDetail()).isEqualTo("2025년 회비");
    }

    @Test
    @DisplayName("연도별 운영비 조회 (생일비 제외)")
    void findOperatingDuesByYear() {
        // given
        duesRepository.save(Dues.createDues(
                "홍길동", 50000L, DuesType.OPERATION, DuesCalcType.PLUS,
                LocalDate.of(2025, 1, 1), "운영비"
        ));
        duesRepository.save(Dues.createDues(
                "김철수", 30000L, DuesType.BIRTH, DuesCalcType.PLUS,
                LocalDate.of(2025, 1, 15), "생일비"
        ));
        em.flush();
        em.clear();

        // when
        List<Dues> duesList = duesRepository.findOperatingDuesByYear(2025);

        // then
        assertThat(duesList).hasSize(1);
        assertThat(duesList.get(0).getType()).isEqualTo(DuesType.OPERATION);
    }

    @Test
    @DisplayName("연도 월별 생일비 합계 조회")
    void findBirthDuesByYearAndMonth() {
        // given
        duesRepository.save(Dues.createDues(
                "홍길동", 30000L, DuesType.BIRTH, DuesCalcType.PLUS,
                LocalDate.of(2025, 1, 15), "1월 생일비"
        ));
        duesRepository.save(Dues.createDues(
                "김철수", 30000L, DuesType.BIRTH, DuesCalcType.PLUS,
                LocalDate.of(2025, 1, 20), "1월 생일비"
        ));
        em.flush();
        em.clear();

        // when
        Long sum = duesRepository.findBirthDuesByYearAndMonth(2025, 1);

        // then
        assertThat(sum).isEqualTo(60000L);
    }

    @Test
    @DisplayName("연도 월별 생일비 합계 조회 - 데이터 없음")
    void findBirthDuesByYearAndMonthEmpty() {
        // when
        Long sum = duesRepository.findBirthDuesByYearAndMonth(2025, 1);

        // then
        assertThat(sum).isNull();
    }

    @Test
    @DisplayName("유저별 월별 생일비 입금내역 조회")
    void findUsersMonthBirthDues() {
        // given
        // User 테이블과 JOIN하므로 해당 userName과 동일한 name을 가진 User가 필요
        User user1 = User.createUser(
                "user1", "password", "홍길동", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 18",
                YNType.N, null, null, CountryCode.KR
        );
        User user2 = User.createUser(
                "user2", "password", "김철수", "user2@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 18",
                YNType.N, null, null, CountryCode.KR
        );
        em.persist(user1);
        em.persist(user2);

        duesRepository.save(Dues.createDues(
                "홍길동", 30000L, DuesType.BIRTH, DuesCalcType.PLUS,
                LocalDate.of(2025, 1, 15), "1월 생일비"
        ));
        duesRepository.save(Dues.createDues(
                "김철수", 30000L, DuesType.BIRTH, DuesCalcType.PLUS,
                LocalDate.of(2025, 2, 15), "2월 생일비"
        ));
        em.flush();
        em.clear();

        // when
        List<UsersMonthBirthDuesDto> result = duesRepository.findUsersMonthBirthDues(2025);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("유저별 월별 생일비 입금내역 조회 시 SYSTEM 계정 제외")
    void findUsersMonthBirthDuesExcludesSystemAccount() {
        // given
        User normalUser = User.createUser(
                "normalUser", "password", "일반유저", "normal@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 18",
                YNType.N, null, null, CountryCode.KR
        );
        User systemUser = User.createUser(
                "systemUser", "password", "시스템유저", "system@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.SYSTEM, "9 ~ 18",
                YNType.N, null, null, CountryCode.KR
        );
        em.persist(normalUser);
        em.persist(systemUser);

        duesRepository.save(Dues.createDues(
                "일반유저", 30000L, DuesType.BIRTH, DuesCalcType.PLUS,
                LocalDate.of(2025, 1, 15), "1월 생일비"
        ));
        duesRepository.save(Dues.createDues(
                "시스템유저", 30000L, DuesType.BIRTH, DuesCalcType.PLUS,
                LocalDate.of(2025, 2, 15), "2월 생일비"
        ));
        em.flush();
        em.clear();

        // when
        List<UsersMonthBirthDuesDto> result = duesRepository.findUsersMonthBirthDues(2025);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserName()).isEqualTo("일반유저");
    }

    @Test
    @DisplayName("회비 삭제")
    void delete() {
        // given
        Dues dues = Dues.createDues(
                "삭제할 회비", 10000L, DuesType.FINE, DuesCalcType.MINUS,
                LocalDate.of(2025, 1, 1), "벌금"
        );
        duesRepository.save(dues);
        em.flush();
        em.clear();

        // when
        Dues foundDues = duesRepository.findById(dues.getId()).orElseThrow();
        duesRepository.delete(foundDues);
        em.flush();
        em.clear();

        // then
        Optional<Dues> deletedDues = duesRepository.findById(dues.getId());
        assertThat(deletedDues.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("회비 수정")
    void updateDues() {
        // given
        Dues dues = Dues.createDues(
                "홍길동", 50000L, DuesType.OPERATION, DuesCalcType.PLUS,
                LocalDate.of(2025, 1, 1), "원래 내용"
        );
        duesRepository.save(dues);
        em.flush();
        em.clear();

        // when
        Dues foundDues = duesRepository.findById(dues.getId()).orElseThrow();
        foundDues.updateDues("김철수", 60000L, DuesType.BIRTH, DuesCalcType.PLUS,
                LocalDate.of(2025, 2, 1), "수정된 내용");
        em.flush();
        em.clear();

        // then
        Dues updatedDues = duesRepository.findById(dues.getId()).orElseThrow();
        assertThat(updatedDues.getUserName()).isEqualTo("김철수");
        assertThat(updatedDues.getAmount()).isEqualTo(60000L);
        assertThat(updatedDues.getType()).isEqualTo(DuesType.BIRTH);
        assertThat(updatedDues.getDetail()).isEqualTo("수정된 내용");
    }
}
