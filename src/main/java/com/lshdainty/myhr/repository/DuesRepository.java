package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.Dues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DuesRepository {
    // 신규 회비 저장
    void save(Dues dues);
    // 단건 회비 조회(delete용)
    Optional<Dues> findById(Long id);
    // 전체 회비 조회
    List<Dues> findDues();
    // 년도에 해당하는 회비 조회
    List<Dues> findDuesByYear(String year);
    // 회비 삭제
    void delete(Dues dues);
}
