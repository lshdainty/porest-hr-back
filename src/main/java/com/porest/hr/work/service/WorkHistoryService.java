package com.porest.hr.work.service;

import com.porest.hr.work.repository.dto.WorkHistorySearchCondition;
import com.porest.hr.work.service.dto.WorkHistoryServiceDto;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 업무 이력 서비스 인터페이스
 */
public interface WorkHistoryService {

    /**
     * 업무 이력 생성
     *
     * @param data 업무 이력 데이터
     * @return 생성된 업무 이력 id
     */
    Long createWorkHistory(WorkHistoryServiceDto data);

    /**
     * 업무 이력 일괄 생성
     *
     * @param dataList 업무 이력 데이터 리스트
     * @return 생성된 업무 이력 id 리스트
     */
    List<Long> createWorkHistories(List<WorkHistoryServiceDto> dataList);

    /**
     * 업무 이력 목록 조회
     *
     * @param condition 조회 조건
     * @return 업무 이력 목록
     */
    List<WorkHistoryServiceDto> findAllWorkHistories(WorkHistorySearchCondition condition);

    /**
     * 업무 이력 단건 조회
     *
     * @param id 업무 이력 id
     * @return 업무 이력 상세 정보
     */
    WorkHistoryServiceDto findWorkHistory(Long id);

    /**
     * 업무 이력 수정
     *
     * @param data 업무 이력 수정 데이터
     */
    void updateWorkHistory(WorkHistoryServiceDto data);

    /**
     * 업무 이력 삭제
     *
     * @param id 업무 이력 id
     */
    void deleteWorkHistory(Long id);

    /**
     * 업무 이력 엑셀 다운로드
     *
     * @param response HTTP 응답 객체
     * @param condition 조회 조건
     * @throws IOException 파일 작성 중 오류 발생 시
     */
    void downloadWorkHistoryExcel(HttpServletResponse response, WorkHistorySearchCondition condition)
            throws IOException;

    /**
     * 업무 미등록 리스트 엑셀 다운로드
     *
     * @param response HTTP 응답 객체
     * @param year 조회할 연도
     * @param month 조회할 월
     * @throws IOException 파일 작성 중 오류 발생 시
     */
    void downloadUnregisteredWorkHistoryExcel(HttpServletResponse response, Integer year, Integer month)
            throws IOException;

    /**
     * 오늘 날짜 기준 로그인한 사용자의 업무 시간 확인<br>
     * 8시간 이상 작성했는지 여부 반환
     *
     * @param userId 사용자 ID
     * @return 업무 시간 정보 (총 업무 시간, 8시간 달성 여부)
     */
    TodayWorkStatus checkTodayWorkStatus(String userId);

    /**
     * 로그인한 사용자의 특정 년/월 미작성 업무 날짜 목록 조회<br>
     * 주말, 공휴일, 휴가 시간을 제외하고 8시간 미만 작성한 날짜 반환
     *
     * @param userId 사용자 ID
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 미작성 업무 날짜 목록
     */
    List<LocalDate> getUnregisteredWorkDates(String userId, Integer year, Integer month);

    /**
     * 오늘 업무 시간 상태 DTO
     */
    public static class TodayWorkStatus {
        private final BigDecimal totalHours;
        private final BigDecimal requiredHours;
        private final boolean isCompleted;

        public TodayWorkStatus(BigDecimal totalHours, BigDecimal requiredHours, boolean isCompleted) {
            this.totalHours = totalHours;
            this.requiredHours = requiredHours;
            this.isCompleted = isCompleted;
        }

        public BigDecimal getTotalHours() { return totalHours; }
        public BigDecimal getRequiredHours() { return requiredHours; }
        public boolean isCompleted() { return isCompleted; }
    }
}
