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
     * @param userId 임시 사용자 ID
     * @param invitationCode 8자리 초대 코드
     */
    void sendInvitationEmail(String toEmail, String userName, String userId, String invitationCode);

    /**
     * 비밀번호 초기화 이메일 발송
     *
     * @param toEmail 수신자 이메일
     * @param userName 수신자 이름
     * @param tempPassword 임시 비밀번호
     */
    void sendPasswordResetEmail(String toEmail, String userName, String tempPassword);
}
