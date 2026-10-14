package dev.vality.fraudbusters.notificator.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;


@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties("schedulator.register-retry")
public class SchedulatorRegisterRetryProperties {

    private Integer maxAttempts;

    private Integer backOffPeriod;

}
