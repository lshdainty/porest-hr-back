package io.groo.ieum.common.controller;

import io.groo.core.controller.ApiResponse;
import io.groo.core.exception.EntityNotFoundException;
import io.groo.core.exception.ErrorCode;
import io.groo.core.type.CountryCode;
import io.groo.core.type.DisplayType;
import io.groo.ieum.common.controller.dto.TypesDto;
import io.groo.ieum.common.repository.CompanyCodeRepository;
import io.groo.ieum.common.repository.SystemCodeRepository;
import io.groo.ieum.holiday.type.HolidayType;
import io.groo.ieum.schedule.type.ScheduleType;
import io.groo.ieum.vacation.type.ApprovalStatus;
import io.groo.ieum.vacation.type.EffectiveType;
import io.groo.ieum.vacation.type.ExpirationType;
import io.groo.ieum.vacation.type.GrantMethod;
import io.groo.ieum.vacation.type.GrantStatus;
import io.groo.ieum.vacation.type.RepeatUnit;
import io.groo.ieum.vacation.type.VacationTimeType;
import io.groo.ieum.vacation.type.VacationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.RestController;

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
    private final CompanyCodeRepository companyCodeRepository;
    private final SystemCodeRepository systemCodeRepository;

    // 단일 타입 매핑
    private static final Map<String, Class<? extends DisplayType>> enumMap;

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
    }

    @Override
    public ApiResponse<List<TypesDto>> getEnumValues(String enumName) {
        String key = enumName.toLowerCase();
        Locale locale = LocaleContextHolder.getLocale();

        // DB 기반 타입 조회 (company-type, system-type)
        if ("company-type".equals(key)) {
            List<TypesDto> companyTypes = companyCodeRepository.findAllActive().stream()
                    .map(c -> TypesDto.builder()
                            .code(c.getCode())
                            .name(c.getName(locale))
                            .orderSeq((long) c.getSortOrder())
                            .build())
                    .toList();
            return ApiResponse.success(companyTypes);
        }

        if ("system-type".equals(key)) {
            List<TypesDto> systemTypes = systemCodeRepository.findAllActive().stream()
                    .map(s -> TypesDto.builder()
                            .code(s.getCode())
                            .name(s.getName(locale))
                            .orderSeq((long) s.getSortOrder())
                            .build())
                    .toList();
            return ApiResponse.success(systemTypes);
        }

        // 단일 enum 타입
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
