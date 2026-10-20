package io.groo.ieum.department.service.dto;

import io.groo.core.type.YNType;
import io.groo.ieum.department.domain.Department;
import io.groo.ieum.user.domain.User;
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
