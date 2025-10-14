package com.lshdainty.porest.dues.controller;

import com.lshdainty.porest.dues.controller.dto.DuesApiDto;
import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.dues.service.DuesService;
import com.lshdainty.porest.dues.service.dto.DuesServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DuesApiController {
    private final DuesService duesService;

    @PostMapping("/api/v1/dues")
    public ApiResponse registDues(@RequestBody DuesApiDto.RegistDuesReq data) {
        Long duesSeq = duesService.registDues(DuesServiceDto.builder()
                .userName(data.getDuesUserName())
                .amount(data.getDuesAmount())
                .type(data.getDuesType())
                .calc(data.getDuesCalc())
                .date(data.getDuesDate())
                .detail(data.getDuesDetail())
                .build()
        );
        return ApiResponse.success(new DuesApiDto.RegistDuesResp(duesSeq));
    }

    @GetMapping("/api/v1/dues")
    public ApiResponse searchYearDues(@RequestParam("year") String year) {
        List<DuesServiceDto> dtos = duesService.searchYearDues(year);
        return ApiResponse.success(dtos.stream()
                .map(d -> new DuesApiDto.SearchYearDuesResp(
                        d.getSeq(),
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

    @GetMapping("/api/v1/dues/operation")
    public ApiResponse searchYearOperationDues(@RequestParam("year") String year) {
        DuesServiceDto serviceDto = duesService.searchYearOperationDues(year);
        return ApiResponse.success(new DuesApiDto.SearchYearOperationDuesResp(
                serviceDto.getTotalDues(),
                serviceDto.getTotalDeposit(),
                serviceDto.getTotalWithdrawal()
        ));
    }

    @GetMapping("/api/v1/dues/birth/month")
    public ApiResponse searchMonthBirthDues(@RequestParam("year") String year, @RequestParam("month") String month) {
        Long birthDues = duesService.searchMonthBirthDues(year, month);
        return ApiResponse.success(new DuesApiDto.SearchMonthBirthDuesResp(birthDues));
    }

    @GetMapping("/api/v1/dues/users/birth/month")
    public ApiResponse searchUsersMonthBirthDues(@RequestParam("year") String year) {
        List<DuesServiceDto> serviceDtos = duesService.searchUsersMonthBirthDues(year);

        Map<String, List<DuesServiceDto>> duesByUserName = serviceDtos.stream()
                .collect(Collectors.groupingBy(DuesServiceDto::getUserName, LinkedHashMap::new, Collectors.toList()));

        List<DuesApiDto.SearchUsersMonthBirthDuesResp> resp = duesByUserName.entrySet().stream()
                .map(entry -> {
                    String userName = entry.getKey();
                    List<DuesServiceDto> userDues = entry.getValue();

                    List<Long> monthBirthDues = new ArrayList<>(Collections.nCopies(12, 0L));
                    for (DuesServiceDto due : userDues) {
                        int month = Integer.parseInt(due.getDate()) - 1; // "01" -> 0
                        if (month >= 0 && month < 12) {
                            monthBirthDues.set(month, due.getAmount());
                        }
                    }

                    return new DuesApiDto.SearchUsersMonthBirthDuesResp(userName, monthBirthDues);
                })
                .collect(Collectors.toList());

        return ApiResponse.success(resp);
    }

    @PutMapping("/api/v1/dues/{seq}")
    public ApiResponse editDues(@PathVariable("seq") Long seq, @RequestBody DuesApiDto.EditDuesReq data) {
        duesService.editDues(DuesServiceDto.builder()
                .seq(seq)
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

    @DeleteMapping("/api/v1/dues/{seq}")
    public ApiResponse deleteDues(@PathVariable("seq") Long seq) {
        duesService.deleteDues(seq);
        return ApiResponse.success();
    }
}