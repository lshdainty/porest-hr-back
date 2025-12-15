package com.lshdainty.porest.user.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.user.controller.dto.UserApiDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 관리 API")
public interface UserApi {

    @Operation(
            summary = "사용자 회원가입",
            description = "새로운 사용자를 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserApiDto.JoinUserResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터"
            )
    })
    @PostMapping("/api/v1/users")
    ApiResponse joinUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserApiDto.JoinUserReq.class))
            )
            @RequestBody UserApiDto.JoinUserReq data
    );

    @Operation(
            summary = "사용자 조회",
            description = "특정 사용자의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserApiDto.SearchUserResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (USER:READ 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @GetMapping("/api/v1/users/{id}")
    ApiResponse searchUser(
            @Parameter(description = "사용자 ID", example = "user123", required = true)
            @PathVariable("id") String userId
    );

    @Operation(
            summary = "사용자 ID 중복 확인",
            description = "사용자 ID가 이미 존재하는지 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = UserApiDto.CheckUserIdDuplicateResp.class))
            )
    })
    @GetMapping("/api/v1/users/check-duplicate")
    ApiResponse checkUserIdDuplicate(
            @Parameter(description = "확인할 사용자 ID", example = "user123", required = true)
            @RequestParam("user_id") String userId
    );

    @Operation(
            summary = "전체 사용자 조회",
            description = "모든 사용자의 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 목록 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (USER:READ 필요)"
            )
    })
    @GetMapping("/api/v1/users")
    ApiResponse searchUsers();

    @Operation(
            summary = "사용자 정보 수정",
            description = "기존 사용자의 정보를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 수정 성공",
                    content = @Content(schema = @Schema(implementation = UserApiDto.EditUserResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (USER:EDIT 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @PutMapping("/api/v1/users/{id}")
    ApiResponse editUser(
            @Parameter(description = "사용자 ID", example = "user123", required = true)
            @PathVariable("id") String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사용자 수정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserApiDto.EditUserReq.class))
            )
            @RequestBody UserApiDto.EditUserReq data
    );

    @Operation(
            summary = "사용자 삭제",
            description = "기존 사용자를 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (USER:MANAGE 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @DeleteMapping("/api/v1/users/{id}")
    ApiResponse deleteUser(
            @Parameter(description = "사용자 ID", example = "user123", required = true)
            @PathVariable("id") String userId
    );

    @Operation(
            summary = "프로필 이미지 업로드",
            description = "사용자 프로필 이미지를 업로드합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 업로드 성공",
                    content = @Content(schema = @Schema(implementation = UserApiDto.UploadProfileResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 파일 형식"
            )
    })
    @PostMapping(value = "/api/v1/users/profiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse uploadProfile(
            @ModelAttribute UserApiDto.UploadProfileReq data
    );

    @Operation(
            summary = "사용자 초대",
            description = "관리자가 새로운 사용자를 초대합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "초대 성공",
                    content = @Content(schema = @Schema(implementation = UserApiDto.InviteUserResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (USER:MANAGE 필요)"
            )
    })
    @PostMapping("/api/v1/users/invitations")
    ApiResponse inviteUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "초대할 사용자 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserApiDto.InviteUserReq.class))
            )
            @RequestBody UserApiDto.InviteUserReq data
    );

    @Operation(
            summary = "초대된 사용자 정보 수정",
            description = "초대된 사용자의 정보를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "초대 사용자 수정 성공",
                    content = @Content(schema = @Schema(implementation = UserApiDto.EditInvitedUserResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (USER:EDIT 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @PutMapping("/api/v1/users/{id}/invitations")
    ApiResponse editInvitedUser(
            @Parameter(description = "사용자 ID", example = "user123", required = true)
            @PathVariable("id") String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 초대 사용자 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserApiDto.EditInvitedUserReq.class))
            )
            @RequestBody UserApiDto.EditInvitedUserReq data
    );

    @Operation(
            summary = "초대 이메일 재전송",
            description = "초대 이메일을 재전송합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "초대 이메일 재전송 성공",
                    content = @Content(schema = @Schema(implementation = UserApiDto.ResendInvitationResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (USER:MANAGE 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @PostMapping("/api/v1/users/{id}/invitations/resend")
    ApiResponse resendInvitation(
            @Parameter(description = "사용자 ID", example = "user123", required = true)
            @PathVariable("id") String userId
    );

    @Operation(
            summary = "메인 부서 존재 여부 확인",
            description = "사용자의 메인 부서가 존재하는지 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserApiDto.CheckMainDepartmentExistenceResp.class))
            )
    })
    @GetMapping("/api/v1/users/{userId}/main-department/existence")
    ApiResponse checkUserMainDepartmentExistence(
            @Parameter(description = "사용자 ID", example = "user123", required = true)
            @PathVariable("userId") String userId
    );

    @Operation(
            summary = "대시보드 수정",
            description = "사용자의 대시보드 설정을 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "대시보드 수정 성공",
                    content = @Content(schema = @Schema(implementation = UserApiDto.UpdateDashboardResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (USER:EDIT 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @PatchMapping("/api/v1/users/{userId}/dashboard")
    ApiResponse updateDashboard(
            @Parameter(description = "사용자 ID", example = "user123", required = true)
            @PathVariable("userId") String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "대시보드 수정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserApiDto.UpdateDashboardReq.class))
            )
            @RequestBody UserApiDto.UpdateDashboardReq data
    );

    @Operation(
            summary = "승인권자 목록 조회",
            description = "특정 사용자의 승인권자 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "승인권자 목록 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @GetMapping("/api/v1/users/{userId}/approvers")
    ApiResponse getUserApprovers(
            @Parameter(description = "사용자 ID", example = "user123", required = true)
            @PathVariable("userId") String userId
    );
}
