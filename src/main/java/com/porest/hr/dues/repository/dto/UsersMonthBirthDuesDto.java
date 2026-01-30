package com.porest.hr.dues.repository.dto;

import lombok.Getter;

@Getter
public class UsersMonthBirthDuesDto {
    private String userName;
    private Integer month;
    private Long amount;
    private String detail;

    public UsersMonthBirthDuesDto(String userName, Integer month, Long amount, String detail) {
        this.userName = userName;
        this.month = month;
        this.amount = amount;
        this.detail = detail;
    }
}
