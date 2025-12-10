package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.department.domain.Department;
import com.lshdainty.porest.department.domain.UserDepartment;
import com.lshdainty.porest.department.repository.DepartmentJpaRepository;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.common.type.YNType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({DepartmentJpaRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPA 부서 레포지토리 테스트")
class DepartmentJpaRepositoryTest {
    @Autowired
    private DepartmentJpaRepository departmentRepository;

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
        User user = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
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
        User user = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
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
        User user = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
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
        User user1 = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        User user2 = User.createUser(
                "user2", "password", "테스트유저2", "user2@test.com",
                LocalDate.of(1991, 2, 2), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
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
        User user1 = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        User user2 = User.createUser(
                "user2", "password", "테스트유저2", "user2@test.com",
                LocalDate.of(1991, 2, 2), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        User user3 = User.createUser(
                "user3", "password", "테스트유저3", "user3@test.com",
                LocalDate.of(1992, 3, 3), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
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
        User user = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
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

    @Test
    @DisplayName("특정 부서에 속한 UserDepartment 목록 조회")
    void findUserDepartmentsInDepartment() {
        // given
        User user1 = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        User user2 = User.createUser(
                "user2", "password", "테스트유저2", "user2@test.com",
                LocalDate.of(1991, 2, 2), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        em.persist(user1);
        em.persist(user2);

        Department department = Department.createDepartment("개발팀", "개발팀", null, null, 1L, "개발 부서", "#FF0000", company);
        departmentRepository.save(department);

        departmentRepository.saveUserDepartment(UserDepartment.createUserDepartment(user1, department, YNType.Y));
        departmentRepository.saveUserDepartment(UserDepartment.createUserDepartment(user2, department, YNType.N));
        em.flush();
        em.clear();

        // when
        List<UserDepartment> userDepartments = departmentRepository.findUserDepartmentsInDepartment(department.getId());

        // then
        assertThat(userDepartments).hasSize(2);
    }

    @Test
    @DisplayName("headUserId로 부서 목록 조회")
    void findByUserIds() {
        // given
        Department dept1 = Department.createDepartment("개발팀", "개발팀", null, "user1", 1L, "개발 부서", "#FF0000", company);
        Department dept2 = Department.createDepartment("디자인팀", "디자인팀", null, "user2", 1L, "디자인 부서", "#00FF00", company);
        Department dept3 = Department.createDepartment("기획팀", "기획팀", null, "user3", 1L, "기획 부서", "#0000FF", company);
        departmentRepository.save(dept1);
        departmentRepository.save(dept2);
        departmentRepository.save(dept3);
        em.flush();
        em.clear();

        // when
        List<Department> departments = departmentRepository.findByUserIds(List.of("user1", "user2"));

        // then
        assertThat(departments).hasSize(2);
    }

    @Test
    @DisplayName("사용자의 상위 결재자 부서 목록 조회")
    void findApproversByUserId() {
        // given
        User user = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        em.persist(user);

        // 부서 계층 생성: 본부 -> 팀 -> 파트
        Department headquarters = Department.createDepartment("본부", "본부", null, "head1", 1L, "본부", "#FF0000", company);
        departmentRepository.save(headquarters);

        Department team = Department.createDepartment("팀", "팀", headquarters, "head2", 2L, "팀", "#00FF00", company);
        departmentRepository.save(team);

        Department part = Department.createDepartment("파트", "파트", team, "head3", 3L, "파트", "#0000FF", company);
        departmentRepository.save(part);

        // user1을 파트에 메인 부서로 등록
        departmentRepository.saveUserDepartment(UserDepartment.createUserDepartment(user, part, YNType.Y));
        em.flush();
        em.clear();

        // when
        List<Department> approvers = departmentRepository.findApproversByUserId("user1");

        // then
        assertThat(approvers).hasSize(2); // 팀, 본부
        assertThat(approvers.get(0).getName()).isEqualTo("팀");
        assertThat(approvers.get(1).getName()).isEqualTo("본부");
    }

    @Test
    @DisplayName("메인 부서가 없는 사용자의 결재자 조회 시 빈 목록 반환")
    void findApproversByUserIdNoMainDepartment() {
        // given & when
        List<Department> approvers = departmentRepository.findApproversByUserId("nonexistent");

        // then
        assertThat(approvers).isEmpty();
    }

    @Test
    @DisplayName("최상위 부서인 경우 결재자가 없다")
    void findApproversByUserIdTopLevel() {
        // given
        User user = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        em.persist(user);

        Department topDept = Department.createDepartment("최상위", "최상위", null, "head1", 1L, "최상위", "#FF0000", company);
        departmentRepository.save(topDept);

        departmentRepository.saveUserDepartment(UserDepartment.createUserDepartment(user, topDept, YNType.Y));
        em.flush();
        em.clear();

        // when
        List<Department> approvers = departmentRepository.findApproversByUserId("user1");

        // then
        assertThat(approvers).isEmpty();
    }
}
