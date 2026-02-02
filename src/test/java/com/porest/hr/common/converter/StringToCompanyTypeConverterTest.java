package com.porest.hr.common.converter;

import com.porest.core.exception.InvalidValueException;
import com.porest.hr.common.type.CompanyType;
import com.porest.hr.common.type.DefaultCompanyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StringToCompanyTypeConverter 테스트")
class StringToCompanyTypeConverterTest {

    private StringToCompanyTypeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StringToCompanyTypeConverter();
    }

    @Test
    @DisplayName("null 입력 시 null 반환")
    void convert_null_returnsNull() {
        // when
        CompanyType result = converter.convert(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("빈 문자열 입력 시 null 반환")
    void convert_emptyString_returnsNull() {
        // when
        CompanyType result = converter.convert("");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("DefaultCompanyType.SYSTEM 변환 성공")
    void convert_defaultCompanyType_success() {
        // when
        CompanyType result = converter.convert("SYSTEM");

        // then
        assertThat(result).isEqualTo(DefaultCompanyType.SYSTEM);
    }

    @Test
    @DisplayName("DefaultCompanyType의 모든 값 변환 성공")
    void convert_allDefaultCompanyTypes_success() {
        for (DefaultCompanyType type : DefaultCompanyType.values()) {
            // when
            CompanyType result = converter.convert(type.name());

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
                .hasMessageContaining("Unknown CompanyType");
    }
}
