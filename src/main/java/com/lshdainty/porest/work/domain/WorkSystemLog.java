package com.lshdainty.porest.work.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.work.type.SystemType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 업무 시스템 로그 엔티티<br>
 * 웹상에서 시스템 체크를 기록하는 테이블<br>
 * - 사용자 정보: AuditingFields.createBy (자동 설정)<br>
 * - 체크 날짜: AuditingFields.createDate (자동 설정)
 */
@Entity
@Table(name = "work_system_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkSystemLog extends AuditingFields {
    /**
     * 시스템 로그 아이디<br>
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "system_log_id")
    private Long id;

    /**
     * 시스템 코드
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "system_code", nullable = false, length = 20)
    private SystemType code;

    /**
     * 시스템 로그 생성 정적 팩토리 메서드<br>
     * userId와 체크 날짜는 AuditingFields에서 자동으로 설정됨
     *
     * @param code 시스템 코드
     * @return WorkSystemLog
     */
    public static WorkSystemLog of(SystemType code) {
        WorkSystemLog log = new WorkSystemLog();
        log.code = code;
        return log;
    }
}
