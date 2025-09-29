package com.lshdainty.porest.login.service.dto;

import com.lshdainty.porest.user.domain.User;
import lombok.*;

@Getter @Setter
@RequiredArgsConstructor
public class LoginServiceDto {
    private String id;
    private String pw;
    private String name;
    private String email;
    private String provider;
    private String providerId;

    public LoginServiceDto(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.provider = "";
        this.providerId = "";
    }
}
