package com.lshdainty.porest.security.service;

import com.lshdainty.porest.user.service.dto.UserServiceDto;

public interface SecurityService {

    /**
     * 초대 토큰으로 사용자 조회 및 유효성 검증
     */
    UserServiceDto validateInvitationToken(String token);
}
