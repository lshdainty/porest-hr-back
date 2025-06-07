//package com.lshdainty.myhr.repository;
//
//import com.lshdainty.myhr.domain.*;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.context.annotation.Import;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.*;
//
//@DataJpaTest
//@Import(ScheduleRepositoryImpl.class)
//@Transactional
//@DisplayName("JPA 스케줄 레포지토리 테스트")
//public class ScheduleRepositoryImplTest {
//    @Autowired
//    private ScheduleRepositoryImpl scheduleRepositoryImpl;
//
//    @Autowired
//    private TestEntityManager em;
//
//    @Test
//    @DisplayName("스케줄 저장 및 단건 조회")
//    void save() {
//        // given
//        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
//        em.persist(user);
//
//        LocalDateTime now = LocalDateTime.now();
//        Vacation vacation = Vacation.createVacation(user, "1분기 휴가", "", VacationType.BASIC, new BigDecimal("4.0000"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), 0L, "");
//        em.persist(vacation);
//
//        String desc = "휴가";
//        ScheduleType type = ScheduleType.DAYOFF;
//        LocalDateTime start = LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0);
//        LocalDateTime end = LocalDateTime.of(now.getYear(), 1, 3, 23, 59, 59);
//
//        Schedule schedule = Schedule.createSchedule(user, vacation, desc, type, start, end, 0L, "");
//
//        // when
//        scheduleRepositoryImpl.save(schedule);
//        em.flush();
//        em.clear();
//
//        // then
//        Schedule findSchedule = scheduleRepositoryImpl.findById(schedule.getId());
//        assertThat(findSchedule).isNotNull();
//        assertThat(findSchedule.getType()).isEqualTo(type);
//        assertThat(findSchedule.getDesc()).isEqualTo(desc);
//        assertThat(findSchedule.getStartDate()).isEqualTo(start);
//        assertThat(findSchedule.getEndDate()).isEqualTo(end);
//    }
//
//    @Test
//    @DisplayName("유저가 가지고 있는 스케줄 조회")
//    void getScheduleByUser() {
//        // given
//        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
//        em.persist(user);
//
//        LocalDateTime now = LocalDateTime.now();
//        Vacation vacation = Vacation.createVacation(user, "1분기 휴가", "", VacationType.BASIC, new BigDecimal("4.0000"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), 0L, "");
//        em.persist(vacation);
//
//        String[] descs = {"휴가", "1시간"};
//        ScheduleType[] types = {ScheduleType.DAYOFF, ScheduleType.ONETIMEOFF};
//        LocalDateTime[] starts = {
//                LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0),
//                LocalDateTime.of(now.getYear(), 2, 2, 9, 0, 0)
//        };
//        LocalDateTime[] ends = {
//                LocalDateTime.of(now.getYear(), 1, 3, 23, 59, 59),
//                LocalDateTime.of(now.getYear(), 2, 2, 10, 0, 0)
//        };
//
//        for (int i = 0; i < descs.length; i++) {
//            Schedule schedule = Schedule.createSchedule(user, vacation, descs[i], types[i], starts[i], ends[i], 0L, "");
//            scheduleRepositoryImpl.save(schedule);
//        }
//
//        // when
//        List<Schedule> findSchedules = scheduleRepositoryImpl.findSchedulesByUserNo(user.getId());
//
//        // then
//        assertThat(findSchedules.size()).isEqualTo(2);
//        assertThat(findSchedules).extracting("desc").containsExactlyInAnyOrder(descs);
//        assertThat(findSchedules).extracting("type").containsExactlyInAnyOrder(types);
//        assertThat(findSchedules).extracting("startDate").containsExactlyInAnyOrder(starts);
//        assertThat(findSchedules).extracting("endDate").containsExactlyInAnyOrder(ends);
//    }
//
//    @Test
//    @DisplayName("사용자가 설정한 시작, 끝 시간에 스케줄 시작 시간이 해당하는 모든 스케줄 조회 (정상 케이스)")
//    void getScheduleByStartEnd() {
//        // given
//        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
//        em.persist(user);
//
//        LocalDateTime now = LocalDateTime.now();
//        Vacation vacation = Vacation.createVacation(user, "1분기 휴가", "", VacationType.BASIC, new BigDecimal("4.0000"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), 0L, "");
//        em.persist(vacation);
//
//        String[] descs = {"휴가", "1시간", "6시간", "오전반차"};
//        ScheduleType[] types = {ScheduleType.DAYOFF, ScheduleType.ONETIMEOFF, ScheduleType.SIXTIMEOFF, ScheduleType.MORNINGOFF};
//        LocalDateTime[] starts = {
//                LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0),
//                LocalDateTime.of(now.getYear(), 2, 2, 9, 0, 0),
//                LocalDateTime.of(now.getYear(), 3, 29, 11, 0, 0),
//                LocalDateTime.of(now.getYear(), 4, 15, 9, 0, 0),
//        };
//        LocalDateTime[] ends = {
//                LocalDateTime.of(now.getYear(), 1, 3, 23, 59, 59),
//                LocalDateTime.of(now.getYear(), 2, 2, 10, 0, 0),
//                LocalDateTime.of(now.getYear(), 3, 29, 18, 0, 0),
//                LocalDateTime.of(now.getYear(), 4, 15, 14, 0, 0),
//        };
//
//        for (int i = 0; i < descs.length; i++) {
//            Schedule schedule = Schedule.createSchedule(user, vacation, descs[i], types[i], starts[i], ends[i], 0L, "");
//            scheduleRepositoryImpl.save(schedule);
//        }
//
//        // when
//        List<Schedule> findSchedules = scheduleRepositoryImpl.findSchedulesByPeriod(
//                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
//                LocalDateTime.of(now.getYear(), 1, 4, 0, 0, 0)
//        );
//
//        // then
//        assertThat(findSchedules.size()).isEqualTo(1);
//        assertThat(findSchedules.get(0).getDesc()).isEqualTo("휴가");
//        assertThat(findSchedules.get(0).getType()).isEqualTo(ScheduleType.DAYOFF);
//        assertThat(findSchedules.get(0).getStartDate()).isEqualTo(LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0));
//        assertThat(findSchedules.get(0).getEndDate()).isEqualTo(LocalDateTime.of(now.getYear(), 1, 3, 23, 59, 59));
//    }
//
//    @Test
//    @DisplayName("사용자가 설정한 시작, 끝 시간에 스케줄 시작 시간이 해당하는 모든 스케줄 조회 (경계값 케이스)")
//    void getScheduleByStartEndBoundary() {
//        // given
//        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
//        em.persist(user);
//
//        LocalDateTime now = LocalDateTime.now();
//        Vacation vacation = Vacation.createVacation(user, "1분기 휴가", "", VacationType.BASIC, new BigDecimal("4.0000"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), 0L, "");
//        em.persist(vacation);
//
//        String[] descs = {"휴가", "1시간", "6시간", "오전반차"};
//        ScheduleType[] types = {ScheduleType.DAYOFF, ScheduleType.ONETIMEOFF, ScheduleType.SIXTIMEOFF, ScheduleType.MORNINGOFF};
//        LocalDateTime[] starts = {
//                LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0),
//                LocalDateTime.of(now.getYear(), 2, 2, 9, 0, 0),
//                LocalDateTime.of(now.getYear(), 3, 29, 11, 0, 0),
//                LocalDateTime.of(now.getYear(), 4, 15, 9, 0, 0),
//        };
//        LocalDateTime[] ends = {
//                LocalDateTime.of(now.getYear(), 1, 3, 23, 59, 59),
//                LocalDateTime.of(now.getYear(), 2, 2, 10, 0, 0),
//                LocalDateTime.of(now.getYear(), 3, 29, 18, 0, 0),
//                LocalDateTime.of(now.getYear(), 4, 15, 14, 0, 0),
//        };
//
//        for (int i = 0; i < descs.length; i++) {
//            Schedule schedule = Schedule.createSchedule(user, vacation, descs[i], types[i], starts[i], ends[i], 0L, "");
//            scheduleRepositoryImpl.save(schedule);
//        }
//
//        // when
//        List<Schedule> scheduleLeft = scheduleRepositoryImpl.findSchedulesByPeriod(
//                LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0),
//                LocalDateTime.of(now.getYear(), 1, 3, 0, 0, 0)
//        );
//        List<Schedule> scheduleRight = scheduleRepositoryImpl.findSchedulesByPeriod(
//                LocalDateTime.of(now.getYear(), 2, 1, 0, 0, 0),
//                LocalDateTime.of(now.getYear(), 2, 3, 0, 0, 0)
//        );
//        List<Schedule> schedule = scheduleRepositoryImpl.findSchedulesByPeriod(
//                LocalDateTime.of(now.getYear(), 3, 27, 0, 0, 0),
//                LocalDateTime.of(now.getYear(), 3, 28, 0, 0, 0)
//        );
//
//        // then
//        assertThat(scheduleLeft.size()).isEqualTo(1);
//        assertThat(scheduleLeft.get(0).getDesc()).isEqualTo("휴가");
//        assertThat(scheduleLeft.get(0).getType()).isEqualTo(ScheduleType.DAYOFF);
//        assertThat(scheduleLeft.get(0).getStartDate()).isEqualTo(LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0));
//        assertThat(scheduleLeft.get(0).getEndDate()).isEqualTo(LocalDateTime.of(now.getYear(), 1, 3, 23, 59, 59));
//
//        assertThat(scheduleRight.size()).isEqualTo(1);
//        assertThat(scheduleRight.get(0).getDesc()).isEqualTo("1시간");
//        assertThat(scheduleRight.get(0).getType()).isEqualTo(ScheduleType.ONETIMEOFF);
//        assertThat(scheduleRight.get(0).getStartDate()).isEqualTo(LocalDateTime.of(now.getYear(), 2, 2, 9, 0, 0));
//        assertThat(scheduleRight.get(0).getEndDate()).isEqualTo(LocalDateTime.of(now.getYear(), 2, 2, 10, 0, 0));
//
//        assertThat(schedule.size()).isEqualTo(0);
//    }
//
//    @Test
//    @DisplayName("사용자가 가진 휴가에 사용된 스케줄 조회")
//    void getScheduleByVacation() {
//        // given
//        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
//        em.persist(user);
//
//        LocalDateTime now = LocalDateTime.now();
//        Vacation vacation = Vacation.createVacation(user, "1분기 휴가", "", VacationType.BASIC, new BigDecimal("4.0000"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), 0L, "");
//        em.persist(vacation);
//
//        String[] descs = {"휴가", "1시간", "6시간", "오전반차"};
//        ScheduleType[] types = {ScheduleType.DAYOFF, ScheduleType.ONETIMEOFF, ScheduleType.SIXTIMEOFF, ScheduleType.MORNINGOFF};
//        LocalDateTime[] starts = {
//                LocalDateTime.of(now.getYear(), 1, 2, 0, 0, 0),
//                LocalDateTime.of(now.getYear(), 2, 2, 9, 0, 0),
//                LocalDateTime.of(now.getYear(), 3, 29, 11, 0, 0),
//                LocalDateTime.of(now.getYear(), 4, 15, 9, 0, 0),
//        };
//        LocalDateTime[] ends = {
//                LocalDateTime.of(now.getYear(), 1, 3, 23, 59, 59),
//                LocalDateTime.of(now.getYear(), 2, 2, 10, 0, 0),
//                LocalDateTime.of(now.getYear(), 3, 29, 18, 0, 0),
//                LocalDateTime.of(now.getYear(), 4, 15, 14, 0, 0),
//        };
//
//        for (int i = 0; i < descs.length; i++) {
//            Schedule schedule = Schedule.createSchedule(user, vacation, descs[i], types[i], starts[i], ends[i], 0L, "");
//            scheduleRepositoryImpl.save(schedule);
//        }
//
//        // when
//        List<Schedule> schedules = scheduleRepositoryImpl.findSchedulesByVacation(vacation);
//
//        // then
//        assertThat(schedules.size()).isEqualTo(4);
//        assertThat(schedules).extracting("desc").containsExactly(descs);
//        assertThat(schedules).extracting("type").containsExactly(types);
//        assertThat(schedules).extracting("startDate").containsExactly(starts);
//        assertThat(schedules).extracting("endDate").containsExactly(ends);
//    }
//}
