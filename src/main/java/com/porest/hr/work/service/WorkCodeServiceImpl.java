package com.porest.hr.work.service;

import com.lshdainty.porest.common.exception.DuplicateException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.InvalidValueException;
import com.porest.hr.work.domain.WorkCode;
import com.porest.hr.work.repository.WorkCodeRepository;
import com.porest.hr.work.service.dto.WorkCodeServiceDto;
import com.porest.hr.work.type.CodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WorkCodeServiceImpl implements WorkCodeService {
    private final WorkCodeRepository workCodeRepository;

    @Override
    public List<WorkCodeServiceDto> findWorkCodes(String parentWorkCode, Long parentWorkCodeId, Boolean parentIsNull, CodeType type) {
        List<WorkCode> workCodes = workCodeRepository.findAllByConditions(parentWorkCode, parentWorkCodeId, parentIsNull, type);
        return workCodes.stream()
                .map(wc -> WorkCodeServiceDto.builder()
                        .id(wc.getId())
                        .code(wc.getCode())
                        .name(wc.getName())
                        .type(wc.getType())
                        .orderSeq(wc.getOrderSeq())
                        .parentId(wc.getParent() != null ? wc.getParent().getId() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createWorkCode(String code, String name, CodeType type, Long parentId, Integer orderSeq) {
        // 부모 코드 조회 (parentId가 있는 경우)
        WorkCode parent = null;
        if (parentId != null) {
            parent = workCodeRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_CODE_NOT_FOUND));
        }

        // 코드 중복 체크
        workCodeRepository.findByCode(code).ifPresent(wc -> {
            throw new DuplicateException(ErrorCode.WORK_CODE_DUPLICATE);
        });

        // 업무 코드 생성
        WorkCode workCode = WorkCode.createWorkCode(code, name, type, parent, orderSeq);
        workCodeRepository.save(workCode);

        log.info("업무 코드 생성 완료: code={}, name={}, type={}, parentId={}, orderSeq={}",
                code, name, type, parentId, orderSeq);

        return workCode.getId();
    }

    @Override
    @Transactional
    public void updateWorkCode(Long id, String code, String name, Long parentId, Integer orderSeq) {
        // 업무 코드 조회
        WorkCode workCode = workCodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_CODE_NOT_FOUND));

        // 부모 코드 조회 (parentId가 있는 경우)
        WorkCode parent = null;
        if (parentId != null) {
            parent = workCodeRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_CODE_NOT_FOUND));

            // 자기 자신을 부모로 설정하는 것 방지
            if (id.equals(parentId)) {
                throw new InvalidValueException(ErrorCode.WORK_CODE_INVALID_PARENT);
            }
        }

        // 코드 중복 체크 (자신 제외)
        if (code != null) {
            workCodeRepository.findByCode(code).ifPresent(wc -> {
                if (!wc.getId().equals(id)) {
                    throw new DuplicateException(ErrorCode.WORK_CODE_DUPLICATE);
                }
            });
        }

        // 업무 코드 수정
        workCode.updateWorkCode(code, name, parent, orderSeq);

        log.info("업무 코드 수정 완료: id={}, code={}, name={}, parentId={}, orderSeq={}",
                id, code, name, parentId, orderSeq);
    }

    @Override
    @Transactional
    public void deleteWorkCode(Long id) {
        // 업무 코드 조회
        WorkCode workCode = workCodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_CODE_NOT_FOUND));

        // 업무 코드 삭제 (Soft Delete)
        workCode.deleteWorkCode();

        log.info("업무 코드 삭제 완료: id={}, code={}", id, workCode.getCode());
    }
}
