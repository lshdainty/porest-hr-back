package com.lshdainty.porest.holiday.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.controller.dto.HolidayApiDto;
import com.lshdainty.porest.holiday.type.HolidayType;
import com.lshdainty.porest.security.annotation.LoginUser;
import com.lshdainty.porest.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Holiday", description = "공휴일 관리 API")
public interface HolidayApi {

    @Operation(
            summary = "공휴일 등록",
            description = "새로운 공휴일을 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "공휴일 등록 성공",
                    content = @Content(schema = @Schema(implementation = HolidayApiDto.RegistHolidayResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (HOLIDAY_MANAGE 권한 필요)"
            )
    })
    @PostMapping("api/v1/holiday")
    ApiResponse registHoliday(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "공휴일 등록 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = HolidayApiDto.RegistHolidayReq.class))
            )
            @RequestBody HolidayApiDto.RegistHolidayReq data
    );

    @Operation(
            summary = "기간별 공휴일 조회",
            description = "시작일과 종료일 사이의 공휴일 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "공휴일 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (HOLIDAY_READ 권한 필요)"
            )
    })
    @GetMapping("api/v1/holidays/date")
    ApiResponse searchHolidaysByStartEndDate(
            @Parameter(description = "시작일 (YYYY-MM-DD)", example = "2024-01-01", required = true)
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "종료일 (YYYY-MM-DD)", example = "2024-12-31", required = true)
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @Parameter(description = "국가 코드 (미입력 시 로그인 사용자의 국가 코드 사용)", example = "KR", required = false)
            @RequestParam(value = "country_code", required = false) CountryCode countryCode,
            @Parameter(hidden = true) @LoginUser User loginUser
    );

    @Operation(
            summary = "타입별 공휴일 조회",
            description = "공휴일 타입별로 공휴일 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "공휴일 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (HOLIDAY_READ 권한 필요)"
            )
    })
    @GetMapping("api/v1/holidays/type/{type}")
    ApiResponse searchHolidaysByType(
            @Parameter(description = "공휴일 타입", example = "NATIONAL", required = true)
            @PathVariable("type") HolidayType type
    );

    @Operation(
            summary = "공휴일 수정",
            description = "기존 공휴일 정보를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "공휴일 수정 성공",
                    content = @Content(schema = @Schema(implementation = HolidayApiDto.EditHolidayResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (HOLIDAY_MANAGE 권한 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "공휴일 정보를 찾을 수 없음"
            )
    })
    @PutMapping("/api/v1/holiday/{id}")
    ApiResponse editHoliday(
            @Parameter(description = "공휴일 아이디", example = "1", required = true)
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "공휴일 수정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = HolidayApiDto.EditHolidayReq.class))
            )
            @RequestBody HolidayApiDto.EditHolidayReq data
    );

    @Operation(
            summary = "공휴일 삭제",
            description = "기존 공휴일을 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "공휴일 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (HOLIDAY_MANAGE 권한 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "공휴일 정보를 찾을 수 없음"
            )
    })
    @DeleteMapping("/api/v1/holiday/{id}")
    ApiResponse deleteHoliday(
            @Parameter(description = "공휴일 아이디", example = "1", required = true)
            @PathVariable("id") Long id
    );
}
