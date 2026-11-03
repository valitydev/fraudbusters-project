package dev.vality.fraudbusters.listener.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.fraud.FraudContextParser;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.listener.TemplateCommandListener;
import dev.vality.fraudo.FraudoRulesParser;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PayoutTemplateListener extends TemplateCommandListener {

    public PayoutTemplateListener(FraudContextParser<FraudoRulesParser.ParseContext> paymentContextParser,
                                  InspectStorage payoutInspectStorage) {
        super(paymentContextParser, payoutInspectStorage);
    }

    @Override
    @KafkaListener(topics = "${kafka.topic.payout-template}",
            containerFactory = "payoutTemplateListenerContainerFactory")
    public void listen(@Payload Command command) {
        handle(command);
    }

}
