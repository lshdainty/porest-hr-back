package com.lshdainty.myhr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.myhr.domain.Dues;
import com.lshdainty.myhr.domain.DuesType;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DuesDto {
    private Long duesSeq;
    private String duesUserName;
    private int duesAmount;
    private DuesType duesType;
    private String duesDate;
    private String duesDetail;

    private int duesTotal;

    public DuesDto(Long seq) {
        this.duesSeq = seq;
    }

    public DuesDto(Dues dues) {
        this.duesSeq = dues.getSeq();
        this.duesUserName = dues.getUserName();
        this.duesAmount = dues.getAmount();
        this.duesType = dues.getType();
        this.duesDate = dues.getDate();
        this.duesDetail = dues.getDetail();
    }
}
