package com.lshdainty.myhr.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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
    private Long createBy;

    @Column(name = "creaet_ip")
    private String createIP;

//    @LastModifiedDate
    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(name = "modify_by")
    private Long modifyBy;

    @Column(name = "modify_ip")
    private String modifyIP;

    public void setCreated(LocalDateTime date, Long no, String ip) {
        this.createDate = date;
        this.createBy = no;
        this.createIP = ip;
    }

    public void setCreated(Long no, String ip) {
        this.createBy = no;
        this.createIP = ip;
    }

    public void setmodified(LocalDateTime date, Long no, String ip) {
        this.modifyDate = date;
        this.modifyBy = no;
        this.modifyIP = ip;
    }

    public void setmodified(Long no, String ip) {
        this.modifyBy = no;
        this.modifyIP = ip;
    }
}
