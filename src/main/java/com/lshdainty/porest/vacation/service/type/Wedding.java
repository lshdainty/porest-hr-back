package com.lshdainty.porest.vacation.service.type;

import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.vacation.service.dto.VacationServiceDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Wedding implements VacationTypeStrategy {
    private final UserService userService;

    @Override
    public Long registVacation(VacationServiceDto data) {
        User user = userService.checkUserExist(data.getUserId());

//        Vacation vacation = Vacation.createVacation(
//                user,
//                data.getType(),
//                data.getGrantTime(),
//                data.getOccurDate(),
//                data.getExpiryDate()
//        );
//        vacationRepository.save(vacation);
//
//        VacationHistory history = VacationHistory.createRegistVacationHistory(vacation, data.getDesc(), data.getGrantTime());
//        vacationHistoryRepository.save(history);
//
//        return vacation.getId();
        return 0L;
    }
}
