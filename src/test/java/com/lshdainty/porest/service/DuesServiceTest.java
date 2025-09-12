package com.lshdainty.porest.service;

import com.lshdainty.porest.domain.Dues;
import com.lshdainty.porest.type.DuesCalcType;
import com.lshdainty.porest.type.DuesType;
import com.lshdainty.porest.repository.DuesRepositoryImpl;
import com.lshdainty.porest.repository.dto.UsersMonthBirthDuesDto;
import com.lshdainty.porest.service.dto.DuesServiceDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("회비 서비스 테스트")
class DuesServiceTest {
    // 삭제하지 말 것 (NullpointException 발생)
    @Mock
    private MessageSource ms;
    @Mock
    private DuesRepositoryImpl duesRepositoryImpl;

    @InjectMocks
    private DuesService duesService;

    @Test
    @DisplayName("회비 저장 테스트 - 성공")
    void saveDuesSuccessTest() {
        // Given
        String userName = "이서준";
        Long amount = 50000L;
        DuesType type = DuesType.BIRTH;
        DuesCalcType calc = DuesCalcType.PLUS;
        String date = "20250101";
        String detail = "1월 회비";
        willDoNothing().given(duesRepositoryImpl).save(any(Dues.class));

        // When
        duesService.save(DuesServiceDto.builder()
                .userName(userName)
                .amount(amount)
                .type(type)
                .calc(calc)
                .date(date)
                .detail(detail)
                .build()
        );

        // Then
        then(duesRepositoryImpl).should().save(any(Dues.class));
    }

    @Test
    @DisplayName("전체 회비 조회 테스트 - 성공 (시간 정렬)")
    void findDuesSuccessTest() {
        // Given
        given(duesRepositoryImpl.findDues()).willReturn(List.of(
                Dues.createDues("이서준", 50000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비"),
                Dues.createDues("김서연", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비"),
                Dues.createDues("김지후", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비")
        ));

        // When
        List<DuesServiceDto> duesList = duesService.findDues();

        // Then
        then(duesRepositoryImpl).should().findDues();
        assertThat(duesList).hasSize(3);
        // date 기준 order by이므로 순서 중요
        assertThat(duesList)
                .extracting("userName")
                .containsExactly("이서준", "김서연", "김지후");
        assertThat(duesList)
                .extracting("amount")
                .containsExactly(50000L, 10000L, 10000L);
    }

    @Test
    @DisplayName("연도별 회비 조회 테스트 - 성공 (시간 정렬)")
    void findDuesByYearSuccessTest() {
        // Given
        String year = "2025";
        given(duesRepositoryImpl.findDuesByYear(year)).willReturn(List.of(
                Dues.createDues("이서준", 50000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비"),
                Dues.createDues("김서연", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비"),
                Dues.createDues("김지후", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비")
        ));

        // When
        List<DuesServiceDto> duesList = duesService.findDuesByYear(year);

        // Then
        then(duesRepositoryImpl).should().findDuesByYear(year);
        assertThat(duesList).hasSize(3);
        assertThat(duesList)
                .extracting("date")
                .allSatisfy(date -> assertThat(String.valueOf(date)).contains("2025"));
    }

    @Test
    @DisplayName("운영 회비 조회 테스트 - 성공")
    void findOperatingDuesByYearSuccessTest() {
        // Given
        String year = "2025";
        given(duesRepositoryImpl.findOperatingDuesByYear(year)).willReturn(List.of(
                Dues.createDues("이서준", 50000L, DuesType.OPERATION, DuesCalcType.PLUS, "20250101", "1월 회비"),
                Dues.createDues("김서연", 10000L, DuesType.OPERATION, DuesCalcType.MINUS, "20250101", "1월 회비")
        ));

        // When
        DuesServiceDto result = duesService.findOperatingDuesByYear(year);

        // Then
        then(duesRepositoryImpl).should().findOperatingDuesByYear(year);
        assertThat(result.getTotalDues()).isEqualTo(40000L);
        assertThat(result.getTotalDeposit()).isEqualTo(50000L);
        assertThat(result.getTotalWithdrawal()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("생일 회비 월별 조회 테스트 - 성공")
    void findBirthDuesByYearAndMonthSuccessTest() {
        // Given
        String year = "2025";
        String month = "01";
        given(duesRepositoryImpl.findBirthDuesByYearAndMonth(year, month)).willReturn(100000L);

        // When
        Long totalAmount = duesService.findBirthDuesByYearAndMonth(year, month);

        // Then
        then(duesRepositoryImpl).should().findBirthDuesByYearAndMonth(year, month);
        assertThat(totalAmount).isEqualTo(100000L);
    }

    @Test
    @DisplayName("월별 생일자 회비 조회 테스트 - 성공")
    void findUsersMonthBirthDuesSuccessTest() {
        // Given
        String year = "2025";
        given(duesRepositoryImpl.findUsersMonthBirthDues(year)).willReturn(List.of(
                new UsersMonthBirthDuesDto("이서준", "01", 50000L, "1월 생일 회비"),
                new UsersMonthBirthDuesDto("김서연", "02", 60000L, "2월 생일 회비")
        ));

        // When
        List<DuesServiceDto> result = duesService.findUsersMonthBirthDues(year);

        // Then
        then(duesRepositoryImpl).should().findUsersMonthBirthDues(year);
        assertThat(result).hasSize(2);
        assertThat(result).extracting("userName").containsExactly("이서준", "김서연");
    }

    @Test
    @DisplayName("회비 수정 테스트 - 성공")
    void editDuesSuccessTest() {
        // Given
        Long seq = 1L;
        String userName = "이서준";
        Long amount = 10000L;
        DuesType type = DuesType.BIRTH;
        DuesCalcType calc = DuesCalcType.PLUS;
        String date = "20250101";
        String detail = "1월 회비";
        Dues dues = Dues.createDues(userName, amount, type, calc, date, detail);

        setDuesSeq(dues, seq);
        given(duesRepositoryImpl.findById(seq)).willReturn(Optional.of(dues));

        // When
        duesService.editDues(DuesServiceDto.builder()
                .seq(seq)
                .userName("이민서")
                .build()
        );

        // Then
        then(duesRepositoryImpl).should().findById(seq);
        assertThat(dues.getUserName()).isEqualTo("이민서");
        assertThat(dues.getAmount()).isEqualTo(amount);
        assertThat(dues.getType()).isEqualTo(type);
        assertThat(dues.getCalc()).isEqualTo(calc);
        assertThat(dues.getDate()).isEqualTo(date);
        assertThat(dues.getDetail()).isEqualTo(detail);
    }

    @Test
    @DisplayName("회비 수정 테스트 - 실패 (회비 없음)")
    void editDuesFailTestNotFoundDues() {
        // Given
        Long seq = 900L;
        DuesServiceDto data = DuesServiceDto.builder().seq(seq).build();
        given(duesRepositoryImpl.findById(seq)).willReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> duesService.editDues(data));
        then(duesRepositoryImpl).should().findById(seq);
    }

    @Test
    @DisplayName("회비 삭제 테스트 - 성공")
    void deleteDuesSuccessTest() {
        // Given
        Long seq = 1L;
        Dues dues = Dues.createDues("이서준", 50000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "1월 회비");

        given(duesRepositoryImpl.findById(seq)).willReturn(Optional.of(dues));
        willDoNothing().given(duesRepositoryImpl).delete(dues);

        // When
        duesService.deleteDues(seq);

        // Then
        then(duesRepositoryImpl).should().findById(seq);
        then(duesRepositoryImpl).should().delete(dues);
    }

    @Test
    @DisplayName("회비 삭제 테스트 - 실패 (회비 없음)")
    void deleteDuesFailTestNotFoundDues() {
        // Given
        Long seq = 900L;
        given(duesRepositoryImpl.findById(seq)).willReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> duesService.deleteDues(seq));
        then(duesRepositoryImpl).should().findById(seq);
        then(duesRepositoryImpl).should(never()).delete(any(Dues.class));
    }

    // 테스트 헬퍼 메서드
    private void setDuesSeq(Dues dues, Long seq) {
        try {
            java.lang.reflect.Field field = Dues.class.getDeclaredField("seq");
            field.setAccessible(true);
            field.set(dues, seq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
