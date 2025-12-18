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

    @Operation(
            summary = "비밀번호 초기화",
            description = "관리자가 특정 사용자의 비밀번호를 초기화합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 초기화 성공"
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
    @PatchMapping("/api/v1/users/{userId}/password")
    ApiResponse resetPassword(
            @Parameter(description = "사용자 ID", example = "user123", required = true)
            @PathVariable("userId") String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "새로운 비밀번호 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserApiDto.ResetPasswordReq.class))
            )
            @RequestBody UserApiDto.ResetPasswordReq data
    );

    @Operation(
            summary = "비밀번호 초기화 요청 (비로그인)",
            description = "사용자가 비밀번호를 잊어버린 경우, ID와 이메일을 입력하여 임시 비밀번호를 이메일로 받습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "임시 비밀번호 이메일 발송 완료"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "사용자 ID 또는 이메일이 일치하지 않음"
            )
    })
    @PostMapping("/api/v1/users/password/reset-request")
    ApiResponse requestPasswordReset(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "비밀번호 초기화 요청 정보 (ID, 이메일)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserApiDto.RequestPasswordResetReq.class))
            )
            @RequestBody UserApiDto.RequestPasswordResetReq data
    );

    @Operation(
            summary = "비밀번호 변경",
            description = "로그인한 사용자가 본인의 비밀번호를 변경합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 변경 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "현재 비밀번호 불일치 / 새 비밀번호 확인 불일치 / 동일한 비밀번호 사용"
            )
    })
    @PatchMapping("/api/v1/users/me/password")
    ApiResponse changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "비밀번호 변경 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserApiDto.ChangePasswordReq.class))
            )
            @RequestBody UserApiDto.ChangePasswordReq data
    );

    @Operation(
            summary = "초대 확인 (회원가입 1단계)",
            description = "사용자가 입력한 정보(userId, userName, userEmail, invitationCode)가 초대 정보와 일치하는지 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "초대 확인 성공",
                    content = @Content(schema = @Schema(implementation = UserApiDto.ValidateRegistrationResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력 정보 불일치"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "초대 정보를 찾을 수 없음"
            )
    })
    @PostMapping("/api/v1/users/registration/validate")
    ApiResponse<UserApiDto.ValidateRegistrationResp> validateRegistration(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "초대 확인 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserApiDto.ValidateRegistrationReq.class))
            )
            @RequestBody UserApiDto.ValidateRegistrationReq data,
            jakarta.servlet.http.HttpSession session
    );

    @Operation(
            summary = "회원가입 완료 (회원가입 2단계)",
            description = "초대 확인 후 새로운 ID/PW와 추가 정보를 입력하여 회원가입을 완료합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 완료 성공",
                    content = @Content(schema = @Schema(implementation = UserApiDto.CompleteRegistrationResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "비밀번호 확인 불일치"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "초대 확인이 완료되지 않음 (세션 만료 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "새 ID 중복"
            )
    })
    @PostMapping("/api/v1/users/registration/complete")
    ApiResponse<UserApiDto.CompleteRegistrationResp> completeRegistration(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 완료 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserApiDto.CompleteRegistrationReq.class))
            )
            @RequestBody UserApiDto.CompleteRegistrationReq data,
            jakarta.servlet.http.HttpSession session
    );
}
