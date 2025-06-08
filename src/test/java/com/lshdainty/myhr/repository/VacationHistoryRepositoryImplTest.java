package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.*;
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
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        Vacation vacation = Vacation.createVacation(user, VacationType.ANNUAL, new BigDecimal("4.0000"),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                0L, "");
        em.persist(vacation);


        String desc = "1분기 휴가";
        BigDecimal grantTime = new BigDecimal("4.0000");
        VacationHistory history = VacationHistory.createRegistVacationHistory(vacation, desc, grantTime, 0L, "");

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
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        Vacation vacation = Vacation.createVacation(user, VacationType.ANNUAL, new BigDecimal("4.0000"),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                0L, "");
        em.persist(vacation);

        String desc = "연차";
        VacationTimeType type = VacationTimeType.DAYOFF;
        LocalDateTime usedDateTime = LocalDateTime.of(2025, 2, 1, 0, 0, 0);
        VacationHistory history = VacationHistory.createUseVacationHistory(vacation, desc, type, usedDateTime, 0L, "");

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
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        Vacation vacation = Vacation.createVacation(user, VacationType.ANNUAL, new BigDecimal("4.0000"),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                0L, "");
        em.persist(vacation);

        String[] descs = {"연차", "1시간"};
        VacationTimeType[] types = {VacationTimeType.DAYOFF, VacationTimeType.ONETIMEOFF};
        LocalDateTime[] usedDateTimes = {
                LocalDateTime.of(2025, 2, 1, 0, 0, 0),
                LocalDateTime.of(2025, 7, 1, 9, 0, 0)
        };

        for (int i = 0; i < descs.length; i++) {
            VacationHistory history = VacationHistory.createUseVacationHistory(vacation, descs[i], types[i], usedDateTimes[i], 0L, "");
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
}