package com.lshdainty.porest.department.service;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.company.service.CompanyService;
import com.lshdainty.porest.department.domain.Department;
import com.lshdainty.porest.department.repository.DepartmentCustomRepositoryImpl;
import com.lshdainty.porest.department.service.dto.DepartmentServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DepartmentService {
    private final MessageSource ms;
    private final DepartmentCustomRepositoryImpl departmentRepository;
    private final CompanyService companyService;

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
                data.getDesc()
        );
    }

    @Transactional
    public void delete(Long departmentId) {
        Department department = checkDepartmentExists(departmentId);

        // 하위에 자식 부서가 있는지 확인
        boolean hasChildren = department.getChildren().stream()
                .anyMatch(child -> child.getDelYN() == YNType.N);

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
                .companyId(department.getCompany().getId())
                .build();
    }

    public DepartmentServiceDto searchDepartmentByIdWithChildren(Long id) {
        Department department = checkDepartmentExists(id);
        return convertToDtoWithChildren(department);
    }

    public Department checkDepartmentExists(Long departmentId) {
        Optional<Department> department = departmentRepository.findById(departmentId);
        if ((department.isEmpty()) || department.get().getDelYN().equals(YNType.Y)) {
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
            if (child.getDelYN() == YNType.N) {
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

    protected DepartmentServiceDto convertToDtoWithChildren(Department department) {
        if (department == null) return null;

        return DepartmentServiceDto.builder()
                .id(department.getId())
                .name(department.getName())
                .nameKR(department.getNameKR())
                .parentId(department.getParentId())
                .headUserId(department.getHeadUserId())
                .level(department.getLevel())
                .desc(department.getDesc())
                .company(department.getCompany())
                .companyId(department.getCompany() != null ? department.getCompany().getId() : null)
                .children(department.getChildren() != null
                        ? department.getChildren().stream()
                        .filter(child -> child.getDelYN() == YNType.N)
                        .map(child -> convertToDtoWithChildren(child))
                        .toList()
                        : null)
                .build();
    }
}