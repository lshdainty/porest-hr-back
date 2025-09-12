package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.User;
import com.lshdainty.porest.domain.Vacation;
import com.lshdainty.porest.type.CompanyType;
import com.lshdainty.porest.type.DepartmentType;
import com.lshdainty.porest.type.VacationType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@DataJpaTest
@Import(UserRepositoryImpl.class)
@Transactional
@DisplayName("JPA 유저 레포지토리 테스트")
class UserRepositoryImplTest {
    @Autowired
    private UserRepositoryImpl userRepositoryImpl;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("유저 등록 및 단건 조회")
    void save() {
        // given
        String id = "user1";
        String name = "홍길동";
        String pwd = "";
        String email = "";
        String birth = "19700204";
        CompanyType company = CompanyType.SKAX;
        DepartmentType department = DepartmentType.SKC;
        String workTime = "9 ~ 6";
        String lunarYN = "N";

        User user = User.createUser(id, pwd, name, email, birth, company, department, workTime, lunarYN);

        // when
        userRepositoryImpl.save(user);
        em.flush();
        em.clear();

        // then
        Optional<User> findUser = userRepositoryImpl.findById(user.getId());
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getId()).isEqualTo(id);
        assertThat(findUser.get().getPwd()).isEqualTo(pwd);
        assertThat(findUser.get().getName()).isEqualTo(name);
        assertThat(findUser.get().getEmail()).isEqualTo(email);
        assertThat(findUser.get().getBirth()).isEqualTo(birth);
        assertThat(findUser.get().getCompany()).isEqualTo(company);
        assertThat(findUser.get().getDepartment()).isEqualTo(department);
        assertThat(findUser.get().getWorkTime()).isEqualTo(workTime);
        assertThat(findUser.get().getLunarYN()).isEqualTo(lunarYN);
    }

    @Test
    @DisplayName("단건 조회 시 유저가 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given
        String userId = "";

        // when
        Optional<User> findUser = userRepositoryImpl.findById(userId);

        // then
        assertThat(findUser.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("delYN이 N인 모든 유저가 보여야한다.")
    void getUsers() {
        // given
        User user1 = User.createUser("user1", "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        User user2 = User.createUser("user2", "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        User user3 = User.createUser("user3", "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        User user4 = User.createUser("user4", "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        List<User> users = List.of(user1, user2, user3, user4);

        for (User user : users) {
            userRepositoryImpl.save(user);
        }

        // when
        user3.deleteUser();
        em.flush();
        em.clear();
        List<User> findUsers = userRepositoryImpl.findUsers();

        // then
        assertThat(findUsers.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("유저가 없어도 Null이 반환되면 안된다.")
    void getUsersEmpty() {
        // given & when
        List<User> users = userRepositoryImpl.findUsers();

        // then
        assertThat(users.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("delYN이 N인 모든 유저가 보여야한다. (vacation fetch join)")
    void getUsersWithVacations() {
        User user1 = User.createUser("user1", "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        User user2 = User.createUser("user2", "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        User user3 = User.createUser("user3", "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        User user4 = User.createUser("user4", "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        List<User> users = List.of(user1, user2, user3, user4);

        for (User user : users) {
            userRepositoryImpl.save(user);
        }

        // when
        user3.deleteUser();
        em.flush();
        em.clear();
        List<User> findUsers = userRepositoryImpl.findUsers();

        // then
        assertThat(findUsers.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("삭제된 유저는 delYN 상태값이 Y여야 한다.")
    void deleteUser() {
        // given
        User user = User.createUser("user1", "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        userRepositoryImpl.save(user);

        // when
        user.deleteUser();
        em.flush();
        em.clear();
        Optional<User> findUser = userRepositoryImpl.findById(user.getId());

        // then
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getDelYN()).isEqualTo("Y");
    }

    @Test
    @DisplayName("유저 수정")
    void updateUser() {
        // given
        String id = "user1";
        String name = "홍길동";
        String pwd = "";
        String email = "";
        String birth = "19700204";
        CompanyType company = CompanyType.SKAX;
        DepartmentType department = DepartmentType.SKC;
        String workTime = "9 ~ 6";
        String lunarYN = "N";

        User user = User.createUser(id, pwd, name, email, birth, company, department, workTime, lunarYN);
        userRepositoryImpl.save(user);

        name = "이서준";
        workTime = "10 ~ 7";

        // when
        user.updateUser(name, null, null, null, null, null, workTime, null);
        em.flush();
        em.clear();
        Optional<User> findUser = userRepositoryImpl.findById(user.getId());

        // then
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getId()).isEqualTo(id);
        assertThat(findUser.get().getPwd()).isEqualTo(pwd);
        assertThat(findUser.get().getName()).isEqualTo(name);
        assertThat(findUser.get().getEmail()).isEqualTo(email);
        assertThat(findUser.get().getBirth()).isEqualTo(birth);
        assertThat(findUser.get().getCompany()).isEqualTo(company);
        assertThat(findUser.get().getDepartment()).isEqualTo(department);
        assertThat(findUser.get().getWorkTime()).isEqualTo(workTime);
        assertThat(findUser.get().getLunarYN()).isEqualTo(lunarYN);
    }

    @Test
    @DisplayName("유저를 조회할 때 각각이 가지고 있는 휴가 리스트도 같이 조회돼야 한다. (vacation fetch join)")
    void getUserWithVacations() {
        // given
        User userA = User.createUser("user1");
        User userB = User.createUser("user2");
        User userC = User.createUser("user3");
        userRepositoryImpl.save(userA);
        userRepositoryImpl.save(userB);
        userRepositoryImpl.save(userC);

        LocalDateTime now = LocalDateTime.now();
        em.persist(Vacation.createVacation(userA, VacationType.ANNUAL, new BigDecimal("4"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), "", "127.0.0.1"));
        em.persist(Vacation.createVacation(userB, VacationType.ANNUAL, new BigDecimal("4"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), "", "127.0.0.1"));
        em.persist(Vacation.createVacation(userC, VacationType.ANNUAL, new BigDecimal("4"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), "", "127.0.0.1"));
        em.persist(Vacation.createVacation(userA, VacationType.ANNUAL, new BigDecimal("4"), LocalDateTime.of(now.getYear(), 4, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), "", "127.0.0.1"));
        em.persist(Vacation.createVacation(userB, VacationType.ANNUAL, new BigDecimal("4"), LocalDateTime.of(now.getYear(), 4, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), "", "127.0.0.1"));

        userB.deleteUser();
        em.flush();
        em.clear();

        // when
        List<User> users = userRepositoryImpl.findUsersWithVacations();
        int countA = 0;
        int countC = 0;
        for (User user : users) {
            List<Vacation> lists = user.getVacations();
            for (Vacation vacation : lists) {
                if (user.getId().equals(userA.getId())) {
                    countA++;
                } else if (user.getId().equals(userC.getId())) {
                    countC++;
                }
            }
        }

        // then
        assertThat(users.size()).isEqualTo(2);
        assertThat(countA).isEqualTo(2);
        assertThat(countC).isEqualTo(1);
    }

    @Test
    @DisplayName("유저가 없어도 Null이 반환되면 안된다. (vacation fetch join)")
    void getUsersWithVacationsEmpty() {
        // given & when
        List<User> users = userRepositoryImpl.findUsers();

        // then
        assertThat(users.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("유저를 조회할 때 휴가가 없어도 에러가 발생하면 안된다. (vacation fetch join)")
    void getUserWithVacationsEmpty() {
        // given
        User userA = User.createUser("user1", "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        User userB = User.createUser("user2", "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        User userC = User.createUser("user3", "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        userRepositoryImpl.save(userA);
        userRepositoryImpl.save(userB);
        userRepositoryImpl.save(userC);

        LocalDateTime now = LocalDateTime.now();
        em.persist(Vacation.createVacation(userA, VacationType.ANNUAL, new BigDecimal("4"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), "", "127.0.0.1"));
        em.persist(Vacation.createVacation(userB, VacationType.ANNUAL, new BigDecimal("4"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), "", "127.0.0.1"));
        em.persist(Vacation.createVacation(userA, VacationType.ANNUAL, new BigDecimal("4"), LocalDateTime.of(now.getYear(), 4, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), "", "127.0.0.1"));
        em.persist(Vacation.createVacation(userB, VacationType.ANNUAL, new BigDecimal("4"), LocalDateTime.of(now.getYear(), 4, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), "", "127.0.0.1"));

        userB.deleteUser();

        // when
        List<User> users = userRepositoryImpl.findUsersWithVacations();
        int countA = 0;
        int countC = 0;
        for (User user : users) {
            List<Vacation> lists = user.getVacations();

            for (Vacation vacation : lists) {
                if (user.getName().equals(userA.getName())) {
                    countA++;
                } else {
                    countC++;
                }
            }
        }

        // then
        assertThat(users.size()).isEqualTo(2);
        assertThat(countA).isEqualTo(2);
        assertThat(countC).isEqualTo(0);
    }
}