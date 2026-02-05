package com.porest.hr.client.sso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SSO 사용자 초대 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SsoInviteRequest {
    /**
     * 클라이언트 코드 (hr, budget, home 등)
     */
    private String clientCode;

    /**
     * 관리자가 입력한 임시 사용자 ID
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
}
