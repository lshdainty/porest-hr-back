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

public class Bereavement extends VacationService {
    MessageSource ms;
    VacationRepositoryImpl vacationRepositoryImpl;
    VacationHistoryRepositoryImpl vacationHistoryRepositoryImpl;
    UserRepositoryImpl userRepositoryImpl;
    HolidayRepositoryImpl holidayRepositoryImpl;
    UserService userService;

    public Bereavement(
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

        Vacation vacation = Vacation.createVacation(
                user,
                data.getType(),
                data.getGrantTime(),
                data.getOccurDate(),
                data.getExpiryDate(),
                crtUserId,
                clientIP
        );
        vacationRepositoryImpl.save(vacation);

        VacationHistory history = VacationHistory.createRegistVacationHistory(vacation, data.getDesc(), data.getGrantTime(), crtUserId, clientIP);
        vacationHistoryRepositoryImpl.save(history);

        return vacation.getId();
    }
}
