package com.porest.hr.client.sso.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SSO 초대 상태 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SsoInvitationStatusResponse {
    /**
     * SSO 사용자 번호 (row_id)
     */
    private Long userNo;

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 초대 상태 (PENDING, ACTIVE, EXPIRED 등)
     */
    private String invitationStatus;

    /**
     * 초대 발송 시간
     */
    private LocalDateTime invitationSentAt;

    /**
     * 초대 만료 시간
     */
    private LocalDateTime invitationExpiresAt;

    /**
     * 회원가입 완료 시간
     */
    private LocalDateTime registeredAt;
}
