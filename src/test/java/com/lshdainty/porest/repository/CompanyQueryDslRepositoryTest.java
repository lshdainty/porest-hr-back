package com.lshdainty.porest.repository;

import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.company.repository.CompanyQueryDslRepository;
import com.lshdainty.porest.department.domain.Department;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@DataJpaTest
@Import({CompanyQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 회사 레포지토리 테스트")
class CompanyQueryDslRepositoryTest {
    @Autowired
    private CompanyQueryDslRepository companyRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("회사 저장 및 단건 조회")
    void save() {
        // given
        Company company = Company.createCompany("company1", "테스트 회사", "회사 설명");

        // when
        companyRepository.save(company);
        em.flush();
        em.clear();

        // then
        Optional<Company> findCompany = companyRepository.findById("company1");
        assertThat(findCompany.isPresent()).isTrue();
        assertThat(findCompany.get().getId()).isEqualTo("company1");
        assertThat(findCompany.get().getName()).isEqualTo("테스트 회사");
    }

    @Test
    @DisplayName("단건 조회 시 회사가 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given & when
        Optional<Company> findCompany = companyRepository.findById("invalid-company");

        // then
        assertThat(findCompany.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("최상위 회사 조회")
    void find() {
        // given
        Company company = Company.createCompany("company1", "테스트 회사", "설명");
        companyRepository.save(company);
        em.flush();
        em.clear();

        // when
        Optional<Company> findCompany = companyRepository.find();

        // then
        assertThat(findCompany.isPresent()).isTrue();
        assertThat(findCompany.get().getId()).isEqualTo("company1");
    }

    @Test
    @DisplayName("최상위 회사가 없어도 Null이 반환되면 안된다.")
    void findEmpty() {
        // given & when
        Optional<Company> findCompany = companyRepository.find();

        // then
        assertThat(findCompany.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("회사와 최상위 부서를 함께 조회")
    void findByIdWithDepartments() {
        // given
        Company company = Company.createCompany("company1", "테스트 회사", "설명");
        companyRepository.save(company);

        em.persist(Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company));
        em.persist(Department.createDepartment("인사팀", "인사팀", null, null, 2L, "인사 부서", "#00FF00", company));
        em.flush();
        em.clear();

        // when
        Optional<Company> findCompany = companyRepository.findByIdWithDepartments("company1");

        // then
        assertThat(findCompany.isPresent()).isTrue();
        assertThat(findCompany.get().getDepartments()).hasSize(2);
    }

    @Test
    @DisplayName("회사 수정")
    void updateCompany() {
        // given
        Company company = Company.createCompany("company1", "테스트 회사", "설명");
        companyRepository.save(company);
        em.flush();
        em.clear();

        // when
        Company foundCompany = companyRepository.findById("company1").orElseThrow();
        foundCompany.updateCompany("수정된 회사", "수정된 설명");
        em.flush();
        em.clear();

        // then
        Company updatedCompany = companyRepository.findById("company1").orElseThrow();
        assertThat(updatedCompany.getName()).isEqualTo("수정된 회사");
        assertThat(updatedCompany.getDesc()).isEqualTo("수정된 설명");
    }
}
