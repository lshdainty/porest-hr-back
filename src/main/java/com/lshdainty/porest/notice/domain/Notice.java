package com.lshdainty.porest.notice.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.notice.type.NoticeType;
import com.lshdainty.porest.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notice")
public class Notice extends AuditingFields {
    /**
     * 공지사항 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id", columnDefinition = "bigint(20) COMMENT '공지사항 아이디'")
    private Long id;

    /**
     * 작성자 객체<br>
     * 테이블 컬럼은 writer_id<br>
     * 공지사항을 작성한 유저 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    /**
     * 공지사항 제목<br>
     * 공지사항의 제목
     */
    @Column(name = "notice_title", nullable = false, length = 100, columnDefinition = "varchar(100) NOT NULL COMMENT '공지사항 제목'")
    private String title;

    /**
     * 공지사항 내용<br>
     * 공지사항의 상세 내용 (Large Object 타입)
     */
    @Lob
    @Column(name = "notice_content", nullable = false, columnDefinition = "longtext NOT NULL COMMENT '공지사항 내용'")
    private String content;

    /**
     * 공지사항 타입<br>
     * 공지사항의 종류를 구분하기 위한 타입
     */
    @Column(name = "notice_type", nullable = false, length = 15, columnDefinition = "varchar(15) NOT NULL COMMENT '공지사항 타입'")
    @Enumerated(EnumType.STRING)
    private NoticeType type;

    /**
     * 상단 고정 여부<br>
     * 공지사항 목록 상단에 고정 표시 여부
     */
    @Column(name = "is_pinned", nullable = false, length = 1, columnDefinition = "varchar(1) DEFAULT 'N' NOT NULL COMMENT '상단 고정 여부'")
    @Enumerated(EnumType.STRING)
    private YNType isPinned;

    /**
     * 조회수<br>
     * 공지사항이 조회된 횟수
     */
    @Column(name = "view_count", nullable = false, columnDefinition = "bigint(20) DEFAULT 0 NOT NULL COMMENT '조회수'")
    private Long viewCount;

    /**
     * 게시 시작일<br>
     * 공지사항이 표시되기 시작하는 날짜
     */
    @Column(name = "start_date", columnDefinition = "date COMMENT '게시 시작일'")
    private LocalDate startDate;

    /**
     * 게시 종료일<br>
     * 공지사항이 표시 종료되는 날짜
     */
    @Column(name = "end_date", columnDefinition = "date COMMENT '게시 종료일'")
    private LocalDate endDate;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Column(name = "is_deleted", nullable = false, length = 1, columnDefinition = "varchar(1) DEFAULT 'N' NOT NULL COMMENT '삭제 여부'")
    @Enumerated(EnumType.STRING)
    private YNType isDeleted;

    /**
     * 공지사항 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 공지사항 생성할 것
     *
     * @param writer 작성자
     * @param title 제목
     * @param content 내용
     * @param type 공지사항 타입
     * @param isPinned 상단 고정 여부
     * @param startDate 게시 시작일
     * @param endDate 게시 종료일
     * @return Notice
     */
    public static Notice createNotice(User writer, String title, String content, NoticeType type,
                                       YNType isPinned, LocalDate startDate, LocalDate endDate) {
        Notice notice = new Notice();
        notice.writer = writer;
        notice.title = title;
        notice.content = content;
        notice.type = type;
        notice.isPinned = isPinned;
        notice.viewCount = 0L;
        notice.startDate = startDate;
        notice.endDate = endDate;
        notice.isDeleted = YNType.N;
        return notice;
    }

    /**
     * 공지사항 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 공지사항 수정할 것
     *
     * @param title 제목
     * @param content 내용
     * @param type 공지사항 타입
     * @param isPinned 상단 고정 여부
     * @param startDate 게시 시작일
     * @param endDate 게시 종료일
     */
    public void updateNotice(String title, String content, NoticeType type,
                              YNType isPinned, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.isPinned = isPinned;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * 공지사항 삭제 함수 (소프트 딜리트)<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 공지사항 삭제할 것
     */
    public void deleteNotice() {
        this.isDeleted = YNType.Y;
    }

    /**
     * 조회수 증가 함수
     */
    public void increaseViewCount() {
        this.viewCount++;
    }
}
