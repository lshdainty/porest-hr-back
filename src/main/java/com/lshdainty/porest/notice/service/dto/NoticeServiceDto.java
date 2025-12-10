package com.lshdainty.porest.notice.service.dto;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.notice.type.NoticeType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NoticeServiceDto {
    private Long id;
    private String writerId;
    private String writerName;
    private String title;
    private String content;
    private NoticeType type;
    private YNType isPinned;
    private Long viewCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
}
