package com.lshdainty.porest.company.controller.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.department.controller.dto.DepartmentApiDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class CompanyApiDto {
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "회사 등록 요청")
    public static class RegistCompanyReq {
        @Schema(description = "회사 ID", example = "POREST")
        private String companyId;

        @Schema(description = "회사 이름", example = "포레스트")
        private String companyName;

        @Schema(description = "회사 설명", example = "우리 회사입니다")
        private String companyDesc;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "회사 등록 응답")
    public static class RegistCompanyResp {
        @Schema(description = "등록된 회사 ID", example = "POREST")
        private String companyId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "회사 조회 응답")
    public static class SearchCompanyResp {
        @Schema(description = "회사 ID", example = "POREST")
        private String companyId;

        @Schema(description = "회사 이름", example = "포레스트")
        private String companyName;

        @Schema(description = "회사 설명", example = "우리 회사입니다")
        private String companyDesc;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "회사 수정 요청")
    public static class EditCompanyReq {
        @Schema(description = "회사 이름", example = "포레스트")
        private String companyName;

        @Schema(description = "회사 설명", example = "우리 회사입니다")
        private String companyDesc;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "회사 및 부서 조회 응답")
    public static class SearchCompanyWithDepartmentsResp {
        @Schema(description = "회사 ID", example = "POREST")
        private String companyId;

        @Schema(description = "회사 이름", example = "포레스트")
        private String companyName;

        @Schema(description = "회사 설명", example = "우리 회사입니다")
        private String companyDesc;

        @Schema(description = "소속 부서 목록")
        private List<DepartmentApiDto.SearchDepartmentWithChildrenResp> departments;
    }
}
