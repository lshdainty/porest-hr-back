package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.Dues;

import java.util.List;

public interface DuesCustomRepository {
    // 신규 회비 저장
    void save(Dues dues);
    // 단건 회비 조회(delete용)
    Dues findById(Long id);
    // 전체 회비 조회
    List<Dues> findDues();
    // 년도에 해당하는 회비 조회
    List<Dues> findDuesByYear(String year);
    // 회비 삭제
    void delete(Dues dues);
}
