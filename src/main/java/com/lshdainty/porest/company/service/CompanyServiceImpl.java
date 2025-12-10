package com.lshdainty.porest.company.service;

import com.lshdainty.porest.common.exception.BusinessRuleViolationException;
import com.lshdainty.porest.common.exception.DuplicateException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.company.repository.CompanyRepository;
import com.lshdainty.porest.company.service.dto.CompanyServiceDto;
import com.lshdainty.porest.department.service.dto.DepartmentServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public String regist(CompanyServiceDto data) {
        log.debug("회사 생성 시작: id={}, name={}", data.getId(), data.getName());
        checkAlreadyCompanyId(data.getId());

        Company company = Company.createCompany(
                data.getId(),
                data.getName(),
                data.getDesc()
        );

        companyRepository.save(company);
        log.info("회사 생성 완료: id={}", company.getId());
        return company.getId();
    }

    @Override
    @Transactional
    public void edit(CompanyServiceDto data) {
        log.debug("회사 수정 시작: id={}", data.getId());
        Company company = checkCompanyExists(data.getId());

        company.updateCompany(
                data.getName(),
                data.getDesc()
        );
        log.info("회사 수정 완료: id={}", data.getId());
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.debug("회사 삭제 시작: id={}", id);
        Company company = checkCompanyExists(id);

        if (!company.getDepartments().isEmpty()) {
            log.warn("회사 삭제 실패 - 부서 존재: id={}", id);
            throw new BusinessRuleViolationException(ErrorCode.DEPARTMENT_HAS_MEMBERS);
        }

        company.deleteCompany();
        log.info("회사 삭제 완료: id={}", id);
    }

    @Override
    public CompanyServiceDto searchCompany() {
        log.debug("회사 조회");
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

    @Override
    public CompanyServiceDto searchCompanyWithDepartments(String id) {
        log.debug("회사 조회 (부서 포함): id={}", id);
        Optional<Company> OCompany = companyRepository.findByIdWithDepartments(id);
        if (OCompany.isEmpty()) {
            log.warn("회사 조회 실패 - 존재하지 않는 회사: id={}", id);
            throw new EntityNotFoundException(ErrorCode.COMPANY_NOT_FOUND);
        }

        Company company = OCompany.get();

        // 최상위(parent가 null) 부서만 필터링, 각 부서 트리를 재귀로 DTO 변환
        List<DepartmentServiceDto> departmentDtos = company.getDepartments().stream()
                .filter(department -> department.getParent() == null && YNType.isN(department.getIsDeleted()))
                .map(DepartmentServiceDto::fromEntityWithChildren)
                .toList();

        return CompanyServiceDto.builder()
                .id(company.getId())
                .name(company.getName())
                .desc(company.getDesc())
                .departments(departmentDtos)
                .build();
    }

    @Override
    public void checkAlreadyCompanyId(String id) {
        Optional<Company> company = companyRepository.findById(id);
        if (company.isPresent()) {
            log.warn("회사 ID 중복: id={}", id);
            throw new DuplicateException(ErrorCode.COMPANY_ALREADY_EXISTS);
        }
    }

    @Override
    public Company checkCompanyExists(String companyId) {
        Optional<Company> company = companyRepository.findById(companyId);
        if ((company.isEmpty()) || YNType.isY(company.get().getIsDeleted())) {
            log.warn("회사 조회 실패 - 존재하지 않거나 삭제된 회사: id={}", companyId);
            throw new EntityNotFoundException(ErrorCode.COMPANY_NOT_FOUND);
        }
        return company.get();
    }
}
