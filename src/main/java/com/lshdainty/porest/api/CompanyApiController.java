package com.lshdainty.porest.api;

import com.lshdainty.porest.domain.Company;
import com.lshdainty.porest.domain.Department;
import com.lshdainty.porest.repository.CompanyCustomRepositoryImpl;
import com.lshdainty.porest.type.YNType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CompanyApiController {
    private final CompanyCustomRepositoryImpl companyRepository;

    @GetMapping("/api/v1/test")
    public void test() {
        Optional<Company> result = companyRepository.findByIdWithDepartments("SKC");

        if(!result.isPresent()) return;

        Company company = result.get();

        log.info("========================================");
        log.info("회사명: " + company.getName() + " (ID: " + company.getId() + ")");
        log.info("회사 설명: " + company.getDesc());
        log.info("========================================");

        // 최상위 부서들만 필터링 (parent가 null인 부서들)
        List<Department> rootDepartments = company.getDepartments().stream()
                .filter(dept -> dept.getParent() == null)
                .sorted(Comparator.comparing(Department::getId)) // ID 순으로 정렬
                .collect(Collectors.toList());

        if (rootDepartments.isEmpty()) {
            log.info("등록된 부서가 없습니다.");
            return;
        }

        // 각 최상위 부서에 대해 재귀적으로 출력
        for (Department rootDept : rootDepartments) {
            printDepartmentRecursive(rootDept, "", true);
        }

        log.info("========================================");
    }

    /**
     * 재귀적으로 부서와 하위 부서들을 출력하는 함수
     *
     * @param department 현재 출력할 부서
     * @param prefix 들여쓰기를 위한 접두사
     * @param isLast 현재 부서가 같은 레벨에서 마지막인지 여부
     */
    private void printDepartmentRecursive(Department department, String prefix, boolean isLast) {
        if (department == null) {
            return;
        }

        // 현재 부서 정보 출력
        String connector = isLast ? "└── " : "├── ";
        log.info(prefix + connector + formatDepartmentInfo(department));

        // 자식 부서들 가져오기 (정렬)
        List<Department> children = department.getChildren().stream()
                .filter(child -> child.getDelYN() == YNType.N) // 삭제되지 않은 부서만
                .sorted(Comparator.comparing(Department::getId)) // ID 순으로 정렬
                .collect(Collectors.toList());

        // 하위 부서가 있는 경우 재귀적으로 출력
        if (!children.isEmpty()) {
            String newPrefix = prefix + (isLast ? "    " : "│   ");

            for (int i = 0; i < children.size(); i++) {
                boolean isLastChild = (i == children.size() - 1);
                printDepartmentRecursive(children.get(i), newPrefix, isLastChild);
            }
        }
    }

    /**
     * 부서 정보를 포맷팅하는 함수
     */
    private String formatDepartmentInfo(Department department) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(department.getId()).append("] ");
        sb.append(department.getName());

        if (department.getNameKR() != null && !department.getNameKR().isEmpty()) {
            sb.append(" (").append(department.getNameKR()).append(")");
        }

        sb.append(" - Level: ").append(department.getLevel());

        if (department.getHeadUserId() != null) {
            sb.append(" - 부서장: ").append(department.getHeadUserId());
        }

        if (department.getDesc() != null && !department.getDesc().isEmpty()) {
            sb.append(" - 설명: ").append(department.getDesc());
        }

        return sb.toString();
    }
}
