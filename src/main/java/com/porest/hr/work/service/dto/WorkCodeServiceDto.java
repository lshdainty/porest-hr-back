package com.porest.hr.work.service.dto;

import com.porest.hr.work.type.CodeType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkCodeServiceDto {
    private Long id;
    private String code;
    private String name;
    private CodeType type;
    private Integer orderSeq;
    private Long parentId;
}
