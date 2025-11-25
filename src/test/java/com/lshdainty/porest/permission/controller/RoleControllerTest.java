package com.lshdainty.porest.permission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.permission.controller.dto.RoleApiDto;
import com.lshdainty.porest.permission.domain.Permission;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.service.RoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/roles - 모든 역할 조회 성공")
    @WithMockUser(authorities = "ROLE_READ")
    void getAllRolesSuccess() throws Exception {
        // given
        Role role = Role.createRole("ADMIN", "관리자");
        given(roleService.getAllRoles()).willReturn(List.of(role));

        // when & then
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].role_name").value("ADMIN"))
                .andExpect(jsonPath("$.data[0].description").value("관리자"));
    }

    @Test
    @DisplayName("GET /api/v1/roles/{roleName} - 특정 역할 조회 성공")
    @WithMockUser(authorities = "ROLE_READ")
    void getRoleSuccess() throws Exception {
        // given
        Role role = Role.createRole("ADMIN", "관리자");
        Permission permission = Permission.createPermission("USER_READ", "유저 조회", "USER", "READ");
        role.addPermission(permission);
        given(roleService.getRole("ADMIN")).willReturn(role);

        // when & then
        mockMvc.perform(get("/api/v1/roles/ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role_name").value("ADMIN"))
                .andExpect(jsonPath("$.data.permissions[0]").value("USER_READ"));
    }

    @Test
    @DisplayName("POST /api/v1/roles - 역할 생성 성공")
    @WithMockUser(authorities = "ROLE_MANAGE")
    void createRoleSuccess() throws Exception {
        // given
        RoleApiDto.CreateRoleReq req = new RoleApiDto.CreateRoleReq("NEW_ROLE", "새로운 역할", null);

        Role role = Role.createRole("NEW_ROLE", "새로운 역할");
        given(roleService.createRole("NEW_ROLE", "새로운 역할")).willReturn(role);

        // when & then
        mockMvc.perform(post("/api/v1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("NEW_ROLE"));
    }

    @Test
    @DisplayName("PUT /api/v1/roles/{roleName}/permissions - 역할 권한 수정 성공")
    @WithMockUser(authorities = "ROLE_MANAGE")
    void updateRolePermissionsSuccess() throws Exception {
        // given
        RoleApiDto.UpdateRolePermissionsReq req = new RoleApiDto.UpdateRolePermissionsReq(List.of("USER_READ", "USER_WRITE"));

        doNothing().when(roleService).updateRolePermissions(eq("ADMIN"), any());

        // when & then
        mockMvc.perform(put("/api/v1/roles/ADMIN/permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(roleService).updateRolePermissions(eq("ADMIN"), eq(List.of("USER_READ", "USER_WRITE")));
    }

    @Test
    @DisplayName("DELETE /api/v1/roles/{roleName} - 역할 삭제 성공")
    @WithMockUser(authorities = "ROLE_MANAGE")
    void deleteRoleSuccess() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/roles/ADMIN")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(roleService).deleteRole("ADMIN");
    }

    @Test
    @DisplayName("GET /api/v1/permissions - 모든 권한 조회 성공")
    @WithMockUser(authorities = "ROLE_READ")
    void getAllPermissionsSuccess() throws Exception {
        // given
        Permission permission = Permission.createPermission("USER_READ", "유저 조회", "USER", "READ");
        given(roleService.getAllPermissions()).willReturn(List.of(permission));

        // when & then
        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("USER_READ"))
                .andExpect(jsonPath("$.data[0].resource").value("USER"))
                .andExpect(jsonPath("$.data[0].action").value("READ"));
    }

    @Test
    @DisplayName("POST /api/v1/roles - 권한 포함 역할 생성 성공")
    @WithMockUser(authorities = "ROLE_MANAGE")
    void createRoleWithPermissionsSuccess() throws Exception {
        // given
        RoleApiDto.CreateRoleReq req = new RoleApiDto.CreateRoleReq(
                "NEW_ROLE",
                "새로운 역할",
                List.of("USER_READ", "USER_WRITE")
        );

        Role role = Role.createRoleWithPermissions("NEW_ROLE", "새로운 역할", List.of(
                Permission.createPermission("USER_READ", "유저 조회", "USER", "READ"),
                Permission.createPermission("USER_WRITE", "유저 쓰기", "USER", "WRITE")
        ));
        given(roleService.createRoleWithPermissions(eq("NEW_ROLE"), eq("새로운 역할"), any())).willReturn(role);

        // when & then
        mockMvc.perform(post("/api/v1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("NEW_ROLE"));
    }

    @Test
    @DisplayName("PUT /api/v1/roles/{roleName} - 역할 수정 성공")
    @WithMockUser(authorities = "ROLE_MANAGE")
    void updateRoleSuccess() throws Exception {
        // given
        RoleApiDto.UpdateRoleReq req = new RoleApiDto.UpdateRoleReq("수정된 설명", null);

        doNothing().when(roleService).updateRole(eq("ADMIN"), eq("수정된 설명"));

        // when & then
        mockMvc.perform(put("/api/v1/roles/ADMIN")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(roleService).updateRole(eq("ADMIN"), eq("수정된 설명"));
    }

    @Test
    @DisplayName("POST /api/v1/permissions - 권한 생성 성공")
    @WithMockUser(authorities = "ROLE_MANAGE")
    void createPermissionSuccess() throws Exception {
        // given
        RoleApiDto.CreatePermissionReq req = new RoleApiDto.CreatePermissionReq(
                "NEW_PERMISSION",
                "새 권한",
                "USER",
                "MANAGE"
        );

        Permission permission = Permission.createPermission("NEW_PERMISSION", "새 권한", "USER", "MANAGE");
        given(roleService.createPermission(eq("NEW_PERMISSION"), eq("새 권한"), eq("USER"), eq("MANAGE")))
                .willReturn(permission);

        // when & then
        mockMvc.perform(post("/api/v1/permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("NEW_PERMISSION"));
    }

    @Test
    @DisplayName("PUT /api/v1/permissions/{permissionName} - 권한 수정 성공")
    @WithMockUser(authorities = "ROLE_MANAGE")
    void updatePermissionSuccess() throws Exception {
        // given
        RoleApiDto.UpdatePermissionReq req = new RoleApiDto.UpdatePermissionReq(
                "수정된 설명",
                "USER",
                "UPDATE"
        );

        doNothing().when(roleService).updatePermission(
                eq("USER_READ"),
                eq("수정된 설명"),
                eq("USER"),
                eq("UPDATE")
        );

        // when & then
        mockMvc.perform(put("/api/v1/permissions/USER_READ")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(roleService).updatePermission(
                eq("USER_READ"),
                eq("수정된 설명"),
                eq("USER"),
                eq("UPDATE")
        );
    }

    @Test
    @DisplayName("DELETE /api/v1/permissions/{permissionName} - 권한 삭제 성공")
    @WithMockUser(authorities = "ROLE_MANAGE")
    void deletePermissionSuccess() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/permissions/USER_READ")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(roleService).deletePermission("USER_READ");
    }
}
