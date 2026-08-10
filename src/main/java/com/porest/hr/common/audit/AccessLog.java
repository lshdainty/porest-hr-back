package com.porest.hr.common.audit;

import com.porest.core.audit.AbstractAccessLog;
import com.porest.core.audit.AccessLogEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 개인정보 접속기록 엔티티<br>
 * 개인정보취급자가 <strong>타인의</strong> 개인정보를 조회·수정·다운로드한 기록<br>
 * 「개인정보의 안전성 확보조치 기준」 제8조
 *
 * <p>본인이 자기 정보를 보는 것은 취급자 접속이 아니므로 기록 대상이 아니다.
 * 공통 컬럼은 {@link AbstractAccessLog} 에 있다.
 *
 * <p>감사 필드(create_by/modify_by)를 상속하지 않는다 — 이 테이블 자체가 감사 기록이라
 * 수정 이력이 있을 수 없고, 오히려 수정·삭제가 불가능해야 한다.
 *
 * @see AbstractAccessLog 공통 컬럼
 * @see AccessLogPortImpl 저장 담당
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "access_logs")
public class AccessLog extends AbstractAccessLog {

    /**
     * 접속기록 순번<br>
     * 테이블 관리용 Primary Key (자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id")
    private Long rowId;

    /**
     * 기록 생성<br>
     * core 가 넘겨준 기록 내용을 엔티티로 옮긴다
     *
     * @param entry 기록 내용
     * @return AccessLog 엔티티
     */
    public static AccessLog from(AccessLogEntry entry) {
        AccessLog accessLog = new AccessLog();
        accessLog.apply(entry);
        return accessLog;
    }
}
