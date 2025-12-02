package com.lshdainty.porest.work.service;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.work.domain.WorkCode;
import com.lshdainty.porest.work.domain.WorkHistory;
import com.lshdainty.porest.work.repository.WorkCodeRepositoryImpl;
import com.lshdainty.porest.work.repository.WorkHistoryCustomRepositoryImpl;
import com.lshdainty.porest.work.repository.dto.WorkHistorySearchCondition;
import com.lshdainty.porest.work.service.dto.WorkCodeServiceDto;
import com.lshdainty.porest.work.service.dto.WorkHistoryServiceDto;
import com.lshdainty.porest.holiday.service.HolidayService;
import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.holiday.type.HolidayType;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.repository.UserRepositoryImpl;
import com.lshdainty.porest.vacation.domain.VacationUsage;
import com.lshdainty.porest.vacation.repository.VacationUsageCustomRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WorkHistoryService {
    private final MessageSource ms;
    private final WorkHistoryCustomRepositoryImpl workHistoryRepository;
    private final WorkCodeRepositoryImpl workCodeRepository;
    private final UserService userService;
    private final HolidayService holidayService;
    private final UserRepositoryImpl userRepository;
    private final VacationUsageCustomRepositoryImpl vacationUsageRepository;

    @Transactional
    public Long createWorkHistory(WorkHistoryServiceDto data) {
        User user = userService.checkUserExist(data.getUserId());
        WorkCode group = checkWorkCodeExist(data.getGroupCode());
        WorkCode part = checkWorkCodeExist(data.getPartCode());
        WorkCode classes = checkWorkCodeExist(data.getClassCode());

        WorkHistory workHistory = WorkHistory.createWorkHistory(
                data.getDate(),
                user,
                group,
                part,
                classes,
                data.getHours(),
                data.getContent());
        workHistoryRepository.save(workHistory);
        return workHistory.getSeq();
    }

    @Transactional
    public List<Long> createWorkHistories(List<WorkHistoryServiceDto> dataList) {
        List<WorkHistory> workHistories = dataList.stream().map(data -> {
            User user = userService.checkUserExist(data.getUserId());
            WorkCode group = checkWorkCodeExist(data.getGroupCode());
            WorkCode part = checkWorkCodeExist(data.getPartCode());
            WorkCode classes = checkWorkCodeExist(data.getClassCode());

            return WorkHistory.createWorkHistory(
                    data.getDate(),
                    user,
                    group,
                    part,
                    classes,
                    data.getHours(),
                    data.getContent());
        }).collect(Collectors.toList());

        workHistoryRepository.saveAll(workHistories);

        return workHistories.stream()
                .map(WorkHistory::getSeq)
                .collect(Collectors.toList());
    }

    public List<WorkHistoryServiceDto> findAllWorkHistories(WorkHistorySearchCondition condition) {
        List<WorkHistory> workHistories = workHistoryRepository.findAll(condition);

        return workHistories.stream()
                .map(w -> WorkHistoryServiceDto.builder()
                        .seq(w.getSeq())
                        .date(w.getDate())
                        .userId(w.getUser().getId())
                        .userName(w.getUser().getName())
                        .groupName(w.getGroup().getName())
                        .partName(w.getPart().getName())
                        .className(w.getDivision().getName())
                        .groupInfo(convertToWorkCodeDto(w.getGroup()))
                        .partInfo(convertToWorkCodeDto(w.getPart()))
                        .classInfo(convertToWorkCodeDto(w.getDivision()))
                        .hours(w.getHours())
                        .content(w.getContent())
                        .build())
                .collect(Collectors.toList());
    }

    public WorkHistoryServiceDto findWorkHistory(Long seq) {
        WorkHistory w = checkWorkHistoryExist(seq);

        return WorkHistoryServiceDto.builder()
                .seq(w.getSeq())
                .date(w.getDate())
                .userId(w.getUser().getId())
                .userName(w.getUser().getName())
                .groupName(w.getGroup().getName())
                .partName(w.getPart().getName())
                .className(w.getDivision().getName())
                .groupInfo(convertToWorkCodeDto(w.getGroup()))
                .partInfo(convertToWorkCodeDto(w.getPart()))
                .classInfo(convertToWorkCodeDto(w.getDivision()))
                .hours(w.getHours())
                .content(w.getContent())
                .build();
    }

    @Transactional
    public void updateWorkHistory(WorkHistoryServiceDto data) {
        WorkHistory workHistory = checkWorkHistoryExist(data.getSeq());
        User user = userService.checkUserExist(data.getUserId());
        WorkCode group = checkWorkCodeExist(data.getGroupCode());
        WorkCode part = checkWorkCodeExist(data.getPartCode());
        WorkCode classes = checkWorkCodeExist(data.getClassCode());

        workHistory.updateWorkHistory(
                data.getDate(),
                user,
                group,
                part,
                classes,
                data.getHours(),
                data.getContent());
    }

    @Transactional
    public void deleteWorkHistory(Long seq) {
        WorkHistory workHistory = checkWorkHistoryExist(seq);
        workHistoryRepository.delete(workHistory);
    }

    private WorkHistory checkWorkHistoryExist(Long seq) {
        Optional<WorkHistory> workHistory = workHistoryRepository.findById(seq);
        workHistory.orElseThrow(
                () -> new IllegalArgumentException(ms.getMessage("error.notfound.work.history", null, null)));
        return workHistory.get();
    }

    private WorkCode checkWorkCodeExist(String code) {
        if (code == null) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.work.code.required", null, null));
        }
        Optional<WorkCode> workCode = workCodeRepository.findByCode(code);
        workCode.orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.work.code", null, null)));
        return workCode.get();
    }

    private WorkCodeServiceDto convertToWorkCodeDto(WorkCode workCode) {
        if (workCode == null) {
            return null;
        }
        return WorkCodeServiceDto.builder()
                .seq(workCode.getSeq())
                .code(workCode.getCode())
                .name(workCode.getName())
                .type(workCode.getType())
                .orderSeq(workCode.getOrderSeq())
                .build();
    }

    @Transactional(readOnly = true)
    public void downloadWorkHistoryExcel(HttpServletResponse response, WorkHistorySearchCondition condition)
            throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) { // keep 100 rows in memory
            Sheet sheet = workbook.createSheet("업무 이력");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = { "No", "일자", "담당자", "업무분류", "업무파트", "업무구분", "소요시간", "내용" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Data Streaming
            AtomicInteger rowNum = new AtomicInteger(1);
            try (Stream<WorkHistory> workHistoryStream = workHistoryRepository.findAllStream(condition)) {
                workHistoryStream.forEach(workHistory -> {
                    Row row = sheet.createRow(rowNum.getAndIncrement());

                    // No
                    row.createCell(0).setCellValue(rowNum.get() - 1);

                    // 일자
                    row.createCell(1)
                            .setCellValue(workHistory.getDate() != null ? workHistory.getDate().toString() : "");

                    // 담당자
                    row.createCell(2)
                            .setCellValue(workHistory.getUser() != null ? workHistory.getUser().getName() : "");

                    // 업무분류 (Group)
                    row.createCell(3)
                            .setCellValue(workHistory.getGroup() != null ? workHistory.getGroup().getName() : "");

                    // 업무파트 (Part)
                    row.createCell(4)
                            .setCellValue(workHistory.getPart() != null ? workHistory.getPart().getName() : "");

                    // 업무구분 (Division)
                    row.createCell(5)
                            .setCellValue(workHistory.getDivision() != null ? workHistory.getDivision().getName() : "");

                    // 소요시간
                    row.createCell(6)
                            .setCellValue(workHistory.getHours() != null ? workHistory.getHours().toString() : "");

                    // 내용
                    row.createCell(7).setCellValue(workHistory.getContent() != null ? workHistory.getContent() : "");
                });
            }

            // Response Header Setting
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=work_history.xlsx");

            // Write to Output Stream
            workbook.write(response.getOutputStream());

            // Dispose of temporary files
            workbook.dispose();
        }
    }

    @Transactional(readOnly = true)
    public void downloadUnregisteredWorkHistoryExcel(HttpServletResponse response, Integer year, Integer month)
            throws IOException {
        // 년월 유효성 검증
        if (year == null || month == null) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.year.month.required", null, null));
        }

        // 해당 년월의 시작일과 마지막일 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 해당 기간의 공휴일 조회 (PUBLIC, SUBSTITUTE만 - ETC 제외)
        List<Holiday> holidays = holidayService.searchHolidaysByStartEndDate(startDate, endDate, null);
        Set<LocalDate> holidayDates = holidays.stream()
                .filter(h -> h.getType() == HolidayType.PUBLIC || h.getType() == HolidayType.SUBSTITUTE)
                .map(Holiday::getDate)
                .collect(Collectors.toSet());

        // 주말과 공휴일을 제외한 근무일 리스트 생성
        List<LocalDate> workingDays = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
                    && !holidayDates.contains(currentDate)) {
                workingDays.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        // 대상 유저 조회: 삭제되지 않은 유저 + 삭제됐지만 modifyDate가 해당 년월인 유저
        List<User> users = userRepository.findUsers(); // 삭제되지 않은 유저

        // 삭제됐지만 modifyDate가 해당 년월인 유저 추가
        List<User> deletedUsersInMonth = findDeletedUsersInYearMonth(year, month);
        users.addAll(deletedUsersInMonth);

        // 유저 ID 리스트 추출
        List<String> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // 벌크 조회: 모든 유저의 해당 기간 업무 시간 (단일 쿼리)
        Map<String, Map<LocalDate, BigDecimal>> workHoursMap = workHistoryRepository
                .findDailyWorkHoursByUsersAndPeriod(userIds, startDate, endDate);

        // 벌크 조회: 모든 유저의 해당 기간 휴가 사용 시간 (단일 쿼리)
        Map<String, Map<LocalDate, BigDecimal>> vacationUsedTimeMap = vacationUsageRepository
                .findDailyVacationHoursByUsersAndPeriod(userIds, startDate, endDate);

        // 유저 정보 Map 생성 (스트림 처리 시 사용)
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user, (existing, replacement) -> existing));

        BigDecimal requiredHours = new BigDecimal("8.0");
        BigDecimal vacationMultiplier = new BigDecimal("8.0");

        // 엑셀 파일 생성 (스트림 방식)
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("업무 이력 미등록 리스트");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = { "No", "유저 ID", "유저명", "일자", "업무 등록 시간", "휴가 사용 시간", "총 등록 시간", "미등록 시간" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 스트림을 사용하여 미등록 데이터 생성 및 엑셀 작성
            AtomicInteger rowNum = new AtomicInteger(1);
            userIds.stream()
                    .flatMap(userId -> workingDays.stream()
                            .map(workDate -> {
                                // 해당 유저의 해당 날짜 업무 시간 조회
                                BigDecimal workHours = workHoursMap
                                        .getOrDefault(userId, Collections.emptyMap())
                                        .getOrDefault(workDate, BigDecimal.ZERO);

                                // 해당 유저의 해당 날짜 휴가 사용 시간 조회 (usedTime * 8시간)
                                BigDecimal vacationUsedTime = vacationUsedTimeMap
                                        .getOrDefault(userId, Collections.emptyMap())
                                        .getOrDefault(workDate, BigDecimal.ZERO);
                                BigDecimal vacationHours = vacationUsedTime.multiply(vacationMultiplier);

                                // 총 시간 계산
                                BigDecimal totalHours = workHours.add(vacationHours);

                                // 8시간 미만인 경우에만 반환
                                if (totalHours.compareTo(requiredHours) < 0) {
                                    User user = userMap.get(userId);
                                    BigDecimal missingHours = requiredHours.subtract(totalHours);
                                    return new UnregisteredWorkData(
                                            userId,
                                            user != null ? user.getName() : "",
                                            workDate,
                                            workHours,
                                            vacationHours,
                                            missingHours
                                    );
                                }
                                return null;
                            }))
                    .filter(Objects::nonNull)
                    .forEach(data -> {
                        Row row = sheet.createRow(rowNum.getAndIncrement());
                        row.createCell(0).setCellValue(rowNum.get() - 1);
                        row.createCell(1).setCellValue(data.getUserId());
                        row.createCell(2).setCellValue(data.getUserName());
                        row.createCell(3).setCellValue(data.getWorkDate().toString());
                        row.createCell(4).setCellValue(data.getWorkHours().toString());
                        row.createCell(5).setCellValue(data.getVacationHours().toString());
                        row.createCell(6).setCellValue(data.getWorkHours().add(data.getVacationHours()).toString());
                        row.createCell(7).setCellValue(data.getMissingHours().toString());
                    });

            // Response Header Setting
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=unregistered_work_history_" + year + "_" + month + ".xlsx");

            // Write to Output Stream
            workbook.write(response.getOutputStream());

            // Dispose of temporary files
            workbook.dispose();
        }
    }

    /**
     * 삭제됐지만 modifyDate가 해당 년월인 유저 조회
     */
    private List<User> findDeletedUsersInYearMonth(Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth().plusDays(1); // 마지막 날 포함을 위해 +1일

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay();

        return userRepository.findDeletedUsersByModifyDateBetween(startDateTime, endDateTime);
    }

    /**
     * 미등록 업무 데이터 내부 클래스
     */
    private static class UnregisteredWorkData {
        private final String userId;
        private final String userName;
        private final LocalDate workDate;
        private final BigDecimal workHours;
        private final BigDecimal vacationHours;
        private final BigDecimal missingHours;

        public UnregisteredWorkData(String userId, String userName, LocalDate workDate,
                                   BigDecimal workHours, BigDecimal vacationHours, BigDecimal missingHours) {
            this.userId = userId;
            this.userName = userName;
            this.workDate = workDate;
            this.workHours = workHours;
            this.vacationHours = vacationHours;
            this.missingHours = missingHours;
        }

        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public LocalDate getWorkDate() { return workDate; }
        public BigDecimal getWorkHours() { return workHours; }
        public BigDecimal getVacationHours() { return vacationHours; }
        public BigDecimal getMissingHours() { return missingHours; }
    }

    /**
     * 오늘 날짜 기준 로그인한 사용자의 업무 시간 확인<br>
     * 8시간 이상 작성했는지 여부 반환
     *
     * @param userId 사용자 ID
     * @return 업무 시간 정보 (총 업무 시간, 8시간 달성 여부)
     */
    public TodayWorkStatus checkTodayWorkStatus(String userId) {
        LocalDate today = LocalDate.now();

        // 오늘 날짜의 업무 내역 리스트 조회
        List<WorkHistory> todayWorkHistories = workHistoryRepository.findByUserAndDate(userId, today);

        // 업무 시간 합계 계산
        BigDecimal totalHours = todayWorkHistories.stream()
                .map(WorkHistory::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal requiredHours = new BigDecimal("8.0");
        boolean isCompleted = totalHours.compareTo(requiredHours) >= 0;

        log.info("Today work status checked - userId: {}, date: {}, totalHours: {}, isCompleted: {}",
                userId, today, totalHours, isCompleted);

        return new TodayWorkStatus(totalHours, requiredHours, isCompleted);
    }

    /**
     * 로그인한 사용자의 특정 년/월 미작성 업무 날짜 목록 조회<br>
     * 주말, 공휴일, 휴가 시간을 제외하고 8시간 미만 작성한 날짜 반환
     *
     * @param userId 사용자 ID
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 미작성 업무 날짜 목록
     */
    public List<LocalDate> getUnregisteredWorkDates(String userId, Integer year, Integer month) {
        // 년월 유효성 검증
        if (year == null || month == null) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.year.month.required", null, null));
        }

        // 사용자 존재 확인
        User user = userService.checkUserExist(userId);

        // 해당 년월의 시작일과 마지막일 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 해당 기간의 공휴일 조회 (PUBLIC, SUBSTITUTE만 - ETC 제외)
        List<Holiday> holidays = holidayService.searchHolidaysByStartEndDate(startDate, endDate, CountryCode.KR);
        Set<LocalDate> holidayDates = holidays.stream()
                .filter(h -> h.getType() == HolidayType.PUBLIC || h.getType() == HolidayType.SUBSTITUTE)
                .map(Holiday::getDate)
                .collect(Collectors.toSet());

        // 주말과 공휴일을 제외한 근무일 리스트 생성
        List<LocalDate> workingDays = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
                    && !holidayDates.contains(currentDate)) {
                workingDays.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        // 기간 내 날짜별 업무 시간 합계 조회 (단일 쿼리)
        Map<LocalDate, BigDecimal> dailyWorkHoursMap = workHistoryRepository
                .findDailyWorkHoursByUserAndPeriod(user.getId(), startDate, endDate);

        // 기간 내 날짜별 휴가 사용 시간 합계 조회 (단일 쿼리)
        Map<LocalDate, BigDecimal> dailyVacationMap = vacationUsageRepository
                .findDailyVacationHoursByUserAndPeriod(user.getId(), startDate, endDate);

        // 미작성 날짜 필터링
        BigDecimal requiredHours = new BigDecimal("8.0");
        BigDecimal vacationMultiplier = new BigDecimal("8.0");

        List<LocalDate> unregisteredDates = workingDays.stream()
                .filter(workDate -> {
                    BigDecimal workHours = dailyWorkHoursMap.getOrDefault(workDate, BigDecimal.ZERO);
                    BigDecimal vacationUsedTime = dailyVacationMap.getOrDefault(workDate, BigDecimal.ZERO);
                    BigDecimal vacationHours = vacationUsedTime.multiply(vacationMultiplier);
                    BigDecimal totalHours = workHours.add(vacationHours);
                    return totalHours.compareTo(requiredHours) < 0;
                })
                .collect(Collectors.toList());

        log.info("Unregistered work dates fetched - userId: {}, year: {}, month: {}, count: {}",
                userId, year, month, unregisteredDates.size());

        return unregisteredDates;
    }

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
