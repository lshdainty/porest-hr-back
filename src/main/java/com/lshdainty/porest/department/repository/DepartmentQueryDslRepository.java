package com.lshdainty.porest.department.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.department.domain.Department;
import com.lshdainty.porest.department.domain.UserDepartment;
import com.lshdainty.porest.user.domain.User;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.lshdainty.porest.department.domain.QDepartment.department;
import static com.lshdainty.porest.department.domain.QUserDepartment.userDepartment;
import static com.lshdainty.porest.user.domain.QUser.user;

@Repository
@Primary
@RequiredArgsConstructor
public class DepartmentQueryDslRepository implements DepartmentRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(Department department) {
        em.persist(department);
    }

    @Override
    public Optional<Department> findById(Long id) {
        return Optional.ofNullable(query
                .selectFrom(department)
                .where(
                        department.id.eq(id),
                        department.isDeleted.eq(YNType.N)
                )
                .fetchOne()
        );
    }

    @Override
    public Optional<Department> findByIdWithChildren(Long id) {
        return Optional.ofNullable(query
                .selectFrom(department)
                .leftJoin(department.children).fetchJoin()
                .where(
                        department.id.eq(id),
                        department.isDeleted.eq(YNType.N)
                )
                .distinct()
                .fetchOne()
        );
    }

    @Override
    public boolean hasActiveChildren(Long departmentId) {
        Long count = query
                .select(department.count())
                .from(department)
                .where(
                        department.parent.id.eq(departmentId),
                        department.isDeleted.eq(YNType.N)
                )
                .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public void saveUserDepartment(UserDepartment userDepartment) {
        em.persist(userDepartment);
    }

    @Override
    public Optional<UserDepartment> findMainDepartmentByUserId(String userId) {
        return Optional.ofNullable(query
                .selectFrom(userDepartment)
                .where(
                        userDepartment.user.id.eq(userId),
                        userDepartment.mainYN.eq(YNType.Y),
                        userDepartment.isDeleted.eq(YNType.N)
                )
                .fetchOne()
        );
    }

    @Override
    public Optional<UserDepartment> findUserDepartment(String userId, Long departmentId) {
        return Optional.ofNullable(query
                .selectFrom(userDepartment)
                .where(
                        userDepartment.user.id.eq(userId),
                        userDepartment.department.id.eq(departmentId),
                        userDepartment.isDeleted.eq(YNType.N)
                )
                .fetchOne()
        );
    }

    @Override
    public List<User> findUsersInDepartment(Long departmentId) {
        return query
                .select(userDepartment.user)
                .from(userDepartment)
                .join(userDepartment.user, user)
                .join(userDepartment.department, department)
                .where(
                        userDepartment.department.id.eq(departmentId),
                        userDepartment.isDeleted.eq(YNType.N),
                        department.isDeleted.eq(YNType.N),
                        user.isDeleted.eq(YNType.N),
                        user.company.ne(OriginCompanyType.SYSTEM)
                )
                .fetch();
    }

    @Override
    public List<User> findUsersNotInDepartment(Long departmentId) {
        return query
                .selectFrom(user)
                .where(
                        user.isDeleted.eq(YNType.N),
                        user.company.ne(OriginCompanyType.SYSTEM),
                        JPAExpressions
                                .selectFrom(userDepartment)
                                .join(userDepartment.department, department)
                                .where(
                                        userDepartment.user.id.eq(user.id),
                                        userDepartment.department.id.eq(departmentId),
                                        userDepartment.isDeleted.eq(YNType.N),
                                        department.isDeleted.eq(YNType.N)
                                )
                                .notExists()
                )
                .fetch();
    }

    @Override
    public List<UserDepartment> findUserDepartmentsInDepartment(Long departmentId) {
        return query
                .selectFrom(userDepartment)
                .join(userDepartment.user, user).fetchJoin()
                .join(userDepartment.department, department)
                .where(
                        userDepartment.department.id.eq(departmentId),
                        userDepartment.isDeleted.eq(YNType.N),
                        department.isDeleted.eq(YNType.N),
                        user.isDeleted.eq(YNType.N),
                        user.company.ne(OriginCompanyType.SYSTEM)
                )
                .fetch();
    }

    @Override
    public boolean hasMainDepartment(String userId) {
        Long count = query
                .select(userDepartment.count())
                .from(userDepartment)
                .where(
                        userDepartment.user.id.eq(userId),
                        userDepartment.mainYN.eq(YNType.Y),
                        userDepartment.isDeleted.eq(YNType.N)
                )
                .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public List<Department> findByUserIds(List<String> userIds) {
        return query
                .selectFrom(department)
                .leftJoin(department.headUser, user).fetchJoin()
                .where(
                        department.headUser.id.in(userIds),
                        department.isDeleted.eq(YNType.N)
                )
                .fetch();
    }

    @Override
    public List<Department> findApproversByUserId(String userId) {
        // 1. 사용자의 메인 부서 조회
        UserDepartment userDept = query
                .selectFrom(userDepartment)
                .join(userDepartment.department, department).fetchJoin()
                .where(
                        userDepartment.user.id.eq(userId),
                        userDepartment.mainYN.eq(YNType.Y),
                        userDepartment.isDeleted.eq(YNType.N)
                )
                .fetchOne();

        if (userDept == null) {
            return List.of();
        }

        // 2. 상위 부서들을 재귀적으로 조회
        List<Department> approverDepartments = new ArrayList<>();
        Department currentDept = userDept.getDepartment();

        // 현재 부서의 parent부터 시작하여 최상위 부서까지 조회
        while (currentDept != null && currentDept.getParentId() != null) {
            // parent 부서 조회
            Department parentDept = query
                    .selectFrom(department)
                    .where(
                            department.id.eq(currentDept.getParentId()),
                            department.isDeleted.eq(YNType.N)
                    )
                    .fetchOne();

            if (parentDept != null && parentDept.getHeadUser() != null) {
                approverDepartments.add(parentDept);
            }

            currentDept = parentDept;
        }

        return approverDepartments;
    }
}
