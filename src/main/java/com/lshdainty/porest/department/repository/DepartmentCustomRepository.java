package com.lshdainty.porest.department.repository;

import com.lshdainty.porest.department.domain.Department;
import com.lshdainty.porest.department.domain.UserDepartment;
import com.lshdainty.porest.user.domain.User;

import java.util.List;
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
    // 유저-부서 연결 저장
    void saveUserDepartment(UserDepartment userDepartment);
    // 특정 유저의 메인 부서 여부 확인
    Optional<UserDepartment> findMainDepartmentByUserId(String userId);
    // 특정 유저와 부서의 연결 조회
    Optional<UserDepartment> findUserDepartment(String userId, Long departmentId);
    // 특정 부서에 속한 유저 조회
    List<User> findUsersInDepartment(Long departmentId);
    // 특정 부서에 속하지 않은 유저 조회
    List<User> findUsersNotInDepartment(Long departmentId);
    // 특정 부서에 속한 UserDepartment 조회 (mainYN 포함)
    List<UserDepartment> findUserDepartmentsInDepartment(Long departmentId);
    // 특정 유저의 메인 부서 존재 여부 확인
    boolean hasMainDepartment(String userId);
    // 여러 유저 ID로 Department 조회 (부서장 확인용)
    List<Department> findByUserIds(List<String> userIds);
}
