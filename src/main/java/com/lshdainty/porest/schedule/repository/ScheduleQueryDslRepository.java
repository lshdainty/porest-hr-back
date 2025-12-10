package com.lshdainty.porest.schedule.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.schedule.domain.Schedule;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.lshdainty.porest.schedule.domain.QSchedule.schedule;

@Repository
@Primary
@RequiredArgsConstructor
public class ScheduleQueryDslRepository implements ScheduleRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(Schedule schedule) {
        em.persist(schedule);
    }

    @Override
    public Optional<Schedule> findById(Long scheduleId) {
        return Optional.ofNullable(em.find(Schedule.class, scheduleId));
    }

    @Override
    public List<Schedule> findSchedulesByUserId(String userId) {
        return query
                .selectFrom(schedule)
                .where(schedule.user.id.eq(userId)
                        .and(schedule.isDeleted.eq(YNType.N)))
                .fetch();
    }

    @Override
    public List<Schedule> findSchedulesByPeriod(LocalDateTime start, LocalDateTime end) {
        return query
                .selectFrom(schedule)
                .where(schedule.startDate.between(start, end)
                        .and(schedule.isDeleted.eq(YNType.N)))
                .fetch();
    }
}
