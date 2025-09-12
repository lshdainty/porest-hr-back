package com.lshdainty.porest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
public class AuditingFields {
    @CreatedDate
    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "create_by")
    private String createBy;

    @Column(name = "create_ip")
    private String createIP;

//    @LastModifiedDate
    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(name = "modify_by")
    private String modifyBy;

    @Column(name = "modify_ip")
    private String modifyIP;

    public void setCreated(LocalDateTime date, String id, String ip) {
        this.createDate = date;
        this.createBy = id;
        this.createIP = ip;
    }

    public void setCreated(String id, String ip) {
        this.createBy = id;
        this.createIP = ip;
    }

    public void setModified(LocalDateTime date, String id, String ip) {
        this.modifyDate = date;
        this.modifyBy = id;
        this.modifyIP = ip;
    }

    public void setModified(String id, String ip) {
        this.modifyBy = id;
        this.modifyIP = ip;
    }
}
