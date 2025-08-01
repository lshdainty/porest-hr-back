package com.lshdainty.myhr.repository.dto;

import lombok.Getter;

@Getter
public class UserBirthDuesGroupMonthDto {
    private String userName;
    private String month;
    private Long amount;
    private String detail;

    public UserBirthDuesGroupMonthDto(String userName, String month, Long amount, String detail) {
        this.userName = userName;
        this.month = month;
        this.amount = amount;
        this.detail = detail;
    }
}
