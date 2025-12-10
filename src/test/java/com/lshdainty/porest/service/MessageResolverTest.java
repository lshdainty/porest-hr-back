package com.lshdainty.porest.service;

import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.message.MessageKey;
import com.lshdainty.porest.common.util.MessageResolver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("메시지 리졸버 테스트")
class MessageResolverTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private MessageResolver messageResolver;

    @Nested
    @DisplayName("MessageKey 기반 메시지 조회")
    class GetMessageByMessageKey {
        @Test
        @DisplayName("성공 - MessageKey로 메시지를 반환한다")
        void getMessageByMessageKeySuccess() {
            // given
            MessageKey messageKey = MessageKey.NOT_FOUND_USER;
            String expectedMessage = "사용자를 찾을 수 없습니다";

            given(messageSource.getMessage(eq(messageKey.getKey()), eq(null), eq(messageKey.getKey()), any(Locale.class)))
                    .willReturn(expectedMessage);

            // when
            String result = messageResolver.getMessage(messageKey);

            // then
            assertThat(result).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("성공 - MessageKey로 파라미터 포함 메시지를 반환한다")
        void getMessageByMessageKeyWithArgs() {
            // given
            MessageKey messageKey = MessageKey.NOT_FOUND_USER;
            String expectedMessage = "사용자 user1을 찾을 수 없습니다";
            Object[] args = new Object[]{"user1"};

            given(messageSource.getMessage(eq(messageKey.getKey()), eq(args), eq(messageKey.getKey()), any(Locale.class)))
                    .willReturn(expectedMessage);

            // when
            String result = messageResolver.getMessage(messageKey, args);

            // then
            assertThat(result).isEqualTo(expectedMessage);
        }
    }

    @Nested
    @DisplayName("ErrorCode 기반 메시지 조회")
    class GetMessageByErrorCode {
        @Test
        @DisplayName("성공 - ErrorCode로 메시지를 반환한다")
        void getMessageByErrorCodeSuccess() {
            // given
            ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;
            String expectedMessage = "사용자를 찾을 수 없습니다";

            given(messageSource.getMessage(eq(errorCode.getMessageKey()), eq(null), eq(errorCode.getCode()), any(Locale.class)))
                    .willReturn(expectedMessage);

            // when
            String result = messageResolver.getMessage(errorCode);

            // then
            assertThat(result).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("성공 - ErrorCode로 파라미터 포함 메시지를 반환한다")
        void getMessageByErrorCodeWithArgs() {
            // given
            ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;
            String expectedMessage = "사용자 user1을 찾을 수 없습니다";
            Object[] args = new Object[]{"user1"};

            given(messageSource.getMessage(eq(errorCode.getMessageKey()), eq(args), eq(errorCode.getCode()), any(Locale.class)))
                    .willReturn(expectedMessage);

            // when
            String result = messageResolver.getMessage(errorCode, args);

            // then
            assertThat(result).isEqualTo(expectedMessage);
        }
    }

    @Nested
    @DisplayName("문자열 키 기반 메시지 조회")
    class GetMessageByStringKey {
        @Test
        @DisplayName("성공 - 문자열 키로 메시지를 반환한다")
        void getMessageByStringKeySuccess() {
            // given
            String messageKey = "test.message";
            String expectedMessage = "테스트 메시지";

            given(messageSource.getMessage(eq(messageKey), eq(null), eq(messageKey), any(Locale.class)))
                    .willReturn(expectedMessage);

            // when
            String result = messageResolver.getMessage(messageKey);

            // then
            assertThat(result).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("성공 - 문자열 키로 파라미터 포함 메시지를 반환한다")
        void getMessageByStringKeyWithArgs() {
            // given
            String messageKey = "test.message.with.param";
            String expectedMessage = "파라미터: value1";
            Object[] args = new Object[]{"value1"};

            given(messageSource.getMessage(eq(messageKey), eq(args), eq(messageKey), any(Locale.class)))
                    .willReturn(expectedMessage);

            // when
            String result = messageResolver.getMessage(messageKey, args);

            // then
            assertThat(result).isEqualTo(expectedMessage);
        }
    }
}
