package com.lshdainty.porest.dues.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.dues.controller.dto.DuesApiDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dues", description = "회비 관리 API")
public interface DuesApi {

    @Operation(
            summary = "회비 등록",
            description = "새로운 회비 내역을 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회비 등록 성공",
                    content = @Content(schema = @Schema(implementation = DuesApiDto.RegistDuesResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (DUES_MANAGE 권한 필요)"
            )
    })
    @PostMapping("/api/v1/dues")
    ApiResponse registDues(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회비 등록 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DuesApiDto.RegistDuesReq.class))
            )
            @RequestBody DuesApiDto.RegistDuesReq data
    );

    @Operation(
            summary = "연도별 회비 조회",
            description = "특정 연도의 모든 회비 내역을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회비 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (DUES_READ 권한 필요)"
            )
    })
    @GetMapping("/api/v1/dues")
    ApiResponse searchYearDues(
            @Parameter(description = "조회할 연도 (YYYY)", example = "2024", required = true)
            @RequestParam("year") Integer year
    );

    @Operation(
            summary = "연도별 운영 회비 조회",
            description = "특정 연도의 운영 회비 총액, 입금, 출금 내역을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "운영 회비 조회 성공",
                    content = @Content(schema = @Schema(implementation = DuesApiDto.SearchYearOperationDuesResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (DUES_READ 권한 필요)"
            )
    })
    @GetMapping("/api/v1/dues/operation")
    ApiResponse searchYearOperationDues(
            @Parameter(description = "조회할 연도 (YYYY)", example = "2024", required = true)
            @RequestParam("year") Integer year
    );

    @Operation(
            summary = "월별 생일 회비 조회",
            description = "특정 연도/월의 생일 회비 총액을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "생일 회비 조회 성공",
                    content = @Content(schema = @Schema(implementation = DuesApiDto.SearchMonthBirthDuesResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (DUES_READ 권한 필요)"
            )
    })
    @GetMapping("/api/v1/dues/birth/month")
    ApiResponse searchMonthBirthDues(
            @Parameter(description = "조회할 연도 (YYYY)", example = "2024", required = true)
            @RequestParam("year") Integer year,
            @Parameter(description = "조회할 월 (1-12)", example = "1", required = true)
            @RequestParam("month") Integer month
    );

    @Operation(
            summary = "사용자별 월별 생일 회비 조회",
            description = "특정 연도의 모든 사용자별 월별 생일 회비 내역을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자별 생일 회비 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (DUES_READ 권한 필요)"
            )
    })
    @GetMapping("/api/v1/dues/users/birth/month")
    ApiResponse searchUsersMonthBirthDues(
            @Parameter(description = "조회할 연도 (YYYY)", example = "2024", required = true)
            @RequestParam("year") Integer year
    );

    @Operation(
            summary = "회비 수정",
            description = "기존 회비 내역을 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회비 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (DUES_MANAGE 권한 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회비 내역을 찾을 수 없음"
            )
    })
    @PutMapping("/api/v1/dues/{id}")
    ApiResponse editDues(
            @Parameter(description = "회비 ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회비 수정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DuesApiDto.EditDuesReq.class))
            )
            @RequestBody DuesApiDto.EditDuesReq data
    );

    @Operation(
            summary = "회비 삭제",
            description = "기존 회비 내역을 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회비 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (DUES_MANAGE 권한 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회비 내역을 찾을 수 없음"
            )
    })
    @DeleteMapping("/api/v1/dues/{id}")
    ApiResponse deleteDues(
            @Parameter(description = "회비 ID", example = "1", required = true)
            @PathVariable("id") Long id
    );
}
