package com.lshdainty.porest.work.service;

import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.work.domain.WorkCode;
import com.lshdainty.porest.work.domain.WorkHistory;
import com.lshdainty.porest.work.repository.WorkCodeRepositoryImpl;
import com.lshdainty.porest.work.repository.WorkHistoryCustomRepositoryImpl;
import com.lshdainty.porest.work.repository.dto.WorkHistorySearchCondition;
import com.lshdainty.porest.work.service.dto.WorkCodeServiceDto;
import com.lshdainty.porest.work.service.dto.WorkHistoryServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
}
