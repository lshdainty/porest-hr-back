package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.Schedule;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepository {
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
        return em.createQuery("select s from Schedule s where s.user.id = :userId and s.delYN = :delYN", Schedule.class)
                .setParameter("userId", userId)
                .setParameter("delYN", "N")
                .getResultList();
    }

    @Override
    public List<Schedule> findSchedulesByPeriod(LocalDateTime start, LocalDateTime end) {
        return em.createQuery("select s from Schedule s where s.startDate between :start and :end and s.delYN = :delYN", Schedule.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("delYN", "N")
                .getResultList();
    }
}
