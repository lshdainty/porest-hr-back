package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.Dues;
import com.lshdainty.myhr.repository.dto.UserBirthDuesGroupMonthDto;

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
    // 해당년도 운영비 조회
    List<Dues> findOperatingDuesByYear(String year);
    // 해당년도 해당월 생일비 합계 조회
    Long findBirthDuesByYearAndMonth(String year, String month);
    // 해당년도 사용자 생일비 월별 입금 내역 조회
    List<UserBirthDuesGroupMonthDto> findBirthDuesByYearGroupByMonth(String year);
    // 회비 삭제
    void delete(Dues dues);
}
