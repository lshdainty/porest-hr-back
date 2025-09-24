package com.lshdainty.porest.department.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentDto {
    private Long departmentId;
    private String departmentName;
    private String departmentNameKR;
    private Long parentId;
    private String headUserId;
    private Long treeLevel;
    private String departmentDesc;
    private List<DepartmentDto> children;
}
