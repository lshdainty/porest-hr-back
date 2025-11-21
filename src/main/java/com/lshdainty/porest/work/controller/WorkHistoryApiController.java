package com.lshdainty.porest.work.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.work.controller.dto.WorkHistoryApiDto;
import com.lshdainty.porest.work.repository.dto.WorkHistorySearchCondition;
import com.lshdainty.porest.work.service.WorkCodeService;
import com.lshdainty.porest.work.service.WorkHistoryService;
import com.lshdainty.porest.work.service.dto.WorkCodeServiceDto;
import com.lshdainty.porest.work.service.dto.WorkHistoryServiceDto;
import com.lshdainty.porest.work.type.CodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WorkHistoryApiController {
    private final WorkHistoryService workHistoryService;
    private final WorkCodeService workCodeService;

    @PostMapping("/api/v1/work-histories")
    public ApiResponse createWorkHistory(@RequestBody WorkHistoryApiDto.CreateWorkHistoryReq data) {
        Long workHistorySeq = workHistoryService.createWorkHistory(WorkHistoryServiceDto.builder()
                .date(data.getWorkDate())
                .userId(data.getWorkUserId())
                .groupCode(data.getWorkGroupCode())
                .partCode(data.getWorkPartCode())
                .classCode(data.getWorkClassCode())
                .hours(data.getWorkHour())
                .content(data.getWorkContent())
                .build());
        return ApiResponse.success(new WorkHistoryApiDto.CreateWorkHistoryResp(workHistorySeq));
    }

    @GetMapping("/api/v1/work-histories")
    public ApiResponse findAllWorkHistories(@ModelAttribute WorkHistorySearchCondition condition) {
        List<WorkHistoryServiceDto> dtos = workHistoryService.findAllWorkHistories(condition);
        return ApiResponse.success(dtos.stream()
                .map(w -> new WorkHistoryApiDto.WorkHistoryResp(
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

    @GetMapping("/api/v1/work-histories/{seq}")
    public ApiResponse findWorkHistory(@PathVariable("seq") Long seq) {
        WorkHistoryServiceDto w = workHistoryService.findWorkHistory(seq);
        return ApiResponse.success(new WorkHistoryApiDto.WorkHistoryResp(
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

    @PutMapping("/api/v1/work-histories/{seq}")
    public ApiResponse updateWorkHistory(@PathVariable("seq") Long seq,
            @RequestBody WorkHistoryApiDto.UpdateWorkHistoryReq data) {
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

    @DeleteMapping("/api/v1/work-histories/{seq}")
    public ApiResponse deleteWorkHistory(@PathVariable("seq") Long seq) {
        workHistoryService.deleteWorkHistory(seq);
        return ApiResponse.success();
    }

    /**
     * 업무 코드 통합 조회 API
     * GET /api/v1/work-codes
     */
    @GetMapping("/api/v1/work-codes")
    public ApiResponse getWorkCodes(
            @RequestParam(value = "parent_work_code", required = false) String parentWorkCode,
            @RequestParam(value = "parent_work_code_seq", required = false) Long parentWorkCodeSeq,
            @RequestParam(value = "parent_is_null", required = false) Boolean parentIsNull,
            @RequestParam(value = "type", required = false) CodeType type) {
        List<WorkCodeServiceDto> workCodes = workCodeService.findWorkCodes(parentWorkCode, parentWorkCodeSeq,
                parentIsNull, type);
        return ApiResponse.success(workCodes.stream()
                .map(this::convertToWorkCodeResp)
                .collect(Collectors.toList()));
    }

    private WorkHistoryApiDto.WorkCodeResp convertToWorkCodeResp(WorkCodeServiceDto dto) {
        if (dto == null) {
            return null;
        }
        return new WorkHistoryApiDto.WorkCodeResp(
                dto.getSeq(),
                dto.getCode(),
                dto.getName(),
                dto.getType(),
                dto.getOrderSeq(),
                dto.getParentSeq());
    }

    @GetMapping("/api/v1/work-histories/excel/download")
    public void downloadWorkHistoryExcel(HttpServletResponse response,
            @ModelAttribute WorkHistorySearchCondition condition) throws IOException {
        workHistoryService.downloadWorkHistoryExcel(response, condition);
    }
}
