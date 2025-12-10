package com.lshdainty.porest.work.service;

import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.InvalidValueException;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.holiday.service.HolidayService;
import com.lshdainty.porest.holiday.type.HolidayType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.repository.UserRepository;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.vacation.domain.VacationUsage;
import com.lshdainty.porest.vacation.repository.VacationUsageRepository;
import com.lshdainty.porest.work.domain.WorkCode;
import com.lshdainty.porest.work.domain.WorkHistory;
import com.lshdainty.porest.work.repository.WorkCodeRepository;
import com.lshdainty.porest.work.repository.WorkHistoryRepository;
import com.lshdainty.porest.work.repository.dto.WorkHistorySearchCondition;
import com.lshdainty.porest.work.service.dto.WorkCodeServiceDto;
import com.lshdainty.porest.work.service.dto.WorkHistoryServiceDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WorkHistoryServiceImpl implements WorkHistoryService {
    private final WorkHistoryRepository workHistoryRepository;
    private final WorkCodeRepository workCodeRepository;
    private final UserService userService;
    private final HolidayService holidayService;
    private final UserRepository userRepository;
    private final VacationUsageRepository vacationUsageRepository;

    @Override
    @Transactional
    public Long createWorkHistory(WorkHistoryServiceDto data) {
        log.debug("업무 이력 생성 시작: userId={}, date={}", data.getUserId(), data.getDate());
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
        log.info("업무 이력 생성 완료: id={}, userId={}, hours={}", workHistory.getId(), data.getUserId(), data.getHours());
        return workHistory.getId();
    }

    @Override
    @Transactional
    public List<Long> createWorkHistories(List<WorkHistoryServiceDto> dataList) {
        log.debug("업무 이력 일괄 생성 시작: count={}", dataList.size());
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
        log.info("업무 이력 일괄 생성 완료: count={}", workHistories.size());

        return workHistories.stream()
                .map(WorkHistory::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkHistoryServiceDto> findAllWorkHistories(WorkHistorySearchCondition condition) {
        log.debug("업무 이력 목록 조회: condition={}", condition);
        List<WorkHistory> workHistories = workHistoryRepository.findAll(condition);

        return workHistories.stream()
                .map(w -> WorkHistoryServiceDto.builder()
                        .id(w.getId())
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

    @Override
    public WorkHistoryServiceDto findWorkHistory(Long id) {
        log.debug("업무 이력 조회: id={}", id);
        WorkHistory w = checkWorkHistoryExist(id);

        return WorkHistoryServiceDto.builder()
                .id(w.getId())
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

    @Override
    @Transactional
    public void updateWorkHistory(WorkHistoryServiceDto data) {
        log.debug("업무 이력 수정 시작: id={}", data.getId());
        WorkHistory workHistory = checkWorkHistoryExist(data.getId());
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
        log.info("업무 이력 수정 완료: id={}", data.getId());
    }

    @Override
    @Transactional
    public void deleteWorkHistory(Long id) {
        log.debug("업무 이력 삭제 시작: id={}", id);
        WorkHistory workHistory = checkWorkHistoryExist(id);
        workHistoryRepository.delete(workHistory);
        log.info("업무 이력 삭제 완료: id={}", id);
    }

    private WorkHistory checkWorkHistoryExist(Long id) {
        Optional<WorkHistory> workHistory = workHistoryRepository.findById(id);
        workHistory.orElseThrow(() -> {
            log.warn("업무 이력 조회 실패 - 존재하지 않는 이력: id={}", id);
            return new EntityNotFoundException(ErrorCode.WORK_NOT_FOUND);
        });
        return workHistory.get();
    }

    private WorkCode checkWorkCodeExist(String code) {
        if (code == null) {
            log.warn("업무 코드 검증 실패 - 코드 미입력");
            throw new InvalidValueException(ErrorCode.WORK_CODE_REQUIRED);
        }
        Optional<WorkCode> workCode = workCodeRepository.findByCode(code);
        workCode.orElseThrow(() -> {
            log.warn("업무 코드 조회 실패 - 존재하지 않는 코드: code={}", code);
            return new EntityNotFoundException(ErrorCode.WORK_CODE_NOT_FOUND);
        });
        return workCode.get();
    }

    private WorkCodeServiceDto convertToWorkCodeDto(WorkCode workCode) {
        if (workCode == null) {
            return null;
        }
        return WorkCodeServiceDto.builder()
                .id(workCode.getId())
                .code(workCode.getCode())
                .name(workCode.getName())
                .type(workCode.getType())
                .orderSeq(workCode.getOrderSeq())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public void downloadWorkHistoryExcel(HttpServletResponse response, WorkHistorySearchCondition condition)
            throws IOException {
        log.debug("업무 이력 엑셀 다운로드 시작: condition={}", condition);
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
            log.info("업무 이력 엑셀 다운로드 완료");

            // Dispose of temporary files
            workbook.dispose();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void downloadUnregisteredWorkHistoryExcel(HttpServletResponse response, Integer year, Integer month)
            throws IOException {
        log.debug("업무 미등록 리스트 엑셀 다운로드 시작: year={}, month={}", year, month);
        // 년월 유효성 검증
        if (year == null || month == null) {
            log.warn("업무 미등록 리스트 다운로드 실패 - 년월 미입력");
            throw new InvalidValueException(ErrorCode.WORK_YEAR_MONTH_REQUIRED);
        }

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
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        Map<String, Map<LocalDate, BigDecimal>> vacationUsedTimeMap = aggregateVacationUsageByUsersAndDate(
                vacationUsageRepository.findByUserIdsAndPeriodForDaily(userIds, startDateTime, endDateTime)
        );

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
            log.info("업무 미등록 리스트 엑셀 다운로드 완료: year={}, month={}", year, month);

            // Dispose of temporary files
            workbook.dispose();
        }
    }

    private List<User> findDeletedUsersInYearMonth(Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth().plusDays(1); // 마지막 날 포함을 위해 +1일

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay();

        return userRepository.findDeletedUsersByModifyDateBetween(startDateTime, endDateTime);
    }

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

    @Override
    public TodayWorkStatus checkTodayWorkStatus(String userId) {
        log.debug("오늘 업무 상태 확인: userId={}", userId);
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

    @Override
    public List<LocalDate> getUnregisteredWorkDates(String userId, Integer year, Integer month) {
        log.debug("업무 미등록 날짜 조회 시작: userId={}, year={}, month={}", userId, year, month);
        // 년월 유효성 검증
        if (year == null || month == null) {
            log.warn("업무 미등록 날짜 조회 실패 - 년월 미입력");
            throw new InvalidValueException(ErrorCode.WORK_YEAR_MONTH_REQUIRED);
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
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        Map<LocalDate, BigDecimal> dailyVacationMap = aggregateVacationUsageByDate(
                vacationUsageRepository.findByUserIdAndPeriodForDaily(user.getId(), startDateTime, endDateTime)
        );

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

    private Map<LocalDate, BigDecimal> aggregateVacationUsageByDate(List<VacationUsage> usages) {
        Map<LocalDate, BigDecimal> dailyHoursMap = new HashMap<>();
        for (VacationUsage usage : usages) {
            LocalDate date = usage.getStartDate().toLocalDate();
            BigDecimal usedTime = usage.getUsedTime() != null ? usage.getUsedTime() : BigDecimal.ZERO;
            dailyHoursMap.merge(date, usedTime, BigDecimal::add);
        }
        return dailyHoursMap;
    }

    private Map<String, Map<LocalDate, BigDecimal>> aggregateVacationUsageByUsersAndDate(List<VacationUsage> usages) {
        Map<String, Map<LocalDate, BigDecimal>> result = new HashMap<>();
        for (VacationUsage usage : usages) {
            String userId = usage.getUser().getId();
            LocalDate date = usage.getStartDate().toLocalDate();
            BigDecimal usedTime = usage.getUsedTime() != null ? usage.getUsedTime() : BigDecimal.ZERO;

            result.computeIfAbsent(userId, k -> new HashMap<>())
                    .merge(date, usedTime, BigDecimal::add);
        }
        return result;
    }
}
