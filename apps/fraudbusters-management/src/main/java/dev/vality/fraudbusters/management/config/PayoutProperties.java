package dev.vality.fraudbusters.management.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PayoutProperties {

    @Value("${kafka.topic.fraudbusters.payout.template}")
    private String templateTopic;
    @Value("${kafka.topic.fraudbusters.payout.reference}")
    private String referenceTopic;
    @Value("${kafka.topic.fraudbusters.payout.group.list}")
    private String groupTopic;
    @Value("${kafka.topic.fraudbusters.payout.group.reference}")
    private String groupReferenceTopic;
}
