package com.lshdainty.porest.work.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.work.controller.dto.WorkSystemLogApiDto;
import com.lshdainty.porest.work.service.WorkSystemLogService;
import com.lshdainty.porest.work.type.SystemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 업무 시스템 로그 API Controller<br>
 * 시스템 체크 관련 RESTful API<br>
 * - 사용자 인증 정보는 SecurityContext에서 자동으로 처리됨<br>
 * - AuditingFields를 통해 생성자/생성일시가 자동으로 기록됨
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class WorkSystemLogApiController {
    private final WorkSystemLogService workSystemLogService;

    /**
     * 시스템 체크 토글<br>
     * POST /api/v1/work/system-logs<br>
     * 오늘 날짜로 이미 체크되어 있으면 삭제, 없으면 생성<br>
     * 사용자 정보는 SecurityContext에서 자동으로 가져옴
     *
     * @param req 시스템 코드
     * @return ToggleSystemCheckResp
     */
    @PostMapping("/api/v1/work/system-logs")
    public ApiResponse<WorkSystemLogApiDto.ToggleSystemCheckResp> toggleSystemCheck(
            @RequestBody WorkSystemLogApiDto.ToggleSystemCheckReq req
    ) {
        boolean checked = workSystemLogService.toggleSystemCheck(req.getSystemCode());

        String message = checked ? "시스템 체크가 등록되었습니다." : "시스템 체크가 해제되었습니다.";

        return ApiResponse.success(
                new WorkSystemLogApiDto.ToggleSystemCheckResp(checked, message)
        );
    }

    /**
     * 오늘 시스템 체크 상태 조회<br>
     * GET /api/v1/work/system-logs/status?system_code={systemCode}<br>
     * 사용자 정보는 SecurityContext에서 자동으로 가져옴
     *
     * @param systemCode 시스템 코드
     * @return CheckSystemStatusResp
     */
    @GetMapping("/api/v1/work/system-logs/status")
    public ApiResponse<WorkSystemLogApiDto.CheckSystemStatusResp> checkSystemStatus(
            @RequestParam("system_code") SystemType systemCode
    ) {
        boolean checked = workSystemLogService.isCheckedToday(systemCode);

        return ApiResponse.success(
                new WorkSystemLogApiDto.CheckSystemStatusResp(systemCode, checked)
        );
    }
}
