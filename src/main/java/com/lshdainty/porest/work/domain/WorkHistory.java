package com.lshdainty.porest.work.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "work_history")
public class WorkHistory extends AuditingFields {
    /**
     * 이력 관리용 시퀀스
     */
    @Id @GeneratedValue
    @Column(name = "work_history_seq")
    private Long seq;

    /**
     * 업무 날짜
     */
    @Column(name = "work_date")
    private LocalDate date;

    /**
     * 근무자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_user")
    private User user;

    /**
     * 업무 그룹
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_group")
    private WorkCode group;

    /**
     * 업무 파트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_part")
    private WorkCode part;

    /**
     * 업무 분류
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_class")
    private WorkCode classes;

    /**
     * 업무 시간
     */
    @Column(name = "work_hour")
    private BigDecimal hours;

    /**
     * 업무 내용
     */
    @Column(name = "work_content")
    private String content;

    /**
     * 삭제 여부
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted")
    private YNType isDeleted;

    /**
     * 업무 이력 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 업무 이력 생성할 것
     *
     * @return WorkHistory
     */
    public static WorkHistory createWorkHistory(LocalDate date, User user, WorkCode group, WorkCode part, WorkCode classes, BigDecimal hours, String content) {
        WorkHistory workHistory = new WorkHistory();
        workHistory.date = date;
        workHistory.user = user;
        workHistory.group = group;
        workHistory.part = part;
        workHistory.classes = classes;
        workHistory.hours = hours;
        workHistory.content = content;
        workHistory.isDeleted = YNType.N;
        return workHistory;
    }

    /**
     * 업무 이력 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 업무 이력 수정할 것
     */
    public void updateWorkHistory(LocalDate date, User user, WorkCode group, WorkCode part, WorkCode classes, BigDecimal hours, String content) {
        if (date != null) { this.date = date; }
        if (user != null) { this.user = user; }
        if (group != null) { this.group = group; }
        if (part != null) { this.part = part; }
        if (classes != null) { this.classes = classes; }
        if (hours != null) { this.hours = hours; }
        if (content != null) { this.content = content; }
    }

    /**
     * 업무 이력 삭제 함수 (Soft Delete)<br>
     * is_deleted 플래그를 Y로 설정
     */
    public void deleteWorkHistory() {
        this.isDeleted = YNType.Y;
    }
}
