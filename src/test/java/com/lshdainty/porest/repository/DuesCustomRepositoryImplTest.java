package com.lshdainty.porest.repository;

import com.lshdainty.porest.TestConfig;
import com.lshdainty.porest.domain.Dues;
import com.lshdainty.porest.type.DuesCalcType;
import com.lshdainty.porest.type.DuesType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({DuesRepositoryImpl.class, DuesCustomRepositoryImpl.class, TestConfig.class})
@Transactional
@DisplayName("QueryDSL 회비 레포지토리 테스트")
public class DuesCustomRepositoryImplTest {
    @Autowired
    private DuesRepositoryImpl duesRepositoryImpl;

    @Autowired
    private DuesCustomRepositoryImpl duesCustomRepositoryImpl;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("회비 단건 조회")
    void findDuesBySeq() {
        // given
        String userName = "이서준";
        Long amount = 10000L;
        DuesType type = DuesType.BIRTH;
        DuesCalcType calc = DuesCalcType.PLUS;
        String date = "20250120";
        String detail = "1월 생일 회비";

        Dues dues = Dues.createDues(userName, amount, type, calc, date, detail);

        // when
        duesRepositoryImpl.save(dues);
        em.flush();
        em.clear();

        // then
        Dues findDues = duesCustomRepositoryImpl.findById(dues.getSeq());
        assertThat(findDues).isNotNull();
    }
}
