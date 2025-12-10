package com.lshdainty.porest.work.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.work.type.CodeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "work_code")
public class WorkCode extends AuditingFields {
    /**
     * 업무 코드 아이디<br>
     * 코드 관리용 ID
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_code_id")
    private Long id;

    /**
     * 업무 코드
     */
    @Column(name = "work_code", nullable = false, length = 50)
    private String code;

    /**
     * 업무 코드명
     */
    @Column(name = "work_code_name", nullable = false, length = 50)
    private String name;

    /**
     * 코드 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "code_type", nullable = false, length = 10)
    private CodeType type;

    /**
     * 상위 코드 (자기 참조)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_code_id")
    private WorkCode parent;

    /**
     * 정렬 순서
     */
    @Column(name = "order_seq")
    private Integer orderSeq;

    /**
     * 삭제 여부
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1)
    private YNType isDeleted;

    /**
     * 업무 코드 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 업무 코드 생성할 것
     *
     * @return WorkCode
     */
    public static WorkCode createWorkCode(String code, String name, CodeType type, WorkCode parent, Integer orderSeq) {
        WorkCode workCode = new WorkCode();
        workCode.code = code;
        workCode.name = name;
        workCode.type = type;
        workCode.parent = parent;
        workCode.orderSeq = orderSeq;
        workCode.isDeleted = YNType.N;
        return workCode;
    }

    /**
     * 업무 코드 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 업무 코드 수정할 것
     */
    public void updateWorkCode(String code, String name, WorkCode parent, Integer orderSeq) {
        if (code != null) { this.code = code; }
        if (name != null) { this.name = name; }
        if (parent != null) { this.parent = parent; }
        if (orderSeq != null) { this.orderSeq = orderSeq; }
    }

    /**
     * 업무 코드 삭제 함수 (Soft Delete)<br>
     * is_deleted 플래그를 Y로 설정
     */
    public void deleteWorkCode() {
        this.isDeleted = YNType.Y;
    }
}
