package com.porest.hr.work.domain;

import com.porest.hr.common.domain.AuditingFieldsWithIp;
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
public class WorkSystemLog extends AuditingFieldsWithIp {
    /**
     * 행 아이디<br>
     * 테이블 관리용 PK (auto increment)
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id")
    private Long rowId;

    /**
     * 시스템 코드
     */
    @Column(name = "system_code", nullable = false, length = 20)
    private String code;

    /**
     * 시스템 로그 생성 정적 팩토리 메서드<br>
     * userId와 체크 날짜는 AuditingFields에서 자동으로 설정됨
     *
     * @param code 시스템 코드
     * @return WorkSystemLog
     */
    public static WorkSystemLog of(String code) {
        WorkSystemLog log = new WorkSystemLog();
        log.code = code;
        return log;
    }
}
