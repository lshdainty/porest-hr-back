package com.porest.hr.common.controller;

import com.porest.core.controller.ApiResponse;
import com.porest.core.exception.EntityNotFoundException;
import com.porest.core.exception.ErrorCode;
import com.porest.core.type.CountryCode;
import com.porest.core.type.DisplayType;
import com.porest.hr.common.controller.dto.TypesDto;
import com.porest.hr.common.type.DefaultCompanyType;
import com.porest.hr.common.type.DefaultSystemType;
import com.porest.hr.holiday.type.HolidayType;
import com.porest.hr.schedule.type.ScheduleType;
import com.porest.hr.vacation.type.ApprovalStatus;
import com.porest.hr.vacation.type.EffectiveType;
import com.porest.hr.vacation.type.ExpirationType;
import com.porest.hr.vacation.type.GrantMethod;
import com.porest.hr.vacation.type.GrantStatus;
import com.porest.hr.vacation.type.RepeatUnit;
import com.porest.hr.vacation.type.VacationTimeType;
import com.porest.hr.vacation.type.VacationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TypesApiController implements TypesApi {
    private final MessageSource messageSource;

    // 단일 타입 매핑
    private static final Map<String, Class<? extends DisplayType>> enumMap;
    // 복합 타입 매핑 (Default + Origin 합침)
    private static final Map<String, List<Class<? extends DisplayType>>> compositeEnumMap;

    static {
        enumMap = new HashMap<>();
        enumMap.put("grant-method", GrantMethod.class);
        enumMap.put("repeat-unit", RepeatUnit.class);
        enumMap.put("vacation-time", VacationTimeType.class);
        enumMap.put("vacation-type", VacationType.class);
        enumMap.put("effective-type", EffectiveType.class);
        enumMap.put("expiration-type", ExpirationType.class);
        enumMap.put("approval-status", ApprovalStatus.class);
        enumMap.put("grant-status", GrantStatus.class);
        enumMap.put("schedule-type", ScheduleType.class);
        enumMap.put("holiday-type", HolidayType.class);
        enumMap.put("country-code", CountryCode.class);

        // company-type, system-type은 복합 타입으로 관리 (Default + Origin 합침)
        compositeEnumMap = new HashMap<>();

        // company-type: DefaultCompanyType + OriginCompanyType (있으면)
        List<Class<? extends DisplayType>> companyTypes = new ArrayList<>();
        companyTypes.add(DefaultCompanyType.class);
        tryAddOriginType(companyTypes, "com.lshdainty.porest.company.type.OriginCompanyType");
        compositeEnumMap.put("company-type", companyTypes);

        // system-type: DefaultSystemType + OriginSystemType (있으면)
        List<Class<? extends DisplayType>> systemTypes = new ArrayList<>();
        systemTypes.add(DefaultSystemType.class);
        tryAddOriginType(systemTypes, "com.porest.hr.work.type.OriginSystemType");
        compositeEnumMap.put("system-type", systemTypes);
    }

    @SuppressWarnings("unchecked")
    private static void tryAddOriginType(List<Class<? extends DisplayType>> list, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            list.add((Class<? extends DisplayType>) clazz);
        } catch (ClassNotFoundException e) {
            // 해당 모듈이 classpath에 없으면 무시
        }
    }

    @Override
    public ApiResponse<List<TypesDto>> getEnumValues(String enumName) {
        String key = enumName.toLowerCase();
        Locale locale = LocaleContextHolder.getLocale();

        // 복합 타입인지 확인 (company-type, system-type)
        if (compositeEnumMap.containsKey(key)) {
            List<TypesDto> enumValues = new ArrayList<>();

            for (Class<? extends DisplayType> enumClass : compositeEnumMap.get(key)) {
                Arrays.stream(enumClass.getEnumConstants())
                        .map(enumConstant -> TypesDto.builder()
                                .code(((Enum<?>) enumConstant).name())
                                .name(messageSource.getMessage(
                                        ((DisplayType) enumConstant).getMessageKey(),
                                        null,
                                        locale
                                ))
                                .orderSeq(((DisplayType) enumConstant).getOrderSeq())
                                .build()
                        )
                        .forEach(enumValues::add);
            }

            // orderSeq로 정렬
            enumValues.sort((a, b) -> Long.compare(a.getOrderSeq(), b.getOrderSeq()));

            return ApiResponse.success(enumValues);
        }

        // 단일 타입
        Class<? extends DisplayType> enumClass = enumMap.get(key);

        if (enumClass == null) {
            throw new EntityNotFoundException(ErrorCode.UNSUPPORTED_TYPE);
        }

        List<TypesDto> enumValues = Arrays.stream(enumClass.getEnumConstants())
                .map(enumConstant -> TypesDto.builder()
                        .code(((Enum<?>) enumConstant).name())
                        .name(messageSource.getMessage(
                                ((DisplayType) enumConstant).getMessageKey(),
                                null,
                                locale
                        ))
                        .orderSeq(((DisplayType) enumConstant).getOrderSeq())
                        .build()
                )
                .toList();

        return ApiResponse.success(enumValues);
    }
}
