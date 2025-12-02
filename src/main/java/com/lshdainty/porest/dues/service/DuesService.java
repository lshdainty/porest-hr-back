package com.lshdainty.porest.dues.service;

import com.lshdainty.porest.common.message.MessageKey;
import com.lshdainty.porest.common.util.MessageResolver;
import com.lshdainty.porest.dues.domain.Dues;
import com.lshdainty.porest.dues.type.DuesCalcType;
import com.lshdainty.porest.dues.repository.DuesRepository;
import com.lshdainty.porest.dues.repository.dto.UsersMonthBirthDuesDto;
import com.lshdainty.porest.dues.service.dto.DuesServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final MessageResolver messageResolver;
    private final DuesRepository duesRepository;

    @Transactional
    public Long registDues(DuesServiceDto data) {
        log.debug("회비 등록 시작: userName={}, amount={}, type={}", data.getUserName(), data.getAmount(), data.getType());
        Dues dues = Dues.createDues(
                data.getUserName(),
                data.getAmount(),
                data.getType(),
                data.getCalc(),
                data.getDate(),
                data.getDetail()
        );
        duesRepository.save(dues);
        log.info("회비 등록 완료: duesSeq={}", dues.getSeq());
        return dues.getSeq();
    }

    public List<DuesServiceDto> searchDues() {
        log.debug("전체 회비 목록 조회");
        List<Dues> dues = duesRepository.findDues();

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

    public List<DuesServiceDto> searchYearDues(int year) {
        log.debug("연도별 회비 목록 조회: year={}", year);
        List<Dues> dues = duesRepository.findDuesByYear(year);

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

    public DuesServiceDto searchYearOperationDues(int year) {
        log.debug("연도별 운영 회비 조회: year={}", year);
        List<Dues> dues = duesRepository.findOperatingDuesByYear(year);
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

    public Long searchMonthBirthDues(int year, int month) {
        return duesRepository.findBirthDuesByYearAndMonth(year, month);
    }

    public List<DuesServiceDto> searchUsersMonthBirthDues(int year) {
        List<UsersMonthBirthDuesDto> repositoryDtos = duesRepository.findUsersMonthBirthDues(year);
        return repositoryDtos.stream()
                .map(d -> DuesServiceDto.builder()
                        .userName(d.getUserName())
                        .month(d.getMonth())
                        .amount(d.getAmount())
                        .detail(d.getDetail())
                        .build())
                .toList();
    }

    @Transactional
    public void editDues(DuesServiceDto data) {
        log.debug("회비 수정 시작: duesSeq={}", data.getSeq());
        Dues dues = checkDuesExist(data.getSeq());
        dues.updateDues(
                data.getUserName(),
                data.getAmount(),
                data.getType(),
                data.getCalc(),
                data.getDate(),
                data.getDetail()
        );
        log.info("회비 수정 완료: duesSeq={}", data.getSeq());
    }

    @Transactional
    public void deleteDues(Long duesSeq) {
        log.debug("회비 삭제 시작: duesSeq={}", duesSeq);
        Dues findDues = checkDuesExist(duesSeq);
        duesRepository.delete(findDues);
        log.info("회비 삭제 완료: duesSeq={}", duesSeq);
    }

    public Dues checkDuesExist(Long duesSeq) {
        Optional<Dues> dues = duesRepository.findById(duesSeq);
        dues.orElseThrow(() -> {
            log.warn("회비 조회 실패 - 존재하지 않는 회비: duesSeq={}", duesSeq);
            return new IllegalArgumentException(messageResolver.getMessage(MessageKey.NOT_FOUND_DUES));
        });
        return dues.get();
    }
}
