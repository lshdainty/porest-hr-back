package com.lshdainty.porest.repository;

import com.lshdainty.porest.dues.domain.Dues;
import com.lshdainty.porest.dues.repository.DuesRepositoryImpl;
import com.lshdainty.porest.dues.type.DuesCalcType;
import com.lshdainty.porest.dues.type.DuesType;
import com.lshdainty.porest.dues.repository.dto.UsersMonthBirthDuesDto;
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
@Import({DuesRepositoryImpl.class, TestQuerydslConfig.class})
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
        Dues dues = Dues.createDues("이서준", 10000L, DuesType.OPERATION, DuesCalcType.PLUS, "20250120", "1월 생일 회비");

        // when
        duesRepositoryImpl.save(dues);
        em.flush();
        em.clear();

        // then
        Optional<Dues> findDues = duesRepositoryImpl.findById(dues.getSeq());
        assertThat(findDues.isPresent()).isTrue();
        assertThat(findDues.get().getUserName()).isEqualTo("이서준");
        assertThat(findDues.get().getAmount()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("단건 조회 시 회비가 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given & when
        Optional<Dues> findDues = duesRepositoryImpl.findById(999L);

        // then
        assertThat(findDues.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("모든 회비 목록이 조회돼야 한다.")
    void getDues() {
        // given
        duesRepositoryImpl.save(Dues.createDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250104", "생일비"));
        duesRepositoryImpl.save(Dues.createDues("조민서", 80000L, DuesType.BIRTH, DuesCalcType.MINUS, "20250131", "생일비 출금"));
        duesRepositoryImpl.save(Dues.createDues("이준우", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250204", "생일비"));

        // when
        List<Dues> result = duesRepositoryImpl.findDues();

        // then
        assertThat(result.size()).isEqualTo(3);
        assertThat(result).extracting("userName").containsExactly("이서준", "조민서", "이준우");
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
        duesRepositoryImpl.save(Dues.createDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20241204", "생일비"));
        duesRepositoryImpl.save(Dues.createDues("조민서", 80000L, DuesType.BIRTH, DuesCalcType.MINUS, "20250131", "생일비 출금"));
        duesRepositoryImpl.save(Dues.createDues("이준우", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250204", "생일비"));

        // when
        List<Dues> result = duesRepositoryImpl.findDuesByYear("2025");

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting("userName").containsExactly("조민서", "이준우");
    }

    @Test
    @DisplayName("년도에 해당하는 회비 목록이 없더라도 Null이 반환되면 안된다.")
    void getDuesByYearEmpty() {
        // given & when
        List<Dues> dues = duesRepositoryImpl.findDuesByYear("2025");

        // then
        assertThat(dues.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("년도가 null이 입력되어도 오류가 발생되면 안된다.")
    void getDuesByYearNull() {
        // given & when
        List<Dues> dues = duesRepositoryImpl.findDuesByYear(null);

        // then
        assertThat(dues.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("회비 삭제")
    void deleteDues() {
        // given
        Dues dues = Dues.createDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250120", "1월 생일 회비");
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
        duesRepositoryImpl.save(Dues.createDues("이서준", 10000L, DuesType.OPERATION, DuesCalcType.PLUS, "20250101", "운영비"));
        duesRepositoryImpl.save(Dues.createDues("김서연", 20000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250102", "생일비"));
        duesRepositoryImpl.save(Dues.createDues("박도윤", 5000L, DuesType.OPERATION, DuesCalcType.MINUS, "20250103", "운영비 사용"));

        // when
        List<Dues> result = duesRepositoryImpl.findOperatingDuesByYear("2025");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("type").containsOnly(DuesType.OPERATION);
    }

    @Test
    @DisplayName("생일 회비 월별 조회")
    void findBirthDuesByYearAndMonth() {
        // given
        duesRepositoryImpl.save(Dues.createDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "생일비"));
        duesRepositoryImpl.save(Dues.createDues("김서연", 20000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250102", "생일비"));
        duesRepositoryImpl.save(Dues.createDues("박도윤", 5000L, DuesType.OPERATION, DuesCalcType.PLUS, "20250103", "운영비"));

        // when
        Long totalAmount = duesRepositoryImpl.findBirthDuesByYearAndMonth("2025", "01");

        // then
        assertThat(totalAmount).isEqualTo(30000L);
    }

    @Test
    @DisplayName("월별 생일자 회비 조회")
    void findUsersMonthBirthDues() {
        // given
        duesRepositoryImpl.save(Dues.createDues("이서준", 10000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250101", "생일비"));
        duesRepositoryImpl.save(Dues.createDues("김서연", 20000L, DuesType.BIRTH, DuesCalcType.PLUS, "20250102", "생일비"));
        duesRepositoryImpl.save(Dues.createDues("박도윤", 5000L, DuesType.OPERATION, DuesCalcType.PLUS, "20250103", "운영비"));

        // when
        List<UsersMonthBirthDuesDto> result = duesRepositoryImpl.findUsersMonthBirthDues("2025");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("userName").containsExactlyInAnyOrder("이서준", "김서연");
    }
}
