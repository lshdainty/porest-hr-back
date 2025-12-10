package com.lshdainty.porest.dues.service;

import com.lshdainty.porest.dues.domain.Dues;
import com.lshdainty.porest.dues.service.dto.DuesServiceDto;

import java.util.List;

/**
 * 회비 관리 서비스
 * 회비 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
public interface DuesService {

    /**
     * 회비를 등록합니다.
     *
     * @param data 회비 등록 정보
     * @return 등록된 회비 ID
     */
    Long registDues(DuesServiceDto data);

    /**
     * 전체 회비 목록을 조회합니다.
     *
     * @return 회비 목록
     */
    List<DuesServiceDto> searchDues();

    /**
     * 특정 연도의 회비 목록을 조회합니다.
     *
     * @param year 조회할 연도
     * @return 연도별 회비 목록
     */
    List<DuesServiceDto> searchYearDues(int year);

    /**
     * 특정 연도의 운영 회비 통계를 조회합니다.
     *
     * @param year 조회할 연도
     * @return 운영 회비 통계 (총액, 입금액, 출금액)
     */
    DuesServiceDto searchYearOperationDues(int year);

    /**
     * 특정 연월의 생일 회비 총액을 조회합니다.
     *
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 생일 회비 총액
     */
    Long searchMonthBirthDues(int year, int month);

    /**
     * 특정 연도의 사용자별 월별 생일 회비를 조회합니다.
     *
     * @param year 조회할 연도
     * @return 사용자별 월별 생일 회비 목록
     */
    List<DuesServiceDto> searchUsersMonthBirthDues(int year);

    /**
     * 회비 정보를 수정합니다.
     *
     * @param data 수정할 회비 정보
     */
    void editDues(DuesServiceDto data);

    /**
     * 회비를 삭제합니다.
     *
     * @param duesId 삭제할 회비 ID
     */
    void deleteDues(Long duesId);

    /**
     * 회비 존재 여부를 확인하고 조회합니다.
     *
     * @param duesId 조회할 회비 ID
     * @return 조회된 회비 엔티티
     * @throws com.lshdainty.porest.common.exception.EntityNotFoundException 회비가 존재하지 않을 경우
     */
    Dues checkDuesExist(Long duesId);
}
