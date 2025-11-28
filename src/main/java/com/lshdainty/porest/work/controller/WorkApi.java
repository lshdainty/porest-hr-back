package com.lshdainty.porest.work.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.work.controller.dto.WorkApiDto;
import com.lshdainty.porest.work.repository.dto.WorkHistorySearchCondition;
import com.lshdainty.porest.work.type.CodeType;
import com.lshdainty.porest.work.type.SystemType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "Work", description = "업무 관리 API")
public interface WorkApi {

    // ========== 업무 내역 관리 ==========

    @Operation(
            summary = "업무 내역 등록",
            description = "새로운 업무 내역을 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업무 내역 등록 성공",
                    content = @Content(schema = @Schema(implementation = WorkApiDto.CreateWorkHistoryResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (WORK_CREATE 권한 필요)"
            )
    })
    @PostMapping("/api/v1/work-histories")
    ApiResponse createWorkHistory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업무 내역 등록 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = WorkApiDto.CreateWorkHistoryReq.class))
            )
            @RequestBody WorkApiDto.CreateWorkHistoryReq data
    );

    @Operation(
            summary = "업무 내역 일괄 등록",
            description = "여러 업무 내역을 일괄 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업무 내역 일괄 등록 성공",
                    content = @Content(schema = @Schema(implementation = WorkApiDto.BulkCreateWorkHistoryResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (WORK_CREATE 권한 필요)"
            )
    })
    @PostMapping("/api/v1/work-histories/bulk")
    ApiResponse createWorkHistories(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업무 내역 일괄 등록 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = WorkApiDto.BulkCreateWorkHistoryReq.class))
            )
            @RequestBody WorkApiDto.BulkCreateWorkHistoryReq data
    );

    @Operation(
            summary = "업무 내역 목록 조회",
            description = "검색 조건에 따라 업무 내역 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업무 내역 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (WORK_MANAGE 권한 필요)"
            )
    })
    @GetMapping("/api/v1/work-histories")
    ApiResponse findAllWorkHistories(@ModelAttribute WorkHistorySearchCondition condition);

    @Operation(
            summary = "업무 내역 단건 조회",
            description = "특정 업무 내역의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업무 내역 조회 성공",
                    content = @Content(schema = @Schema(implementation = WorkApiDto.WorkHistoryResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (WORK_READ 권한 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "업무 내역을 찾을 수 없음"
            )
    })
    @GetMapping("/api/v1/work-histories/{seq}")
    ApiResponse findWorkHistory(
            @Parameter(description = "업무 내역 시퀀스", example = "1", required = true)
            @PathVariable("seq") Long seq
    );

    @Operation(
            summary = "업무 내역 수정",
            description = "기존 업무 내역을 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업무 내역 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (WORK_UPDATE 권한 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "업무 내역을 찾을 수 없음"
            )
    })
    @PutMapping("/api/v1/work-histories/{seq}")
    ApiResponse updateWorkHistory(
            @Parameter(description = "업무 내역 시퀀스", example = "1", required = true)
            @PathVariable("seq") Long seq,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업무 내역 수정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = WorkApiDto.UpdateWorkHistoryReq.class))
            )
            @RequestBody WorkApiDto.UpdateWorkHistoryReq data
    );

    @Operation(
            summary = "업무 내역 삭제",
            description = "기존 업무 내역을 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업무 내역 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (WORK_UPDATE 권한 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "업무 내역을 찾을 수 없음"
            )
    })
    @DeleteMapping("/api/v1/work-histories/{seq}")
    ApiResponse deleteWorkHistory(
            @Parameter(description = "업무 내역 시퀀스", example = "1", required = true)
            @PathVariable("seq") Long seq
    );

    @Operation(
            summary = "업무 내역 엑셀 다운로드",
            description = "검색 조건에 따른 업무 내역을 엑셀 파일로 다운로드합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "엑셀 다운로드 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (WORK_MANAGE 권한 필요)"
            )
    })
    @GetMapping("/api/v1/work-histories/excel/download")
    void downloadWorkHistoryExcel(
            HttpServletResponse response,
            @ModelAttribute WorkHistorySearchCondition condition
    ) throws IOException;

    @Operation(
            summary = "미등록 업무 시간 엑셀 다운로드",
            description = "특정 연월의 미등록 업무 시간을 엑셀 파일로 다운로드합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "엑셀 다운로드 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (WORK_MANAGE 권한 필요)"
            )
    })
    @GetMapping("/api/v1/work-histories/unregistered-hours/download")
    void downloadUnregisteredWorkHistoryExcel(
            HttpServletResponse response,
            @Parameter(description = "연도", example = "2024", required = true)
            @RequestParam("year") Integer year,
            @Parameter(description = "월", example = "1", required = true)
            @RequestParam("month") Integer month
    ) throws IOException;

    // ========== 업무 코드 관리 ==========

    @Operation(
            summary = "업무 코드 생성",
            description = "새로운 업무 코드를 생성합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업무 코드 생성 성공",
                    content = @Content(schema = @Schema(implementation = WorkApiDto.CreateWorkCodeResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (WORK_MANAGE 권한 필요)"
            )
    })
    @PostMapping("/api/v1/work-codes")
    ApiResponse createWorkCode(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업무 코드 생성 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = WorkApiDto.CreateWorkCodeReq.class))
            )
            @RequestBody WorkApiDto.CreateWorkCodeReq data
    );

    @Operation(
            summary = "업무 코드 조회",
            description = "검색 조건에 따라 업무 코드 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업무 코드 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (WORK_READ 권한 필요)"
            )
    })
    @GetMapping("/api/v1/work-codes")
    ApiResponse getWorkCodes(
            @Parameter(description = "부모 업무 코드", example = "GROUP_A")
            @RequestParam(value = "parent_work_code", required = false) String parentWorkCode,
            @Parameter(description = "부모 업무 코드 시퀀스", example = "1")
            @RequestParam(value = "parent_work_code_seq", required = false) Long parentWorkCodeSeq,
            @Parameter(description = "부모 코드가 null인 항목만 조회", example = "true")
            @RequestParam(value = "parent_is_null", required = false) Boolean parentIsNull,
            @Parameter(description = "코드 타입", example = "GROUP")
            @RequestParam(value = "type", required = false) CodeType type
    );

    @Operation(
            summary = "업무 코드 수정",
            description = "기존 업무 코드를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업무 코드 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (WORK_MANAGE 권한 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "업무 코드를 찾을 수 없음"
            )
    })
    @PutMapping("/api/v1/work-codes/{seq}")
    ApiResponse updateWorkCode(
            @Parameter(description = "업무 코드 시퀀스", example = "1", required = true)
            @PathVariable("seq") Long seq,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업무 코드 수정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = WorkApiDto.UpdateWorkCodeReq.class))
            )
            @RequestBody WorkApiDto.UpdateWorkCodeReq data
    );

    @Operation(
            summary = "업무 코드 삭제 (Soft Delete)",
            description = "기존 업무 코드를 논리 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업무 코드 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (WORK_MANAGE 권한 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "업무 코드를 찾을 수 없음"
            )
    })
    @DeleteMapping("/api/v1/work-codes/{seq}")
    ApiResponse deleteWorkCode(
            @Parameter(description = "업무 코드 시퀀스", example = "1", required = true)
            @PathVariable("seq") Long seq
    );

    // ========== 시스템 로그 관리 ==========

    @Operation(
            summary = "시스템 체크 토글",
            description = "오늘 날짜로 시스템 체크 상태를 토글합니다. 이미 체크되어 있으면 삭제, 없으면 생성합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "시스템 체크 토글 성공",
                    content = @Content(schema = @Schema(implementation = WorkApiDto.ToggleSystemCheckResp.class))
            )
    })
    @PostMapping("/api/v1/work/system-logs")
    ApiResponse toggleSystemCheck(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "시스템 코드",
                    required = true,
                    content = @Content(schema = @Schema(implementation = WorkApiDto.ToggleSystemCheckReq.class))
            )
            @RequestBody WorkApiDto.ToggleSystemCheckReq req
    );

    @Operation(
            summary = "오늘 시스템 체크 상태 조회",
            description = "특정 시스템의 오늘 체크 상태를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "시스템 체크 상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = WorkApiDto.CheckSystemStatusResp.class))
            )
    })
    @GetMapping("/api/v1/work/system-logs/status")
    ApiResponse checkSystemStatus(
            @Parameter(description = "시스템 코드", example = "ERP", required = true)
            @RequestParam("system_code") SystemType systemCode
    );
}
