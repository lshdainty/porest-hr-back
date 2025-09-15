package com.lshdainty.porest.service.vacation;

import com.lshdainty.porest.domain.User;
import com.lshdainty.porest.domain.Vacation;
import com.lshdainty.porest.domain.VacationHistory;
import com.lshdainty.porest.repository.*;
import com.lshdainty.porest.service.UserService;
import com.lshdainty.porest.service.VacationService;
import com.lshdainty.porest.service.dto.VacationServiceDto;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public class Annual extends VacationService {
    VacationRepositoryImpl vacationRepository;
    VacationHistoryRepositoryImpl vacationHistoryRepository;
    UserService userService;

    public Annual(
            VacationRepositoryImpl vacationRepository,
            VacationHistoryRepositoryImpl vacationHistoryRepository,
            UserService userService
    ) {
        super(null, vacationRepository, vacationHistoryRepository, null, null, null, userService);
        this.vacationRepository = vacationRepository;
        this.vacationHistoryRepository = vacationHistoryRepository;
        this.userService = userService;
    }

    @Override
    public Long registVacation(VacationServiceDto data, String crtUserId, String clientIP) {
        User user = userService.checkUserExist(data.getUserId());

        Optional<Vacation> vacation = vacationRepository.findVacationByTypeWithYear(data.getUserId(), data.getType(), String.valueOf(data.getOccurDate().getYear()));
        if (vacation.isPresent()) {
            vacation.get().addVacation(data.getGrantTime(), crtUserId, clientIP);
        } else {
            // 연차의 경우 당해년도 1월 1일부터 12월 31일로 고정 생성
            Vacation newVacation = Vacation.createVacation(
                    user,
                    data.getType(),
                    data.getGrantTime(),
                    LocalDateTime.of(data.getOccurDate().getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(data.getOccurDate().getYear(), 12, 31, 23, 59, 59),
                    crtUserId,
                    clientIP
            );
            vacationRepository.save(newVacation);
            vacation = Optional.of(newVacation);
        }

        VacationHistory history = VacationHistory.createRegistVacationHistory(vacation.get(), data.getDesc(), data.getGrantTime(), crtUserId, clientIP);
        vacationHistoryRepository.save(history);

        return vacation.get().getId();
    }
}
