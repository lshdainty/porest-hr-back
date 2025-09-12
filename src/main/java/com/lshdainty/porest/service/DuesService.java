package com.lshdainty.porest.service;

import com.lshdainty.porest.domain.Dues;
import com.lshdainty.porest.type.DuesCalcType;
import com.lshdainty.porest.repository.DuesRepositoryImpl;
import com.lshdainty.porest.repository.dto.UsersMonthBirthDuesDto;
import com.lshdainty.porest.service.dto.DuesServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DuesService {
    private final MessageSource ms;
    private final DuesRepositoryImpl duesRepositoryImpl;

    @Transactional
    public Long save(DuesServiceDto data) {
        Dues dues = Dues.createDues(
                data.getUserName(),
                data.getAmount(),
                data.getType(),
                data.getCalc(),
                data.getDate(),
                data.getDetail()
        );
        duesRepositoryImpl.save(dues);
        return dues.getSeq();
    }

    public List<DuesServiceDto> findDues() {
        List<Dues> dues = duesRepositoryImpl.findDues();

        List<DuesServiceDto> dtos = dues.stream()
                .map(d -> DuesServiceDto.builder()
                        .seq(d.getSeq())
                        .userName(d.getUserName())
                        .amount(d.getAmount())
                        .type(d.getType())
                        .calc(d.getCalc())
                        .date(d.getDate())
                        .detail(d.getDetail())
                        .build())
                .collect(Collectors.toList());

        Long total = 0L;
        for (DuesServiceDto dto : dtos) {
            dto.setTotalDues(total = dto.getCalc().applyAsType(total, dto.getAmount()));
        }

        return dtos;
    }

    public List<DuesServiceDto> findDuesByYear(String year) {
        List<Dues> dues = duesRepositoryImpl.findDuesByYear(year);

        List<DuesServiceDto> dtos = dues.stream()
                .map(d -> DuesServiceDto.builder()
                        .seq(d.getSeq())
                        .userName(d.getUserName())
                        .amount(d.getAmount())
                        .type(d.getType())
                        .calc(d.getCalc())
                        .date(d.getDate())
                        .detail(d.getDetail())
                        .build())
                .collect(Collectors.toList());

        Long total = 0L;
        for (DuesServiceDto dto : dtos) {
            dto.setTotalDues(total = dto.getCalc().applyAsType(total, dto.getAmount()));
        }

        return dtos;
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
                withdraw = DuesCalcType.PLUS.applyAsType(withdraw, due.getAmount());
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
    public void editDues(DuesServiceDto data) {
        Dues dues = checkDuesExist(data.getSeq());
        dues.updateDues(
                data.getUserName(),
                data.getAmount(),
                data.getType(),
                data.getCalc(),
                data.getDate(),
                data.getDetail()
        );
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
