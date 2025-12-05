package com.lshdainty.porest.work.repository;

import com.lshdainty.porest.work.domain.WorkCode;
import com.lshdainty.porest.work.type.CodeType;

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
     * Seq로 업무 코드 조회
     *
     * @param seq 업무 코드 Seq
     * @return Optional&lt;WorkCode&gt;
     */
    Optional<WorkCode> findBySeq(Long seq);

    /**
     * 동적 조건으로 업무 코드 목록 조회
     *
     * @param parentWorkCode 부모 업무 코드
     * @param parentWorkCodeSeq 부모 업무 코드 Seq
     * @param parentIsNull 부모 null 여부
     * @param type 코드 타입
     * @return List&lt;WorkCode&gt;
     */
    List<WorkCode> findAllByConditions(String parentWorkCode, Long parentWorkCodeSeq, Boolean parentIsNull, CodeType type);
}
