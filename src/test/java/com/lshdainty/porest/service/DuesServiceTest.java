package com.lshdainty.porest.service;

import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.dues.domain.Dues;
import com.lshdainty.porest.dues.service.DuesService;
import com.lshdainty.porest.dues.service.DuesServiceImpl;
import com.lshdainty.porest.dues.type.DuesCalcType;
import com.lshdainty.porest.dues.type.DuesType;
import com.lshdainty.porest.dues.repository.DuesRepository;
import com.lshdainty.porest.dues.repository.dto.UsersMonthBirthDuesDto;
import com.lshdainty.porest.dues.service.dto.DuesServiceDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("회비 서비스 테스트")
class DuesServiceTest {
    @Mock
    private DuesRepository duesRepository;

    @InjectMocks
    private DuesServiceImpl duesService;

    @Nested
    @DisplayName("회비 등록")
    class RegistDues {
        @Test
        @DisplayName("성공 - 회비가 정상적으로 저장된다")
        void registDuesSuccess() {
            // given
            DuesServiceDto data = DuesServiceDto.builder()
                    .userName("이서준")
                    .amount(50000L)
                    .type(DuesType.BIRTH)
                    .calc(DuesCalcType.PLUS)
                    .date(LocalDate.of(2025, 1, 1))
                    .detail("1월 회비")
                    .build();
            willDoNothing().given(duesRepository).save(any(Dues.class));

            // when
            duesService.registDues(data);

            // then
            then(duesRepository).should().save(any(Dues.class));
        }
    }

    @Nested
    @DisplayName("전체 회비 조회")
    class SearchDues {
        @Test
        @DisplayName("성공 - 전체 회비가 조회되고 누적 금액이 계산된다")
        void searchDuesSuccess() {
            // given
            given(duesRepository.findDues()).willReturn(List.of(
                    Dues.createDues("이서준", 50000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 1, 1), "1월 회비"),
                    Dues.createDues("김서연", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 1, 1), "1월 회비"),
                    Dues.createDues("김지후", 10000L, DuesType.BIRTH, DuesCalcType.MINUS, LocalDate.of(2025, 1, 1), "1월 회비")
            ));

            // when
            List<DuesServiceDto> duesList = duesService.searchDues();

            // then
            then(duesRepository).should().findDues();
            assertThat(duesList).hasSize(3);
            assertThat(duesList.get(0).getTotalDues()).isEqualTo(50000L);
            assertThat(duesList.get(1).getTotalDues()).isEqualTo(60000L);
            assertThat(duesList.get(2).getTotalDues()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("성공 - 회비가 없을 경우 빈 리스트가 반환된다")
        void searchDuesEmptyList() {
            // given
            given(duesRepository.findDues()).willReturn(List.of());

            // when
            List<DuesServiceDto> duesList = duesService.searchDues();

            // then
            then(duesRepository).should().findDues();
            assertThat(duesList).isEmpty();
        }
    }

    @Nested
    @DisplayName("연도별 회비 조회")
    class SearchYearDues {
        @Test
        @DisplayName("성공 - 특정 연도 회비가 조회된다")
        void searchYearDuesSuccess() {
            // given
            int year = 2025;
            given(duesRepository.findDuesByYear(year)).willReturn(List.of(
                    Dues.createDues("이서준", 50000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 1, 1), "1월 회비"),
                    Dues.createDues("김서연", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 2, 1), "2월 회비")
            ));

            // when
            List<DuesServiceDto> duesList = duesService.searchYearDues(year);

            // then
            then(duesRepository).should().findDuesByYear(year);
            assertThat(duesList).hasSize(2);
            assertThat(duesList).extracting("date")
                    .allSatisfy(date -> assertThat(((LocalDate)date).getYear()).isEqualTo(2025));
        }
    }

    @Nested
    @DisplayName("운영 회비 조회")
    class SearchYearOperationDues {
        @Test
        @DisplayName("성공 - 입금/출금/총액이 정확히 계산된다")
        void searchYearOperationDuesSuccess() {
            // given
            int year = 2025;
            given(duesRepository.findOperatingDuesByYear(year)).willReturn(List.of(
                    Dues.createDues("이서준", 50000L, DuesType.OPERATION, DuesCalcType.PLUS, LocalDate.of(2025, 1, 1), "입금"),
                    Dues.createDues("김서연", 10000L, DuesType.OPERATION, DuesCalcType.MINUS, LocalDate.of(2025, 1, 1), "출금"),
                    Dues.createDues("김지후", 20000L, DuesType.OPERATION, DuesCalcType.PLUS, LocalDate.of(2025, 1, 1), "입금")
            ));

            // when
            DuesServiceDto result = duesService.searchYearOperationDues(year);

            // then
            then(duesRepository).should().findOperatingDuesByYear(year);
            assertThat(result.getTotalDues()).isEqualTo(60000L);
            assertThat(result.getTotalDeposit()).isEqualTo(70000L);
            assertThat(result.getTotalWithdrawal()).isEqualTo(10000L);
        }
    }

    @Nested
    @DisplayName("월별 생일 회비 조회")
    class SearchMonthBirthDues {
        @Test
        @DisplayName("성공 - 월별 생일 회비 총액이 반환된다")
        void searchMonthBirthDuesSuccess() {
            // given
            int year = 2025;
            int month = 1;
            given(duesRepository.findBirthDuesByYearAndMonth(year, month)).willReturn(100000L);

            // when
            Long totalAmount = duesService.searchMonthBirthDues(year, month);

            // then
            then(duesRepository).should().findBirthDuesByYearAndMonth(year, month);
            assertThat(totalAmount).isEqualTo(100000L);
        }
    }

    @Nested
    @DisplayName("사용자별 월별 생일 회비 조회")
    class SearchUsersMonthBirthDues {
        @Test
        @DisplayName("성공 - 사용자별 월별 생일 회비가 조회된다")
        void searchUsersMonthBirthDuesSuccess() {
            // given
            int year = 2025;
            given(duesRepository.findUsersMonthBirthDues(year)).willReturn(List.of(
                    new UsersMonthBirthDuesDto("이서준", 1, 50000L, "1월 생일 회비"),
                    new UsersMonthBirthDuesDto("김서연", 2, 60000L, "2월 생일 회비")
            ));

            // when
            List<DuesServiceDto> result = duesService.searchUsersMonthBirthDues(year);

            // then
            then(duesRepository).should().findUsersMonthBirthDues(year);
            assertThat(result).hasSize(2);
            assertThat(result).extracting("userName").containsExactly("이서준", "김서연");
            assertThat(result).extracting("amount").containsExactly(50000L, 60000L);
        }
    }

    @Nested
    @DisplayName("회비 수정")
    class EditDues {
        @Test
        @DisplayName("성공 - 회비 정보가 수정된다")
        void editDuesSuccess() {
            // given
            Long id = 1L;
            Dues dues = Dues.createDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 1, 1), "1월 회비");
            setDuesId(dues, id);
            given(duesRepository.findById(id)).willReturn(Optional.of(dues));

            DuesServiceDto data = DuesServiceDto.builder()
                    .id(id)
                    .userName("이민서")
                    .amount(20000L)
                    .build();

            // when
            duesService.editDues(data);

            // then
            then(duesRepository).should().findById(id);
            assertThat(dues.getUserName()).isEqualTo("이민서");
            assertThat(dues.getAmount()).isEqualTo(20000L);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회비를 수정하려 하면 예외가 발생한다")
        void editDuesFailNotFound() {
            // given
            Long id = 999L;
            DuesServiceDto data = DuesServiceDto.builder().id(id).build();
            given(duesRepository.findById(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> duesService.editDues(data))
                    .isInstanceOf(EntityNotFoundException.class);
            then(duesRepository).should().findById(id);
        }
    }

    @Nested
    @DisplayName("회비 삭제")
    class DeleteDues {
        @Test
        @DisplayName("성공 - 회비가 삭제된다")
        void deleteDuesSuccess() {
            // given
            Long id = 1L;
            Dues dues = Dues.createDues("이서준", 50000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 1, 1), "1월 회비");
            given(duesRepository.findById(id)).willReturn(Optional.of(dues));
            willDoNothing().given(duesRepository).delete(dues);

            // when
            duesService.deleteDues(id);

            // then
            then(duesRepository).should().findById(id);
            then(duesRepository).should().delete(dues);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회비를 삭제하려 하면 예외가 발생한다")
        void deleteDuesFailNotFound() {
            // given
            Long id = 999L;
            given(duesRepository.findById(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> duesService.deleteDues(id))
                    .isInstanceOf(EntityNotFoundException.class);
            then(duesRepository).should().findById(id);
            then(duesRepository).should(never()).delete(any(Dues.class));
        }
    }

    @Nested
    @DisplayName("회비 존재 확인")
    class CheckDuesExist {
        @Test
        @DisplayName("성공 - 존재하는 회비를 반환한다")
        void checkDuesExistSuccess() {
            // given
            Long id = 1L;
            Dues dues = Dues.createDues("이서준", 50000L, DuesType.BIRTH, DuesCalcType.PLUS, LocalDate.of(2025, 1, 1), "1월 회비");
            given(duesRepository.findById(id)).willReturn(Optional.of(dues));

            // when
            Dues result = duesService.checkDuesExist(id);

            // then
            then(duesRepository).should().findById(id);
            assertThat(result).isEqualTo(dues);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회비면 예외가 발생한다")
        void checkDuesExistFailNotFound() {
            // given
            Long id = 999L;
            given(duesRepository.findById(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> duesService.checkDuesExist(id))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // 테스트 헬퍼 메서드
    private void setDuesId(Dues dues, Long id) {
        try {
            java.lang.reflect.Field field = Dues.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(dues, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
