package com.lshdainty.myhr.api;

import com.lshdainty.myhr.domain.Dues;
import com.lshdainty.myhr.dto.DuesDto;
import com.lshdainty.myhr.service.DuesService;
import com.lshdainty.myhr.service.dto.DuesServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DuesApiController {
    private final DuesService duesService;

    @PostMapping("/api/v1/dues")
    public ApiResponse registDues(@RequestBody DuesDto duesDto) {
        Long duesSeq = duesService.save(
                duesDto.getDuesUserName(),
                duesDto.getDuesAmount(),
                duesDto.getDuesType(),
                duesDto.getDuesCalc(),
                duesDto.getDuesDate(),
                duesDto.getDuesDetail()
        );
        return ApiResponse.success(DuesDto.builder().duesSeq(duesSeq).build());
    }

    @GetMapping("/api/v1/dues")
    public ApiResponse yearDues(@RequestParam("year") String year) {
        List<Dues> dues = duesService.findDuesByYear(year);

        List<DuesDto> resp = dues.stream()
                .map(d -> DuesDto.builder()
                        .duesSeq(d.getSeq())
                        .duesUserName(d.getUserName())
                        .duesAmount(d.getAmount())
                        .duesType(d.getType())
                        .duesCalc(d.getCalc())
                        .duesDate(d.getDate())
                        .duesDetail(d.getDetail())
                        .build())
                .collect(Collectors.toList());

        Long total = 0L;
        for (DuesDto duesDto : resp) {
            duesDto.setTotalDues(total = duesDto.getDuesCalc().applyAsType(total, duesDto.getDuesAmount()));
        }

        return ApiResponse.success(resp);
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
        return ApiResponse.success(serviceDtos.stream()
                .map(d -> DuesDto.builder()
                        .duesUserName(d.getUserName())
                        .duesAmount(d.getAmount())
                        .month(d.getDate())
                        .duesDetail(d.getDetail())
                        .build())
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/api/v1/dues/{seq}")
    public ApiResponse deleteDues(@PathVariable("seq") Long seq) {
        duesService.deleteDues(seq);
        return ApiResponse.success();
    }
}