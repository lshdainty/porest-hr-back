package com.lshdainty.porest.work.service;

import com.lshdainty.porest.work.domain.WorkCode;
import com.lshdainty.porest.work.repository.WorkCodeRepository;
import com.lshdainty.porest.work.service.dto.WorkCodeServiceDto;
import com.lshdainty.porest.work.type.CodeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkCodeService {
    private final WorkCodeRepository workCodeRepository;

    /**
     * 동적 조건으로 업무 코드 목록 조회
     *
     * @param parentWorkCode 부모 코드 문자열 (null 가능)
     * @param parentWorkCodeSeq 부모 코드 seq (null 가능)
     * @param parentIsNull 최상위 코드 조회 여부 (null 가능)
     * @param type 코드 타입 (null 가능)
     * @return 조건에 맞는 업무 코드 목록
     *
     * 사용 예시:
     * - findWorkCodes(null, null, true, CodeType.LABEL) → 최상위 LABEL 조회
     * - findWorkCodes("work_group", null, null, CodeType.OPTION) → work_group의 OPTION 하위 코드 조회
     * - findWorkCodes(null, 1L, null, CodeType.OPTION) → seq가 1인 부모의 OPTION 하위 코드 조회
     * - findWorkCodes("assignment", null, null, CodeType.LABEL) → assignment의 LABEL 하위 코드 조회
     * - findWorkCodes("work_part", null, null, CodeType.OPTION) → work_part의 OPTION 하위 코드 조회
     */
    public List<WorkCodeServiceDto> findWorkCodes(String parentWorkCode, Long parentWorkCodeSeq, Boolean parentIsNull, CodeType type) {
        List<WorkCode> workCodes = workCodeRepository.findAllByConditions(parentWorkCode, parentWorkCodeSeq, parentIsNull, type);
        return workCodes.stream()
                .map(wc -> WorkCodeServiceDto.builder()
                        .seq(wc.getSeq())
                        .code(wc.getCode())
                        .name(wc.getName())
                        .type(wc.getType())
                        .orderSeq(wc.getOrderSeq())
                        .parentSeq(wc.getParent() != null ? wc.getParent().getSeq() : null)
                        .build())
                .collect(Collectors.toList());
    }
}
