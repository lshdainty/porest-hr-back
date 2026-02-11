package com.porest.hr.client.sso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SSO 초대 상태 조회 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SsoInvitationStatusRequest {
    /**
     * SSO 사용자 번호 목록
     */
    private List<Long> userNos;
}
