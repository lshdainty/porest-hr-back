package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.User;
import com.lshdainty.myhr.domain.Vacation;
import com.lshdainty.myhr.domain.VacationType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(VacationRepositoryImpl.class)
@Transactional
@DisplayName("JPA 휴가 레포지토리 테스트")
public class VacationRepositoryImplTest {
    @Autowired
    private VacationRepositoryImpl vacationRepositoryImpl;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("휴가 저장 및 단건 조회")
    void save() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        String name = "1분기 휴가";
        String desc = "";
        VacationType type = VacationType.BASIC;
        BigDecimal grantTime = new BigDecimal("4.0000");
        LocalDateTime occurDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59);

        Vacation vacation = Vacation.createVacation(user, name, desc, type, grantTime, occurDate, expiryDate, 0L, "");

        // when
        vacationRepositoryImpl.save(vacation);
        em.flush();
        em.clear();

        // then
        Vacation findVacation = vacationRepositoryImpl.findById(vacation.getId());
        assertThat(findVacation).isNotNull();
        assertThat(findVacation.getUser().getId()).isEqualTo(user.getId());
        assertThat(findVacation.getName()).isEqualTo(name);
        assertThat(findVacation.getDesc()).isEqualTo(desc);
        assertThat(findVacation.getType()).isEqualTo(type);
        assertThat(findVacation.getGrantTime()).isEqualTo(grantTime);
        assertThat(findVacation.getOccurDate()).isEqualTo(occurDate);
        assertThat(findVacation.getExpiryDate()).isEqualTo(expiryDate);
    }

    @Test
    @DisplayName("유저에 부여된 전체 휴가가 보여야한다.")
    void getVacationsByUserNo() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        String[] names = {"1분기 휴가", "OT 정산"};
        String[] descs = {"1분기 휴가부여", "야간 배포에 따른 OT정산"};
        VacationType[] types = {VacationType.BASIC, VacationType.ADDED};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("0.1250")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear(), 3, 31, 23, 59, 59),
        };

        for (int i = 0; i < names.length; i++) {
            Vacation vacation = Vacation.createVacation(user, names[i], descs[i], types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
        }

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByUserNo(user.getId());

        // then
        assertThat(vacations.size()).isEqualTo(2);
        assertThat(vacations).extracting("name").containsExactlyInAnyOrder(names);
        assertThat(vacations).extracting("desc").containsExactlyInAnyOrder(descs);
        assertThat(vacations).extracting("type").containsExactlyInAnyOrder(types);
        assertThat(vacations).extracting("grantTime").containsExactlyInAnyOrder(grantTimes);
        assertThat(vacations).extracting("occurDate").containsExactlyInAnyOrder(occurDates);
        assertThat(vacations).extracting("expiryDate").containsExactlyInAnyOrder(expiryDates);
    }

    @Test
    @DisplayName("유효 기간이 연도에 해당하는 휴가들만 조회돼야 한다.")
    void getVacationsByYear() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        String[] names = {"작년 1분기 휴가", "올해 1분기 휴가"};
        String[] descs = {"", ""};
        VacationType[] types = {VacationType.BASIC, VacationType.BASIC};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("4.0000")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear() - 1, 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear() - 1, 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
        };

        for (int i = 0; i < names.length; i++) {
            Vacation vacation = Vacation.createVacation(user, names[i], descs[i], types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
        }

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByYear(String.valueOf(now.getYear()));

        // then
        assertThat(vacations).hasSize(1);
        assertThat(vacations.get(0).getName()).isEqualTo("올해 1분기 휴가");
        assertThat(vacations.get(0).getOccurDate()).isEqualTo(occurDates[1]);
        assertThat(vacations.get(0).getExpiryDate()).isEqualTo(expiryDates[1]);
    }

    @Test
    @DisplayName("Today가 발생시간 및 유효기간 안에 해당하는 휴가들만 조회돼야 한다.")
    void getVacationsByParamTime() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        String[] names = {"작년 1분기 휴가", "올해 1분기 휴가"};
        String[] descs = {"", ""};
        VacationType[] types = {VacationType.BASIC, VacationType.BASIC};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("4.0000")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear() - 1, 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear() - 1, 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
        };

        for (int i = 0; i < names.length; i++) {
            Vacation vacation = Vacation.createVacation(user, names[i], descs[i], types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
        }

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByParameterTime(user.getId(), now);

        // then
        assertThat(vacations).hasSize(1);
        assertThat(vacations.get(0).getName()).isEqualTo("올해 1분기 휴가");
        assertThat(vacations.get(0).getOccurDate()).isEqualTo(occurDates[1]);
        assertThat(vacations.get(0).getExpiryDate()).isEqualTo(expiryDates[1]);
    }

    @Test
    @DisplayName("Today가 발생시간 및 유효기간 안에 해당하는 휴가들과 그에 관련된 스케줄만 조회돼야 한다.")
    void getVacationsByParamTimeWithSchedules() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        String[] names = {"작년 1분기 휴가", "올해 1분기 휴가"};
        String[] descs = {"", ""};
        VacationType[] types = {VacationType.BASIC, VacationType.BASIC};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("4.0000")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear() - 1, 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear() - 1, 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
        };

        for (int i = 0; i < names.length; i++) {
            Vacation vacation = Vacation.createVacation(user, names[i], descs[i], types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
        }

        // when
//        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByParameterTime(user.getId(), now);

        // then
//        assertThat(vacations).hasSize(1);
//        assertThat(vacations.get(0).getName()).isEqualTo("올해 1분기 휴가");
//        assertThat(vacations.get(0).getOccurDate()).isEqualTo(occurDates[1]);
//        assertThat(vacations.get(0).getExpiryDate()).isEqualTo(expiryDates[1]);
    }

    @Test
    @DisplayName("휴가 삭제")
    void deleteVacation() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        String name = "1분기 휴가";
        String desc = "";
        VacationType type = VacationType.BASIC;
        BigDecimal grantTime = new BigDecimal("4.0000");
        LocalDateTime occurDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59);

        Vacation vacation = Vacation.createVacation(user, name, desc, type, grantTime, occurDate, expiryDate, 0L, "");
        vacationRepositoryImpl.save(vacation);

        // when
        vacation.deleteVacation(0L, "");
        em.flush();
        em.clear();

        // then
//        Vacation findVacation = vacationRepositoryImpl.findById(vacation.getId());
//        assertThat(findVacation.getDelYN()).isEqualTo("Y");
    }
}
