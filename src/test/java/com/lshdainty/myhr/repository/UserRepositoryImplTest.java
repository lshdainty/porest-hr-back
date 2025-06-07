//package com.lshdainty.myhr.repository;
//
//import com.lshdainty.myhr.domain.User;
//import com.lshdainty.myhr.domain.Vacation;
//import com.lshdainty.myhr.domain.VacationType;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.context.annotation.Import;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.*;
//
//@DataJpaTest
//@Import(UserRepositoryImpl.class)
//@Transactional
//@DisplayName("JPA 유저 레포지토리 테스트")
//class UserRepositoryImplTest {
//    @Autowired
//    private UserRepositoryImpl userRepositoryImpl;
//
//    @Autowired
//    private TestEntityManager em;
//
//    @Test
//    @DisplayName("유저 등록 및 단건 조회")
//    void save() {
//        // given
//        String name = "홍길동";
//        String birth = "19700204";
//        String employ = "BP";
//        String workTime = "9 ~ 6";
//        String lunarYN = "N";
//
//        User user = User.createUser(name, birth, employ, workTime, lunarYN);
//
//        // when
//        userRepositoryImpl.save(user);
//        em.flush();
//        em.clear();
//
//        // then
//        User findUser = userRepositoryImpl.findById(user.getId());
//        assertThat(findUser).isNotNull();
//        assertThat(findUser.getName()).isEqualTo(name);
//        assertThat(findUser.getBirth()).isEqualTo(birth);
//        assertThat(findUser.getEmploy()).isEqualTo(employ);
//        assertThat(findUser.getWorkTime()).isEqualTo(workTime);
//        assertThat(findUser.getLunarYN()).isEqualTo(lunarYN);
//    }
//
//    @Test
//    @DisplayName("유저 리스트 조회")
//    void getUsers() {
//        // given
//        String[] names = {"이서준", "김서연", "김지후"};
//        String[] births = {"19700723", "19701026", "19740115"};
//        String[] employs = {"9 ~ 6", "8 ~ 5", "10 ~ 7"};
//        String[] workTimes = {"ADMIN", "BP", "BP"};
//        String[] lunarYNs = {"N", "N", "Y"};
//
//        for (int i = 0; i < names.length; i++) {
//            User user = User.createUser(names[i], births[i], employs[i], workTimes[i], lunarYNs[i]);
//            userRepositoryImpl.save(user);
//        }
//
//        // when
//        List<User> users = userRepositoryImpl.findUsers();
//
//        // then
//        assertThat(users.size()).isEqualTo(names.length);
//        assertThat(users).extracting("name").containsExactlyInAnyOrder(names);
//        assertThat(users).extracting("birth").containsExactlyInAnyOrder(births);
//        assertThat(users).extracting("employ").containsExactlyInAnyOrder(employs);
//        assertThat(users).extracting("workTime").containsExactlyInAnyOrder(workTimes);
//        assertThat(users).extracting("lunarYN").containsExactlyInAnyOrder(lunarYNs);
//    }
//
//    @Test
//    @DisplayName("유저 삭제")
//    void deleteUser() {
//        // given
//        String name = "홍길동";
//        String birth = "19700204";
//        String employ = "BP";
//        String workTime = "9 ~ 6";
//        String lunarYN = "N";
//
//        User user = User.createUser(name, birth, employ, workTime, lunarYN);
//        userRepositoryImpl.save(user);
//
//        // when
//        user.deleteUser();
//        em.flush();
//        em.clear();
//
//        // then
//        User findUser = userRepositoryImpl.findById(user.getId());
//        assertThat(findUser.getDelYN()).isEqualTo("Y");
//    }
//
//    @Test
//    @DisplayName("유저 수정")
//    void updateUser() {
//        // given
//        String name = "홍길동";
//        String birth = "19700204";
//        String employ = "BP";
//        String workTime = "9 ~ 6";
//        String lunarYN = "N";
//
//        User user = User.createUser(name, birth, employ, workTime, lunarYN);
//        userRepositoryImpl.save(user);
//
//        name = "이서준";
//        workTime = "10 ~ 7";
//
//        // when
//        user.updateUser(name, birth, employ, workTime, lunarYN);
//        em.flush();
//        em.clear();
//
//        // then
//        User findUser = userRepositoryImpl.findById(user.getId());
//        assertThat(findUser.getName()).isEqualTo(name);
//        assertThat(findUser.getBirth()).isEqualTo(birth);
//        assertThat(findUser.getEmploy()).isEqualTo(employ);
//        assertThat(findUser.getWorkTime()).isEqualTo(workTime);
//        assertThat(findUser.getLunarYN()).isEqualTo(lunarYN);
//    }
//
//    @Test
//    @DisplayName("유저 각각이 가지고 있는 휴가 리스트 조회")
//    void getUserWithVacations() {
//        // given
//        User userA = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
//        User userB = User.createUser("김서연", "19701026", "8 ~ 5", "BP", "N");
//        User userC = User.createUser("김지후", "19740115", "10 ~ 7", "BP", "Y");
//        userRepositoryImpl.save(userA);
//        userRepositoryImpl.save(userB);
//        userRepositoryImpl.save(userC);
//
//        LocalDateTime now = LocalDateTime.now();
//        em.persist(Vacation.createVacation(userA, "1분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), 0L, "127.0.0.1"));
//        em.persist(Vacation.createVacation(userB, "1분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), 0L, "127.0.0.1"));
//        em.persist(Vacation.createVacation(userC, "1분기 휴가", "작년 하루 사용", VacationType.BASIC, new BigDecimal("24"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), 0L, "127.0.0.1"));
//        em.persist(Vacation.createVacation(userA, "2분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 4, 1, 0, 0, 0), 0L, "127.0.0.1"));
//        em.persist(Vacation.createVacation(userB, "2분기 휴가", "", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), LocalDateTime.of(now.getYear(), 4, 1, 0, 0, 0), 0L, "127.0.0.1"));
//
//        userB.deleteUser();
//
//        // when
//        List<User> users = userRepositoryImpl.findUsersWithVacations();
//        int countA = 0;
//        int countC = 0;
//        for (User user : users) {
//            List<Vacation> lists = user.getVacations();
//
//            for (Vacation vacation : lists) {
//                if (user.getName().equals(userA.getName())) {
//                    countA++;
//                } else {
//                    countC++;
//                }
//            }
//        }
//
//        // then
//        assertThat(users.size()).isEqualTo(2);
//        assertThat(countA).isEqualTo(2);
//        assertThat(countC).isEqualTo(1);
//    }
//}