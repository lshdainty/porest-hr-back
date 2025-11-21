package com.lshdainty.porest.work.repository.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class WorkHistorySearchCondition {
    private LocalDate startDate;
    private LocalDate endDate;
    private String userName; // 작성자 이름 (검색용)
    private String userId; // 작성자 ID (필터용)
    private Long groupSeq;
    private Long partSeq;
    private Long divisionSeq;
    private String sortType; // "LATEST" (default), "OLDEST"
}
