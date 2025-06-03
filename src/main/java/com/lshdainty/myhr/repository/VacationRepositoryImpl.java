package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.Vacation;
import com.lshdainty.myhr.domain.VacationType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VacationRepositoryImpl implements VacationRepository {
    private final EntityManager em;

    // 휴가 저장
    @Override
    public void save(Vacation vacation) {
        em.persist(vacation);
    }

    // 단건 휴가 조회(delete용)
    @Override
    public Optional<Vacation> findById(Long vacationId) {
        return Optional.ofNullable(em.find(Vacation.class, vacationId));
    }

    // 유저에 부여된 전체 휴가 조회
    @Override
    public List<Vacation> findVacationsByUserNo(Long userNo) {
        return em.createQuery("select v from Vacation v where v.user.id = :userNo", Vacation.class)
                .setParameter("userNo", userNo)
                .getResultList();
    }

    // 연도에 해당하는 휴가 조회
    @Override
    public List<Vacation> findVacationsByYear(String year) {
        return em.createQuery("select v from Vacation v where year(v.expiryDate) = :year", Vacation.class)
                .setParameter("year", year)
//                .setParameter("delYN", "N")
                .getResultList();
    }

    @Override
    public Optional<Vacation> findVacationByTypeWithYear(Long userNo, VacationType type, String year) {
        return em.createQuery("select v from Vacation v where v.user.id = :userNo and v.type = :type and year(v.expiryDate) = :year", Vacation.class)
                .setParameter("userNo", userNo)
                .setParameter("type", type)
                .setParameter("year", year)
                .getResultList()
                .stream()
                .findFirst();
    }

    // 유저가 가진 휴가 중 기준 시간을 포함하는 휴가 리스트 조회
    @Override
    public List<Vacation> findVacationsByParameterTime(Long userNo, LocalDateTime standardTime) {
        return em.createQuery("select v from Vacation v where v.user.id = :userNo and :standardTime between v.occurDate and v.expiryDate and delYN = :delYN", Vacation.class)
                .setParameter("userNo", userNo)
                .setParameter("standardTime", standardTime)
                .setParameter("delYN", "N")
                .getResultList();
    }

    // 유저가 가진 휴가 중 기준 시간을 포함하는 휴가와 스케줄 조회
    @Override
    public List<Vacation> findVacationsByParameterTimeWithSchedules(Long userNo, LocalDateTime standardTime) {
        return em.createQuery("select v from Vacation v left join fetch v.schedules s where v.user.id = :userNo and :standardTime between v.occurDate and v.expiryDate and v.delYN = :delYN", Vacation.class)
                .setParameter("userNo", userNo)
                .setParameter("standardTime", standardTime)
                .setParameter("delYN", "N")
                .getResultList();
    }
}
