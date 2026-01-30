package com.porest.hr.work.repository;

import com.porest.hr.work.domain.WorkCode;
import com.porest.hr.work.type.CodeType;

import java.util.List;
import java.util.Optional;

/**
 * WorkCode Repository Interface
 */
public interface WorkCodeRepository {
    /**
     * 업무 코드 저장
     *
     * @param workCode 저장할 업무 코드
     */
    void save(WorkCode workCode);

    /**
     * 코드로 업무 코드 조회
     *
     * @param code 업무 코드
     * @return Optional&lt;WorkCode&gt;
     */
    Optional<WorkCode> findByCode(String code);

    /**
     * Id로 업무 코드 조회
     *
     * @param id 업무 코드 Id
     * @return Optional&lt;WorkCode&gt;
     */
    Optional<WorkCode> findById(Long id);

    /**
     * 동적 조건으로 업무 코드 목록 조회
     *
     * @param parentWorkCode 부모 업무 코드
     * @param parentWorkCodeId 부모 업무 코드 Id
     * @param parentIsNull 부모 null 여부
     * @param type 코드 타입
     * @return List&lt;WorkCode&gt;
     */
    List<WorkCode> findAllByConditions(String parentWorkCode, Long parentWorkCodeId, Boolean parentIsNull, CodeType type);
}
