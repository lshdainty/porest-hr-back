package com.lshdainty.porest.service;

import com.lshdainty.porest.dues.domain.Dues;
import com.lshdainty.porest.dues.service.DuesService;
import com.lshdainty.porest.dues.type.DuesCalcType;
import com.lshdainty.porest.dues.type.DuesType;
import com.lshdainty.porest.dues.repository.DuesRepositoryImpl;
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
import org.springframework.context.MessageSource;

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
    private MessageSource ms;
    @Mock
    private DuesRepositoryImpl duesRepositoryImpl;

    @InjectMocks
    private DuesService duesService;

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
                    .date("20250101")
                    .detail("1월 회비")
                    .build();
            willDoNothing().given(duesRepositoryImpl).save(any(Dues.class));

            // when
            duesService.registDues(data);

            // then
            then(duesRepositoryImpl).should().save(any(Dues.class));
        }
    }

    @Nested
    @DisplayName("전체 회비 조회")
    class SearchDues {
        @Test
        @DisplayName("성공 - 전체 회비가 조회되고 누적 금액이 계산된다")
        void searchDuesSuccess() {
            // given
            given(duesRepositoryImpl.findDues()).willReturn(List.of(
                    Dues.createDues("이서준", 50000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비"),
                    Dues.createDues("김서연", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비"),
                    Dues.createDues("김지후", 10000L, DuesType.BIRTH, DuesCalcType.MINUS, "20250101", "1월 회비")
            ));

            // when
            List<DuesServiceDto> duesList = duesService.searchDues();

            // then
            then(duesRepositoryImpl).should().findDues();
            assertThat(duesList).hasSize(3);
            assertThat(duesList.get(0).getTotalDues()).isEqualTo(50000L);
            assertThat(duesList.get(1).getTotalDues()).isEqualTo(60000L);
            assertThat(duesList.get(2).getTotalDues()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("성공 - 회비가 없을 경우 빈 리스트가 반환된다")
        void searchDuesEmptyList() {
            // given
            given(duesRepositoryImpl.findDues()).willReturn(List.of());

            // when
            List<DuesServiceDto> duesList = duesService.searchDues();

            // then
            then(duesRepositoryImpl).should().findDues();
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
            String year = "2025";
            given(duesRepositoryImpl.findDuesByYear(year)).willReturn(List.of(
                    Dues.createDues("이서준", 50000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비"),
                    Dues.createDues("김서연", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250201", "2월 회비")
            ));

            // when
            List<DuesServiceDto> duesList = duesService.searchYearDues(year);

            // then
            then(duesRepositoryImpl).should().findDuesByYear(year);
            assertThat(duesList).hasSize(2);
            assertThat(duesList).extracting("date")
                    .allSatisfy(date -> assertThat(String.valueOf(date)).contains("2025"));
        }
    }

    @Nested
    @DisplayName("운영 회비 조회")
    class SearchYearOperationDues {
        @Test
        @DisplayName("성공 - 입금/출금/총액이 정확히 계산된다")
        void searchYearOperationDuesSuccess() {
            // given
            String year = "2025";
            given(duesRepositoryImpl.findOperatingDuesByYear(year)).willReturn(List.of(
                    Dues.createDues("이서준", 50000L, DuesType.OPERATION, DuesCalcType.PLUS, "20250101", "입금"),
                    Dues.createDues("김서연", 10000L, DuesType.OPERATION, DuesCalcType.MINUS, "20250101", "출금"),
                    Dues.createDues("김지후", 20000L, DuesType.OPERATION, DuesCalcType.PLUS, "20250101", "입금")
            ));

            // when
            DuesServiceDto result = duesService.searchYearOperationDues(year);

            // then
            then(duesRepositoryImpl).should().findOperatingDuesByYear(year);
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
            String year = "2025";
            String month = "01";
            given(duesRepositoryImpl.findBirthDuesByYearAndMonth(year, month)).willReturn(100000L);

            // when
            Long totalAmount = duesService.searchMonthBirthDues(year, month);

            // then
            then(duesRepositoryImpl).should().findBirthDuesByYearAndMonth(year, month);
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
            String year = "2025";
            given(duesRepositoryImpl.findUsersMonthBirthDues(year)).willReturn(List.of(
                    new UsersMonthBirthDuesDto("이서준", "01", 50000L, "1월 생일 회비"),
                    new UsersMonthBirthDuesDto("김서연", "02", 60000L, "2월 생일 회비")
            ));

            // when
            List<DuesServiceDto> result = duesService.searchUsersMonthBirthDues(year);

            // then
            then(duesRepositoryImpl).should().findUsersMonthBirthDues(year);
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
            Long seq = 1L;
            Dues dues = Dues.createDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비");
            setDuesSeq(dues, seq);
            given(duesRepositoryImpl.findById(seq)).willReturn(Optional.of(dues));

            DuesServiceDto data = DuesServiceDto.builder()
                    .seq(seq)
                    .userName("이민서")
                    .amount(20000L)
                    .build();

            // when
            duesService.editDues(data);

            // then
            then(duesRepositoryImpl).should().findById(seq);
            assertThat(dues.getUserName()).isEqualTo("이민서");
            assertThat(dues.getAmount()).isEqualTo(20000L);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회비를 수정하려 하면 예외가 발생한다")
        void editDuesFailNotFound() {
            // given
            Long seq = 999L;
            DuesServiceDto data = DuesServiceDto.builder().seq(seq).build();
            given(duesRepositoryImpl.findById(seq)).willReturn(Optional.empty());
            given(ms.getMessage(eq("error.notfound.dues"), any(), any())).willReturn("회비를 찾을 수 없습니다");

            // when & then
            assertThatThrownBy(() -> duesService.editDues(data))
                    .isInstanceOf(IllegalArgumentException.class);
            then(duesRepositoryImpl).should().findById(seq);
        }
    }

    @Nested
    @DisplayName("회비 삭제")
    class DeleteDues {
        @Test
        @DisplayName("성공 - 회비가 삭제된다")
        void deleteDuesSuccess() {
            // given
            Long seq = 1L;
            Dues dues = Dues.createDues("이서준", 50000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비");
            given(duesRepositoryImpl.findById(seq)).willReturn(Optional.of(dues));
            willDoNothing().given(duesRepositoryImpl).delete(dues);

            // when
            duesService.deleteDues(seq);

            // then
            then(duesRepositoryImpl).should().findById(seq);
            then(duesRepositoryImpl).should().delete(dues);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회비를 삭제하려 하면 예외가 발생한다")
        void deleteDuesFailNotFound() {
            // given
            Long seq = 999L;
            given(duesRepositoryImpl.findById(seq)).willReturn(Optional.empty());
            given(ms.getMessage(eq("error.notfound.dues"), any(), any())).willReturn("회비를 찾을 수 없습니다");

            // when & then
            assertThatThrownBy(() -> duesService.deleteDues(seq))
                    .isInstanceOf(IllegalArgumentException.class);
            then(duesRepositoryImpl).should().findById(seq);
            then(duesRepositoryImpl).should(never()).delete(any(Dues.class));
        }
    }

    @Nested
    @DisplayName("회비 존재 확인")
    class CheckDuesExist {
        @Test
        @DisplayName("성공 - 존재하는 회비를 반환한다")
        void checkDuesExistSuccess() {
            // given
            Long seq = 1L;
            Dues dues = Dues.createDues("이서준", 50000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비");
            given(duesRepositoryImpl.findById(seq)).willReturn(Optional.of(dues));

            // when
            Dues result = duesService.checkDuesExist(seq);

            // then
            then(duesRepositoryImpl).should().findById(seq);
            assertThat(result).isEqualTo(dues);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회비면 예외가 발생한다")
        void checkDuesExistFailNotFound() {
            // given
            Long seq = 999L;
            given(duesRepositoryImpl.findById(seq)).willReturn(Optional.empty());
            given(ms.getMessage(eq("error.notfound.dues"), any(), any())).willReturn("회비를 찾을 수 없습니다");

            // when & then
            assertThatThrownBy(() -> duesService.checkDuesExist(seq))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // 테스트 헬퍼 메서드
    private void setDuesSeq(Dues dues, Long seq) {
        try {
            java.lang.reflect.Field field = Dues.class.getDeclaredField("seq");
            field.setAccessible(true);
            field.set(dues, seq);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
