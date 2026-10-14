package dev.vality.wb.list.manager.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka.correction-stream")
public class WbListCorrectionStreamProperties {

    private Boolean enabled;
    private String applicationId;
    private String clientId;

}
