package com.lshdainty.porest.common.converter;

import com.lshdainty.porest.common.exception.InvalidValueException;
import com.lshdainty.porest.common.type.CompanyType;
import com.lshdainty.porest.common.type.DefaultCompanyType;
import com.lshdainty.porest.company.type.OriginCompanyType;
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
    @DisplayName("OriginCompanyType 역직렬화 성공")
    void deserialize_originCompanyType_success() throws Exception {
        // given
        when(jsonParser.getText()).thenReturn("SKAX");

        // when
        CompanyType result = deserializer.deserialize(jsonParser, context);

        // then
        assertThat(result).isEqualTo(OriginCompanyType.SKAX);
    }

    @Test
    @DisplayName("OriginCompanyType의 모든 값 역직렬화 성공")
    void deserialize_allOriginCompanyTypes_success() throws Exception {
        for (OriginCompanyType type : OriginCompanyType.values()) {
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
