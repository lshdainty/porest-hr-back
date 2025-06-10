package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.Dues;
import com.lshdainty.myhr.domain.DuesType;
import com.lshdainty.myhr.repository.DuesRepositoryImpl;
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
        int amount = 50000;
        DuesType type = DuesType.PLUS;
        String date = "20250101";
        String detail = "1월 회비";
        willDoNothing().given(duesRepositoryImpl).save(any(Dues.class));

        // When
        Long duesSeq = duesService.save(userName, amount, type, date, detail);

        // Then
        then(duesRepositoryImpl).should().save(any(Dues.class));
    }

    @Test
    @DisplayName("전체 회비 조회 테스트 - 성공 (시간 정렬)")
    void findDuesSuccessTest() {
        // Given
        given(duesRepositoryImpl.findDues()).willReturn(List.of(
                Dues.createDues("이서준", 50000, DuesType.PLUS, "20250101", "1월 회비"),
                Dues.createDues("김서연", 10000, DuesType.PLUS, "20250101", "1월 회비"),
                Dues.createDues("김지후", 10000, DuesType.PLUS, "20250101", "1월 회비")
        ));

        // When
        List<Dues> duesList = duesService.findDues();

        // Then
        then(duesRepositoryImpl).should().findDues();
        assertThat(duesList).hasSize(3);
        // date 기준 order by이므로 순서 중요
        assertThat(duesList)
                .extracting("userName")
                .containsExactly("이서준", "김서연", "김지후");
        assertThat(duesList)
                .extracting("amount")
                .containsExactly(50000, 10000, 10000);
    }

    @Test
    @DisplayName("연도별 회비 조회 테스트 - 성공 (시간 정렬)")
    void findDuesByYearSuccessTest() {
        // Given
        String year = "2025";
        given(duesRepositoryImpl.findDuesByYear(year)).willReturn(List.of(
                Dues.createDues("이서준", 50000, DuesType.PLUS, "20250101", "1월 회비"),
                Dues.createDues("김서연", 10000, DuesType.PLUS, "20250101", "1월 회비"),
                Dues.createDues("김지후", 10000, DuesType.PLUS, "20250101", "1월 회비")
        ));

        // When
        List<Dues> duesList = duesService.findDuesByYear(year);

        // Then
        then(duesRepositoryImpl).should().findDuesByYear(year);
        assertThat(duesList).hasSize(3);
        assertThat(duesList)
                .extracting("date")
                .allSatisfy(date -> assertThat(String.valueOf(date)).contains("2025"));
    }

    @Test
    @DisplayName("회비 삭제 테스트 - 성공")
    void deleteDuesSuccessTest() {
        // Given
        Long seq = 1L;
        Dues dues = Dues.createDues("이서준", 50000, DuesType.PLUS, "20250101", "1월 회비");

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
}
