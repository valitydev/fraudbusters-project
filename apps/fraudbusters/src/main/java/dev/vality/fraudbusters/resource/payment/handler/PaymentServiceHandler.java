package dev.vality.fraudbusters.resource.payment.handler;

import dev.vality.damsel.fraudbusters.*;
import dev.vality.fraudbusters.fraud.ListTemplateValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceHandler implements PaymentServiceSrv.Iface {

    private final ListTemplateValidator paymentTemplatesValidator;
    private final KafkaTemplate<String, Payment> paymentKafkaTemplate;
    private final KafkaTemplate<String, Refund> refundKafkaTemplate;
    private final KafkaTemplate<String, Chargeback> chargebackKafkaTemplate;
    private final KafkaTemplate<String, FraudPayment> kafkaFraudPaymentTemplate;
    private final KafkaTemplate<String, Payout> kafkaFraudPayoutTemplate;

    @Value("${kafka.topic.event.sink.payment}")
    public String paymentEventTopic;
    @Value("${kafka.topic.event.sink.refund}")
    public String refundEventTopic;
    @Value("${kafka.topic.event.sink.chargeback}")
    public String chargebackEventTopic;
    @Value("${kafka.topic.event.sink.payout}")
    public String payoutEventTopic;
    @Value("${kafka.topic.fraud.payment}")
    private String fraudPaymentTopic;

    @Override
    public ValidateTemplateResponse validateCompilationTemplate(List<Template> list) throws TException {
        try {
            return paymentTemplatesValidator.validateCompilationTemplate(list);
        } catch (Exception e) {
            log.error("Error when validateCompilationTemplate() e: ", e);
            throw new TException("PaymentTemplateValidatorHandler error when validateCompilationTemplate() e: ", e);
        }
    }

    @Override
    public void insertFraudPayments(List<FraudPayment> payments) throws TException {
        payments.forEach(p -> kafkaFraudPaymentTemplate.send(fraudPaymentTopic, p));
    }

    @Override
    public void insertPayments(List<Payment> list) throws InsertionException {
        log.debug("InsertPayments list: {}", list);
        for (Payment payment : list) {
            send(paymentKafkaTemplate, paymentEventTopic, payment.getId(), payment);
        }
    }

    @Override
    public void insertPayouts(List<Payout> list) throws TException {
        log.debug("InsertPayouts list: {}", list);
        for (Payout payout : list) {
            send(kafkaFraudPayoutTemplate, payoutEventTopic, payout.getId(), payout);
        }
    }

    @Override
    public void insertRefunds(List<Refund> list) throws InsertionException {
        log.debug("InsertRefunds list: {}", list);
        for (Refund refund : list) {
            send(refundKafkaTemplate, refundEventTopic, refund.getId(), refund);
        }
    }

    @Override
    public void insertChargebacks(List<Chargeback> list) throws InsertionException {
        log.debug("InsertChargebacks list: {}", list);
        for (Chargeback chargeback : list) {
            send(chargebackKafkaTemplate, chargebackEventTopic, chargeback.getId(), chargeback);
        }
    }

    private <T> void send(KafkaTemplate<String, T> template, String topic, String id, T t) throws InsertionException {
        try {
            template.send(topic, id, t).get();
        } catch (Exception e) {
            log.error("Error when insert to: {} : {}", topic, t);
            throw new InsertionException()
                    .setId(id)
                    .setReason(e.getMessage());
        }
    }

}
