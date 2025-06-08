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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        VacationType type = VacationType.ANNUAL;
        BigDecimal grantTime = new BigDecimal("4.0000");
        LocalDateTime occurDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59);

        Vacation vacation = Vacation.createVacation(user, type, grantTime, occurDate, expiryDate, 0L, "");

        // when
        vacationRepositoryImpl.save(vacation);
        em.flush();
        em.clear();

        // then
        Optional<Vacation> findVacation = vacationRepositoryImpl.findById(vacation.getId());
        assertThat(findVacation.isPresent()).isTrue();
        assertThat(findVacation.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(findVacation.get().getType()).isEqualTo(type);
        assertThat(findVacation.get().getRemainTime()).isEqualTo(grantTime);
        assertThat(findVacation.get().getOccurDate()).isEqualTo(occurDate);
        assertThat(findVacation.get().getExpiryDate()).isEqualTo(expiryDate);
    }

    @Test
    @DisplayName("단건 조회 시 휴가가 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given
        Long vacationId = 999L;

        // when
        Optional<Vacation> findVacation = vacationRepositoryImpl.findById(vacationId);

        // then
        assertThat(findVacation.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("유저에 부여된 전체 휴가가 보여야한다.")
    void getVacationsByUserNo() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        VacationType[] types = {VacationType.ANNUAL, VacationType.OVERTIME};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("0.1250")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
        };

        for (int i = 0; i < types.length; i++) {
            Vacation vacation = Vacation.createVacation(user, types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
        }

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByUserNo(user.getId());

        // then
        assertThat(vacations.size()).isEqualTo(types.length);
        assertThat(vacations).extracting("type").containsExactlyInAnyOrder(types);
        assertThat(vacations).extracting("remainTime").containsExactlyInAnyOrder(grantTimes);
        assertThat(vacations).extracting("occurDate").containsExactlyInAnyOrder(occurDates);
        assertThat(vacations).extracting("expiryDate").containsExactlyInAnyOrder(expiryDates);
    }

    @Test
    @DisplayName("유저에 부여된 휴가가 없더라도 Null이 반환되면 안된다.")
    void getVacationsByUserNoEmpty() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByUserNo(user.getId());

        // then
        assertThat(vacations.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("유저 id가 null이 입력되어도 오류가 발생되면 안된다.")
    void getVacationsByUserNoIsNull() {
        // given
        Long userNo = null;

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByUserNo(userNo);

        // then
        assertThat(vacations.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("해당 년도에 같은 타입으로 등록된 휴가가 있는지 확인되어야 한다.")
    void getVacationsByTypeWithYear() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        VacationType[] types = {VacationType.ANNUAL, VacationType.ANNUAL};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("4.0000")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear() - 1, 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear() - 1, 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
        };

        for (int i = 0; i < types.length; i++) {
            Vacation vacation = Vacation.createVacation(user, types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
        }

        // when
        Optional<Vacation> vacation = vacationRepositoryImpl.findVacationByTypeWithYear(user.getId(), VacationType.ANNUAL, String.valueOf(now.getYear()));

        // then
        assertThat(vacation.isPresent()).isTrue();
        assertThat(vacation.get().getType()).isEqualTo(VacationType.ANNUAL);
        assertThat(vacation.get().getOccurDate()).isEqualTo(occurDates[1]);
        assertThat(vacation.get().getExpiryDate()).isEqualTo(expiryDates[1]);
    }

    @Test
    @DisplayName("해당 년도에 같은 타입으로 등록된 휴가가 없어도 Null이 반환되면 안된다.")
    void getVacationsByTypeWithYearEmpty() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        VacationType[] types = {VacationType.ANNUAL, VacationType.OVERTIME};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("0.1250")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear() - 1, 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear() - 1, 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
        };

        for (int i = 0; i < types.length; i++) {
            Vacation vacation = Vacation.createVacation(user, types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
        }

        // when
        Optional<Vacation> vacation = vacationRepositoryImpl.findVacationByTypeWithYear(user.getId(), VacationType.ANNUAL, String.valueOf(now.getYear()));

        // then
        assertThat(vacation.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("vacation type이 null이 입력되어도 오류가 발생되면 안된다.")
    void getVacationsByTypeNullWithYear() {
        // given
        Long userNo = 1L;
        VacationType type = null;
        String year = String.valueOf(LocalDateTime.now().getYear());

        // when
        Optional<Vacation> vacation = vacationRepositoryImpl.findVacationByTypeWithYear(userNo, type, year);

        // then
        assertThat(vacation.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("년도가 null이 입력되어도 오류가 발생되면 안된다.")
    void getVacationsByTypeWithYearNull() {
        // given
        Long userNo = 1L;
        VacationType type = VacationType.ANNUAL;
        String year = null;

        // when
        Optional<Vacation> vacation = vacationRepositoryImpl.findVacationByTypeWithYear(userNo, type, year);

        // then
        assertThat(vacation.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("baseTime이 발생시간 및 유효기간 안에 해당하는 휴가들만 조회돼야 한다.")
    void getVacationsByBaseTime() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        VacationType[] types = {VacationType.ANNUAL, VacationType.ANNUAL};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("4.0000")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear() - 1, 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear() - 1, 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
        };

        for (int i = 0; i < types.length; i++) {
            Vacation vacation = Vacation.createVacation(user, types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
        }

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByBaseTime(user.getId(), now);

        // then
        assertThat(vacations).hasSize(1);
        assertThat(vacations.get(0).getOccurDate()).isEqualTo(occurDates[1]);
        assertThat(vacations.get(0).getExpiryDate()).isEqualTo(expiryDates[1]);
    }

    @Test
    @DisplayName("baseTime이 발생시간 및 유효기간 안에 해당하는 휴가가 없어도 Null이 반환되면 안된다.")
    void getVacationsByBaseTimeEmpty() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        VacationType[] types = {VacationType.ANNUAL, VacationType.OVERTIME};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("0.1250")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear() - 1, 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear()-1, 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear() - 1, 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear()-1, 12, 31, 23, 59, 59),
        };

        for (int i = 0; i < types.length; i++) {
            Vacation vacation = Vacation.createVacation(user, types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
        }

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByBaseTime(user.getId(), now);

        // then
        assertThat(vacations).isEmpty();
    }

    @Test
    @DisplayName("userNo가 null이 입력되어도 오류가 발생되면 안된다.")
    void getVacationsByBaseTimeUserNoNull() {
        // given
        Long userNo = null;
        LocalDateTime baseTime = LocalDateTime.now();

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByBaseTime(userNo, baseTime);

        // then
        assertThat(vacations.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("baseTime이 null이 입력되어도 오류가 발생되면 안된다.")
    void getVacationsByBaseTimeNull() {
        // given
        Long userNo = 1L;
        LocalDateTime baseTime = null;

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByBaseTime(userNo, baseTime);

        // then
        assertThat(vacations.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("baseTime이 발생시간 및 유효기간 안에 해당하는 휴가가 조회돼야 한다.(history fetch join)")
    void getVacationsByBaseTimeWithHistory() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        VacationType[] types = {VacationType.ANNUAL, VacationType.ANNUAL};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("4.0000")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear() - 1, 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear() - 1, 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
        };

        for (int i = 0; i < types.length; i++) {
            Vacation vacation = Vacation.createVacation(user, types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
        }

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByBaseTimeWithHistory(user.getId(), now);

        // then
        assertThat(vacations).hasSize(1);
        assertThat(vacations.get(0).getOccurDate()).isEqualTo(occurDates[1]);
        assertThat(vacations.get(0).getExpiryDate()).isEqualTo(expiryDates[1]);
    }

    @Test
    @DisplayName("baseTime이 발생시간 및 유효기간 안에 해당하는 휴가가 없어도 Null이 반환되면 안된다.(history fetch join)")
    void getVacationsByBaseTimeWithHistoryEmpty() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        VacationType[] types = {VacationType.ANNUAL, VacationType.OVERTIME};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("0.1250")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear() - 1, 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear()-1, 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear() - 1, 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear()-1, 12, 31, 23, 59, 59),
        };

        for (int i = 0; i < types.length; i++) {
            Vacation vacation = Vacation.createVacation(user, types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
        }

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByBaseTimeWithHistory(user.getId(), now);

        // then
        assertThat(vacations).isEmpty();
    }

    @Test
    @DisplayName("userNo가 null이 입력되어도 오류가 발생되면 안된다.(history fetch join)")
    void getVacationsByBaseTimeWithHistoryUserNoNull() {
        // given
        Long userNo = null;
        LocalDateTime baseTime = LocalDateTime.now();

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByBaseTimeWithHistory(userNo, baseTime);

        // then
        assertThat(vacations.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("baseTime이 null이 입력되어도 오류가 발생되면 안된다.(history fetch join)")
    void getVacationsByBaseTimeWithHistoryNull() {
        // given
        Long userNo = 1L;
        LocalDateTime baseTime = null;

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByBaseTimeWithHistory(userNo, baseTime);

        // then
        assertThat(vacations.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("vacation id목록을 조회 조건으로 사용하여 조회한다.")
    void getVacationsByIdsWithUser() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        VacationType[] types = {VacationType.ANNUAL, VacationType.OVERTIME};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("0.1250")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
        };

        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            Vacation vacation = Vacation.createVacation(user, types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
            ids.add(vacation.getId());
        }

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByIdsWithUser(ids);

        // then
        assertThat(vacations).hasSize(2);
        assertThat(vacations).extracting("type").containsExactlyInAnyOrder(types);
        assertThat(vacations).extracting("remainTime").containsExactlyInAnyOrder(grantTimes);
        assertThat(vacations).extracting("occurDate").containsExactlyInAnyOrder(occurDates);
        assertThat(vacations).extracting("expiryDate").containsExactlyInAnyOrder(expiryDates);
    }

    @Test
    @DisplayName("vacation id목록을 조회 조건으로 사용하여 조회한 데이터가 없어도 Null이 반환되면 안된다.")
    void getVacationsByIdsWithUserEmpty() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        LocalDateTime now = LocalDateTime.now();
        VacationType[] types = {VacationType.ANNUAL, VacationType.OVERTIME};
        BigDecimal[] grantTimes = {new BigDecimal("4.0000"), new BigDecimal("0.1250")};
        LocalDateTime[] occurDates = {
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0)
        };
        LocalDateTime[] expiryDates = {
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
        };

        List<Long> ids = List.of(998L, 999L);
        for (int i = 0; i < types.length; i++) {
            Vacation vacation = Vacation.createVacation(user, types[i], grantTimes[i], occurDates[i], expiryDates[i], 0L, "");
            vacationRepositoryImpl.save(vacation);
        }

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByIdsWithUser(ids);

        // then
        assertThat(vacations).isEmpty();
    }

    @Test
    @DisplayName("vacation id목록이 비어있어도 오류가 발생되면 안된다.")
    void getVacationsByIdsEmptyWithUser() {
        // given
        List<Long> ids = List.of();

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByIdsWithUser(ids);

        // then
        assertThat(vacations).isEmpty();
    }

    @Test
    @DisplayName("vacation id목록이 null이 입력되어도 오류가 발생되면 안된다.")
    void getVacationsByIdsNullWithUser() {
        // given
        List<Long> ids = null;

        // when
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByIdsWithUser(ids);

        // then
        assertThat(vacations).isEmpty();
    }
}
