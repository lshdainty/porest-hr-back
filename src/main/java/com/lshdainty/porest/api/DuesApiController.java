package com.lshdainty.porest.api;

import com.lshdainty.porest.api.dto.DuesDto;
import com.lshdainty.porest.service.DuesService;
import com.lshdainty.porest.service.dto.DuesServiceDto;
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
    public ApiResponse registDues(@RequestBody DuesDto data) {
        Long duesSeq = duesService.save(DuesServiceDto.builder()
                .userName(data.getDuesUserName())
                .amount(data.getDuesAmount())
                .type(data.getDuesType())
                .calc(data.getDuesCalc())
                .date(data.getDuesDate())
                .detail(data.getDuesDetail())
                .build()
        );
        return ApiResponse.success(DuesDto.builder().duesSeq(duesSeq).build());
    }

    @GetMapping("/api/v1/dues")
    public ApiResponse yearDues(@RequestParam("year") String year) {
        List<DuesServiceDto> dtos = duesService.findDuesByYear(year);
        return ApiResponse.success(dtos.stream()
                .map(d -> DuesDto.builder()
                        .duesSeq(d.getSeq())
                        .duesUserName(d.getUserName())
                        .duesAmount(d.getAmount())
                        .duesType(d.getType())
                        .duesCalc(d.getCalc())
                        .duesDate(d.getDate())
                        .duesDetail(d.getDetail())
                        .totalDues(d.getTotalDues())
                        .build())
                .collect(Collectors.toList()));
    }

    @GetMapping("/api/v1/dues/operation")
    public ApiResponse yearOperationDues(@RequestParam("year") String year) {
        DuesServiceDto serviceDto = duesService.findOperatingDuesByYear(year);
        return ApiResponse.success(DuesDto.builder()
                .totalDues(serviceDto.getTotalDues())
                .totalDeposit(serviceDto.getTotalDeposit())
                .totalWithdrawal(serviceDto.getTotalWithdrawal())
                .build());
    }

    @GetMapping("/api/v1/dues/birth/month")
    public ApiResponse monthBirthDues(@RequestParam("year") String year, @RequestParam("month") String month) {
        Long birthDues = duesService.findBirthDuesByYearAndMonth(year, month);
        return ApiResponse.success(DuesDto.builder()
                .birthMonthDues(birthDues)
                .build());
    }

    @GetMapping("/api/v1/dues/users/birth/month")
    public ApiResponse usersMonthBirthDues(@RequestParam("year") String year) {
        List<DuesServiceDto> serviceDtos = duesService.findUsersMonthBirthDues(year);

        Map<String, List<DuesServiceDto>> duesByUserName = serviceDtos.stream()
                .collect(Collectors.groupingBy(DuesServiceDto::getUserName, LinkedHashMap::new, Collectors.toList()));

        List<DuesDto> resp = duesByUserName.entrySet().stream()
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

                    return DuesDto.builder()
                            .duesUserName(userName)
                            .monthBirthDues(monthBirthDues)
                            .build();
                })
                .collect(Collectors.toList());

        return ApiResponse.success(resp);
    }

    @PutMapping("/api/v1/dues/{seq}")
    public ApiResponse editDues(@PathVariable("seq") Long seq, @RequestBody DuesDto data) {
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