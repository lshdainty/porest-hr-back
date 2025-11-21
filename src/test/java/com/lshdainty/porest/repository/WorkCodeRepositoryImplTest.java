package com.lshdainty.porest.repository;

import com.lshdainty.porest.work.domain.WorkCode;
import com.lshdainty.porest.work.repository.WorkCodeRepositoryImpl;
import com.lshdainty.porest.work.type.CodeType;
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
@Import({WorkCodeRepositoryImpl.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPA 업무코드 레포지토리 테스트")
class WorkCodeRepositoryImplTest {
    @Autowired
    private WorkCodeRepositoryImpl workCodeRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("업무코드 코드값으로 조회")
    void findByCode() {
        // given
        em.persist(WorkCode.createWorkCode("DEV", "개발", CodeType.LABEL, null, 1));
        em.flush();
        em.clear();

        // when
        Optional<WorkCode> findCode = workCodeRepository.findByCode("DEV");

        // then
        assertThat(findCode.isPresent()).isTrue();
        assertThat(findCode.get().getCode()).isEqualTo("DEV");
        assertThat(findCode.get().getName()).isEqualTo("개발");
    }

    @Test
    @DisplayName("업무코드 코드값으로 조회 - 없는 경우")
    void findByCodeEmpty() {
        // given & when
        Optional<WorkCode> findCode = workCodeRepository.findByCode("INVALID");

        // then
        assertThat(findCode.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("업무코드 시퀀스로 조회")
    void findBySeq() {
        // given
        WorkCode workCode = WorkCode.createWorkCode("DEV", "개발", CodeType.LABEL, null, 1);
        em.persist(workCode);
        em.flush();
        em.clear();

        // when
        Optional<WorkCode> findCode = workCodeRepository.findBySeq(workCode.getSeq());

        // then
        assertThat(findCode.isPresent()).isTrue();
        assertThat(findCode.get().getCode()).isEqualTo("DEV");
    }

    @Test
    @DisplayName("업무코드 시퀀스로 조회 - 없는 경우")
    void findBySeqEmpty() {
        // given & when
        Optional<WorkCode> findCode = workCodeRepository.findBySeq(999L);

        // then
        assertThat(findCode.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("조건으로 업무코드 조회 - 최상위 코드만 (parent가 null)")
    void findAllByConditionsParentIsNull() {
        // given
        WorkCode parent = WorkCode.createWorkCode("DEV", "개발", CodeType.LABEL, null, 1);
        em.persist(parent);
        em.persist(WorkCode.createWorkCode("BACKEND", "백엔드", CodeType.OPTION, parent, 1));
        em.flush();
        em.clear();

        // when
        List<WorkCode> rootCodes = workCodeRepository.findAllByConditions(null, null, true, null);

        // then
        assertThat(rootCodes).hasSize(1);
        assertThat(rootCodes.get(0).getCode()).isEqualTo("DEV");
    }

    @Test
    @DisplayName("조건으로 업무코드 조회 - 특정 부모의 자식 코드")
    void findAllByConditionsWithParent() {
        // given
        WorkCode parent = WorkCode.createWorkCode("DEV", "개발", CodeType.LABEL, null, 1);
        em.persist(parent);
        em.persist(WorkCode.createWorkCode("BACKEND", "백엔드", CodeType.OPTION, parent, 1));
        em.persist(WorkCode.createWorkCode("FRONTEND", "프론트엔드", CodeType.OPTION, parent, 2));
        em.flush();
        em.clear();

        // when
        List<WorkCode> childCodes = workCodeRepository.findAllByConditions(null, parent.getSeq(), false, null);

        // then
        assertThat(childCodes).hasSize(2);
    }

    @Test
    @DisplayName("조건으로 업무코드 조회 - 타입으로 필터링")
    void findAllByConditionsWithType() {
        // given
        em.persist(WorkCode.createWorkCode("DEV", "개발", CodeType.LABEL, null, 1));
        em.persist(WorkCode.createWorkCode("BACKEND", "백엔드", CodeType.OPTION, null, 2));
        em.flush();
        em.clear();

        // when
        List<WorkCode> labelCodes = workCodeRepository.findAllByConditions(null, null, null, CodeType.LABEL);
        List<WorkCode> optionCodes = workCodeRepository.findAllByConditions(null, null, null, CodeType.OPTION);

        // then
        assertThat(labelCodes).hasSize(1);
        assertThat(optionCodes).hasSize(1);
    }

    @Test
    @DisplayName("삭제된 업무코드는 조회되지 않는다.")
    void deletedCodeNotFound() {
        // given
        WorkCode workCode = WorkCode.createWorkCode("DEV", "개발", CodeType.LABEL, null, 1);
        em.persist(workCode);
        Long seq = workCode.getSeq();

        workCode.deleteWorkCode();
        em.flush();
        em.clear();

        // when
        Optional<WorkCode> findCode = workCodeRepository.findBySeq(seq);

        // then
        assertThat(findCode.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("업무코드 계층 구조 생성 및 조회")
    void createHierarchy() {
        // given
        WorkCode group = WorkCode.createWorkCode("DEV", "개발", CodeType.LABEL, null, 1);
        em.persist(group);

        WorkCode part = WorkCode.createWorkCode("BACKEND", "백엔드", CodeType.LABEL, group, 1);
        em.persist(part);

        em.persist(WorkCode.createWorkCode("API", "API 개발", CodeType.OPTION, part, 1));
        em.flush();
        em.clear();

        // when
        Optional<WorkCode> findDivision = workCodeRepository.findByCode("API");

        // then
        assertThat(findDivision.isPresent()).isTrue();
        assertThat(findDivision.get().getParent().getCode()).isEqualTo("BACKEND");
    }
}
