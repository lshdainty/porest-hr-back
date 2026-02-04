package com.porest.hr.common.domain;

import com.porest.core.domain.AuditingFields;
import com.porest.core.util.HttpUtils;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * IP 주소 추적 기능이 포함된 Auditing 필드
 * <p>
 * Core의 AuditingFields를 상속받아 IP 필드를 추가합니다.
 * 엔티티 생성/수정 시 클라이언트 IP 주소를 자동으로 기록합니다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingFieldsWithIp.IpAuditingListener.class)
public abstract class AuditingFieldsWithIp extends AuditingFields {

    /**
     * 생성 IP
     */
    @Setter
    @Column(name = "create_ip", length = 45, updatable = false)
    private String createIp;

    /**
     * 최종 수정 IP
     */
    @Setter
    @Column(name = "modify_ip", length = 45)
    private String modifyIp;

    /**
     * IP 주소 자동 설정을 위한 JPA EntityListener
     */
    public static class IpAuditingListener {
        @PrePersist
        public void prePersist(Object entity) {
            if (entity instanceof AuditingFieldsWithIp e) {
                String ip = HttpUtils.getClientIp();
                if (ip != null) {
                    e.setCreateIp(ip);
                    e.setModifyIp(ip);
                }
            }
        }

        @PreUpdate
        public void preUpdate(Object entity) {
            if (entity instanceof AuditingFieldsWithIp e) {
                String ip = HttpUtils.getClientIp();
                if (ip != null) {
                    e.setModifyIp(ip);
                }
            }
        }
    }
}
