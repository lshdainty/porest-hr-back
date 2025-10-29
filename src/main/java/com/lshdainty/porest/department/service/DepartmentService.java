package com.lshdainty.porest.department.service;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.company.service.CompanyService;
import com.lshdainty.porest.department.domain.Department;
import com.lshdainty.porest.department.domain.UserDepartment;
import com.lshdainty.porest.department.repository.DepartmentCustomRepositoryImpl;
import com.lshdainty.porest.department.service.dto.DepartmentServiceDto;
import com.lshdainty.porest.department.service.dto.UserDepartmentServiceDto;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DepartmentService {
    private final MessageSource ms;
    private final DepartmentCustomRepositoryImpl departmentRepository;
    private final CompanyService companyService;
    private final UserService userService;

    @Transactional
    public Long regist(DepartmentServiceDto data) {
        // 회사 조회
        Company company = companyService.checkCompanyExists(data.getCompanyId());

        // 부모 부서 조회
        Department parent = null;
        if (data.getParentId() != null) {
            parent = checkDepartmentExists(data.getParentId());

            // 부모 부서와 같은 회사인지 검증
            if (!parent.getCompany().getId().equals(data.getCompanyId())) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.different.company", null, null));
            }
        }

        Department department = Department.createDepartment(
                data.getName(),
                data.getNameKR(),
                parent,
                data.getHeadUserId(),
                data.getLevel(),
                data.getDesc(),
                data.getColor(),
                company
        );
        departmentRepository.save(department);
        return department.getId();
    }

    @Transactional
    public void edit(DepartmentServiceDto data) {
        Department department = checkDepartmentExists(data.getId());

        // 부모 부서 변경이 있는 경우 검증
        Department newParent = null;
        if (data.getParentId() != null) {
            newParent = checkDepartmentExists(data.getParentId());

            // 자기 자신을 부모로 설정하는 것 방지
            if (newParent.getId().equals(data.getId())) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.self.parent", null, null));
            }

            // 순환 참조 방지 (자신의 하위 부서를 부모로 설정하는 것 방지)
            if (isDescendant(department, newParent)) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.circular.reference", null, null));
            }

            // 같은 회사인지 검증
            if (!newParent.getCompany().getId().equals(department.getCompany().getId())) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.different.company", null, null));
            }
        }

        department.updateDepartment(
                data.getName(),
                data.getNameKR(),
                newParent,
                data.getHeadUserId(),
                data.getLevel(),
                data.getDesc(),
                data.getColor()
        );
    }

    @Transactional
    public void delete(Long departmentId) {
        Department department = checkDepartmentExists(departmentId);

        // 하위에 자식 부서가 있는지 확인
        boolean hasChildren = department.getChildren().stream()
                .anyMatch(child -> child.getIsDeleted() == YNType.N);

        if (hasChildren) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.has.children.department", null, null));
        }

        // 논리 삭제 실행
        department.deleteDepartment();
    }

    public DepartmentServiceDto searchDepartmentById(Long id) {
        Department department = checkDepartmentExists(id);
        return DepartmentServiceDto.builder()
                .id(department.getId())
                .name(department.getName())
                .nameKR(department.getNameKR())
                .parentId(department.getParentId())
                .headUserId(department.getHeadUserId())
                .level(department.getLevel())
                .desc(department.getDesc())
                .color(department.getColor())
                .companyId(department.getCompany().getId())
                .build();
    }

    public DepartmentServiceDto searchDepartmentByIdWithChildren(Long id) {
        Department department = checkDepartmentExists(id);
        return DepartmentServiceDto.fromEntityWithChildren(department);
    }

    @Transactional
    public List<Long> registUserDepartments(List<UserDepartmentServiceDto> userDataList, Long departmentId) {
        // 부서 존재 여부 확인
        Department department = checkDepartmentExists(departmentId);

        List<Long> userDepartmentIds = new java.util.ArrayList<>();

        for (UserDepartmentServiceDto data : userDataList) {
            // userId로 User 조회
            User user = userService.checkUserExist(data.getUserId());

            // mainYN이 Y인 경우, 해당 유저의 기존 메인 부서가 있는지 확인
            if (data.getMainYN() == YNType.Y) {
                Optional<UserDepartment> existingMainDepartment =
                        departmentRepository.findMainDepartmentByUserId(user.getId());

                if (existingMainDepartment.isPresent()) {
                    throw new IllegalArgumentException(
                            ms.getMessage("error.validate.main.department.already.exists", null, null)
                    );
                }
            }

            // UserDepartment 생성 및 저장
            UserDepartment userDepartment = UserDepartment.createUserDepartment(
                    user,
                    department,
                    data.getMainYN()
            );
            departmentRepository.saveUserDepartment(userDepartment);
            userDepartmentIds.add(userDepartment.getId());
        }

        return userDepartmentIds;
    }

    @Transactional
    public void deleteUserDepartments(List<String> userIds, Long departmentId) {
        for (String userId : userIds) {
            // UserDepartment 조회
            Optional<UserDepartment> userDepartmentOpt =
                    departmentRepository.findUserDepartment(userId, departmentId);

            if (userDepartmentOpt.isEmpty()) {
                throw new IllegalArgumentException(
                        ms.getMessage("error.notfound.user.department", null, null)
                );
            }

            // 논리 삭제 실행
            UserDepartment userDepartment = userDepartmentOpt.get();
            userDepartment.deleteUserDepartment();
        }
    }

    public DepartmentServiceDto getUsersInAndNotInDepartment(Long departmentId) {
        // 부서 존재 여부 확인 및 부서 정보 조회
        Department department = checkDepartmentExists(departmentId);

        // Repository에서 부서에 속한 UserDepartment 조회 (mainYN 포함)
        List<UserDepartment> userDepartmentsIn = departmentRepository.findUserDepartmentsInDepartment(departmentId);

        // Repository에서 부서에 속하지 않은 유저 조회
        List<User> usersNotIn = departmentRepository.findUsersNotInDepartment(departmentId);

        // UserDepartment Entity -> UserDepartmentServiceDto 변환
        List<UserDepartmentServiceDto> usersInDepartmentDto = userDepartmentsIn.stream()
                .map(userDepartment -> UserDepartmentServiceDto.builder()
                        .user(userDepartment.getUser())
                        .mainYN(userDepartment.getMainYN())
                        .build())
                .toList();

        // User Entity -> UserDepartmentServiceDto 변환 (부서에 속하지 않은 유저는 mainYN이 null)
        List<UserDepartmentServiceDto> usersNotInDepartmentDto = usersNotIn.stream()
                .map(user -> UserDepartmentServiceDto.builder()
                        .user(user)
                        .mainYN(null)
                        .build())
                .toList();

        // Department 정보 포함하여 반환
        return DepartmentServiceDto.builder()
                .id(department.getId())
                .name(department.getName())
                .nameKR(department.getNameKR())
                .parentId(department.getParentId())
                .headUserId(department.getHeadUserId())
                .level(department.getLevel())
                .desc(department.getDesc())
                .color(department.getColor())
                .company(department.getCompany())
                .companyId(department.getCompany() != null ? department.getCompany().getId() : null)
                .usersInDepartment(usersInDepartmentDto)
                .usersNotInDepartment(usersNotInDepartmentDto)
                .build();
    }

    public Department checkDepartmentExists(Long departmentId) {
        Optional<Department> department = departmentRepository.findById(departmentId);
        if ((department.isEmpty()) || department.get().getIsDeleted().equals(YNType.Y)) {
            throw new IllegalArgumentException(ms.getMessage("error.notfound.department", null, null));
        }
        return department.get();
    }

    /**
     * 순환 참조 검사: targetDepartment가 currentDepartment의 하위 부서인지 확인
     */
    private boolean isDescendant(Department currentDepartment, Department targetDepartment) {
        if (currentDepartment == null || targetDepartment == null) {
            return false;
        }

        for (Department child : currentDepartment.getChildren()) {
            if (child.getIsDeleted() == YNType.N) {
                if (child.getId().equals(targetDepartment.getId())) {
                    return true;
                }
                if (isDescendant(child, targetDepartment)) {
                    return true;
                }
            }
        }
        return false;
    }
}