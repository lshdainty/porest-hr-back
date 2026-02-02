package com.porest.hr.common.converter;

import com.porest.core.exception.InvalidValueException;
import com.porest.hr.common.type.CompanyType;
import com.porest.hr.common.type.DefaultCompanyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyTypeJsonDeserializer 테스트")
class CompanyTypeJsonDeserializerTest {

    private CompanyTypeJsonDeserializer deserializer;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext context;

    @BeforeEach
    void setUp() {
        deserializer = new CompanyTypeJsonDeserializer();
    }

    @Test
    @DisplayName("null 입력 시 null 반환")
    void deserialize_null_returnsNull() throws Exception {
        // given
        when(jsonParser.getText()).thenReturn(null);

        // when
        CompanyType result = deserializer.deserialize(jsonParser, context);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("빈 문자열 입력 시 null 반환")
    void deserialize_emptyString_returnsNull() throws Exception {
        // given
        when(jsonParser.getText()).thenReturn("");

        // when
        CompanyType result = deserializer.deserialize(jsonParser, context);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("DefaultCompanyType.SYSTEM 역직렬화 성공")
    void deserialize_defaultCompanyType_success() throws Exception {
        // given
        when(jsonParser.getText()).thenReturn("SYSTEM");

        // when
        CompanyType result = deserializer.deserialize(jsonParser, context);

        // then
        assertThat(result).isEqualTo(DefaultCompanyType.SYSTEM);
    }

    @Test
    @DisplayName("DefaultCompanyType의 모든 값 역직렬화 성공")
    void deserialize_allDefaultCompanyTypes_success() throws Exception {
        for (DefaultCompanyType type : DefaultCompanyType.values()) {
            // given
            when(jsonParser.getText()).thenReturn(type.name());

            // when
            CompanyType result = deserializer.deserialize(jsonParser, context);

            // then
            assertThat(result).isEqualTo(type);
        }
    }

    @Test
    @DisplayName("존재하지 않는 타입 역직렬화 시 InvalidValueException 발생")
    void deserialize_unknownType_throwsException() throws Exception {
        // given
        when(jsonParser.getText()).thenReturn("UNKNOWN_TYPE");

        // when & then
        assertThatThrownBy(() -> deserializer.deserialize(jsonParser, context))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining("Unknown CompanyType");
    }
}
