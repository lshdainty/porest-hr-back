package com.lshdainty.porest.service;

import com.lshdainty.porest.common.config.properties.AppProperties;
import com.lshdainty.porest.user.service.EmailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("이메일 서비스 테스트")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
    }

    @Nested
    @DisplayName("초대 이메일 발송")
    class SendInvitationEmail {
        @Test
        @DisplayName("성공 - 초대 이메일이 발송된다")
        void sendInvitationEmailSuccess() throws MessagingException {
            // given
            String toEmail = "user@test.com";
            String userName = "테스트유저";
            String invitationToken = "test-token-123";

            MimeMessage mimeMessage = mock(MimeMessage.class);
            given(mailSender.createMimeMessage()).willReturn(mimeMessage);
            willDoNothing().given(mailSender).send(any(MimeMessage.class));

            AppProperties.Company company = new AppProperties.Company();
            company.setName("테스트회사");
            given(appProperties.getCompany()).willReturn(company);

            AppProperties.Frontend frontend = new AppProperties.Frontend();
            frontend.setBaseUrl("http://localhost:3000");
            given(appProperties.getFrontend()).willReturn(frontend);

            AppProperties.Email email = new AppProperties.Email();
            AppProperties.Email.Logo logo = new AppProperties.Email.Logo();
            logo.setPath("templates/email/logo.png");
            email.setLogo(logo);
            given(appProperties.getEmail()).willReturn(email);

            // when
            emailService.sendInvitationEmail(toEmail, userName, invitationToken);

            // then
            then(mailSender).should().send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("실패 - 이메일 발송 실패 시 예외가 발생한다")
        void sendInvitationEmailFailure() throws MessagingException {
            // given
            String toEmail = "user@test.com";
            String userName = "테스트유저";
            String invitationToken = "test-token-123";

            MimeMessage mimeMessage = mock(MimeMessage.class);
            given(mailSender.createMimeMessage()).willReturn(mimeMessage);

            AppProperties.Company company = new AppProperties.Company();
            company.setName("테스트회사");
            given(appProperties.getCompany()).willReturn(company);

            AppProperties.Frontend frontend = new AppProperties.Frontend();
            frontend.setBaseUrl("http://localhost:3000");
            given(appProperties.getFrontend()).willReturn(frontend);

            AppProperties.Email email = new AppProperties.Email();
            AppProperties.Email.Logo logo = new AppProperties.Email.Logo();
            logo.setPath("nonexistent/path/logo.png");
            email.setLogo(logo);
            given(appProperties.getEmail()).willReturn(email);

            willThrow(new RuntimeException("Mail send failed"))
                    .given(mailSender).send(any(MimeMessage.class));

            // when & then
            assertThatThrownBy(() -> emailService.sendInvitationEmail(toEmail, userName, invitationToken))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("성공 - 로고 이미지 로드 실패해도 이메일은 발송된다")
        void sendInvitationEmailWithoutLogo() throws MessagingException {
            // given
            String toEmail = "user@test.com";
            String userName = "테스트유저";
            String invitationToken = "test-token-123";

            MimeMessage mimeMessage = mock(MimeMessage.class);
            given(mailSender.createMimeMessage()).willReturn(mimeMessage);
            willDoNothing().given(mailSender).send(any(MimeMessage.class));

            AppProperties.Company company = new AppProperties.Company();
            company.setName("테스트회사");
            given(appProperties.getCompany()).willReturn(company);

            AppProperties.Frontend frontend = new AppProperties.Frontend();
            frontend.setBaseUrl("http://localhost:3000");
            given(appProperties.getFrontend()).willReturn(frontend);

            AppProperties.Email email = new AppProperties.Email();
            AppProperties.Email.Logo logo = new AppProperties.Email.Logo();
            logo.setPath("nonexistent/path/logo.png"); // 존재하지 않는 경로
            email.setLogo(logo);
            given(appProperties.getEmail()).willReturn(email);

            // when
            emailService.sendInvitationEmail(toEmail, userName, invitationToken);

            // then
            then(mailSender).should().send(any(MimeMessage.class));
        }
    }
}
