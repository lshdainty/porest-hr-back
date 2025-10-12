package com.lshdainty.porest.company.service;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.company.repository.CompanyCustomRepositoryImpl;
import com.lshdainty.porest.company.service.dto.CompanyServiceDto;
import com.lshdainty.porest.department.domain.Department;
import com.lshdainty.porest.department.service.dto.DepartmentServiceDto;
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
public class CompanyService {
    private final MessageSource ms;
    private final CompanyCustomRepositoryImpl companyRepository;

    @Transactional
    public String regist(CompanyServiceDto data) {
        checkAlreadyCompanyId(data.getId());

        Company company = Company.createCompany(
                data.getId(),
                data.getName(),
                data.getDesc()
        );

        companyRepository.save(company);
        return company.getId();
    }

    @Transactional
    public void edit(CompanyServiceDto data) {
        Company company = checkCompanyExists(data.getId());

        company.updateCompany(
                data.getName(),
                data.getDesc()
        );
    }

    @Transactional
    public void delete(String id) {
        Company company = checkCompanyExists(id);

        if (!company.getDepartments().isEmpty()) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.notnull.department", null, null));
        }

        company.deleteCompany();
    }

    public CompanyServiceDto searchCompany() {
        Optional<Company> OCompany = companyRepository.find();
        if (OCompany.isPresent()) {
            Company company = OCompany.get();
            return CompanyServiceDto.builder()
                    .id(company.getId())
                    .name(company.getName())
                    .desc(company.getDesc())
                    .build();
        } else {
            return CompanyServiceDto.builder().build();
        }
    }

    public CompanyServiceDto searchCompanyWithDepartments(String id) {
        Optional<Company> OCompany = companyRepository.findByIdWithDepartments(id);
        if (OCompany.isEmpty()) {
            throw new IllegalArgumentException(ms.getMessage("error.notfound.company", null, null));
        }

        Company company = OCompany.get();

        // 최상위(parent가 null) 부서만 필터링, 각 부서 트리를 재귀로 DTO 변환
        List<DepartmentServiceDto> departmentDtos = company.getDepartments().stream()
                .filter(department -> department.getParent() == null && department.getDelYN() == YNType.N)
                .map(this::convertToDtoWithChildren)
                .toList();

        return CompanyServiceDto.builder()
                .id(company.getId())
                .name(company.getName())
                .desc(company.getDesc())
                .departments(departmentDtos)
                .build();
    }

    public void checkAlreadyCompanyId(String id) {
        Optional<Company> company = companyRepository.findById(id);
        if (company.isPresent()) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.duplicate.company", null, null));
        }
    }

    public Company checkCompanyExists(String companyId) {
        Optional<Company> company = companyRepository.findById(companyId);
        if ((company.isEmpty()) || (company.get().getDelYN().equals(YNType.Y))) {
            throw new IllegalArgumentException(ms.getMessage("error.notfound.company", null, null));
        }
        return company.get();
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
