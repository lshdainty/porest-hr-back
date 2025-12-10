package com.lshdainty.porest.department.controller.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.department.service.dto.DepartmentServiceDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class DepartmentApiDto {
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "부서 등록 요청")
    public static class RegistDepartmentReq {
        @Schema(description = "부서명 (영문)", example = "Development")
        private String departmentName;

        @Schema(description = "부서명 (한글)", example = "개발팀")
        private String departmentNameKr;

        @Schema(description = "상위 부서 ID", example = "1")
        private Long parentId;

        @Schema(description = "부서장 사용자 ID", example = "admin")
        private String headUserId;

        @Schema(description = "트리 레벨", example = "1")
        private Long treeLevel;

        @Schema(description = "부서 설명", example = "개발 담당 부서")
        private String departmentDesc;

        @Schema(description = "색상 코드", example = "#FF5733")
        private String colorCode;

        @Schema(description = "회사 ID", example = "POREST")
        private String companyId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "부서 등록 응답")
    public static class RegistDepartmentResp {
        @Schema(description = "등록된 부서 ID", example = "1")
        private Long departmentId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "부서 수정 요청")
    public static class EditDepartmentReq {
        @Schema(description = "부서명 (영문)", example = "Development")
        private String departmentName;

        @Schema(description = "부서명 (한글)", example = "개발팀")
        private String departmentNameKr;

        @Schema(description = "상위 부서 ID", example = "1")
        private Long parentId;

        @Schema(description = "부서장 사용자 ID", example = "admin")
        private String headUserId;

        @Schema(description = "트리 레벨", example = "1")
        private Long treeLevel;

        @Schema(description = "부서 설명", example = "개발 담당 부서")
        private String departmentDesc;

        @Schema(description = "색상 코드", example = "#FF5733")
        private String colorCode;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "부서 조회 응답")
    public static class SearchDepartmentResp {
        @Schema(description = "부서 ID", example = "1")
        private Long departmentId;

        @Schema(description = "부서명 (영문)", example = "Development")
        private String departmentName;

        @Schema(description = "부서명 (한글)", example = "개발팀")
        private String departmentNameKr;

        @Schema(description = "상위 부서 ID", example = "1")
        private Long parentId;

        @Schema(description = "부서장 사용자 ID", example = "admin")
        private String headUserId;

        @Schema(description = "트리 레벨", example = "1")
        private Long treeLevel;

        @Schema(description = "부서 설명", example = "개발 담당 부서")
        private String departmentDesc;

        @Schema(description = "색상 코드", example = "#FF5733")
        private String colorCode;

        @Schema(description = "회사 ID", example = "POREST")
        private String companyId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "부서 및 하위 부서 조회 응답")
    public static class SearchDepartmentWithChildrenResp {
        @Schema(description = "부서 ID", example = "1")
        private Long departmentId;

        @Schema(description = "부서명 (영문)", example = "Development")
        private String departmentName;

        @Schema(description = "부서명 (한글)", example = "개발팀")
        private String departmentNameKr;

        @Schema(description = "상위 부서 ID", example = "1")
        private Long parentId;

        @Schema(description = "부서장 사용자 ID", example = "admin")
        private String headUserId;

        @Schema(description = "트리 레벨", example = "1")
        private Long treeLevel;

        @Schema(description = "부서 설명", example = "개발 담당 부서")
        private String departmentDesc;

        @Schema(description = "색상 코드", example = "#FF5733")
        private String colorCode;

        @Schema(description = "회사 ID", example = "POREST")
        private String companyId;

        @Schema(description = "하위 부서 목록")
        private List<SearchDepartmentWithChildrenResp> children;

        /**
         * DepartmentServiceDto -> SearchDepartmentWithChildrenResp 변환 (자식 포함, 재귀적)
         */
        public static SearchDepartmentWithChildrenResp fromServiceDto(DepartmentServiceDto serviceDto) {
            if (serviceDto == null) return null;

            return new SearchDepartmentWithChildrenResp(
                    serviceDto.getId(),
                    serviceDto.getName(),
                    serviceDto.getNameKR(),
                    serviceDto.getParentId(),
                    serviceDto.getHeadUserId(),
                    serviceDto.getLevel(),
                    serviceDto.getDesc(),
                    serviceDto.getColor(),
                    serviceDto.getCompanyId(),
                    serviceDto.getChildren() != null
                            ? serviceDto.getChildren().stream()
                            .map(SearchDepartmentWithChildrenResp::fromServiceDto)
                            .toList()
                            : null
            );
        }
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "사용자-부서 정보")
    public static class UserDepartmentInfo {
        @Schema(description = "사용자 ID", example = "admin")
        private String userId;

        @Schema(description = "주부서 여부", example = "Y")
        private YNType mainYn;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "부서에 사용자 추가 요청")
    public static class RegistDepartmentUserReq {
        @Schema(description = "추가할 사용자 목록")
        private List<UserDepartmentInfo> users;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "부서에 사용자 추가 응답")
    public static class RegistDepartmentUserResp {
        @Schema(description = "생성된 사용자-부서 관계 ID 목록")
        private List<Long> userDepartmentIds;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "부서에서 사용자 제거 요청")
    public static class DeleteDepartmentUserReq {
        @Schema(description = "제거할 사용자 ID 목록")
        private List<String> userIds;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "사용자 정보")
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "admin")
        private String userId;

        @Schema(description = "사용자 이름", example = "관리자")
        private String userName;

        @Schema(description = "주부서 여부", example = "Y")
        private YNType mainYn;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "부서 소속 사용자 조회 응답")
    public static class GetDepartmentUsersResp {
        @Schema(description = "부서 ID", example = "1")
        private Long departmentId;

        @Schema(description = "부서명 (영문)", example = "Development")
        private String departmentName;

        @Schema(description = "부서명 (한글)", example = "개발팀")
        private String departmentNameKr;

        @Schema(description = "상위 부서 ID", example = "1")
        private Long parentId;

        @Schema(description = "부서장 사용자 ID", example = "admin")
        private String headUserId;

        @Schema(description = "트리 레벨", example = "1")
        private Long treeLevel;

        @Schema(description = "부서 설명", example = "개발 담당 부서")
        private String departmentDesc;

        @Schema(description = "색상 코드", example = "#FF5733")
        private String colorCode;

        @Schema(description = "회사 ID", example = "POREST")
        private String companyId;

        @Schema(description = "부서에 소속된 사용자 목록")
        private List<UserInfo> usersInDepartment;

        @Schema(description = "부서에 소속되지 않은 사용자 목록")
        private List<UserInfo> usersNotInDepartment;
    }
}