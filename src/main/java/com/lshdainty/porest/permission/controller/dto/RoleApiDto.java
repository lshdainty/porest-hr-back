package com.lshdainty.porest.permission.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Role & Permission API DTO
 */
public class RoleApiDto {

    /* ==================== Role Response DTO ==================== */

    /**
     * 역할 응답 DTO
     */
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RoleResp {
        private String roleName;
        private String description;
        private List<String> permissions;
    }

    /**
     * 역할 간단 응답 DTO (권한 제외)
     */
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RoleSimpleResp {
        private String roleName;
        private String description;
    }

    /* ==================== Role Request DTO ==================== */

    /**
     * 역할 생성 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CreateRoleReq {
        private String roleName;
        private String description;
        private List<String> permissionNames; // optional
    }

    /**
     * 역할 수정 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UpdateRoleReq {
        private String description;
        private List<String> permissionNames; // optional
    }

    /**
     * 역할 권한 수정 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UpdateRolePermissionsReq {
        private List<String> permissionNames;
    }

    /**
     * 역할에 권한 추가/제거 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RolePermissionReq {
        private String permissionName;
    }

    /* ==================== Permission Response DTO ==================== */

    /**
     * 권한 응답 DTO
     */
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PermissionResp {
        private String id;
        private String name;
        private String description;
        private String resource;
        private String action;
    }

    /* ==================== Permission Request DTO ==================== */

    /**
     * 권한 생성 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CreatePermissionReq {
        private String id;
        private String name;
        private String description;
        private String resource;
        private String action;
    }

    /**
     * 권한 수정 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UpdatePermissionReq {
        private String name;
        private String description;
        private String resource;
        private String action;
    }
}
