package com.porest.hr.work.service;

import com.porest.hr.work.service.dto.WorkCodeServiceDto;
import com.porest.hr.work.type.CodeType;

import java.util.List;

/**
 * 업무 코드 서비스 인터페이스
 */
public interface WorkCodeService {

    /**
     * 동적 조건으로 업무 코드 목록 조회
     *
     * @param parentWorkCode 부모 코드 문자열 (null 가능)
     * @param parentWorkCodeId 부모 코드 id (null 가능)
     * @param parentIsNull 최상위 코드 조회 여부 (null 가능)
     * @param type 코드 타입 (null 가능)
     * @return 조건에 맞는 업무 코드 목록
     *
     * 사용 예시:
     * - findWorkCodes(null, null, true, CodeType.LABEL) → 최상위 LABEL 조회
     * - findWorkCodes("work_group", null, null, CodeType.OPTION) → work_group의 OPTION 하위 코드 조회
     * - findWorkCodes(null, 1L, null, CodeType.OPTION) → id가 1인 부모의 OPTION 하위 코드 조회
     * - findWorkCodes("assignment", null, null, CodeType.LABEL) → assignment의 LABEL 하위 코드 조회
     * - findWorkCodes("work_part", null, null, CodeType.OPTION) → work_part의 OPTION 하위 코드 조회
     */
    List<WorkCodeServiceDto> findWorkCodes(String parentWorkCode, Long parentWorkCodeId, Boolean parentIsNull, CodeType type);

    /**
     * 업무 코드 생성
     *
     * @param code 코드 값
     * @param name 코드명
     * @param type 코드 타입 (LABEL/OPTION)
     * @param parentId 부모 코드 id (null 가능)
     * @param orderSeq 정렬 순서
     * @return 생성된 업무 코드 id
     */
    Long createWorkCode(String code, String name, CodeType type, Long parentId, Integer orderSeq);

    /**
     * 업무 코드 수정
     *
     * @param id 수정할 업무 코드 id
     * @param code 코드 값
     * @param name 코드명
     * @param parentId 부모 코드 id (null 가능)
     * @param orderSeq 정렬 순서
     */
    void updateWorkCode(Long id, String code, String name, Long parentId, Integer orderSeq);

    /**
     * 업무 코드 삭제 (Soft Delete)
     *
     * @param id 삭제할 업무 코드 id
     */
    void deleteWorkCode(Long id);
}
