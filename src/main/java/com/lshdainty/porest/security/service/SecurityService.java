package com.lshdainty.porest.security.service;

import com.lshdainty.porest.common.message.MessageKey;
import com.lshdainty.porest.common.util.MessageResolver;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.repository.UserRepository;
import com.lshdainty.porest.user.service.dto.UserServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SecurityService {
    private final MessageResolver messageResolver;
    private final UserRepository userRepository;

    /**
     * 초대 토큰으로 사용자 조회 및 유효성 검증
     */
    public UserServiceDto validateInvitationToken(String token) {
        log.debug("초대 토큰 검증 시작: token={}", token);
        Optional<User> findUser = userRepository.findByInvitationToken(token);
        if (findUser.isEmpty()) {
            log.warn("초대 토큰 검증 실패 - 토큰 없음: token={}", token);
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.NOT_FOUND_INVITATION));
        }

        User user = findUser.get();
        if (!user.isInvitationValid()) {
            log.warn("초대 토큰 검증 실패 - 만료된 토큰: userId={}, expiresAt={}", user.getId(), user.getInvitationExpiresAt());
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VALIDATE_EXPIRED_INVITATION));
        }

        log.info("초대 토큰 검증 성공: userId={}", user.getId());
        return UserServiceDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .company(user.getCompany())
                .workTime(user.getWorkTime())
                .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .invitationSentAt(user.getInvitationSentAt())
                .invitationExpiresAt(user.getInvitationExpiresAt())
                .invitationStatus(user.getInvitationStatus())
                .build();
    }
}
