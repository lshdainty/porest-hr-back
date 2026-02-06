package com.porest.hr.client.sso;

import com.porest.hr.client.sso.dto.SsoInvitationStatusResponse;
import com.porest.hr.client.sso.dto.SsoInviteRequest;
import com.porest.hr.client.sso.dto.SsoInviteResponse;

import java.util.List;

/**
 * SSO API 클라이언트 인터페이스<br>
 * SSO 서비스와의 HTTP 통신을 담당
 */
public interface SsoApiClient {

    /**
     * SSO에 사용자 초대 요청<br>
     * 신규 사용자면 User + UserClientAccess 생성, 초대 이메일 발송<br>
     * 기존 SSO 사용자면 UserClientAccess만 생성 (자동 연결)
     *
     * @param request 초대 요청 정보 (clientCode, userId, name, email)
     * @return 초대 결과 (userNo, alreadyExists 등)
     * @throws com.porest.core.exception.ExternalServiceException SSO 서비스 연동 실패 시
     */
    SsoInviteResponse inviteUser(SsoInviteRequest request);

    /**
     * SSO에 초대 재전송 요청<br>
     * PENDING 상태의 사용자에게 초대 이메일을 재발송
     *
     * @param userId 재전송할 사용자 ID
     * @throws com.porest.core.exception.ExternalServiceException SSO 서비스 연동 실패 시
     */
    void resendInvitation(String userId);

    /**
     * SSO에서 여러 사용자의 초대 상태 조회<br>
     * 사용자 목록 조회 시 초대 상태를 표시하기 위해 사용
     *
     * @param userNos SSO 사용자 번호 목록
     * @return 각 사용자의 초대 상태 목록
     * @throws com.porest.core.exception.ExternalServiceException SSO 서비스 연동 실패 시
     */
    List<SsoInvitationStatusResponse> getInvitationStatus(List<Long> userNos);
}
