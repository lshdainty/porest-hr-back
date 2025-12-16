package com.lshdainty.porest.user.service;

/**
 * 이메일 발송 서비스 인터페이스
 */
public interface EmailService {

    /**
     * 회원가입 초대 이메일 발송
     *
     * @param toEmail 수신자 이메일
     * @param userName 수신자 이름
     * @param invitationToken 초대 토큰
     */
    void sendInvitationEmail(String toEmail, String userName, String invitationToken);

    /**
     * 비밀번호 초기화 이메일 발송
     *
     * @param toEmail 수신자 이메일
     * @param userName 수신자 이름
     * @param tempPassword 임시 비밀번호
     */
    void sendPasswordResetEmail(String toEmail, String userName, String tempPassword);
}
