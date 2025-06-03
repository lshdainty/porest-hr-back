package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.Vacation;
import com.lshdainty.myhr.domain.VacationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VacationRepository {
    void save(Vacation vacation);
    Optional<Vacation> findById(Long vacationId);
    List<Vacation> findVacationsByUserNo(Long userNo);
    List<Vacation> findVacationsByYear(String year);
    Optional<Vacation> findVacationByTypeWithYear(Long userNo, VacationType type, String year);
    List<Vacation> findVacationsByParameterTime(Long userNo, LocalDateTime standardTime);
    List<Vacation> findVacationsByParameterTimeWithSchedules(Long userNo, LocalDateTime standardTime);
}
