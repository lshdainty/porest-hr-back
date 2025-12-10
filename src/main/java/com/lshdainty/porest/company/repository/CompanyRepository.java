package com.lshdainty.porest.company.repository;

import com.lshdainty.porest.company.domain.Company;

import java.util.Optional;

/**
 * Company Repository Interface
 */
public interface CompanyRepository {
    /**
     * 신규 회사 저장
     *
     * @param company 저장할 회사
     */
    void save(Company company);

    /**
     * 최상위 회사 조회 (다중회사 기능 추가 전까지 사용)
     *
     * @return Optional&lt;Company&gt;
     */
    Optional<Company> find();

    /**
     * 단건 회사 조회
     *
     * @param id 회사 ID
     * @return Optional&lt;Company&gt;
     */
    Optional<Company> findById(String id);

    /**
     * 단건 회사 조회 (최상위 부서 포함)
     *
     * @param id 회사 ID
     * @return Optional&lt;Company&gt;
     */
    Optional<Company> findByIdWithDepartments(String id);
}
