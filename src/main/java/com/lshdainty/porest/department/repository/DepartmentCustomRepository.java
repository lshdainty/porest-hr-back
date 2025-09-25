package com.lshdainty.porest.department.repository;

import com.lshdainty.porest.department.domain.Department;

import java.util.Optional;

public interface DepartmentCustomRepository {
    // 신규 부서 저장
    void save(Department department);
    // 단건 부서 조회
    Optional<Department> findById(Long id);
    // 자식 부서까지 모두 조회
    Optional<Department> findByIdWithChildren(Long id);
    // 자식 부서가 존재하는지 체크
    boolean hasActiveChildren(Long departmentId); // 추가
}
