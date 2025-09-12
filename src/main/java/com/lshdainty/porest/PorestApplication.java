package com.lshdainty.porest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PorestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PorestApplication.class, args);
    }

}
