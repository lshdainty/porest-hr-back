package com.lshdainty.porest.common.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Frontend frontend = new Frontend();
    private Company company = new Company();
    private Email email = new Email();

    @Getter
    @Setter
    public static class Frontend {
        private String baseUrl;
    }

    @Getter
    @Setter
    public static class Company {
        private String name;
    }

    @Getter
    @Setter
    public static class Email {
        private Logo logo = new Logo();

        @Getter
        @Setter
        public static class Logo {
            private String path = "templates/email/logo.png";
        }
    }
}
