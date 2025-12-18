package com.lshdainty.porest.user.service;

import com.lshdainty.porest.common.config.properties.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private byte[] loadLogoImage() {
        try {
            String logoPath = appProperties.getEmail().getLogo().getPath();
            ClassPathResource resource = new ClassPathResource(logoPath);
            // InputStream을 사용하여 파일 읽기 (JAR 내부에서도 작동)
            return resource.getInputStream().readAllBytes();
        } catch (Exception e) {
            log.error("로고 이미지 파일 로드 실패: {}", appProperties.getEmail().getLogo().getPath(), e);
            return null;
        }
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendInvitationEmail(String toEmail, String userName, String userId, String invitationCode) {
        log.debug("초대 이메일 발송 시작: toEmail={}, userName={}, userId={}", toEmail, userName, userId);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String companyName = appProperties.getCompany().getName();
            String frontendBaseUrl = appProperties.getFrontend().getBaseUrl();

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(String.format("%s 회원가입 초대", companyName));

            String signupLink = String.format("%s/signup", frontendBaseUrl);

            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 40px 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif; background-color: #f5f5f5;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); overflow: hidden;">
                        <div style="padding: 40px 30px;">
                            <h2 style="color: #333333; margin-top: 0; font-size: 24px;">%s 회원가입 초대</h2>
                            <p style="color: #666666; line-height: 1.6; margin: 16px 0;">안녕하세요, %s님</p>
                            <p style="color: #666666; line-height: 1.6; margin: 16px 0;">%s 서비스에 초대되셨습니다. 아래 정보를 사용하여 회원가입을 진행해주세요.</p>
                            <div style="background-color: #f8f9fa; border: 1px solid #e9ecef; border-radius: 6px; padding: 20px; margin: 24px 0;">
                                <div style="margin-bottom: 12px;">
                                    <p style="color: #666666; font-size: 14px; margin: 0 0 4px 0;">아이디 (임시)</p>
                                    <p style="color: #333333; font-size: 16px; font-weight: bold; margin: 0;">%s</p>
                                </div>
                                <div style="margin-bottom: 12px;">
                                    <p style="color: #666666; font-size: 14px; margin: 0 0 4px 0;">이름</p>
                                    <p style="color: #333333; font-size: 16px; font-weight: bold; margin: 0;">%s</p>
                                </div>
                                <div style="margin-bottom: 12px;">
                                    <p style="color: #666666; font-size: 14px; margin: 0 0 4px 0;">이메일</p>
                                    <p style="color: #333333; font-size: 16px; font-weight: bold; margin: 0;">%s</p>
                                </div>
                                <div>
                                    <p style="color: #666666; font-size: 14px; margin: 0 0 4px 0;">초대 코드</p>
                                    <p style="color: #007bff; font-size: 24px; font-weight: bold; margin: 0; letter-spacing: 2px;">%s</p>
                                </div>
                            </div>
                            <p style="color: #666666; line-height: 1.6; margin: 16px 0;">아래 버튼을 클릭하여 회원가입 페이지로 이동한 후, 위 정보를 입력해주세요.</p>
                            <a href="%s" style="display: inline-block; background-color: #007bff; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 6px; margin: 24px 0; font-weight: 500;">회원가입 하기</a>
                            <p style="color: #999999; font-size: 14px; line-height: 1.6; margin: 16px 0;">이 초대는 48시간 후에 만료됩니다.</p>
                        </div>
                        <div style="background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef;">
                            <div style="margin-bottom: 16px;">
                                <img src="cid:logo" alt="Logo" style="max-width: 200px; height: auto;"/>
                            </div>
                            <p style="color: #6c757d; font-size: 12px; line-height: 1.5; margin: 8px 0;">Copyright 2025 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, companyName, userName, companyName, userId, userName, toEmail, invitationCode, signupLink, companyName);

            helper.setText(htmlContent, true);

            // 로고 이미지를 CID 첨부로 추가
            byte[] logoImageBytes = loadLogoImage();
            if (logoImageBytes != null) {
                String logoPath = appProperties.getEmail().getLogo().getPath();
                String contentType = logoPath.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
                helper.addInline("logo", new ByteArrayResource(logoImageBytes), contentType);
            } else {
                log.warn("로고 이미지 로드 실패, 이미지 없이 이메일 발송");
            }

            mailSender.send(message);
            log.info("초대 이메일 발송 완료: {}", toEmail);
        } catch (MessagingException e) {
            log.error("초대 이메일 발송 실패: {}", toEmail, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String tempPassword) {
        log.debug("비밀번호 초기화 이메일 발송 시작: toEmail={}, userName={}", toEmail, userName);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String companyName = appProperties.getCompany().getName();
            String frontendBaseUrl = appProperties.getFrontend().getBaseUrl();

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(String.format("%s 비밀번호 초기화 안내", companyName));

            String loginLink = String.format("%s/login", frontendBaseUrl);

            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 40px 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif; background-color: #f5f5f5;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); overflow: hidden;">
                        <div style="padding: 40px 30px;">
                            <h2 style="color: #333333; margin-top: 0; font-size: 24px;">%s 비밀번호 초기화</h2>
                            <p style="color: #666666; line-height: 1.6; margin: 16px 0;">안녕하세요, %s님</p>
                            <p style="color: #666666; line-height: 1.6; margin: 16px 0;">비밀번호 초기화 요청에 따라 임시 비밀번호가 발급되었습니다.</p>
                            <div style="background-color: #f8f9fa; border: 1px solid #e9ecef; border-radius: 6px; padding: 20px; margin: 24px 0; text-align: center;">
                                <p style="color: #666666; font-size: 14px; margin: 0 0 8px 0;">임시 비밀번호</p>
                                <p style="color: #333333; font-size: 24px; font-weight: bold; margin: 0; letter-spacing: 2px;">%s</p>
                            </div>
                            <p style="color: #666666; line-height: 1.6; margin: 16px 0;">위 임시 비밀번호로 로그인 후 보안을 위해 비밀번호를 변경해주세요.</p>
                            <a href="%s" style="display: inline-block; background-color: #007bff; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 6px; margin: 24px 0; font-weight: 500;">로그인 하기</a>
                            <p style="color: #dc3545; font-size: 14px; line-height: 1.6; margin: 16px 0;">※ 본인이 요청하지 않은 경우 즉시 관리자에게 문의해주세요.</p>
                        </div>
                        <div style="background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef;">
                            <div style="margin-bottom: 16px;">
                                <img src="cid:logo" alt="Logo" style="max-width: 200px; height: auto;"/>
                            </div>
                            <p style="color: #6c757d; font-size: 12px; line-height: 1.5; margin: 8px 0;">Copyright 2025 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, companyName, userName, tempPassword, loginLink, companyName);

            helper.setText(htmlContent, true);

            // 로고 이미지를 CID 첨부로 추가
            byte[] logoImageBytes = loadLogoImage();
            if (logoImageBytes != null) {
                String logoPath = appProperties.getEmail().getLogo().getPath();
                String fileName = logoPath.substring(logoPath.lastIndexOf("/") + 1);
                String contentType = logoPath.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
                helper.addInline("logo", new ByteArrayResource(logoImageBytes), contentType);
            } else {
                log.warn("로고 이미지 로드 실패, 이미지 없이 이메일 발송");
            }

            mailSender.send(message);
            log.info("비밀번호 초기화 이메일 발송 완료: {}", toEmail);
        } catch (MessagingException e) {
            log.error("비밀번호 초기화 이메일 발송 실패: {}", toEmail, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}
