package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.Dues;
import com.lshdainty.myhr.domain.DuesCalcType;
import com.lshdainty.myhr.domain.DuesType;
import com.lshdainty.myhr.repository.DuesRepositoryImpl;
import com.lshdainty.myhr.repository.dto.UsersMonthBirthDuesDto;
import com.lshdainty.myhr.service.dto.DuesServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DuesService {
    private final MessageSource ms;
    private final DuesRepositoryImpl duesRepositoryImpl;

    @Transactional
    public Long save(String userName, Long amount, DuesType type, DuesCalcType calc, String date, String detail) {
        Dues dues = Dues.createDues(userName, amount, type, calc, date, detail);
        duesRepositoryImpl.save(dues);
        return dues.getSeq();
    }

    public List<Dues> findDues() {
        return duesRepositoryImpl.findDues();
    }

    public List<Dues> findDuesByYear(String year) {
        return duesRepositoryImpl.findDuesByYear(year);
    }

    public DuesServiceDto findOperatingDuesByYear(String year) {
        List<Dues> dues = duesRepositoryImpl.findOperatingDuesByYear(year);
        Long total = 0L;
        Long deposit = 0L;
        Long withdraw = 0L;
        for (Dues due : dues) {
            total = due.getCalc().applyAsType(total, due.getAmount());
            if (due.getCalc().equals(DuesCalcType.PLUS)) {
                deposit = due.getCalc().applyAsType(deposit, due.getAmount());
            } else {
                withdraw = due.getCalc().applyAsType(withdraw, due.getAmount());
            }
        }

        return DuesServiceDto.builder()
                .totalDues(total)
                .totalDeposit(deposit)
                .totalWithdrawal(withdraw)
                .build();
    }

    public Long findBirthDuesByYearAndMonth(String year, String month) {
        return duesRepositoryImpl.findBirthDuesByYearAndMonth(year, month);
    }

    public List<DuesServiceDto> findUsersMonthBirthDues(String year) {
        List<UsersMonthBirthDuesDto> repositoryDtos = duesRepositoryImpl.findUsersMonthBirthDues(year);
        return repositoryDtos.stream()
                .map(d -> DuesServiceDto.builder()
                        .userName(d.getUserName())
                        .date(d.getMonth())
                        .amount(d.getAmount())
                        .detail(d.getDetail())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteDues(Long duesSeq) {
        Dues findDues = checkDuesExist(duesSeq);
        duesRepositoryImpl.delete(findDues);
    }

    public Dues checkDuesExist(Long duesSeq) {
        Optional<Dues> dues = duesRepositoryImpl.findById(duesSeq);
        dues.orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.dues", null, null)));
        return dues.get();
    }
}
