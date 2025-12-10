package com.lshdainty.porest.service;

import com.lshdainty.porest.common.exception.DuplicateException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.InvalidValueException;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.work.domain.WorkCode;
import com.lshdainty.porest.work.repository.WorkCodeRepository;
import com.lshdainty.porest.work.service.WorkCodeServiceImpl;
import com.lshdainty.porest.work.service.dto.WorkCodeServiceDto;
import com.lshdainty.porest.work.type.CodeType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("업무 코드 서비스 테스트")
class WorkCodeServiceTest {

    @Mock
    private WorkCodeRepository workCodeRepository;

    @InjectMocks
    private WorkCodeServiceImpl workCodeService;

    private WorkCode createTestWorkCode(Long id, String code, String name, CodeType type) {
        WorkCode workCode = WorkCode.createWorkCode(code, name, type, null, 1);
        ReflectionTestUtils.setField(workCode, "id", id);
        return workCode;
    }

    @Nested
    @DisplayName("업무 코드 목록 조회")
    class FindWorkCodes {
        @Test
        @DisplayName("성공 - 업무 코드 목록을 반환한다")
        void findWorkCodesSuccess() {
            // given
            WorkCode code1 = createTestWorkCode(1L, "WC001", "업무코드1", CodeType.LABEL);
            WorkCode code2 = createTestWorkCode(2L, "WC002", "업무코드2", CodeType.LABEL);

            given(workCodeRepository.findAllByConditions(null, null, null, null))
                    .willReturn(List.of(code1, code2));

            // when
            List<WorkCodeServiceDto> result = workCodeService.findWorkCodes(null, null, null, null);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting("code").containsExactly("WC001", "WC002");
        }

        @Test
        @DisplayName("성공 - 업무 코드가 없으면 빈 리스트를 반환한다")
        void findWorkCodesEmpty() {
            // given
            given(workCodeRepository.findAllByConditions(null, null, null, null))
                    .willReturn(List.of());

            // when
            List<WorkCodeServiceDto> result = workCodeService.findWorkCodes(null, null, null, null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - 타입별 업무 코드 목록을 반환한다")
        void findWorkCodesByType() {
            // given
            CodeType type = CodeType.LABEL;
            WorkCode code1 = createTestWorkCode(1L, "WC001", "업무코드1", type);

            given(workCodeRepository.findAllByConditions(null, null, null, type))
                    .willReturn(List.of(code1));

            // when
            List<WorkCodeServiceDto> result = workCodeService.findWorkCodes(null, null, null, type);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo(type);
        }

        @Test
        @DisplayName("성공 - 상위 코드별 업무 코드 목록을 반환한다")
        void findWorkCodesByParentCode() {
            // given
            String parentCode = "PARENT";
            WorkCode code1 = createTestWorkCode(1L, "WC001", "업무코드1", CodeType.OPTION);

            given(workCodeRepository.findAllByConditions(eq(parentCode), any(), any(), any()))
                    .willReturn(List.of(code1));

            // when
            List<WorkCodeServiceDto> result = workCodeService.findWorkCodes(parentCode, null, null, null);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 상위 코드가 null인 업무 코드 목록을 반환한다")
        void findWorkCodesParentIsNull() {
            // given
            WorkCode code1 = createTestWorkCode(1L, "WC001", "업무코드1", CodeType.LABEL);

            given(workCodeRepository.findAllByConditions(null, null, true, null))
                    .willReturn(List.of(code1));

            // when
            List<WorkCodeServiceDto> result = workCodeService.findWorkCodes(null, null, true, null);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("업무 코드 생성")
    class CreateWorkCode {
        @Test
        @DisplayName("성공 - 업무 코드가 생성된다")
        void createWorkCodeSuccess() {
            // given
            String code = "WC001";
            String name = "업무코드1";
            CodeType type = CodeType.LABEL;
            Integer orderSeq = 1;

            given(workCodeRepository.findByCode(code)).willReturn(Optional.empty());
            willDoNothing().given(workCodeRepository).save(any(WorkCode.class));

            // when
            Long result = workCodeService.createWorkCode(code, name, type, null, orderSeq);

            // then
            then(workCodeRepository).should().save(any(WorkCode.class));
        }

        @Test
        @DisplayName("성공 - 상위 코드와 함께 업무 코드가 생성된다")
        void createWorkCodeWithParent() {
            // given
            Long parentId = 1L;
            WorkCode parentCode = createTestWorkCode(parentId, "PARENT", "상위코드", CodeType.LABEL);

            String code = "WC001";
            String name = "업무코드1";
            CodeType type = CodeType.OPTION;
            Integer orderSeq = 1;

            given(workCodeRepository.findById(parentId)).willReturn(Optional.of(parentCode));
            given(workCodeRepository.findByCode(code)).willReturn(Optional.empty());
            willDoNothing().given(workCodeRepository).save(any(WorkCode.class));

            // when
            Long result = workCodeService.createWorkCode(code, name, type, parentId, orderSeq);

            // then
            then(workCodeRepository).should().save(any(WorkCode.class));
        }

        @Test
        @DisplayName("실패 - 중복된 코드면 예외가 발생한다")
        void createWorkCodeFailDuplicate() {
            // given
            String code = "WC001";
            WorkCode existingCode = createTestWorkCode(1L, code, "업무코드1", CodeType.LABEL);

            given(workCodeRepository.findByCode(code)).willReturn(Optional.of(existingCode));

            // when & then
            assertThatThrownBy(() -> workCodeService.createWorkCode(code, "새로운 코드", CodeType.LABEL, null, 1))
                    .isInstanceOf(DuplicateException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상위 코드면 예외가 발생한다")
        void createWorkCodeFailParentNotFound() {
            // given
            Long parentId = 999L;
            given(workCodeRepository.findById(parentId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> workCodeService.createWorkCode("WC001", "업무코드1", CodeType.OPTION, parentId, 1))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("업무 코드 수정")
    class UpdateWorkCode {
        @Test
        @DisplayName("성공 - 업무 코드가 수정된다")
        void updateWorkCodeSuccess() {
            // given
            Long id = 1L;
            WorkCode workCode = createTestWorkCode(id, "WC001", "업무코드1", CodeType.LABEL);

            String newCode = "WC001-NEW";
            String newName = "수정된 업무코드";
            Integer newOrderSeq = 2;

            given(workCodeRepository.findById(id)).willReturn(Optional.of(workCode));
            given(workCodeRepository.findByCode(newCode)).willReturn(Optional.empty());

            // when
            workCodeService.updateWorkCode(id, newCode, newName, null, newOrderSeq);

            // then
            assertThat(workCode.getCode()).isEqualTo(newCode);
            assertThat(workCode.getName()).isEqualTo(newName);
            assertThat(workCode.getOrderSeq()).isEqualTo(newOrderSeq);
        }

        @Test
        @DisplayName("성공 - 상위 코드와 함께 수정된다")
        void updateWorkCodeWithParent() {
            // given
            Long id = 1L;
            Long parentId = 2L;
            WorkCode workCode = createTestWorkCode(id, "WC001", "업무코드1", CodeType.OPTION);
            WorkCode parentCode = createTestWorkCode(parentId, "PARENT", "상위코드", CodeType.LABEL);

            given(workCodeRepository.findById(id)).willReturn(Optional.of(workCode));
            given(workCodeRepository.findById(parentId)).willReturn(Optional.of(parentCode));

            // when
            workCodeService.updateWorkCode(id, null, null, parentId, null);

            // then
            assertThat(workCode.getParent()).isEqualTo(parentCode);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 업무 코드면 예외가 발생한다")
        void updateWorkCodeFailNotFound() {
            // given
            Long id = 999L;
            given(workCodeRepository.findById(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> workCodeService.updateWorkCode(id, "WC001", "업무코드1", null, 1))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 자기 자신을 부모로 설정하면 예외가 발생한다")
        void updateWorkCodeFailSelfParent() {
            // given
            Long id = 1L;
            WorkCode workCode = createTestWorkCode(id, "WC001", "업무코드1", CodeType.LABEL);

            given(workCodeRepository.findById(id)).willReturn(Optional.of(workCode));

            // when & then
            assertThatThrownBy(() -> workCodeService.updateWorkCode(id, null, null, id, null))
                    .isInstanceOf(InvalidValueException.class);
        }

        @Test
        @DisplayName("실패 - 중복된 코드로 수정하면 예외가 발생한다")
        void updateWorkCodeFailDuplicate() {
            // given
            Long id = 1L;
            Long otherId = 2L;
            WorkCode workCode = createTestWorkCode(id, "WC001", "업무코드1", CodeType.LABEL);
            WorkCode otherCode = createTestWorkCode(otherId, "WC002", "업무코드2", CodeType.LABEL);

            given(workCodeRepository.findById(id)).willReturn(Optional.of(workCode));
            given(workCodeRepository.findByCode("WC002")).willReturn(Optional.of(otherCode));

            // when & then
            assertThatThrownBy(() -> workCodeService.updateWorkCode(id, "WC002", null, null, null))
                    .isInstanceOf(DuplicateException.class);
        }
    }

    @Nested
    @DisplayName("업무 코드 삭제")
    class DeleteWorkCode {
        @Test
        @DisplayName("성공 - 업무 코드가 삭제된다")
        void deleteWorkCodeSuccess() {
            // given
            Long id = 1L;
            WorkCode workCode = createTestWorkCode(id, "WC001", "업무코드1", CodeType.LABEL);

            given(workCodeRepository.findById(id)).willReturn(Optional.of(workCode));

            // when
            workCodeService.deleteWorkCode(id);

            // then
            assertThat(workCode.getIsDeleted()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 업무 코드면 예외가 발생한다")
        void deleteWorkCodeFailNotFound() {
            // given
            Long id = 999L;
            given(workCodeRepository.findById(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> workCodeService.deleteWorkCode(id))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
