package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.*;
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
@Import(ScheduleRepositoryImpl.class)
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
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        String desc = "건강검진";
        ScheduleType type = ScheduleType.HEALTHCHECKHALF;
        LocalDateTime start = LocalDateTime.of(2025, 1, 2, 9, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 2, 14, 0, 0);

        Schedule schedule = Schedule.createSchedule(user, desc, type, start, end, 0L, "");

        // when
        scheduleRepositoryImpl.save(schedule);
        em.flush();
        em.clear();
        Optional<Schedule> findSchedule = scheduleRepositoryImpl.findById(schedule.getId());

        // then
        assertThat(findSchedule.isPresent()).isTrue();
        assertThat(findSchedule.get().getType()).isEqualTo(type);
        assertThat(findSchedule.get().getDesc()).isEqualTo(desc);
        assertThat(findSchedule.get().getStartDate()).isEqualTo(start);
        assertThat(findSchedule.get().getEndDate()).isEqualTo(end);
    }

    @Test
    @DisplayName("단건 조회 시 스케줄이 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given
        Long scheduleId = 999L;

        // when
        Optional<Schedule> findSchedule = scheduleRepositoryImpl.findById(scheduleId);

        // then
        assertThat(findSchedule.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("유저가 가지고 있는 스케줄을 조회한다.")
    void getScheduleByUser() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        String[] descs = {"건강검진", "예비군"};
        ScheduleType[] types = {ScheduleType.HEALTHCHECK, ScheduleType.DEFENSE};
        LocalDateTime[] starts = {
                LocalDateTime.of(2025, 1, 2, 0, 0, 0),
                LocalDateTime.of(2025, 2, 3, 0, 0, 0)
        };
        LocalDateTime[] ends = {
                LocalDateTime.of(2025, 1, 2, 23, 59, 59),
                LocalDateTime.of(2025, 2, 3, 23, 59, 59)
        };

        for (int i = 0; i < descs.length; i++) {
            Schedule schedule = Schedule.createSchedule(user, descs[i], types[i], starts[i], ends[i], 0L, "");
            scheduleRepositoryImpl.save(schedule);
        }

        // when
        List<Schedule> findSchedules = scheduleRepositoryImpl.findSchedulesByUserNo(user.getId());

        // then
        assertThat(findSchedules.size()).isEqualTo(types.length);
        assertThat(findSchedules).extracting("desc").containsExactlyInAnyOrder(descs);
        assertThat(findSchedules).extracting("type").containsExactlyInAnyOrder(types);
        assertThat(findSchedules).extracting("startDate").containsExactlyInAnyOrder(starts);
        assertThat(findSchedules).extracting("endDate").containsExactlyInAnyOrder(ends);
    }

    @Test
    @DisplayName("유저가 가지고 있는 스케줄이 없더라도 Null이 반환되면 안된다.")
    void getScheduleByUserNoEmpty() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        // when
        List<Schedule> findSchedules = scheduleRepositoryImpl.findSchedulesByUserNo(user.getId());

        // then
        assertThat(findSchedules.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("유저 id가 null이 입력되어도 오류가 발생되면 안된다.")
    void getScheduleByUserNoIsNull() {
        // given
        Long userNo = null;

        // when
        List<Schedule> findSchedules = scheduleRepositoryImpl.findSchedulesByUserNo(userNo);

        // then
        assertThat(findSchedules.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("사용자가 설정한 시작, 끝 시간에 스케줄 시작 시간이 해당하는 모든 스케줄을 조회한다.")
    void getScheduleByStartEnd() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        String[] descs = {"건강검진", "예비군"};
        ScheduleType[] types = {ScheduleType.HEALTHCHECK, ScheduleType.DEFENSE};
        LocalDateTime[] starts = {
                LocalDateTime.of(2025, 1, 2, 0, 0, 0),
                LocalDateTime.of(2025, 4, 3, 0, 0, 0)
        };
        LocalDateTime[] ends = {
                LocalDateTime.of(2025, 1, 2, 23, 59, 59),
                LocalDateTime.of(2025, 4, 3, 23, 59, 59)
        };

        for (int i = 0; i < descs.length; i++) {
            Schedule schedule = Schedule.createSchedule(user, descs[i], types[i], starts[i], ends[i], 0L, "");
            scheduleRepositoryImpl.save(schedule);
        }

        // when
        List<Schedule> findSchedules = scheduleRepositoryImpl.findSchedulesByPeriod(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 31, 0, 0, 0)
        );

        // then
        assertThat(findSchedules.size()).isEqualTo(1);
        assertThat(findSchedules.get(0).getDesc()).isEqualTo("건강검진");
        assertThat(findSchedules.get(0).getType()).isEqualTo(ScheduleType.HEALTHCHECK);
        assertThat(findSchedules.get(0).getStartDate()).isEqualTo(LocalDateTime.of(2025, 1, 2, 0, 0, 0));
        assertThat(findSchedules.get(0).getEndDate()).isEqualTo(LocalDateTime.of(2025, 1, 2, 23, 59, 59));
    }

    @Test
    @DisplayName("사용자가 설정한 시작, 끝 시간에 스케줄 시작 시간이 해당하는 모든 스케줄 조회한다. (경계값 케이스)")
    void getScheduleByStartEndBoundary() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        String[] descs = {"건강검진", "예비군"};
        ScheduleType[] types = {ScheduleType.HEALTHCHECK, ScheduleType.DEFENSE};
        LocalDateTime[] starts = {
                LocalDateTime.of(2025, 1, 2, 0, 0, 0),
                LocalDateTime.of(2025, 4, 3, 0, 0, 0)
        };
        LocalDateTime[] ends = {
                LocalDateTime.of(2025, 1, 2, 23, 59, 59),
                LocalDateTime.of(2025, 4, 3, 23, 59, 59)
        };

        for (int i = 0; i < descs.length; i++) {
            Schedule schedule = Schedule.createSchedule(user, descs[i], types[i], starts[i], ends[i], 0L, "");
            scheduleRepositoryImpl.save(schedule);
        }

        // when
        List<Schedule> scheduleLeft = scheduleRepositoryImpl.findSchedulesByPeriod(
                LocalDateTime.of(2025, 1, 2, 0, 0, 0),
                LocalDateTime.of(2025, 1, 3, 0, 0, 0)
        );
        List<Schedule> scheduleRight = scheduleRepositoryImpl.findSchedulesByPeriod(
                LocalDateTime.of(2025, 4, 3, 0, 0, 0),
                LocalDateTime.of(2025, 4, 4, 0, 0, 0)
        );
        List<Schedule> schedule = scheduleRepositoryImpl.findSchedulesByPeriod(
                LocalDateTime.of(2025, 3, 27, 0, 0, 0),
                LocalDateTime.of(2025, 3, 28, 0, 0, 0)
        );

        // then
        assertThat(scheduleLeft.size()).isEqualTo(1);
        assertThat(scheduleLeft.get(0).getDesc()).isEqualTo("건강검진");
        assertThat(scheduleLeft.get(0).getType()).isEqualTo(ScheduleType.HEALTHCHECK);
        assertThat(scheduleLeft.get(0).getStartDate()).isEqualTo(LocalDateTime.of(2025, 1, 2, 0, 0, 0));
        assertThat(scheduleLeft.get(0).getEndDate()).isEqualTo(LocalDateTime.of(2025, 1, 2, 23, 59, 59));

        assertThat(scheduleRight.size()).isEqualTo(1);
        assertThat(scheduleRight.get(0).getDesc()).isEqualTo("예비군");
        assertThat(scheduleRight.get(0).getType()).isEqualTo(ScheduleType.DEFENSE);
        assertThat(scheduleRight.get(0).getStartDate()).isEqualTo(LocalDateTime.of(2025, 4, 3, 0, 0, 0));
        assertThat(scheduleRight.get(0).getEndDate()).isEqualTo(LocalDateTime.of(2025, 4, 3, 23, 59, 59));

        assertThat(schedule.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("사용자가 설정한 시작, 끝 시간에 스케줄 시작 시간이 해당하는 스케줄이 없어도 Null이 반환되면 안된다.")
    void getScheduleByStartEndEmpty() {
        // given
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        em.persist(user);

        // when
        List<Schedule> findSchedules = scheduleRepositoryImpl.findSchedulesByPeriod(
                LocalDateTime.of(2025, 1, 2, 0, 0, 0),
                LocalDateTime.of(2025, 1, 3, 0, 0, 0)
        );

        // then
        assertThat(findSchedules.isEmpty()).isTrue();
    }
}
