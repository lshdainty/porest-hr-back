package com.lshdainty.porest.company.repository;

import com.lshdainty.porest.company.domain.Company;

import java.util.Optional;

public interface CompanyCustomRepository {
    // 신규 회사 저장
    void save(Company company);
    // 최상위 회사 조회( 다중회사 기능 추가 전까지 사용)
    Optional<Company> find();
    // 단건 회사 조회
    Optional<Company> findById(String id);
    // 단건 회사 조회(최상위 부서 포함)
    Optional<Company> findByIdWithDepartments(String id);
}
