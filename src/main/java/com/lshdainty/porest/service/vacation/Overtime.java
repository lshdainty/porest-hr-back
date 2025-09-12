package com.lshdainty.porest.service.vacation;

import com.lshdainty.porest.domain.User;
import com.lshdainty.porest.domain.Vacation;
import com.lshdainty.porest.domain.VacationHistory;
import com.lshdainty.porest.repository.HolidayRepositoryImpl;
import com.lshdainty.porest.repository.UserRepositoryImpl;
import com.lshdainty.porest.repository.VacationHistoryRepositoryImpl;
import com.lshdainty.porest.repository.VacationRepositoryImpl;
import com.lshdainty.porest.service.UserService;
import com.lshdainty.porest.service.VacationService;
import com.lshdainty.porest.service.dto.VacationServiceDto;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.util.Optional;

public class Overtime extends VacationService {
    MessageSource ms;
    VacationRepositoryImpl vacationRepositoryImpl;
    VacationHistoryRepositoryImpl vacationHistoryRepositoryImpl;
    UserRepositoryImpl userRepositoryImpl;
    HolidayRepositoryImpl holidayRepositoryImpl;
    UserService userService;

    public Overtime(
            MessageSource ms,
            VacationRepositoryImpl vacationRepositoryImpl,
            VacationHistoryRepositoryImpl vacationHistoryRepositoryImpl,
            UserRepositoryImpl userRepositoryImpl,
            HolidayRepositoryImpl holidayRepositoryImpl,
            UserService userService
    ) {
        super(ms, vacationRepositoryImpl, vacationHistoryRepositoryImpl, userRepositoryImpl, holidayRepositoryImpl, userService);
        this.ms = ms;
        this.vacationRepositoryImpl = vacationRepositoryImpl;
        this.vacationHistoryRepositoryImpl = vacationHistoryRepositoryImpl;
        this.userRepositoryImpl = userRepositoryImpl;
        this.holidayRepositoryImpl = holidayRepositoryImpl;
        this.userService = userService;
    }

    @Override
    public Long registVacation(VacationServiceDto data, String crtUserId, String clientIP) {
        User user = userService.checkUserExist(data.getUserId());

        Optional<Vacation> vacation = vacationRepositoryImpl.findVacationByTypeWithYear(data.getUserId(), data.getType(), String.valueOf(data.getOccurDate().getYear()));
        if (vacation.isPresent()) {
            vacation.get().addVacation(data.getGrantTime(), crtUserId, clientIP);
        } else {
            // 보상연차의 경우 당해년도 1월 1일부터 12월 31일로 고정 생성
            Vacation newVacation = Vacation.createVacation(
                    user,
                    data.getType(),
                    data.getGrantTime(),
                    LocalDateTime.of(data.getOccurDate().getYear(), 1, 1, 0, 0, 0),
                    LocalDateTime.of(data.getOccurDate().getYear(), 12, 31, 23, 59, 59),
                    crtUserId,
                    clientIP
            );
            vacationRepositoryImpl.save(newVacation);
            vacation = Optional.of(newVacation);
        }

        VacationHistory history = VacationHistory.createRegistVacationHistory(vacation.get(), data.getDesc(), data.getGrantTime(), crtUserId, clientIP);
        vacationHistoryRepositoryImpl.save(history);

        return vacation.get().getId();
    }
}
