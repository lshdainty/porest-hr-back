package com.porest.hr.client.sso;

import com.porest.core.controller.ApiResponse;
import com.porest.core.exception.ExternalServiceException;
import com.porest.hr.client.sso.dto.SsoInvitationStatusRequest;
import com.porest.hr.client.sso.dto.SsoInvitationStatusResponse;
import com.porest.hr.client.sso.dto.SsoInviteRequest;
import com.porest.hr.client.sso.dto.SsoInviteResponse;
import com.porest.hr.common.exception.HrErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SSO API 클라이언트 구현체<br>
 * RestTemplate을 사용하여 SSO 서비스와 HTTP 통신
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsoApiClientImpl implements SsoApiClient {

    @Qualifier("ssoRestTemplate")
    private final RestTemplate ssoRestTemplate;

    private static final String INVITE_USER_PATH = "/api/v1/users/invite";
    private static final String RESEND_INVITATION_PATH = "/api/v1/users/resend";
    private static final String INVITATION_STATUS_PATH = "/api/v1/users/invitation-status";

    @Override
    public SsoInviteResponse inviteUser(SsoInviteRequest request) {
        log.debug("Calling SSO inviteUser API: userId={}, email={}", request.getUserId(), request.getEmail());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<SsoInviteRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ApiResponse<SsoInviteResponse>> response = ssoRestTemplate.exchange(
                    INVITE_USER_PATH,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            ApiResponse<SsoInviteResponse> body = response.getBody();
            if (body == null || body.getData() == null) {
                throw new ExternalServiceException(HrErrorCode.SSO_SERVICE_ERROR, "SSO 응답이 비어있습니다");
            }

            log.info("SSO inviteUser API success: userNo={}, alreadyExists={}",
                    body.getData().getUserNo(), body.getData().isAlreadyExists());

            return body.getData();

        } catch (RestClientException e) {
            log.error("SSO inviteUser API failed: {}", e.getMessage(), e);
            throw new ExternalServiceException(HrErrorCode.SSO_SERVICE_ERROR, "SSO 초대 API 호출 실패", e);
        }
    }

    @Override
    public void resendInvitation(String userId) {
        log.debug("Calling SSO resendInvitation API: userId={}", userId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = Map.of("userId", userId);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ssoRestTemplate.exchange(
                    RESEND_INVITATION_PATH,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<Void>>() {}
            );

            log.info("SSO resendInvitation API success: userId={}", userId);

        } catch (RestClientException e) {
            log.error("SSO resendInvitation API failed: {}", e.getMessage(), e);
            throw new ExternalServiceException(HrErrorCode.SSO_SERVICE_ERROR, "SSO 초대 재전송 API 호출 실패", e);
        }
    }

    @Override
    public List<SsoInvitationStatusResponse> getInvitationStatus(List<Long> userNos) {
        if (userNos == null || userNos.isEmpty()) {
            return Collections.emptyList();
        }

        log.debug("Calling SSO getInvitationStatus API: userNos count={}", userNos.size());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            SsoInvitationStatusRequest request = SsoInvitationStatusRequest.builder()
                    .userNos(userNos)
                    .build();

            HttpEntity<SsoInvitationStatusRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ApiResponse<List<SsoInvitationStatusResponse>>> response = ssoRestTemplate.exchange(
                    INVITATION_STATUS_PATH,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            ApiResponse<List<SsoInvitationStatusResponse>> body = response.getBody();
            if (body == null || body.getData() == null) {
                log.warn("SSO getInvitationStatus API returned empty response");
                return Collections.emptyList();
            }

            log.info("SSO getInvitationStatus API success: count={}", body.getData().size());

            return body.getData();

        } catch (RestClientException e) {
            log.error("SSO getInvitationStatus API failed: {}", e.getMessage(), e);
            throw new ExternalServiceException(HrErrorCode.SSO_SERVICE_ERROR, "SSO 초대 상태 조회 API 호출 실패", e);
        }
    }
}
