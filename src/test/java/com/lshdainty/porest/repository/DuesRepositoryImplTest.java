package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.Dues;
import com.lshdainty.porest.type.DuesCalcType;
import com.lshdainty.porest.type.DuesType;
import com.lshdainty.porest.repository.dto.UsersMonthBirthDuesDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(DuesRepositoryImpl.class)
@Transactional
@DisplayName("JPA 회비 레포지토리 테스트")
class DuesRepositoryImplTest {
    @Autowired
    private DuesRepositoryImpl duesRepositoryImpl;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("회비 저장 및 단건 조회")
    void save() {
        // given
        String userName = "이서준";
        Long amount = 10000L;
        DuesType type = DuesType.OPERATION;
        DuesCalcType calc = DuesCalcType.PLUS;
        String date = "20250120";
        String detail = "1월 생일 회비";

        Dues dues = Dues.createDues(userName, amount, type, calc, date, detail);

        // when
        duesRepositoryImpl.save(dues);
        em.flush();
        em.clear();

        // then
        Optional<Dues> findDues = duesRepositoryImpl.findById(dues.getSeq());
        assertThat(findDues.isPresent()).isTrue();
        assertThat(findDues.get().getUserName()).isEqualTo(userName);
        assertThat(findDues.get().getAmount()).isEqualTo(amount);
        assertThat(findDues.get().getType()).isEqualTo(type);
        assertThat(findDues.get().getCalc()).isEqualTo(calc);
        assertThat(findDues.get().getDate()).isEqualTo(date);
        assertThat(findDues.get().getDetail()).isEqualTo(detail);
    }

    @Test
    @DisplayName("단건 조회 시 회비가 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given
        Long duesId = 999L;

        // when
        Optional<Dues> findDues = duesRepositoryImpl.findById(duesId);

        // then
        assertThat(findDues.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("모든 회비 목록이 조회돼야 한다.")
    void getDues() {
        // given
        List<Dues> dues = List.of(
                Dues.createDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250104", "생일비"),
                Dues.createDues("조민서", 80000L, DuesType.BIRTH, DuesCalcType.MINUS, "20250131", "생일비 출금"),
                Dues.createDues("이준우", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250204", "생일비")
        );
        for (Dues due : dues) {
            duesRepositoryImpl.save(due);
        }

        // when
        List<Dues> result = duesRepositoryImpl.findDues();

        // then
        assertThat(result.size()).isEqualTo(dues.size());
        // 쿼리에서 날짜 기준으로 정렬하므로 순서까지 맞아야함
        assertThat(result).extracting("userName").containsExactly("이서준", "조민서", "이준우");
        assertThat(result).extracting("amount").containsExactly(10000L, 80000L, 10000L);
        assertThat(result).extracting("type").containsExactly(DuesType.BIRTH, DuesType.BIRTH, DuesType.BIRTH);
        assertThat(result).extracting("calc").containsExactly(DuesCalcType.PLUS, DuesCalcType.MINUS, DuesCalcType.PLUS);
        assertThat(result).extracting("date").containsExactly("20250104", "20250131", "20250204");
        assertThat(result).extracting("detail").containsExactly("생일비", "생일비 출금", "생일비");
    }

    @Test
    @DisplayName("회비 목록이 없더라도 Null이 반환되면 안된다.")
    void getDuesEmpty() {
        // given & when
        List<Dues> dues = duesRepositoryImpl.findDues();

        // then
        assertThat(dues.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("년도에 해당하는 회비 목록이 정렬되어 반환돼야 한다.")
    void getDuesByYear() {
        // given
        String year = "2025";
        List<Dues> dues = List.of(
                Dues.createDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20241204", "생일비"),
                Dues.createDues("조민서", 80000L, DuesType.BIRTH, DuesCalcType.MINUS, "20250131", "생일비 출금"),
                Dues.createDues("이준우", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250204", "생일비")
        );
        for (Dues due : dues) {
            duesRepositoryImpl.save(due);
        }

        // when
        List<Dues> result = duesRepositoryImpl.findDuesByYear(year);

        // then
        assertThat(result.size()).isEqualTo(2);
        // 쿼리에서 날짜 기준으로 정렬하므로 순서까지 맞아야함
        assertThat(result).extracting("userName").containsExactly("조민서" ,"이준우");
        assertThat(result).extracting("amount").containsExactly(80000L, 10000L);
        assertThat(result).extracting("type").containsExactly(DuesType.BIRTH, DuesType.BIRTH);
        assertThat(result).extracting("calc").containsExactly(DuesCalcType.MINUS, DuesCalcType.PLUS);
        assertThat(result).extracting("date").containsExactly("20250131", "20250204");
        assertThat(result).extracting("detail").containsExactly("생일비 출금", "생일비");
    }

    @Test
    @DisplayName("년도에 해당하는 회비 목록이 없더라도 Null이 반환되면 안된다.")
    void getDuesByYearEmpty() {
        // given
        String year = "2025";

        // given & when
        List<Dues> dues = duesRepositoryImpl.findDuesByYear(year);

        // then
        assertThat(dues.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("년도가 null이 입력되어도 오류가 발생되면 안된다.")
    void getDuesByYearNull() {
        // given
        String year = null;

        // given & when
        List<Dues> dues = duesRepositoryImpl.findDuesByYear(year);

        // then
        assertThat(dues.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("회비 삭제")
    void deleteDues() {
        // given
        String userName = "이서준";
        Long amount = 10000L;
        DuesType type = DuesType.BIRTH;
        DuesCalcType calc = DuesCalcType.PLUS;
        String date = "20250120";
        String detail = "1월 생일 회비";

        Dues dues = Dues.createDues(userName, amount, type, calc, date, detail);
        duesRepositoryImpl.save(dues);

        // when
        duesRepositoryImpl.delete(dues);
        em.flush();
        em.clear();
        Optional<Dues> findDues = duesRepositoryImpl.findById(dues.getSeq());

        // then
        assertThat(findDues.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("운영 회비 조회")
    void findOperatingDuesByYear() {
        // given
        String year = "2025";
        List<Dues> dues = List.of(
                Dues.createDues("이서준", 10000L, DuesType.OPERATION, DuesCalcType.PLUS, "20250101", "운영비"),
                Dues.createDues("김서연", 20000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250102", "생일비"),
                Dues.createDues("박도윤", 5000L, DuesType.OPERATION, DuesCalcType.MINUS, "20250103", "운영비 사용")
        );
        for (Dues due : dues) {
            duesRepositoryImpl.save(due);
        }

        // when
        List<Dues> result = duesRepositoryImpl.findOperatingDuesByYear(year);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("type").containsOnly(DuesType.OPERATION);
    }

    @Test
    @DisplayName("생일 회비 월별 조회")
    void findBirthDuesByYearAndMonth() {
        // given
        String year = "2025";
        String month = "01";
        List<Dues> dues = List.of(
                Dues.createDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "생일비"),
                Dues.createDues("김서연", 20000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250102", "생일비"),
                Dues.createDues("박도윤", 5000L, DuesType.OPERATION, DuesCalcType.PLUS, "20250103", "운영비")
        );
        for (Dues due : dues) {
            duesRepositoryImpl.save(due);
        }

        // when
        Long totalAmount = duesRepositoryImpl.findBirthDuesByYearAndMonth(year, month);

        // then
        assertThat(totalAmount).isEqualTo(30000L);
    }

    @Test
    @DisplayName("월별 생일자 회비 조회")
    void findUsersMonthBirthDues() {
        // given
        String year = "2025";
        List<Dues> dues = List.of(
                Dues.createDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "생일비"),
                Dues.createDues("김서연", 20000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250102", "생일비"),
                Dues.createDues("박도윤", 5000L, DuesType.OPERATION, DuesCalcType.PLUS, "20250103", "운영비")
        );
        for (Dues due : dues) {
            duesRepositoryImpl.save(due);
        }

        // when
        List<UsersMonthBirthDuesDto> result = duesRepositoryImpl.findUsersMonthBirthDues(year);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("userName").containsExactlyInAnyOrder("이서준", "김서연");
    }
}
