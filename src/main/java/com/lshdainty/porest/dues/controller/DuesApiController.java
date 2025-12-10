package com.lshdainty.porest.dues.controller;

import com.lshdainty.porest.dues.controller.dto.DuesApiDto;
import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.dues.service.DuesService;
import com.lshdainty.porest.dues.service.dto.DuesServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DuesApiController implements DuesApi {
    private final DuesService duesService;

    @Override
    @PreAuthorize("hasAuthority('DUES_MANAGE')")
    public ApiResponse registDues(DuesApiDto.RegistDuesReq data) {
        Long duesId = duesService.registDues(DuesServiceDto.builder()
                .userName(data.getDuesUserName())
                .amount(data.getDuesAmount())
                .type(data.getDuesType())
                .calc(data.getDuesCalc())
                .date(data.getDuesDate())
                .detail(data.getDuesDetail())
                .build()
        );
        return ApiResponse.success(new DuesApiDto.RegistDuesResp(duesId));
    }

    @Override
    @PreAuthorize("hasAuthority('DUES_READ')")
    public ApiResponse searchYearDues(Integer year) {
        List<DuesServiceDto> dtos = duesService.searchYearDues(year);
        return ApiResponse.success(dtos.stream()
                .map(d -> new DuesApiDto.SearchYearDuesResp(
                        d.getId(),
                        d.getUserName(),
                        d.getAmount(),
                        d.getType(),
                        d.getCalc(),
                        d.getDate(),
                        d.getDetail(),
                        d.getTotalDues()
                ))
                .collect(Collectors.toList()));
    }

    @Override
    @PreAuthorize("hasAuthority('DUES_READ')")
    public ApiResponse searchYearOperationDues(Integer year) {
        DuesServiceDto serviceDto = duesService.searchYearOperationDues(year);
        return ApiResponse.success(new DuesApiDto.SearchYearOperationDuesResp(
                serviceDto.getTotalDues(),
                serviceDto.getTotalDeposit(),
                serviceDto.getTotalWithdrawal()
        ));
    }

    @Override
    @PreAuthorize("hasAuthority('DUES_READ')")
    public ApiResponse searchMonthBirthDues(Integer year, Integer month) {
        Long birthDues = duesService.searchMonthBirthDues(year, month);
        return ApiResponse.success(new DuesApiDto.SearchMonthBirthDuesResp(birthDues));
    }

    @Override
    @PreAuthorize("hasAuthority('DUES_READ')")
    public ApiResponse searchUsersMonthBirthDues(Integer year) {
        List<DuesServiceDto> serviceDtos = duesService.searchUsersMonthBirthDues(year);

        Map<String, List<DuesServiceDto>> duesByUserName = serviceDtos.stream()
                .collect(Collectors.groupingBy(DuesServiceDto::getUserName, LinkedHashMap::new, Collectors.toList()));

        List<DuesApiDto.SearchUsersMonthBirthDuesResp> resp = duesByUserName.entrySet().stream()
                .map(entry -> {
                    String userName = entry.getKey();
                    List<DuesServiceDto> userDues = entry.getValue();

                    List<Long> monthBirthDues = new ArrayList<>(Collections.nCopies(12, 0L));
                    for (DuesServiceDto due : userDues) {
                        int monthIndex = due.getMonth() - 1; // 1 -> 0
                        if (monthIndex >= 0 && monthIndex < 12) {
                            monthBirthDues.set(monthIndex, due.getAmount());
                        }
                    }

                    return new DuesApiDto.SearchUsersMonthBirthDuesResp(userName, monthBirthDues);
                })
                .collect(Collectors.toList());

        return ApiResponse.success(resp);
    }

    @Override
    @PreAuthorize("hasAuthority('DUES_MANAGE')")
    public ApiResponse editDues(Long id, DuesApiDto.EditDuesReq data) {
        duesService.editDues(DuesServiceDto.builder()
                .id(id)
                .userName(data.getDuesUserName())
                .amount(data.getDuesAmount())
                .type(data.getDuesType())
                .calc(data.getDuesCalc())
                .date(data.getDuesDate())
                .detail(data.getDuesDetail())
                .build()
        );
        return ApiResponse.success();
    }

    @Override
    @PreAuthorize("hasAuthority('DUES_MANAGE')")
    public ApiResponse deleteDues(Long id) {
        duesService.deleteDues(id);
        return ApiResponse.success();
    }
}