package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.User;
import com.lshdainty.myhr.domain.Vacation;
import com.lshdainty.myhr.domain.VacationType;
import com.lshdainty.myhr.repository.UserRepositoryImpl;
import com.lshdainty.myhr.repository.VacationRepositoryImpl;
import com.lshdainty.myhr.service.dto.ScheduleServiceDto;
import com.lshdainty.myhr.service.dto.VacationServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VacationService {
    private final MessageSource ms;
    private final VacationRepositoryImpl vacationRepositoryImpl;
    private final UserRepositoryImpl userRepositoryImpl;
    private final UserService userService;
    private final ScheduleService scheduleService;

    @Transactional
    public Long addVacation(Long userNo, String name, String desc, VacationType type, BigDecimal grantTime, LocalDateTime occurDate, LocalDateTime expiryDate, Long addUserNo, String clientIP) {
        User user = userService.checkUserExist(userNo);

        Vacation vacation = Vacation.createVacation(user, name, desc, type, grantTime, occurDate, expiryDate, addUserNo, clientIP);

        if (vacation.isBeforeOccur()) { throw new IllegalArgumentException("the expiration date is earlier than the occurrence date"); }

        vacationRepositoryImpl.save(vacation);

        return vacation.getId();
    }

    public List<Vacation> findVacationsByUser(Long userNo) {
        return vacationRepositoryImpl.findVacationsByUserNo(userNo);
    }

    public List<User> findVacationsByUserGroup() {
        return userRepositoryImpl.findUsersWithVacations();
    }

    @Transactional
    public Vacation editVacation(Long vacationId, Long userNo, String reqName, String reqDesc, VacationType reqType, BigDecimal reqGrantTime, LocalDateTime reqOccurDate, LocalDateTime reqExpiryDate, Long addUserNo, String clientIP) {
        Vacation findVacation = checkVacationExist(vacationId);

        String name = "";
        if (Objects.isNull(reqName)) { name = findVacation.getName(); } else { name = reqName; }

        String desc = "";
        if (Objects.isNull(reqDesc)) { desc = findVacation.getDesc(); } else { desc = reqDesc; }

        VacationType type = null;
        if (Objects.isNull(reqType)) { type = findVacation.getType(); } else { type = reqType; }

        BigDecimal grantTime = new BigDecimal(0);
        if (reqGrantTime.compareTo(grantTime) == 0) { grantTime = findVacation.getGrantTime(); } else { grantTime = reqGrantTime; }

        LocalDateTime occurDate = null;
        if (Objects.isNull(reqOccurDate)) { occurDate = findVacation.getOccurDate(); } else { occurDate = reqOccurDate; }

        LocalDateTime expiryDate = null;
        if (Objects.isNull(reqExpiryDate)) { expiryDate = findVacation.getExpiryDate(); } else { expiryDate = reqExpiryDate; }

        User user = userService.checkUserExist(userNo);

        Vacation newVacation = Vacation.createVacation(user, name, desc, type, grantTime, occurDate, expiryDate, addUserNo, clientIP);

        if (newVacation.isBeforeOccur()) { throw new IllegalArgumentException("the expiration date is earlier than the occurrence date"); }

        findVacation.deleteVacation(addUserNo, clientIP);
        vacationRepositoryImpl.save(newVacation);

        return vacationRepositoryImpl.findById(newVacation.getId());
    }

    @Transactional
    public void deleteVacation(Long vacationId, Long delUserNo, String clientIP) {
        Vacation vacation = checkVacationExist(vacationId);
        vacation.deleteVacation(delUserNo, clientIP);
    }

    public List<VacationServiceDto> checkPossibleVacations(Long userNo, LocalDateTime standardTime) {
        // 유저 조회
        userService.checkUserExist(userNo);

        // 시작 날짜를 기준으로 등록 가능한 휴가날짜를 조회
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByParameterTimeWithSchedules(userNo, standardTime);

        List<VacationServiceDto> vacationDtos = new ArrayList<>();
        for (Vacation vacation : vacations) {
            VacationServiceDto vacationDto = VacationServiceDto.builder()
                    .id(vacation.getId())
                    .user(vacation.getUser())
                    .schedules(vacation.getSchedules())
                    .scheduleDtos(new ArrayList<>())
                    .name(vacation.getName())
                    .desc(vacation.getDesc())
                    .type(vacation.getType())
                    .grantTime(vacation.getGrantTime())
                    .usedTime(new BigDecimal("0.0000"))
                    .remainTime(new BigDecimal("0.0000"))
                    .occurDate(vacation.getOccurDate())
                    .expiryDate(vacation.getExpiryDate())
                    .delYN(vacation.getDelYN())
                    .build();

            if (vacation.getSchedules().isEmpty()) {
                vacationDtos.add(vacationDto);
                continue;
            }

            List<ScheduleServiceDto> scheduleDtos = scheduleService.convertRealUsedTimeDto(vacation.getSchedules());
            BigDecimal usedTime = new BigDecimal("0.0000");
            for (ScheduleServiceDto scheduleDto : scheduleDtos) {
                usedTime = usedTime.add(scheduleDto.getRealUsedTime());
            }

            if (vacation.getGrantTime().compareTo(usedTime) == 0) { continue; }

            vacationDto.setScheduleDtos(scheduleDtos);
            vacationDto.setUsedTime(usedTime);
            vacationDto.setRemainTime(vacation.getGrantTime().subtract(usedTime));

            vacationDtos.add(vacationDto);
        }

        return vacationDtos;
    }

    public Vacation checkVacationExist(Long vacationId) {
        Vacation findVacation = vacationRepositoryImpl.findById(vacationId);
        if (Objects.isNull(findVacation) || findVacation.getDelYN().equals("Y")) { throw new IllegalArgumentException(ms.getMessage("error.notfound.vacation", null, null)); }
        return findVacation;
    }
}
