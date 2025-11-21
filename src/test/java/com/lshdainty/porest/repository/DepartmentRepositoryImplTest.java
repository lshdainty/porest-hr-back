package com.lshdainty.porest.repository;

import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.department.domain.Department;
import com.lshdainty.porest.department.domain.UserDepartment;
import com.lshdainty.porest.department.repository.DepartmentCustomRepositoryImpl;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.common.type.YNType;
import org.junit.jupiter.api.BeforeEach;
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
@Import({DepartmentCustomRepositoryImpl.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPA 부서 레포지토리 테스트")
class DepartmentRepositoryImplTest {
    @Autowired
    private DepartmentCustomRepositoryImpl departmentRepository;

    @Autowired
    private TestEntityManager em;

    private Company company;

    @BeforeEach
    void setUp() {
        company = Company.createCompany("company1", "테스트 회사", "설명");
        em.persist(company);
    }

    @Test
    @DisplayName("부서 저장 및 단건 조회")
    void save() {
        // given
        Department department = Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company);

        // when
        departmentRepository.save(department);
        em.flush();
        em.clear();

        // then
        Optional<Department> findDepartment = departmentRepository.findById(department.getId());
        assertThat(findDepartment.isPresent()).isTrue();
        assertThat(findDepartment.get().getName()).isEqualTo("개발팀");
        assertThat(findDepartment.get().getLevel()).isEqualTo(1L);
    }

    @Test
    @DisplayName("단건 조회 시 부서가 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given & when
        Optional<Department> findDepartment = departmentRepository.findById(999L);

        // then
        assertThat(findDepartment.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("부서와 자식 부서를 함께 조회")
    void findByIdWithChildren() {
        // given
        Department parent = Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company);
        departmentRepository.save(parent);

        departmentRepository.save(Department.createDepartment("프론트엔드", "프론트엔드", parent, null, 2L, "프론트엔드 팀", "#00FF00", company));
        departmentRepository.save(Department.createDepartment("백엔드", "백엔드", parent, null, 2L, "백엔드 팀", "#0000FF", company));

        em.flush();
        em.clear();

        // when
        Optional<Department> findDepartment = departmentRepository.findByIdWithChildren(parent.getId());

        // then
        assertThat(findDepartment.isPresent()).isTrue();
        assertThat(findDepartment.get().getChildren()).hasSize(2);
    }

    @Test
    @DisplayName("자식 부서 존재 여부 확인 - 있음")
    void hasActiveChildrenTrue() {
        // given
        Department parent = Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company);
        departmentRepository.save(parent);

        departmentRepository.save(Department.createDepartment("프론트엔드", "프론트엔드", parent, null, 2L, "프론트엔드 팀", "#00FF00", company));

        em.flush();
        em.clear();

        // when
        boolean hasChildren = departmentRepository.hasActiveChildren(parent.getId());

        // then
        assertThat(hasChildren).isTrue();
    }

    @Test
    @DisplayName("자식 부서 존재 여부 확인 - 없음")
    void hasActiveChildrenFalse() {
        // given
        Department department = Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company);
        departmentRepository.save(department);

        em.flush();
        em.clear();

        // when
        boolean hasChildren = departmentRepository.hasActiveChildren(department.getId());

        // then
        assertThat(hasChildren).isFalse();
    }

    @Test
    @DisplayName("유저-부서 연결 저장")
    void saveUserDepartment() {
        // given
        User user = User.createUser("user1");
        em.persist(user);

        Department department = Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company);
        departmentRepository.save(department);

        // when
        departmentRepository.saveUserDepartment(UserDepartment.createUserDepartment(user, department, YNType.Y));
        em.flush();
        em.clear();

        // then
        List<User> users = departmentRepository.findUsersInDepartment(department.getId());
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo("user1");
    }

    @Test
    @DisplayName("특정 유저의 메인 부서 조회")
    void findMainDepartmentByUserId() {
        // given
        User user = User.createUser("user1");
        em.persist(user);

        Department department = Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company);
        departmentRepository.save(department);

        departmentRepository.saveUserDepartment(UserDepartment.createUserDepartment(user, department, YNType.Y));
        em.flush();
        em.clear();

        // when
        Optional<UserDepartment> mainDepartment = departmentRepository.findMainDepartmentByUserId("user1");

        // then
        assertThat(mainDepartment.isPresent()).isTrue();
        assertThat(mainDepartment.get().getDepartment().getName()).isEqualTo("개발팀");
    }

    @Test
    @DisplayName("특정 유저와 부서의 연결 조회")
    void findUserDepartment() {
        // given
        User user = User.createUser("user1");
        em.persist(user);

        Department department = Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company);
        departmentRepository.save(department);

        departmentRepository.saveUserDepartment(UserDepartment.createUserDepartment(user, department, YNType.Y));
        em.flush();
        em.clear();

        // when
        Optional<UserDepartment> found = departmentRepository.findUserDepartment("user1", department.getId());

        // then
        assertThat(found.isPresent()).isTrue();
    }

    @Test
    @DisplayName("특정 부서에 속한 유저 조회")
    void findUsersInDepartment() {
        // given
        User user1 = User.createUser("user1");
        User user2 = User.createUser("user2");
        em.persist(user1);
        em.persist(user2);

        Department department = Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company);
        departmentRepository.save(department);

        departmentRepository.saveUserDepartment(UserDepartment.createUserDepartment(user1, department, YNType.Y));
        departmentRepository.saveUserDepartment(UserDepartment.createUserDepartment(user2, department, YNType.Y));
        em.flush();
        em.clear();

        // when
        List<User> users = departmentRepository.findUsersInDepartment(department.getId());

        // then
        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("특정 부서에 속하지 않은 유저 조회")
    void findUsersNotInDepartment() {
        // given
        User user1 = User.createUser("user1");
        User user2 = User.createUser("user2");
        User user3 = User.createUser("user3");
        em.persist(user1);
        em.persist(user2);
        em.persist(user3);

        Department department = Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company);
        departmentRepository.save(department);

        departmentRepository.saveUserDepartment(UserDepartment.createUserDepartment(user1, department, YNType.Y));
        em.flush();
        em.clear();

        // when
        List<User> usersNotIn = departmentRepository.findUsersNotInDepartment(department.getId());

        // then
        assertThat(usersNotIn).hasSize(2);
    }

    @Test
    @DisplayName("특정 유저의 메인 부서 존재 여부 확인")
    void hasMainDepartment() {
        // given
        User user = User.createUser("user1");
        em.persist(user);

        Department department = Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company);
        departmentRepository.save(department);

        departmentRepository.saveUserDepartment(UserDepartment.createUserDepartment(user, department, YNType.Y));
        em.flush();
        em.clear();

        // when
        boolean hasMain = departmentRepository.hasMainDepartment("user1");
        boolean hasNoMain = departmentRepository.hasMainDepartment("user2");

        // then
        assertThat(hasMain).isTrue();
        assertThat(hasNoMain).isFalse();
    }

    @Test
    @DisplayName("삭제된 부서는 조회되지 않는다.")
    void deletedDepartmentNotFound() {
        // given
        Department department = Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company);
        departmentRepository.save(department);
        Long departmentId = department.getId();

        department.deleteDepartment();
        em.flush();
        em.clear();

        // when
        Optional<Department> findDepartment = departmentRepository.findById(departmentId);

        // then
        assertThat(findDepartment.isEmpty()).isTrue();
    }
}
