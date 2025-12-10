package com.lshdainty.porest.service;

import com.lshdainty.porest.common.exception.BusinessRuleViolationException;
import com.lshdainty.porest.common.exception.DuplicateException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.company.repository.CompanyRepository;
import com.lshdainty.porest.company.service.CompanyService;
import com.lshdainty.porest.company.service.CompanyServiceImpl;
import com.lshdainty.porest.company.service.dto.CompanyServiceDto;
import com.lshdainty.porest.department.domain.Department;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("회사 서비스 테스트")
class CompanyServiceTest {
    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    @Nested
    @DisplayName("회사 등록")
    class Regist {
        @Test
        @DisplayName("성공 - 회사가 정상적으로 등록된다")
        void registSuccess() {
            // given
            CompanyServiceDto data = CompanyServiceDto.builder()
                    .id("COMPANY001")
                    .name("테스트 회사")
                    .desc("테스트 회사 설명")
                    .build();
            given(companyRepository.findById("COMPANY001")).willReturn(Optional.empty());
            willDoNothing().given(companyRepository).save(any(Company.class));

            // when
            String companyId = companyService.regist(data);

            // then
            then(companyRepository).should().findById("COMPANY001");
            then(companyRepository).should().save(any(Company.class));
            assertThat(companyId).isEqualTo("COMPANY001");
        }

        @Test
        @DisplayName("실패 - 이미 존재하는 회사 ID로 등록하면 예외가 발생한다")
        void registFailDuplicateId() {
            // given
            CompanyServiceDto data = CompanyServiceDto.builder()
                    .id("COMPANY001")
                    .name("테스트 회사")
                    .build();
            Company existingCompany = Company.createCompany("COMPANY001", "기존 회사", "기존 설명");
            given(companyRepository.findById("COMPANY001")).willReturn(Optional.of(existingCompany));

            // when & then
            assertThatThrownBy(() -> companyService.regist(data))
                    .isInstanceOf(DuplicateException.class);
        }
    }

    @Nested
    @DisplayName("회사 수정")
    class Edit {
        @Test
        @DisplayName("성공 - 회사 정보가 수정된다")
        void editSuccess() {
            // given
            Company company = Company.createCompany("COMPANY001", "기존 회사", "기존 설명");
            given(companyRepository.findById("COMPANY001")).willReturn(Optional.of(company));

            CompanyServiceDto data = CompanyServiceDto.builder()
                    .id("COMPANY001")
                    .name("수정된 회사")
                    .desc("수정된 설명")
                    .build();

            // when
            companyService.edit(data);

            // then
            then(companyRepository).should().findById("COMPANY001");
            assertThat(company.getName()).isEqualTo("수정된 회사");
            assertThat(company.getDesc()).isEqualTo("수정된 설명");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회사를 수정하려 하면 예외가 발생한다")
        void editFailNotFound() {
            // given
            CompanyServiceDto data = CompanyServiceDto.builder()
                    .id("NOTEXIST")
                    .name("회사")
                    .build();
            given(companyRepository.findById("NOTEXIST")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> companyService.edit(data))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 삭제된 회사를 수정하려 하면 예외가 발생한다")
        void editFailDeleted() {
            // given
            Company company = Company.createCompany("COMPANY001", "회사", "설명");
            company.deleteCompany();
            given(companyRepository.findById("COMPANY001")).willReturn(Optional.of(company));

            CompanyServiceDto data = CompanyServiceDto.builder()
                    .id("COMPANY001")
                    .name("수정된 회사")
                    .build();

            // when & then
            assertThatThrownBy(() -> companyService.edit(data))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("회사 삭제")
    class Delete {
        @Test
        @DisplayName("성공 - 부서가 없는 회사가 삭제된다")
        void deleteSuccess() {
            // given
            Company company = Company.createCompany("COMPANY001", "테스트 회사", "설명");
            given(companyRepository.findById("COMPANY001")).willReturn(Optional.of(company));

            // when
            companyService.delete("COMPANY001");

            // then
            then(companyRepository).should().findById("COMPANY001");
            assertThat(company.getIsDeleted()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("실패 - 부서가 있는 회사를 삭제하려 하면 예외가 발생한다")
        void deleteFailHasDepartments() {
            // given
            Company company = Company.createCompany("COMPANY001", "테스트 회사", "설명");
            Department department = Department.createDepartment("개발팀", "개발팀", null, "user1", 1L, "설명", "#FF0000", company);

            given(companyRepository.findById("COMPANY001")).willReturn(Optional.of(company));

            // when & then
            assertThatThrownBy(() -> companyService.delete("COMPANY001"))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회사를 삭제하려 하면 예외가 발생한다")
        void deleteFailNotFound() {
            // given
            given(companyRepository.findById("NOTEXIST")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> companyService.delete("NOTEXIST"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("회사 조회")
    class SearchCompany {
        @Test
        @DisplayName("성공 - 회사 정보를 반환한다")
        void searchCompanySuccess() {
            // given
            Company company = Company.createCompany("COMPANY001", "테스트 회사", "설명");
            given(companyRepository.find()).willReturn(Optional.of(company));

            // when
            CompanyServiceDto result = companyService.searchCompany();

            // then
            then(companyRepository).should().find();
            assertThat(result.getId()).isEqualTo("COMPANY001");
            assertThat(result.getName()).isEqualTo("테스트 회사");
        }

        @Test
        @DisplayName("성공 - 회사가 없으면 빈 DTO를 반환한다")
        void searchCompanyEmpty() {
            // given
            given(companyRepository.find()).willReturn(Optional.empty());

            // when
            CompanyServiceDto result = companyService.searchCompany();

            // then
            then(companyRepository).should().find();
            assertThat(result.getId()).isNull();
        }
    }

    @Nested
    @DisplayName("회사와 부서 조회")
    class SearchCompanyWithDepartments {
        @Test
        @DisplayName("성공 - 회사와 부서 정보를 반환한다")
        void searchCompanyWithDepartmentsSuccess() {
            // given
            Company company = Company.createCompany("COMPANY001", "테스트 회사", "설명");
            given(companyRepository.findByIdWithDepartments("COMPANY001")).willReturn(Optional.of(company));

            // when
            CompanyServiceDto result = companyService.searchCompanyWithDepartments("COMPANY001");

            // then
            then(companyRepository).should().findByIdWithDepartments("COMPANY001");
            assertThat(result.getId()).isEqualTo("COMPANY001");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회사면 예외가 발생한다")
        void searchCompanyWithDepartmentsFailNotFound() {
            // given
            given(companyRepository.findByIdWithDepartments("NOTEXIST")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> companyService.searchCompanyWithDepartments("NOTEXIST"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("회사 ID 중복 확인")
    class CheckAlreadyCompanyId {
        @Test
        @DisplayName("성공 - 중복되지 않는 ID는 통과한다")
        void checkAlreadyCompanyIdSuccess() {
            // given
            given(companyRepository.findById("NEWCOMPANY")).willReturn(Optional.empty());

            // when & then - 예외 없이 통과
            companyService.checkAlreadyCompanyId("NEWCOMPANY");
            then(companyRepository).should().findById("NEWCOMPANY");
        }

        @Test
        @DisplayName("실패 - 중복되는 ID면 예외가 발생한다")
        void checkAlreadyCompanyIdFailDuplicate() {
            // given
            Company company = Company.createCompany("COMPANY001", "회사", "설명");
            given(companyRepository.findById("COMPANY001")).willReturn(Optional.of(company));

            // when & then
            assertThatThrownBy(() -> companyService.checkAlreadyCompanyId("COMPANY001"))
                    .isInstanceOf(DuplicateException.class);
        }
    }

    @Nested
    @DisplayName("회사 존재 확인")
    class CheckCompanyExists {
        @Test
        @DisplayName("성공 - 존재하는 회사를 반환한다")
        void checkCompanyExistsSuccess() {
            // given
            Company company = Company.createCompany("COMPANY001", "회사", "설명");
            given(companyRepository.findById("COMPANY001")).willReturn(Optional.of(company));

            // when
            Company result = companyService.checkCompanyExists("COMPANY001");

            // then
            assertThat(result).isEqualTo(company);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회사면 예외가 발생한다")
        void checkCompanyExistsFailNotFound() {
            // given
            given(companyRepository.findById("NOTEXIST")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> companyService.checkCompanyExists("NOTEXIST"))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 삭제된 회사면 예외가 발생한다")
        void checkCompanyExistsFailDeleted() {
            // given
            Company company = Company.createCompany("COMPANY001", "회사", "설명");
            company.deleteCompany();
            given(companyRepository.findById("COMPANY001")).willReturn(Optional.of(company));

            // when & then
            assertThatThrownBy(() -> companyService.checkCompanyExists("COMPANY001"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
