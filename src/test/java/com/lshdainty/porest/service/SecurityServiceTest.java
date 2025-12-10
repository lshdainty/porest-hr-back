package com.lshdainty.porest.service;

import com.lshdainty.porest.common.exception.BusinessRuleViolationException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.security.service.SecurityServiceImpl;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.repository.UserRepository;
import com.lshdainty.porest.user.service.dto.UserServiceDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("보안 서비스 테스트")
class SecurityServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityServiceImpl securityService;

    @Nested
    @DisplayName("초대 토큰 검증")
    class ValidateInvitationToken {
        @Test
        @DisplayName("성공 - 유효한 초대 토큰을 검증한다")
        void validateInvitationTokenSuccess() {
            // given
            User user = User.createInvitedUser("user1", "테스트유저", "test@test.com",
                    OriginCompanyType.SKAX, "9 ~ 6", LocalDate.now(), CountryCode.KR);
            String token = user.getInvitationToken();

            given(userRepository.findByInvitationToken(token)).willReturn(Optional.of(user));

            // when
            UserServiceDto result = securityService.validateInvitationToken(token);

            // then
            assertThat(result.getId()).isEqualTo("user1");
            assertThat(result.getName()).isEqualTo("테스트유저");
            assertThat(result.getEmail()).isEqualTo("test@test.com");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 토큰이면 예외가 발생한다")
        void validateInvitationTokenFailNotFound() {
            // given
            String invalidToken = "invalid-token";
            given(userRepository.findByInvitationToken(invalidToken)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> securityService.validateInvitationToken(invalidToken))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 만료된 토큰이면 예외가 발생한다")
        void validateInvitationTokenFailExpired() {
            // given
            User user = User.createInvitedUser("user1", "테스트유저", "test@test.com",
                    OriginCompanyType.SKAX, "9 ~ 6", LocalDate.now(), CountryCode.KR);
            String token = user.getInvitationToken();

            // 만료일을 과거로 설정
            ReflectionTestUtils.setField(user, "invitationExpiresAt", LocalDateTime.now().minusDays(1));

            given(userRepository.findByInvitationToken(token)).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> securityService.validateInvitationToken(token))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test
        @DisplayName("성공 - 역할 정보도 포함된다")
        void validateInvitationTokenWithRoles() {
            // given
            User user = User.createInvitedUser("user1", "테스트유저", "test@test.com",
                    OriginCompanyType.SKAX, "9 ~ 6", LocalDate.now(), CountryCode.KR);
            String token = user.getInvitationToken();

            given(userRepository.findByInvitationToken(token)).willReturn(Optional.of(user));

            // when
            UserServiceDto result = securityService.validateInvitationToken(token);

            // then
            assertThat(result.getId()).isEqualTo("user1");
            assertThat(result.getRoleNames()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 초대 상태 정보가 포함된다")
        void validateInvitationTokenWithInvitationStatus() {
            // given
            User user = User.createInvitedUser("user1", "테스트유저", "test@test.com",
                    OriginCompanyType.SKAX, "9 ~ 6", LocalDate.now(), CountryCode.KR);
            String token = user.getInvitationToken();

            given(userRepository.findByInvitationToken(token)).willReturn(Optional.of(user));

            // when
            UserServiceDto result = securityService.validateInvitationToken(token);

            // then
            assertThat(result.getInvitationStatus()).isNotNull();
            assertThat(result.getInvitationSentAt()).isNotNull();
            assertThat(result.getInvitationExpiresAt()).isNotNull();
        }
    }
}
