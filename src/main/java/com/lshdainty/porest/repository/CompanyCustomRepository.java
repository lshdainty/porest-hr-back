package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyCustomRepository {
    // 신규 회사 저장
    void save(Company company);
    // 단건 회사 조회
    Optional<Company> findById(String id);
    // 단건 회사 조회(부서 포함)
    Optional<Company> findByIdWithDepartments(String id);
}
