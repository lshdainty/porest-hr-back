package com.lshdainty.porest.common.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class commonApiController {
    @GetMapping("/health")
    public ApiResponse check() {return ApiResponse.success("success");}
}
