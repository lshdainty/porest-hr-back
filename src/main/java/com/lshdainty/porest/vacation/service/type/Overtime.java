package com.lshdainty.porest.vacation.service.type;

import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.vacation.service.dto.VacationServiceDto;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
public class Overtime implements VacationTypeStrategy {
    private final UserService userService;

    @Override
    public Long registVacation(VacationServiceDto data) {
        User user = userService.checkUserExist(data.getUserId());

//        Optional<Vacation> vacation = vacationRepository.findVacationByTypeWithYear(data.getUserId(), data.getType(), String.valueOf(data.getOccurDate().getYear()));
//        if (vacation.isPresent()) {
//            vacation.get().addVacation(data.getGrantTime());
//        } else {
//            // 보상연차의 경우 당해년도 1월 1일부터 12월 31일로 고정 생성
//            Vacation newVacation = Vacation.createVacation(
//                    user,
//                    data.getType(),
//                    data.getGrantTime(),
//                    LocalDateTime.of(data.getOccurDate().getYear(), 1, 1, 0, 0, 0),
//                    LocalDateTime.of(data.getOccurDate().getYear(), 12, 31, 23, 59, 59)
//            );
//            vacationRepository.save(newVacation);
//            vacation = Optional.of(newVacation);
//        }
//
//        VacationHistory history = VacationHistory.createRegistVacationHistory(vacation.get(), data.getDesc(), data.getGrantTime());
//        vacationHistoryRepository.save(history);
//
//        return vacation.get().getId();
        return 0L;
    }
}
