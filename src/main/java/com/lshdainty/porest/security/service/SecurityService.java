package com.lshdainty.porest.security.service;

import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.repository.UserRepositoryImpl;
import com.lshdainty.porest.user.service.dto.UserServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SecurityService {
    private final MessageSource ms;
    private final UserRepositoryImpl userRepositoryImpl;

    /**
     * 초대 토큰으로 사용자 조회 및 유효성 검증
     */
    public UserServiceDto validateInvitationToken(String token) {
        Optional<User> findUser = userRepositoryImpl.findByInvitationToken(token);
        if (findUser.isEmpty()) {
            throw new IllegalArgumentException(ms.getMessage("error.notfound.invitation", null, null));
        }

        User user = findUser.get();
        if (!user.isInvitationValid()) {
            throw new IllegalArgumentException(ms.getMessage("error.expired.invitation", null, null));
        }

        return UserServiceDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .company(user.getCompany())
                .workTime(user.getWorkTime())
                .role(user.getRole())
                .invitationSentAt(user.getInvitationSentAt())
                .invitationExpiresAt(user.getInvitationExpiresAt())
                .invitationStatus(user.getInvitationStatus())
                .build();
    }
}
