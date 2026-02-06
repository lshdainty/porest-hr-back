package com.porest.hr.client.sso.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SSO 사용자 초대 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SsoInviteResponse {
    /**
     * 기존 SSO 사용자 여부
     */
    private boolean alreadyExists;

    /**
     * SSO에서 발급한 사용자 번호 (row_id)
     */
    private Long userNo;

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 사용자 이름
     */
    private String name;

    /**
     * 사용자 이메일
     */
    private String email;

    /**
     * 초대 토큰 (신규 사용자만)
     */
    private String invitationToken;

    /**
     * 메시지
     */
    private String message;

    /**
     * 초대 발송 시간
     */
    private LocalDateTime invitationSentAt;

    /**
     * 초대 만료 시간
     */
    private LocalDateTime invitationExpiresAt;

    /**
     * 초대 상태 (PENDING, ACTIVE, EXPIRED 등)
     */
    private String invitationStatus;
}
