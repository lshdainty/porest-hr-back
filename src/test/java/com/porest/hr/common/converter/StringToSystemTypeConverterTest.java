package com.porest.hr.common.converter;

import com.porest.core.exception.InvalidValueException;
import com.porest.hr.common.type.DefaultSystemType;
import com.porest.hr.common.type.SystemType;
import com.porest.hr.work.type.TestSystemType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StringToSystemTypeConverter 테스트")
class StringToSystemTypeConverterTest {

    private StringToSystemTypeConverter converter;

    @BeforeAll
    static void setUpClass() {
        // 테스트용 SystemType 구현체 등록
        StringToSystemTypeConverter.register(TestSystemType.class);
    }

    @BeforeEach
    void setUp() {
        converter = new StringToSystemTypeConverter();
    }

    @Test
    @DisplayName("null 입력 시 null 반환")
    void convert_null_returnsNull() {
        // when
        SystemType result = converter.convert(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("빈 문자열 입력 시 null 반환")
    void convert_emptyString_returnsNull() {
        // when
        SystemType result = converter.convert("");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("DefaultSystemType.ETC 변환 성공")
    void convert_defaultSystemType_success() {
        // when
        SystemType result = converter.convert("ETC");

        // then
        assertThat(result).isEqualTo(DefaultSystemType.ETC);
    }

    @Test
    @DisplayName("TestSystemType 변환 성공")
    void convert_originSystemType_success() {
        // when
        SystemType result = converter.convert("ERP");

        // then
        assertThat(result).isEqualTo(TestSystemType.ERP);
    }

    @Test
    @DisplayName("TestSystemType의 모든 값 변환 성공")
    void convert_allTestSystemTypes_success() {
        for (TestSystemType type : TestSystemType.values()) {
            // when
            SystemType result = converter.convert(type.name());

            // then
            assertThat(result).isEqualTo(type);
        }
    }

    @Test
    @DisplayName("존재하지 않는 타입 변환 시 InvalidValueException 발생")
    void convert_unknownType_throwsException() {
        // when & then
        assertThatThrownBy(() -> converter.convert("UNKNOWN_TYPE"))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining("Unknown SystemType");
    }
}
