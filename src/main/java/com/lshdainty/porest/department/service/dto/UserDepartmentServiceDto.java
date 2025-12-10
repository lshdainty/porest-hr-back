package com.lshdainty.porest.department.service.dto;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.department.domain.Department;
import com.lshdainty.porest.user.domain.User;
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
