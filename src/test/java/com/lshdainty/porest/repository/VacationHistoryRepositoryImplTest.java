package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.*;
import com.lshdainty.porest.type.VacationTimeType;
import com.lshdainty.porest.type.VacationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(VacationHistoryRepositoryImpl.class)
@Transactional
@DisplayName("JPA 휴가 내역 레포지토리 테스트")
class VacationHistoryRepositoryImplTest {
    @Autowired
    private VacationHistoryRepositoryImpl vacationHistoryRepositoryImpl;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("휴가 등록 내역 저장 및 단건 조회")
    void saveRegist() {
        User user = User.createUser("test1");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        Vacation vacation = Vacation.createVacation(user, VacationType.ANNUAL, new BigDecimal("4.0000"),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                "", "");
        em.persist(vacation);

        String desc = "1분기 휴가";
        BigDecimal grantTime = new BigDecimal("4.0000");
        VacationHistory history = VacationHistory.createRegistVacationHistory(vacation, desc, grantTime, "", "");

        // when
        vacationHistoryRepositoryImpl.save(history);
        em.flush();
        em.clear();
        Optional<VacationHistory> findHistory = vacationHistoryRepositoryImpl.findById(history.getId());

        // then
        assertThat(findHistory.isPresent()).isTrue();
        assertThat(findHistory.get().getDesc()).isEqualTo(desc);
        assertThat(findHistory.get().getGrantTime()).isEqualTo(grantTime);
    }

    @Test
    @DisplayName("휴가 사용 내역 저장 및 단건 조회")
    void saveUse() {
        User user = User.createUser("test1");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        Vacation vacation = Vacation.createVacation(user, VacationType.ANNUAL, new BigDecimal("4.0000"),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                "", "");
        em.persist(vacation);

        String desc = "연차";
        VacationTimeType type = VacationTimeType.DAYOFF;
        LocalDateTime usedDateTime = LocalDateTime.of(2025, 2, 1, 0, 0, 0);
        VacationHistory history = VacationHistory.createUseVacationHistory(vacation, desc, type, usedDateTime, "", "");

        // when
        vacationHistoryRepositoryImpl.save(history);
        em.flush();
        em.clear();
        Optional<VacationHistory> findHistory = vacationHistoryRepositoryImpl.findById(history.getId());

        // then
        assertThat(findHistory.isPresent()).isTrue();
        assertThat(findHistory.get().getDesc()).isEqualTo(desc);
        assertThat(findHistory.get().getType()).isEqualTo(type);
        assertThat(findHistory.get().getUsedDateTime()).isEqualTo(usedDateTime);
    }

    @Test
    @DisplayName("단건 조회 시 휴가 내역이 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given
        Long historyNo = 999L;

        // when
        Optional<VacationHistory> findHistory = vacationHistoryRepositoryImpl.findById(historyNo);

        // then
        assertThat(findHistory.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("usedDateTime이 start, end 사이에 해당하는 목록을 조회한다.")
    void getVacationHistorysByPeriod() {
        User user = User.createUser("test1");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        Vacation vacation = Vacation.createVacation(user, VacationType.ANNUAL, new BigDecimal("4.0000"),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                "", "");
        em.persist(vacation);

        String[] descs = {"연차", "1시간"};
        VacationTimeType[] types = {VacationTimeType.DAYOFF, VacationTimeType.ONETIMEOFF};
        LocalDateTime[] usedDateTimes = {
                LocalDateTime.of(2025, 2, 1, 0, 0, 0),
                LocalDateTime.of(2025, 7, 1, 9, 0, 0)
        };

        for (int i = 0; i < descs.length; i++) {
            VacationHistory history = VacationHistory.createUseVacationHistory(vacation, descs[i], types[i], usedDateTimes[i], "", "");
            vacationHistoryRepositoryImpl.save(history);
        }

        // when
        List<VacationHistory> histories = vacationHistoryRepositoryImpl.findVacationHistorysByPeriod(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        );

        // then
        assertThat(histories.size()).isEqualTo(descs.length);
        assertThat(histories).extracting("desc").containsExactlyInAnyOrder(descs);
        assertThat(histories).extracting("type").containsExactlyInAnyOrder(types);
        assertThat(histories).extracting("usedDateTime").containsExactlyInAnyOrder(usedDateTimes);
    }

    @Test
    @DisplayName("usedDateTime이 start, end 사이에 해당하는 목록이 없더라도 Null이 반환되면 안된다.")
    void getVacationHistorysByPeriodEmpty() {
        // given & when
        List<VacationHistory> histories = vacationHistoryRepositoryImpl.findVacationHistorysByPeriod(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        );

        // then
        assertThat(histories.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("사용자 ID와 기간으로 휴가 사용 내역을 조회한다.")
    void findVacationUseHistorysByUserAndPeriod() {
        // given
        User user1 = User.createUser("test1");
        em.persist(user1);
        User user2 = User.createUser("test2");
        em.persist(user2);

        LocalDateTime now = LocalDateTime.now();
        Vacation vacation1 = Vacation.createVacation(user1, VacationType.ANNUAL, new BigDecimal("1.0"), now.withDayOfYear(1), now.withDayOfYear(365), "", "");
        em.persist(vacation1);
        Vacation vacation2 = Vacation.createVacation(user2, VacationType.ANNUAL, new BigDecimal("1.0"), now.withDayOfYear(1), now.withDayOfYear(365), "", "");
        em.persist(vacation2);

        VacationHistory history1 = VacationHistory.createUseVacationHistory(vacation1, "user1 vacation", VacationTimeType.DAYOFF, now.withMonth(3), "", "");
        em.persist(history1);
        VacationHistory history2 = VacationHistory.createUseVacationHistory(vacation2, "user2 vacation", VacationTimeType.DAYOFF, now.withMonth(4), "", "");
        em.persist(history2);


        // when
        List<VacationHistory> result = vacationHistoryRepositoryImpl.findVacationUseHistorysByUserAndPeriod(
                user1.getId(),
                now.withDayOfYear(1),
                now.withDayOfYear(365)
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDesc()).isEqualTo("user1 vacation");
    }

    @Test
    @DisplayName("사용자 ID와 기간으로 조회시 내역이 없으면 빈 리스트를 반환한다.")
    void findVacationUseHistorysByUserAndPeriod_Empty() {
        // given
        User user1 = User.createUser("test1");
        em.persist(user1);
        LocalDateTime now = LocalDateTime.now();

        // when
        List<VacationHistory> result = vacationHistoryRepositoryImpl.findVacationUseHistorysByUserAndPeriod(
                user1.getId(),
                now.withDayOfYear(1),
                now.withDayOfYear(365)
        );

        // then
        assertThat(result).isEmpty();
    }
}