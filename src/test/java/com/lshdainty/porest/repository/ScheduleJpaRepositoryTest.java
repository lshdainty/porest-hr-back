package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.schedule.domain.Schedule;
import com.lshdainty.porest.schedule.repository.ScheduleJpaRepository;
import com.lshdainty.porest.schedule.type.ScheduleType;
import com.lshdainty.porest.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ScheduleJpaRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPA 스케줄 레포지토리 테스트")
class ScheduleJpaRepositoryTest {
    @Autowired
    private ScheduleJpaRepository scheduleRepository;

    @Autowired
    private TestEntityManager em;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        em.persist(user);
    }

    @Test
    @DisplayName("스케줄 저장 및 단건 조회")
    void save() {
        // given
        Schedule schedule = Schedule.createSchedule(
                user, "테스트 교육", ScheduleType.EDUCATION,
                LocalDateTime.of(2025, 6, 1, 9, 0),
                LocalDateTime.of(2025, 6, 1, 18, 0)
        );

        // when
        scheduleRepository.save(schedule);
        em.flush();
        em.clear();

        // then
        Optional<Schedule> findSchedule = scheduleRepository.findById(schedule.getId());
        assertThat(findSchedule.isPresent()).isTrue();
        assertThat(findSchedule.get().getDesc()).isEqualTo("테스트 교육");
        assertThat(findSchedule.get().getType()).isEqualTo(ScheduleType.EDUCATION);
    }

    @Test
    @DisplayName("단건 조회 시 스케줄이 없으면 빈 Optional 반환")
    void findByIdEmpty() {
        // when
        Optional<Schedule> findSchedule = scheduleRepository.findById(999L);

        // then
        assertThat(findSchedule.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("유저 ID로 스케줄 목록 조회")
    void findSchedulesByUserId() {
        // given
        scheduleRepository.save(Schedule.createSchedule(
                user, "교육1", ScheduleType.EDUCATION,
                LocalDateTime.of(2025, 6, 1, 9, 0),
                LocalDateTime.of(2025, 6, 1, 18, 0)
        ));
        scheduleRepository.save(Schedule.createSchedule(
                user, "출장", ScheduleType.BUSINESSTRIP,
                LocalDateTime.of(2025, 6, 2, 9, 0),
                LocalDateTime.of(2025, 6, 2, 18, 0)
        ));
        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findSchedulesByUserId("user1");

        // then
        assertThat(schedules).hasSize(2);
        assertThat(schedules).extracting("desc").containsExactlyInAnyOrder("교육1", "출장");
    }

    @Test
    @DisplayName("유저 ID로 조회 시 삭제된 스케줄 제외")
    void findSchedulesByUserIdExcludesDeleted() {
        // given
        Schedule activeSchedule = Schedule.createSchedule(
                user, "활성 스케줄", ScheduleType.EDUCATION,
                LocalDateTime.of(2025, 6, 1, 9, 0),
                LocalDateTime.of(2025, 6, 1, 18, 0)
        );
        Schedule deletedSchedule = Schedule.createSchedule(
                user, "삭제 스케줄", ScheduleType.BUSINESSTRIP,
                LocalDateTime.of(2025, 6, 2, 9, 0),
                LocalDateTime.of(2025, 6, 2, 18, 0)
        );
        scheduleRepository.save(activeSchedule);
        scheduleRepository.save(deletedSchedule);
        deletedSchedule.deleteSchedule();
        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findSchedulesByUserId("user1");

        // then
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).getDesc()).isEqualTo("활성 스케줄");
    }

    @Test
    @DisplayName("유저 ID로 조회 시 스케줄이 없으면 빈 리스트 반환")
    void findSchedulesByUserIdEmpty() {
        // when
        List<Schedule> schedules = scheduleRepository.findSchedulesByUserId("user1");

        // then
        assertThat(schedules).isEmpty();
    }

    @Test
    @DisplayName("기간으로 스케줄 조회")
    void findSchedulesByPeriod() {
        // given
        scheduleRepository.save(Schedule.createSchedule(
                user, "6월 스케줄", ScheduleType.EDUCATION,
                LocalDateTime.of(2025, 6, 15, 9, 0),
                LocalDateTime.of(2025, 6, 15, 18, 0)
        ));
        scheduleRepository.save(Schedule.createSchedule(
                user, "7월 스케줄", ScheduleType.BUSINESSTRIP,
                LocalDateTime.of(2025, 7, 15, 9, 0),
                LocalDateTime.of(2025, 7, 15, 18, 0)
        ));
        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findSchedulesByPeriod(
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
        );

        // then
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).getDesc()).isEqualTo("6월 스케줄");
    }

    @Test
    @DisplayName("기간으로 조회 시 삭제된 스케줄 제외")
    void findSchedulesByPeriodExcludesDeleted() {
        // given
        Schedule activeSchedule = Schedule.createSchedule(
                user, "활성 스케줄", ScheduleType.EDUCATION,
                LocalDateTime.of(2025, 6, 15, 9, 0),
                LocalDateTime.of(2025, 6, 15, 18, 0)
        );
        Schedule deletedSchedule = Schedule.createSchedule(
                user, "삭제 스케줄", ScheduleType.BUSINESSTRIP,
                LocalDateTime.of(2025, 6, 20, 9, 0),
                LocalDateTime.of(2025, 6, 20, 18, 0)
        );
        scheduleRepository.save(activeSchedule);
        scheduleRepository.save(deletedSchedule);
        deletedSchedule.deleteSchedule();
        em.flush();
        em.clear();

        // when
        List<Schedule> schedules = scheduleRepository.findSchedulesByPeriod(
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
        );

        // then
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).getDesc()).isEqualTo("활성 스케줄");
    }

    @Test
    @DisplayName("스케줄 삭제 (소프트 딜리트)")
    void deleteSchedule() {
        // given
        Schedule schedule = Schedule.createSchedule(
                user, "삭제할 스케줄", ScheduleType.EDUCATION,
                LocalDateTime.of(2025, 6, 1, 9, 0),
                LocalDateTime.of(2025, 6, 1, 18, 0)
        );
        scheduleRepository.save(schedule);
        em.flush();
        em.clear();

        // when
        Schedule foundSchedule = scheduleRepository.findById(schedule.getId()).orElseThrow();
        foundSchedule.deleteSchedule();
        em.flush();
        em.clear();

        // then
        List<Schedule> schedules = scheduleRepository.findSchedulesByUserId("user1");
        assertThat(schedules).isEmpty();
    }
}
