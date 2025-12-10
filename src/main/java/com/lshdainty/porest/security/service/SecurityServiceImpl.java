package com.lshdainty.porest.security.service;

import com.lshdainty.porest.common.exception.BusinessRuleViolationException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ErrorCode;
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
public class SecurityServiceImpl implements SecurityService {
    private final UserRepository userRepository;

    @Override
    public UserServiceDto validateInvitationToken(String token) {
        log.debug("초대 토큰 검증 시작: token={}", token);
        Optional<User> findUser = userRepository.findByInvitationToken(token);
        if (findUser.isEmpty()) {
            log.warn("초대 토큰 검증 실패 - 토큰 없음: token={}", token);
            throw new EntityNotFoundException(ErrorCode.INVITATION_NOT_FOUND);
        }

        User user = findUser.get();
        if (!user.isInvitationValid()) {
            log.warn("초대 토큰 검증 실패 - 만료된 토큰: userId={}, expiresAt={}", user.getId(), user.getInvitationExpiresAt());
            throw new BusinessRuleViolationException(ErrorCode.INVITATION_EXPIRED);
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
