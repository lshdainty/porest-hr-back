package com.lshdainty.porest.common.converter;

import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.InvalidValueException;
import com.lshdainty.porest.common.type.CompanyType;
import com.lshdainty.porest.common.type.DefaultCompanyType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON 요청 Body에서 CompanyType 인터페이스로 역직렬화하는 Jackson Deserializer
 */
public class CompanyTypeJsonDeserializer extends StdDeserializer<CompanyType> {

    private static final List<Class<? extends Enum<? extends CompanyType>>> IMPLEMENTATIONS = new ArrayList<>();

    static {
        IMPLEMENTATIONS.add(DefaultCompanyType.class);
        tryRegister("com.lshdainty.porest.company.type.OriginCompanyType");
    }

    public CompanyTypeJsonDeserializer() {
        super(CompanyType.class);
    }

    @SuppressWarnings("unchecked")
    private static void tryRegister(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            IMPLEMENTATIONS.add((Class<? extends Enum<? extends CompanyType>>) clazz);
        } catch (ClassNotFoundException e) {
            // 해당 모듈이 classpath에 없으면 무시
        }
    }

    @Override
    public CompanyType deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String value = p.getText();

        if (value == null || value.isEmpty()) {
            return null;
        }

        for (Class<? extends Enum<? extends CompanyType>> implClass : IMPLEMENTATIONS) {
            for (Enum<? extends CompanyType> enumConstant : implClass.getEnumConstants()) {
                if (enumConstant.name().equals(value)) {
                    return (CompanyType) enumConstant;
                }
            }
        }

        throw new InvalidValueException(ErrorCode.UNSUPPORTED_TYPE, "Unknown CompanyType: " + value);
    }
}
