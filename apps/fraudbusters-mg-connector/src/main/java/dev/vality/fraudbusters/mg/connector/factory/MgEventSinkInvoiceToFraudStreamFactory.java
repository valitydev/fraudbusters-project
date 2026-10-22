package dev.vality.fraudbusters.mg.connector.factory;

import dev.vality.damsel.payment_processing.EventPayload;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.fraudbusters.mg.connector.constant.StreamType;
import dev.vality.fraudbusters.mg.connector.domain.MgEventWrapper;
import dev.vality.fraudbusters.mg.connector.exception.StreamInitializationException;
import dev.vality.fraudbusters.mg.connector.mapper.impl.ChargebackPaymentMapper;
import dev.vality.fraudbusters.mg.connector.mapper.impl.PaymentMapper;
import dev.vality.fraudbusters.mg.connector.mapper.impl.RefundPaymentMapper;
import dev.vality.fraudbusters.mg.connector.parser.EventParser;
import dev.vality.fraudbusters.mg.connector.serde.ChargebackSerde;
import dev.vality.fraudbusters.mg.connector.serde.MachineEventSerde;
import dev.vality.fraudbusters.mg.connector.serde.PaymentSerde;
import dev.vality.fraudbusters.mg.connector.serde.RefundSerde;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(
        value = "fb.stream.invoiceEnabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class MgEventSinkInvoiceToFraudStreamFactory implements EventSinkFactory {

    private final Serde<MachineEvent> machineEventSerde = new MachineEventSerde();
    private final PaymentMapper paymentMapper;
    private final ChargebackPaymentMapper chargebackPaymentMapper;
    private final RefundPaymentMapper refundPaymentMapper;
    private final EventParser<EventPayload> paymentEventParser;
    private final RetryTemplate retryTemplate;
    private final PaymentSerde paymentSerde = new PaymentSerde();
    private final RefundSerde refundSerde = new RefundSerde();
    private final ChargebackSerde chargebackSerde = new ChargebackSerde();
    private final Properties mgInvoiceEventStreamProperties;
    @Value("${kafka.topic.source.invoicing}")
    private String readTopic;
    @Value("${kafka.topic.sink.refund}")
    private String refundTopic;
    @Value("${kafka.topic.sink.payment}")
    private String paymentTopic;
    @Value("${kafka.topic.sink.chargeback}")
    private String chargebackTopic;

    @Override
    public StreamType getType() {
        return StreamType.INVOICE;
    }

    @Override
    public KafkaStreams create() {
        try {
            StreamsBuilder builder = new StreamsBuilder();
            Map<String, KStream<String, MgEventWrapper>> branch =
                    builder.stream(readTopic, Consumed.with(Serdes.String(), machineEventSerde))
                            .mapValues(machineEvent -> Map
                                    .entry(machineEvent, paymentEventParser.parseEvent(machineEvent)))
                            .peek((s, payment) ->
                                    log.debug("MgEventSinkToFraudStreamFactory machineEvent: {}", payment))
                            .filter((s, entry) -> entry.getValue().isSetInvoiceChanges())
                            .peek((s, payment) ->
                                    log.debug("MgEventSinkToFraudStreamFactory machineEvent: {}", payment))
                            .flatMapValues(entry -> entry.getValue().getInvoiceChanges().stream()
                                    .map(invoiceChange -> wrapMgEvent(entry, invoiceChange))
                                    .collect(Collectors.toList()))
                            .split(Named.as("branch-"))
                            .branch((id, change) -> paymentMapper.accept(change.getChange()))
                            .branch((id, change) -> chargebackPaymentMapper.accept(change.getChange()))
                            .branch((id, change) -> refundPaymentMapper.accept(change.getChange()))
                            .defaultBranch();
            branch.get("branch-1").mapValues(mgEventWrapper ->
                            retryTemplate.execute(args ->
                                    paymentMapper.map(mgEventWrapper.getChange(), mgEventWrapper.getEvent())))
                    .to(paymentTopic, Produced.with(Serdes.String(), paymentSerde));

            branch.get("branch-2").mapValues(mgEventWrapper ->
                            retryTemplate.execute(args ->
                                    chargebackPaymentMapper.map(mgEventWrapper.getChange(), mgEventWrapper.getEvent())))
                    .to(chargebackTopic, Produced.with(Serdes.String(), chargebackSerde));

            branch.get("branch-3").mapValues(mgEventWrapper ->
                            retryTemplate.execute(args ->
                                    refundPaymentMapper.map(mgEventWrapper.getChange(), mgEventWrapper.getEvent())))
                    .to(refundTopic, Produced.with(Serdes.String(), refundSerde));

            return new KafkaStreams(builder.build(), mgInvoiceEventStreamProperties);
        } catch (Exception e) {
            log.error("WbListStreamFactory error when create stream e: ", e);
            throw new StreamInitializationException(e);
        }
    }

    private MgEventWrapper wrapMgEvent(Map.Entry<MachineEvent, EventPayload> entry, InvoiceChange invoiceChange) {
        return MgEventWrapper.builder()
                .change(invoiceChange)
                .event(entry.getKey())
                .build();
    }

}
