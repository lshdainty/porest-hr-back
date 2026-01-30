package com.porest.hr.schedule.repository;

import com.lshdainty.porest.common.type.YNType;
import com.porest.hr.schedule.domain.Schedule;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository("scheduleJpaRepository")
@RequiredArgsConstructor
public class ScheduleJpaRepository implements ScheduleRepository {
    private final EntityManager em;

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
        return em.createQuery("select s from Schedule s where s.user.id = :userId and s.isDeleted = :isDeleted", Schedule.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public List<Schedule> findSchedulesByPeriod(LocalDateTime start, LocalDateTime end) {
        return em.createQuery("select s from Schedule s where s.startDate between :start and :end and s.isDeleted = :isDeleted", Schedule.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }
}
