package com.lshdainty.porest.company.service;

import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.company.service.dto.CompanyServiceDto;

/**
 * 회사(Company) 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface CompanyService {
    /**
     * 회사를 등록합니다.
     *
     * @param data 회사 등록 정보
     * @return 생성된 회사 ID
     * @throws com.lshdainty.porest.common.exception.DuplicateException 이미 존재하는 회사 ID인 경우
     */
    String regist(CompanyServiceDto data);

    /**
     * 회사 정보를 수정합니다.
     *
     * @param data 회사 수정 정보
     * @throws com.lshdainty.porest.common.exception.EntityNotFoundException 회사가 존재하지 않는 경우
     */
    void edit(CompanyServiceDto data);

    /**
     * 회사를 삭제합니다.
     *
     * @param id 회사 ID
     * @throws com.lshdainty.porest.common.exception.EntityNotFoundException 회사가 존재하지 않는 경우
     * @throws com.lshdainty.porest.common.exception.BusinessRuleViolationException 부서가 존재하는 경우
     */
    void delete(String id);

    /**
     * 회사 정보를 조회합니다.
     *
     * @return 회사 정보 (존재하지 않으면 빈 DTO 반환)
     */
    CompanyServiceDto searchCompany();

    /**
     * 회사 정보를 부서 트리와 함께 조회합니다.
     *
     * @param id 회사 ID
     * @return 회사 정보 (부서 트리 포함)
     * @throws com.lshdainty.porest.common.exception.EntityNotFoundException 회사가 존재하지 않는 경우
     */
    CompanyServiceDto searchCompanyWithDepartments(String id);

    /**
     * 회사 ID 중복을 검증합니다.
     *
     * @param id 회사 ID
     * @throws com.lshdainty.porest.common.exception.DuplicateException 이미 존재하는 회사 ID인 경우
     */
    void checkAlreadyCompanyId(String id);

    /**
     * 회사 존재 여부를 검증하고 회사 엔티티를 반환합니다.
     *
     * @param companyId 회사 ID
     * @return 회사 엔티티
     * @throws com.lshdainty.porest.common.exception.EntityNotFoundException 회사가 존재하지 않거나 삭제된 경우
     */
    Company checkCompanyExists(String companyId);
}
