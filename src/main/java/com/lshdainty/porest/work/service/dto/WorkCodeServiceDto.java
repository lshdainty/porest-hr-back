package com.lshdainty.porest.work.service.dto;

import com.lshdainty.porest.work.type.CodeType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkCodeServiceDto {
    private Long seq;
    private String code;
    private String name;
    private CodeType type;
    private Integer orderSeq;
    private Long parentSeq;
}
