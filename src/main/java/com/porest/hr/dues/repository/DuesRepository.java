package com.porest.hr.dues.repository;

import com.porest.hr.dues.domain.Dues;
import com.porest.hr.dues.repository.dto.UsersMonthBirthDuesDto;

import java.util.List;
import java.util.Optional;

/**
 * Dues Repository Interface
 */
public interface DuesRepository {
    /**
     * 신규 회비 저장
     *
     * @param dues 저장할 회비
     */
    void save(Dues dues);

    /**
     * 단건 회비 조회
     *
     * @param id 회비 ID
     * @return Optional&lt;Dues&gt;
     */
    Optional<Dues> findById(Long id);

    /**
     * 전체 회비 조회
     *
     * @return List&lt;Dues&gt;
     */
    List<Dues> findDues();

    /**
     * 년도에 해당하는 회비 조회
     *
     * @param year 조회 년도
     * @return List&lt;Dues&gt;
     */
    List<Dues> findDuesByYear(int year);

    /**
     * 해당년도 운영비 조회
     *
     * @param year 조회 년도
     * @return List&lt;Dues&gt;
     */
    List<Dues> findOperatingDuesByYear(int year);

    /**
     * 해당년도 해당월 생일비 합계 조회
     *
     * @param year 조회 년도
     * @param month 조회 월
     * @return 생일비 합계
     */
    Long findBirthDuesByYearAndMonth(int year, int month);

    /**
     * 해당년도 사용자 월별 생일비 입금내역 조회
     *
     * @param year 조회 년도
     * @return List&lt;UsersMonthBirthDuesDto&gt;
     */
    List<UsersMonthBirthDuesDto> findUsersMonthBirthDues(int year);

    /**
     * 회비 삭제
     *
     * @param dues 삭제할 회비
     */
    void delete(Dues dues);
}
