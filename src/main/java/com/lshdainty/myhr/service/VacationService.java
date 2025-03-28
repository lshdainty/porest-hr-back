package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.User;
import com.lshdainty.myhr.domain.Vacation;
import com.lshdainty.myhr.domain.VacationType;
import com.lshdainty.myhr.repository.UserRepositoryImpl;
import com.lshdainty.myhr.repository.VacationRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public Vacation checkVacationExist(Long vacationId) {
        Vacation findVacation = vacationRepositoryImpl.findById(vacationId);
        if (Objects.isNull(findVacation) || findVacation.getDelYN().equals("Y")) { throw new IllegalArgumentException(ms.getMessage("error.notfound.vacation", null, null)); }
        return findVacation;
    }
}
