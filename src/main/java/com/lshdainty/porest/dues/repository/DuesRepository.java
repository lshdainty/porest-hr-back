package com.lshdainty.porest.dues.repository;

import com.lshdainty.porest.dues.domain.Dues;
import com.lshdainty.porest.dues.repository.dto.UsersMonthBirthDuesDto;

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
    List<Dues> findDuesByYear(int year);
    // 해당년도 운영비 조회
    List<Dues> findOperatingDuesByYear(int year);
    // 해당년도 해당월 생일비 합계 조회
    Long findBirthDuesByYearAndMonth(int year, int month);
    // 해당년도 사용자 월별 생일비 입금내역 조회
    List<UsersMonthBirthDuesDto> findUsersMonthBirthDues(int year);
    // 회비 삭제
    void delete(Dues dues);
}
