package com.lshdainty.porest.work.repository;

import com.lshdainty.porest.work.domain.WorkHistory;
import com.lshdainty.porest.work.repository.dto.WorkHistorySearchCondition;

import java.util.List;
import java.util.Optional;

public interface WorkHistoryCustomRepository {
    // 신규 업무 이력 저장
    void save(WorkHistory workHistory);

    // 신규 업무 이력 다건 저장
    void saveAll(List<WorkHistory> workHistories);

    // 단건 업무 이력 조회
    Optional<WorkHistory> findById(Long id);

    // 전체 업무 이력 조회
    List<WorkHistory> findAll(WorkHistorySearchCondition condition);

    // 업무 이력 삭제 (Soft Delete)
    // 업무 이력 삭제 (Soft Delete)
    void delete(WorkHistory workHistory);

    // 전체 업무 이력 스트림 조회
    java.util.stream.Stream<WorkHistory> findAllStream(WorkHistorySearchCondition condition);
}
