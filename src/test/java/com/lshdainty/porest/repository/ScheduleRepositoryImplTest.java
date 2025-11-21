package com.lshdainty.porest.repository;

import com.lshdainty.porest.schedule.domain.Schedule;
import com.lshdainty.porest.schedule.repository.ScheduleRepositoryImpl;
import com.lshdainty.porest.schedule.type.ScheduleType;
import com.lshdainty.porest.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({ScheduleRepositoryImpl.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPA 스케줄 레포지토리 테스트")
public class ScheduleRepositoryImplTest {
    @Autowired
    private ScheduleRepositoryImpl scheduleRepositoryImpl;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("스케줄 저장 및 단건 조회")
    void save() {
        // given
        User user = User.createUser("test1");
        em.persist(user);

        Schedule schedule = Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION,
                LocalDateTime.of(2025, 1, 2, 9, 0, 0), LocalDateTime.of(2025, 1, 2, 14, 0, 0));

        // when
        scheduleRepositoryImpl.save(schedule);
        em.flush();
        em.clear();
        Optional<Schedule> findSchedule = scheduleRepositoryImpl.findById(schedule.getId());

        // then
        assertThat(findSchedule.isPresent()).isTrue();
        assertThat(findSchedule.get().getType()).isEqualTo(ScheduleType.EDUCATION);
        assertThat(findSchedule.get().getDesc()).isEqualTo("교육");
    }

    @Test
    @DisplayName("단건 조회 시 스케줄이 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given & when
        Optional<Schedule> findSchedule = scheduleRepositoryImpl.findById(999L);

        // then
        assertThat(findSchedule.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("유저가 가지고 있는 스케줄을 조회한다.")
    void getScheduleByUser() {
        // given
        User user = User.createUser("test1");
        em.persist(user);

        scheduleRepositoryImpl.save(Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION,
                LocalDateTime.of(2025, 1, 2, 9, 0, 0), LocalDateTime.of(2025, 1, 2, 23, 59, 59)));
        scheduleRepositoryImpl.save(Schedule.createSchedule(user, "출장", ScheduleType.BUSINESSTRIP,
                LocalDateTime.of(2025, 2, 3, 14, 0, 0), LocalDateTime.of(2025, 2, 3, 23, 59, 59)));

        // when
        List<Schedule> findSchedules = scheduleRepositoryImpl.findSchedulesByUserId(user.getId());

        // then
        assertThat(findSchedules.size()).isEqualTo(2);
        assertThat(findSchedules).extracting("desc").containsExactlyInAnyOrder("교육", "출장");
    }

    @Test
    @DisplayName("유저가 가지고 있는 스케줄이 없더라도 Null이 반환되면 안된다.")
    void getScheduleByUserNoEmpty() {
        // given
        User user = User.createUser("test1");
        em.persist(user);

        // when
        List<Schedule> findSchedules = scheduleRepositoryImpl.findSchedulesByUserId(user.getId());

        // then
        assertThat(findSchedules.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("유저 id가 null이 입력되어도 오류가 발생되면 안된다.")
    void getScheduleByUserNoIsNull() {
        // given & when
        List<Schedule> findSchedules = scheduleRepositoryImpl.findSchedulesByUserId(null);

        // then
        assertThat(findSchedules.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("사용자가 설정한 시작, 끝 시간에 스케줄 시작 시간이 해당하는 모든 스케줄을 조회한다.")
    void getScheduleByStartEnd() {
        // given
        User user = User.createUser("test1");
        em.persist(user);

        scheduleRepositoryImpl.save(Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION,
                LocalDateTime.of(2025, 1, 2, 9, 0, 0), LocalDateTime.of(2025, 1, 2, 14, 0, 0)));
        scheduleRepositoryImpl.save(Schedule.createSchedule(user, "출장", ScheduleType.BUSINESSTRIP,
                LocalDateTime.of(2025, 4, 3, 0, 0, 0), LocalDateTime.of(2025, 4, 3, 23, 59, 59)));

        // when
        List<Schedule> findSchedules = scheduleRepositoryImpl.findSchedulesByPeriod(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 1, 31, 0, 0, 0));

        // then
        assertThat(findSchedules.size()).isEqualTo(1);
        assertThat(findSchedules.get(0).getDesc()).isEqualTo("교육");
    }

    @Test
    @DisplayName("사용자가 설정한 시작, 끝 시간에 스케줄 시작 시간이 해당하는 모든 스케줄 조회한다. (경계값 케이스)")
    void getScheduleByStartEndBoundary() {
        // given
        User user = User.createUser("test1");
        em.persist(user);

        scheduleRepositoryImpl.save(Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION,
                LocalDateTime.of(2025, 1, 2, 9, 0, 0), LocalDateTime.of(2025, 1, 2, 14, 0, 0)));
        scheduleRepositoryImpl.save(Schedule.createSchedule(user, "출장", ScheduleType.BUSINESSTRIP,
                LocalDateTime.of(2025, 4, 3, 0, 0, 0), LocalDateTime.of(2025, 4, 3, 23, 59, 59)));

        // when
        List<Schedule> scheduleLeft = scheduleRepositoryImpl.findSchedulesByPeriod(
                LocalDateTime.of(2025, 1, 2, 0, 0, 0), LocalDateTime.of(2025, 1, 3, 0, 0, 0));
        List<Schedule> scheduleRight = scheduleRepositoryImpl.findSchedulesByPeriod(
                LocalDateTime.of(2025, 4, 3, 0, 0, 0), LocalDateTime.of(2025, 4, 4, 0, 0, 0));
        List<Schedule> schedule = scheduleRepositoryImpl.findSchedulesByPeriod(
                LocalDateTime.of(2025, 3, 27, 0, 0, 0), LocalDateTime.of(2025, 3, 28, 0, 0, 0));

        // then
        assertThat(scheduleLeft.size()).isEqualTo(1);
        assertThat(scheduleLeft.get(0).getDesc()).isEqualTo("교육");

        assertThat(scheduleRight.size()).isEqualTo(1);
        assertThat(scheduleRight.get(0).getDesc()).isEqualTo("출장");

        assertThat(schedule.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("사용자가 설정한 시작, 끝 시간에 스케줄 시작 시간이 해당하는 스케줄이 없어도 Null이 반환되면 안된다.")
    void getScheduleByStartEndEmpty() {
        // given
        User user = User.createUser("test1");
        em.persist(user);

        // when
        List<Schedule> findSchedules = scheduleRepositoryImpl.findSchedulesByPeriod(
                LocalDateTime.of(2025, 1, 2, 0, 0, 0), LocalDateTime.of(2025, 1, 3, 0, 0, 0));

        // then
        assertThat(findSchedules.isEmpty()).isTrue();
    }
}
