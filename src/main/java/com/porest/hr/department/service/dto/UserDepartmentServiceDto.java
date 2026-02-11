package com.porest.hr.department.service.dto;

import com.porest.core.type.YNType;
import com.porest.hr.department.domain.Department;
import com.porest.hr.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class UserDepartmentServiceDto {
    private Long id;
    private User user;
    private Department department;
    private YNType mainYN;

    private String userId;
    private Long departmentId;
}
