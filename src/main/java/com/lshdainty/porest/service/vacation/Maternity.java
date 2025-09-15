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

public class Maternity extends VacationService {
    VacationRepositoryImpl vacationRepository;
    VacationHistoryRepositoryImpl vacationHistoryRepository;
    UserService userService;

    public Maternity(
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

        Vacation vacation = Vacation.createVacation(
                user,
                data.getType(),
                data.getGrantTime(),
                data.getOccurDate(),
                data.getExpiryDate(),
                crtUserId,
                clientIP
        );
        vacationRepository.save(vacation);

        VacationHistory history = VacationHistory.createRegistVacationHistory(vacation, data.getDesc(), data.getGrantTime(), crtUserId, clientIP);
        vacationHistoryRepository.save(history);

        return vacation.getId();
    }
}
