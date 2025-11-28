package com.lshdainty.porest.work.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.work.controller.dto.WorkApiDto;
import com.lshdainty.porest.work.repository.dto.WorkHistorySearchCondition;
import com.lshdainty.porest.work.service.WorkCodeService;
import com.lshdainty.porest.work.service.WorkHistoryService;
import com.lshdainty.porest.work.service.WorkSystemLogService;
import com.lshdainty.porest.work.service.dto.WorkCodeServiceDto;
import com.lshdainty.porest.work.service.dto.WorkHistoryServiceDto;
import com.lshdainty.porest.work.type.CodeType;
import com.lshdainty.porest.work.type.SystemType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WorkApiController implements WorkApi {
    private final WorkHistoryService workHistoryService;
    private final WorkCodeService workCodeService;
    private final WorkSystemLogService workSystemLogService;

    // ========== 업무 내역 관리 ==========

    @Override
    @PreAuthorize("hasAuthority('WORK_CREATE')")
    public ApiResponse createWorkHistory(WorkApiDto.CreateWorkHistoryReq data) {
        Long workHistorySeq = workHistoryService.createWorkHistory(WorkHistoryServiceDto.builder()
                .date(data.getWorkDate())
                .userId(data.getWorkUserId())
                .groupCode(data.getWorkGroupCode())
                .partCode(data.getWorkPartCode())
                .classCode(data.getWorkClassCode())
                .hours(data.getWorkHour())
                .content(data.getWorkContent())
                .build());
        return ApiResponse.success(new WorkApiDto.CreateWorkHistoryResp(workHistorySeq));
    }

    @Override
    @PreAuthorize("hasAuthority('WORK_CREATE')")
    public ApiResponse createWorkHistories(WorkApiDto.BulkCreateWorkHistoryReq data) {
        List<WorkHistoryServiceDto> dtos = data.getWorkHistories().stream()
                .map(req -> WorkHistoryServiceDto.builder()
                        .date(req.getWorkDate())
                        .userId(req.getWorkUserId())
                        .groupCode(req.getWorkGroupCode())
                        .partCode(req.getWorkPartCode())
                        .classCode(req.getWorkClassCode())
                        .hours(req.getWorkHour())
                        .content(req.getWorkContent())
                        .build())
                .collect(Collectors.toList());

        List<Long> workHistorySeqs = workHistoryService.createWorkHistories(dtos);
        return ApiResponse.success(new WorkApiDto.BulkCreateWorkHistoryResp(workHistorySeqs));
    }

    @Override
    @PreAuthorize("hasAuthority('WORK_MANAGE')")
    public ApiResponse findAllWorkHistories(WorkHistorySearchCondition condition) {
        List<WorkHistoryServiceDto> dtos = workHistoryService.findAllWorkHistories(condition);
        return ApiResponse.success(dtos.stream()
                .map(w -> new WorkApiDto.WorkHistoryResp(
                        w.getSeq(),
                        w.getDate(),
                        w.getUserId(),
                        w.getUserName(),
                        convertToWorkCodeResp(w.getGroupInfo()),
                        convertToWorkCodeResp(w.getPartInfo()),
                        convertToWorkCodeResp(w.getClassInfo()),
                        w.getHours(),
                        w.getContent()))
                .collect(Collectors.toList()));
    }

    @Override
    @PreAuthorize("hasAuthority('WORK_READ')")
    public ApiResponse findWorkHistory(Long seq) {
        WorkHistoryServiceDto w = workHistoryService.findWorkHistory(seq);
        return ApiResponse.success(new WorkApiDto.WorkHistoryResp(
                w.getSeq(),
                w.getDate(),
                w.getUserId(),
                w.getUserName(),
                convertToWorkCodeResp(w.getGroupInfo()),
                convertToWorkCodeResp(w.getPartInfo()),
                convertToWorkCodeResp(w.getClassInfo()),
                w.getHours(),
                w.getContent()));
    }

    @Override
    @PreAuthorize("hasAuthority('WORK_UPDATE')")
    public ApiResponse updateWorkHistory(Long seq, WorkApiDto.UpdateWorkHistoryReq data) {
        workHistoryService.updateWorkHistory(WorkHistoryServiceDto.builder()
                .seq(seq)
                .date(data.getWorkDate())
                .userId(data.getWorkUserId())
                .groupCode(data.getWorkGroupCode())
                .partCode(data.getWorkPartCode())
                .classCode(data.getWorkClassCode())
                .hours(data.getWorkHour())
                .content(data.getWorkContent())
                .build());
        return ApiResponse.success();
    }

    @Override
    @PreAuthorize("hasAuthority('WORK_UPDATE')")
    public ApiResponse deleteWorkHistory(Long seq) {
        workHistoryService.deleteWorkHistory(seq);
        return ApiResponse.success();
    }

    @Override
    @PreAuthorize("hasAuthority('WORK_MANAGE')")
    public void downloadWorkHistoryExcel(HttpServletResponse response, WorkHistorySearchCondition condition) throws IOException {
        workHistoryService.downloadWorkHistoryExcel(response, condition);
    }

    @Override
    @PreAuthorize("hasAuthority('WORK_MANAGE')")
    public void downloadUnregisteredWorkHistoryExcel(HttpServletResponse response, Integer year, Integer month) throws IOException {
        workHistoryService.downloadUnregisteredWorkHistoryExcel(response, year, month);
    }

    // ========== 업무 코드 관리 ==========

    @Override
    @PreAuthorize("hasAuthority('WORK_MANAGE')")
    public ApiResponse createWorkCode(WorkApiDto.CreateWorkCodeReq data) {
        Long workCodeSeq = workCodeService.createWorkCode(
                data.getWorkCode(),
                data.getWorkCodeName(),
                data.getCodeType(),
                data.getParentWorkCodeSeq(),
                data.getOrderSeq()
        );
        return ApiResponse.success(new WorkApiDto.CreateWorkCodeResp(workCodeSeq));
    }

    @Override
    @PreAuthorize("hasAuthority('WORK_READ')")
    public ApiResponse getWorkCodes(String parentWorkCode, Long parentWorkCodeSeq, Boolean parentIsNull, CodeType type) {
        List<WorkCodeServiceDto> workCodes = workCodeService.findWorkCodes(parentWorkCode, parentWorkCodeSeq, parentIsNull, type);
        return ApiResponse.success(workCodes.stream()
                .map(this::convertToWorkCodeResp)
                .collect(Collectors.toList()));
    }

    @Override
    @PreAuthorize("hasAuthority('WORK_MANAGE')")
    public ApiResponse updateWorkCode(Long seq, WorkApiDto.UpdateWorkCodeReq data) {
        workCodeService.updateWorkCode(
                seq,
                data.getWorkCode(),
                data.getWorkCodeName(),
                data.getParentWorkCodeSeq(),
                data.getOrderSeq()
        );
        return ApiResponse.success();
    }

    @Override
    @PreAuthorize("hasAuthority('WORK_MANAGE')")
    public ApiResponse deleteWorkCode(Long seq) {
        workCodeService.deleteWorkCode(seq);
        return ApiResponse.success();
    }

    // ========== 시스템 로그 관리 ==========

    @Override
    public ApiResponse toggleSystemCheck(WorkApiDto.ToggleSystemCheckReq req) {
        boolean checked = workSystemLogService.toggleSystemCheck(req.getSystemCode());
        String message = checked ? "시스템 체크가 등록되었습니다." : "시스템 체크가 해제되었습니다.";
        return ApiResponse.success(new WorkApiDto.ToggleSystemCheckResp(checked, message));
    }

    @Override
    public ApiResponse checkSystemStatus(SystemType systemCode) {
        boolean checked = workSystemLogService.isCheckedToday(systemCode);
        return ApiResponse.success(new WorkApiDto.CheckSystemStatusResp(systemCode, checked));
    }

    // ========== Private Helper Methods ==========

    private WorkApiDto.WorkCodeResp convertToWorkCodeResp(WorkCodeServiceDto dto) {
        if (dto == null) {
            return null;
        }
        return new WorkApiDto.WorkCodeResp(
                dto.getSeq(),
                dto.getCode(),
                dto.getName(),
                dto.getType(),
                dto.getOrderSeq(),
                dto.getParentSeq());
    }
}
