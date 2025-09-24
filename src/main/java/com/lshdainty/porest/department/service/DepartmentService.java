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
    public Long save(DepartmentServiceDto data) {
        // 회사 조회
        Company company = companyService.checkCompanyExists(data.getCompanyId());

        // 부모 부서 조회
        Department parent = checkDepartmentExists(data.getParentId());

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

    public DepartmentServiceDto getDepartmentById(Long id) {
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

    public DepartmentServiceDto getDepartmentByIdWithChildren(Long id) {
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