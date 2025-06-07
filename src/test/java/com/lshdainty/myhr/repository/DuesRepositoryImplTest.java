//package com.lshdainty.myhr.repository;
//
//import com.lshdainty.myhr.domain.Dues;
//import com.lshdainty.myhr.domain.DuesType;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.context.annotation.Import;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.*;
//
//@DataJpaTest
//@Import(DuesRepositoryImpl.class)
//@Transactional
//@DisplayName("JPA 회비 레포지토리 테스트")
//class DuesRepositoryImplTest {
//    @Autowired
//    private DuesRepositoryImpl duesRepositoryImpl;
//
//    @Autowired
//    private TestEntityManager em;
//
//    @Test
//    @DisplayName("회비 저장 및 단건 조회")
//    void save() {
//        // given
//        String userName = "이서준";
//        int amount = 10000;
//        DuesType type = DuesType.PLUS;
//        String date = "20250120";
//        String detail = "1월 생일 회비";
//
//        Dues dues = Dues.createDues(userName, amount, type, date, detail);
//
//        // when
//        duesRepositoryImpl.save(dues);
//        em.flush();
//        em.clear();
//
//        // then
//        Dues findDues = duesRepositoryImpl.findById(dues.getSeq());
//        assertThat(findDues).isNotNull();
//        assertThat(findDues.getUserName()).isEqualTo(userName);
//        assertThat(findDues.getAmount()).isEqualTo(amount);
//        assertThat(findDues.getType()).isEqualTo(type);
//        assertThat(findDues.getDate()).isEqualTo(date);
//        assertThat(findDues.getDetail()).isEqualTo(detail);
//    }
//
//    @Test
//    @DisplayName("전체 회비 목록 조회")
//    void getDues() {
//        // given
//        String[] names = {"이서준" ,"조민서" ,"이준우"};
//        int[] amounts = {10000, 80000, 10000};
//        DuesType[] types = {DuesType.PLUS, DuesType.MINUS, DuesType.PLUS};
//        String[] dates = {"20250104", "20250131", "20250204"};
//        String[] details = {"생일비", "생일비 출금", "생일비"};
//
//        for (int i = 0; i < names.length; i++) {
//            Dues dues = Dues.createDues(names[i], amounts[i], types[i], dates[i], details[i]);
//            duesRepositoryImpl.save(dues);
//        }
//
//        // when
//        List<Dues> dues = duesRepositoryImpl.findDues();
//
//        // then
//        assertThat(dues.size()).isEqualTo(names.length);
//        // 쿼리에서 날짜 기준으로 정렬하므로 순서까지 맞아야함
//        assertThat(dues).extracting("userName").containsExactly(names);
//        assertThat(dues).extracting("amount").containsExactly(10000, 80000, 10000);
//        assertThat(dues).extracting("type").containsExactly(types);
//        assertThat(dues).extracting("date").containsExactly(dates);
//        assertThat(dues).extracting("detail").containsExactly(details);
//    }
//
//    @Test
//    @DisplayName("년도에 해당하는 회비 목록 조회")
//    void getDuesByYear() {
//        // given
//        String[] names = {"이서준" ,"조민서" ,"이준우"};
//        int[] amounts = {10000, 80000, 10000};
//        DuesType[] types = {DuesType.PLUS, DuesType.MINUS, DuesType.PLUS};
//        String[] dates = {"20241204", "20250131", "20250204"};
//        String[] details = {"생일비", "생일비 출금", "생일비"};
//
//        for (int i = 0; i < names.length; i++) {
//            Dues dues = Dues.createDues(names[i], amounts[i], types[i], dates[i], details[i]);
//            duesRepositoryImpl.save(dues);
//        }
//
//        // when
//        List<Dues> dues = duesRepositoryImpl.findDuesByYear("2025");
//
//        // then
//        assertThat(dues.size()).isEqualTo(2);
//        // 쿼리에서 날짜 기준으로 정렬하므로 순서까지 맞아야함
//        assertThat(dues).extracting("userName").containsExactly("조민서" ,"이준우");
//        assertThat(dues).extracting("amount").containsExactly(80000, 10000);
//        assertThat(dues).extracting("type").containsExactly(DuesType.MINUS, DuesType.PLUS);
//        assertThat(dues).extracting("date").containsExactly("20250131", "20250204");
//        assertThat(dues).extracting("detail").containsExactly("생일비 출금", "생일비");
//    }
//
//    @Test
//    @DisplayName("회비 삭제")
//    void deleteDues() {
//        // given
//        String userName = "이서준";
//        int amount = 10000;
//        DuesType type = DuesType.PLUS;
//        String date = "20250120";
//        String detail = "1월 생일 회비";
//
//        Dues dues = Dues.createDues(userName, amount, type, date, detail);
//        duesRepositoryImpl.save(dues);
//
//        // when
//        duesRepositoryImpl.delete(dues);
//        em.flush();
//        em.clear();
//
//        // then
//        Dues findDues = duesRepositoryImpl.findById(dues.getSeq());
//        assertThat(findDues).isNull();
//    }
//}
