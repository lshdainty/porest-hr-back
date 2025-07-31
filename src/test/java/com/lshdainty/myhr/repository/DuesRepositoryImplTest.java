package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.Dues;
import com.lshdainty.myhr.domain.DuesCalcType;
import com.lshdainty.myhr.domain.DuesType;
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
        String[] names = {"이서준" ,"조민서" ,"이준우"};
        Long[] amounts = {10000L, 80000L, 10000L};
        DuesType[] types = {DuesType.BIRTH, DuesType.BIRTH, DuesType.BIRTH};
        DuesCalcType[] calcs = {DuesCalcType.PLUS, DuesCalcType.MINUS, DuesCalcType.PLUS};
        String[] dates = {"20250104", "20250131", "20250204"};
        String[] details = {"생일비", "생일비 출금", "생일비"};

        for (int i = 0; i < names.length; i++) {
            Dues dues = Dues.createDues(names[i], amounts[i], types[i], calcs[i], dates[i], details[i]);
            duesRepositoryImpl.save(dues);
        }

        // when
        List<Dues> dues = duesRepositoryImpl.findDues();

        // then
        assertThat(dues.size()).isEqualTo(names.length);
        // 쿼리에서 날짜 기준으로 정렬하므로 순서까지 맞아야함
        assertThat(dues).extracting("userName").containsExactly(names);
        assertThat(dues).extracting("amount").containsExactly(10000L, 80000L, 10000L);
        assertThat(dues).extracting("type").containsExactly(types);
        assertThat(dues).extracting("calc").containsExactly(calcs);
        assertThat(dues).extracting("date").containsExactly(dates);
        assertThat(dues).extracting("detail").containsExactly(details);
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
        String[] names = {"이서준" ,"조민서" ,"이준우"};
        Long[] amounts = {10000L, 80000L, 10000L};
        DuesType[] types = {DuesType.BIRTH, DuesType.BIRTH, DuesType.BIRTH};
        DuesCalcType[] calcs = {DuesCalcType.PLUS, DuesCalcType.MINUS, DuesCalcType.PLUS};
        String[] dates = {"20241204", "20250131", "20250204"};
        String[] details = {"생일비", "생일비 출금", "생일비"};

        for (int i = 0; i < names.length; i++) {
            Dues dues = Dues.createDues(names[i], amounts[i], types[i], calcs[i], dates[i], details[i]);
            duesRepositoryImpl.save(dues);
        }

        // when
        List<Dues> dues = duesRepositoryImpl.findDuesByYear(year);

        // then
        assertThat(dues.size()).isEqualTo(2);
        // 쿼리에서 날짜 기준으로 정렬하므로 순서까지 맞아야함
        assertThat(dues).extracting("userName").containsExactly("조민서" ,"이준우");
        assertThat(dues).extracting("amount").containsExactly(80000L, 10000L);
        assertThat(dues).extracting("type").containsExactly(DuesType.BIRTH, DuesType.BIRTH);
        assertThat(dues).extracting("calc").containsExactly(DuesCalcType.MINUS, DuesCalcType.PLUS);
        assertThat(dues).extracting("date").containsExactly("20250131", "20250204");
        assertThat(dues).extracting("detail").containsExactly("생일비 출금", "생일비");
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
}
