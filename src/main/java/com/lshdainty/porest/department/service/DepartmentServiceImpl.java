package com.lshdainty.porest.department.service;

import com.lshdainty.porest.common.exception.BusinessRuleViolationException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.InvalidValueException;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.company.service.CompanyService;
import com.lshdainty.porest.department.domain.Department;
import com.lshdainty.porest.department.domain.UserDepartment;
import com.lshdainty.porest.department.repository.DepartmentRepository;
import com.lshdainty.porest.department.service.dto.DepartmentServiceDto;
import com.lshdainty.porest.department.service.dto.UserDepartmentServiceDto;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final CompanyService companyService;
    private final UserService userService;

    @Override
    @Transactional
    public Long regist(DepartmentServiceDto data) {
        log.debug("부서 생성 시작: name={}, companyId={}", data.getName(), data.getCompanyId());
        // 회사 조회
        Company company = companyService.checkCompanyExists(data.getCompanyId());

        // 부모 부서 조회
        Department parent = null;
        if (data.getParentId() != null) {
            parent = checkDepartmentExists(data.getParentId());

            // 부모 부서와 같은 회사인지 검증
            if (!parent.getCompany().getId().equals(data.getCompanyId())) {
                log.warn("부서 생성 실패 - 부모 부서와 다른 회사: parentCompanyId={}, companyId={}", parent.getCompany().getId(), data.getCompanyId());
                throw new InvalidValueException(ErrorCode.DEPARTMENT_COMPANY_MISMATCH);
            }
        }

        Department department = Department.createDepartment(
                data.getName(),
                data.getNameKR(),
                parent,
                data.getHeadUserId(),
                data.getLevel(),
                data.getDesc(),
                data.getColor(),
                company
        );
        departmentRepository.save(department);
        log.info("부서 생성 완료: id={}, name={}", department.getId(), department.getName());
        return department.getId();
    }

    @Override
    @Transactional
    public void edit(DepartmentServiceDto data) {
        log.debug("부서 수정 시작: id={}", data.getId());
        Department department = checkDepartmentExists(data.getId());

        // 부모 부서 변경이 있는 경우 검증
        Department newParent = null;
        if (data.getParentId() != null) {
            newParent = checkDepartmentExists(data.getParentId());

            // 자기 자신을 부모로 설정하는 것 방지
            if (newParent.getId().equals(data.getId())) {
                log.warn("부서 수정 실패 - 자기 자신을 부모로 설정: id={}", data.getId());
                throw new InvalidValueException(ErrorCode.DEPARTMENT_SELF_REFERENCE);
            }

            // 순환 참조 방지 (자신의 하위 부서를 부모로 설정하는 것 방지)
            if (isDescendant(department, newParent)) {
                log.warn("부서 수정 실패 - 순환 참조: id={}, parentId={}", data.getId(), data.getParentId());
                throw new InvalidValueException(ErrorCode.DEPARTMENT_CIRCULAR_REFERENCE);
            }

            // 같은 회사인지 검증
            if (!newParent.getCompany().getId().equals(department.getCompany().getId())) {
                log.warn("부서 수정 실패 - 다른 회사 부서: id={}, parentCompanyId={}", data.getId(), newParent.getCompany().getId());
                throw new InvalidValueException(ErrorCode.DEPARTMENT_COMPANY_MISMATCH);
            }
        }

        department.updateDepartment(
                data.getName(),
                data.getNameKR(),
                newParent,
                data.getHeadUserId(),
                data.getLevel(),
                data.getDesc(),
                data.getColor()
        );
        log.info("부서 수정 완료: id={}", data.getId());
    }

    @Override
    @Transactional
    public void delete(Long departmentId) {
        log.debug("부서 삭제 시작: departmentId={}", departmentId);
        Department department = checkDepartmentExists(departmentId);

        // 하위에 자식 부서가 있는지 확인
        boolean hasChildren = department.getChildren().stream()
                .anyMatch(child -> YNType.isN(child.getIsDeleted()));

        if (hasChildren) {
            log.warn("부서 삭제 실패 - 하위 부서 존재: departmentId={}", departmentId);
            throw new BusinessRuleViolationException(ErrorCode.DEPARTMENT_HAS_MEMBERS);
        }

        // 논리 삭제 실행
        department.deleteDepartment();
        log.info("부서 삭제 완료: departmentId={}", departmentId);
    }

    @Override
    public DepartmentServiceDto searchDepartmentById(Long id) {
        log.debug("부서 조회: departmentId={}", id);
        Department department = checkDepartmentExists(id);
        return DepartmentServiceDto.builder()
                .id(department.getId())
                .name(department.getName())
                .nameKR(department.getNameKR())
                .parentId(department.getParentId())
                .headUserId(department.getHeadUserId())
                .level(department.getLevel())
                .desc(department.getDesc())
                .color(department.getColor())
                .companyId(department.getCompany().getId())
                .build();
    }

    @Override
    public DepartmentServiceDto searchDepartmentByIdWithChildren(Long id) {
        log.debug("부서 조회 (하위 부서 포함): departmentId={}", id);
        Department department = checkDepartmentExists(id);
        return DepartmentServiceDto.fromEntityWithChildren(department);
    }

    @Override
    @Transactional
    public List<Long> registUserDepartments(List<UserDepartmentServiceDto> userDataList, Long departmentId) {
        log.debug("부서 사용자 등록 시작: departmentId={}, userCount={}", departmentId, userDataList.size());
        // 부서 존재 여부 확인
        Department department = checkDepartmentExists(departmentId);

        List<Long> userDepartmentIds = new ArrayList<>();

        for (UserDepartmentServiceDto data : userDataList) {
            // userId로 User 조회
            User user = userService.checkUserExist(data.getUserId());

            // mainYN이 Y인 경우, 해당 유저의 기존 메인 부서가 있는지 확인
            if (YNType.isY(data.getMainYN())) {
                Optional<UserDepartment> existingMainDepartment =
                        departmentRepository.findMainDepartmentByUserId(user.getId());

                if (existingMainDepartment.isPresent()) {
                    log.warn("부서 사용자 등록 실패 - 메인 부서 이미 존재: userId={}", data.getUserId());
                    throw new BusinessRuleViolationException(ErrorCode.DEPARTMENT_HAS_MEMBERS);
                }
            }

            // UserDepartment 생성 및 저장
            UserDepartment userDepartment = UserDepartment.createUserDepartment(
                    user,
                    department,
                    data.getMainYN()
            );
            departmentRepository.saveUserDepartment(userDepartment);
            userDepartmentIds.add(userDepartment.getId());
        }

        log.info("부서 사용자 등록 완료: departmentId={}, count={}", departmentId, userDepartmentIds.size());
        return userDepartmentIds;
    }

    @Override
    @Transactional
    public void deleteUserDepartments(List<String> userIds, Long departmentId) {
        log.debug("부서 사용자 삭제 시작: departmentId={}, userIds={}", departmentId, userIds);
        for (String userId : userIds) {
            // UserDepartment 조회
            Optional<UserDepartment> userDepartmentOpt =
                    departmentRepository.findUserDepartment(userId, departmentId);

            if (userDepartmentOpt.isEmpty()) {
                log.warn("부서 사용자 삭제 실패 - 사용자 부서 관계 없음: userId={}, departmentId={}", userId, departmentId);
                throw new EntityNotFoundException(ErrorCode.DEPARTMENT_NOT_FOUND);
            }

            // 논리 삭제 실행
            UserDepartment userDepartment = userDepartmentOpt.get();
            userDepartment.deleteUserDepartment();
        }
        log.info("부서 사용자 삭제 완료: departmentId={}, count={}", departmentId, userIds.size());
    }

    @Override
    public DepartmentServiceDto getUsersInAndNotInDepartment(Long departmentId) {
        log.debug("부서별 사용자 조회: departmentId={}", departmentId);
        // 부서 존재 여부 확인 및 부서 정보 조회
        Department department = checkDepartmentExists(departmentId);

        // Repository에서 부서에 속한 UserDepartment 조회 (mainYN 포함)
        List<UserDepartment> userDepartmentsIn = departmentRepository.findUserDepartmentsInDepartment(departmentId);

        // Repository에서 부서에 속하지 않은 유저 조회
        List<User> usersNotIn = departmentRepository.findUsersNotInDepartment(departmentId);

        // UserDepartment Entity -> UserDepartmentServiceDto 변환
        List<UserDepartmentServiceDto> usersInDepartmentDto = userDepartmentsIn.stream()
                .map(userDepartment -> UserDepartmentServiceDto.builder()
                        .user(userDepartment.getUser())
                        .mainYN(userDepartment.getMainYN())
                        .build())
                .toList();

        // User Entity -> UserDepartmentServiceDto 변환 (부서에 속하지 않은 유저는 mainYN이 null)
        List<UserDepartmentServiceDto> usersNotInDepartmentDto = usersNotIn.stream()
                .map(user -> UserDepartmentServiceDto.builder()
                        .user(user)
                        .mainYN(null)
                        .build())
                .toList();

        // Department 정보 포함하여 반환
        return DepartmentServiceDto.builder()
                .id(department.getId())
                .name(department.getName())
                .nameKR(department.getNameKR())
                .parentId(department.getParentId())
                .headUserId(department.getHeadUserId())
                .level(department.getLevel())
                .desc(department.getDesc())
                .color(department.getColor())
                .company(department.getCompany())
                .companyId(department.getCompany() != null ? department.getCompany().getId() : null)
                .usersInDepartment(usersInDepartmentDto)
                .usersNotInDepartment(usersNotInDepartmentDto)
                .build();
    }

    @Override
    public Department checkDepartmentExists(Long departmentId) {
        Optional<Department> department = departmentRepository.findById(departmentId);
        if ((department.isEmpty()) || YNType.isY(department.get().getIsDeleted())) {
            log.warn("부서 조회 실패 - 존재하지 않거나 삭제된 부서: departmentId={}", departmentId);
            throw new EntityNotFoundException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }
        return department.get();
    }

    /**
     * 순환 참조 검사: targetDepartment가 currentDepartment의 하위 부서인지 확인
     */
    private boolean isDescendant(Department currentDepartment, Department targetDepartment) {
        if (currentDepartment == null || targetDepartment == null) {
            return false;
        }

        for (Department child : currentDepartment.getChildren()) {
            if (YNType.isN(child.getIsDeleted())) {
                if (child.getId().equals(targetDepartment.getId())) {
                    return true;
                }
                if (isDescendant(child, targetDepartment)) {
                    return true;
                }
            }
        }
        return false;
    }
}
