package dev.vality.fraudbusters.mg.connector;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.domain_config_v2.RepositoryClientSrv;
import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.damsel.payment_processing.Invoice;
import dev.vality.damsel.payment_processing.InvoicingSrv;
import dev.vality.fistful.withdrawal.ManagementSrv;
import dev.vality.fistful.withdrawal.WithdrawalState;
import dev.vality.fraudbusters.mg.connector.factory.EventRangeFactory;
import dev.vality.fraudbusters.mg.connector.mapper.impl.WithdrawalBeanUtils;
import dev.vality.fraudbusters.mg.connector.serde.deserializer.ChargebackDeserializer;
import dev.vality.fraudbusters.mg.connector.serde.deserializer.PaymentDeserializer;
import dev.vality.fraudbusters.mg.connector.serde.deserializer.RefundDeserializer;
import dev.vality.fraudbusters.mg.connector.serde.deserializer.WithdrawalDeserializer;
import dev.vality.fraudbusters.mg.connector.utils.BuildUtils;
import dev.vality.fraudbusters.mg.connector.utils.MgEventSinkFlowGenerator;
import dev.vality.fraudbusters.mg.connector.utils.WithdrawalFlowGenerator;
import dev.vality.machinegun.eventsink.SinkEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = FraudbustersMgConnectorApplication.class,
        properties = {
                "stream.withdrawal.debug=false",
                "spring.kafka.streams.properties.retries=1",
                "spring.kafka.streams.properties.retry.backoff.ms=100",
                "spring.kafka.streams.properties.fixed.rate.timeout.ms=100"
        })
public class FraudbustersMgConnectorApplicationTest extends KafkaAbstractTest {

    public static final String SOURCE_ID = "source_id";
    public static final long TIMEOUT = 20000L;

    @MockitoBean
    InvoicingSrv.Iface invoicingClient;
    @MockitoBean
    ManagementSrv.Iface fistfulClient;
    @MockitoBean
    dev.vality.fistful.destination.ManagementSrv.Iface destinationClient;
    @MockitoBean
    RepositoryClientSrv.Iface dominantClient;

    @Autowired
    private EventRangeFactory eventRangeFactory;

    @Test
    public void contextLoads() throws TException, IOException, InterruptedException {
        List<SinkEvent> sinkEvents = MgEventSinkFlowGenerator.generateSuccessFlow(SOURCE_ID);
        mockPayment(SOURCE_ID);
        sinkEvents.forEach(sinkEvent -> produceMessageToEventSink(MG_EVENT, sinkEvent));
        checkMessageInTopic(PAYMENT, PaymentDeserializer.class, 5);

        String sourceIdRefund2 = "sourceIdRefund2";
        mockPayment(sourceIdRefund2);
        mockPayment(sourceIdRefund2, 6);
        mockRefund(sourceIdRefund2, 7, "1");
        mockRefund(sourceIdRefund2, 8, "1");
        mockRefund(sourceIdRefund2, 9, "2");
        mockRefund(sourceIdRefund2, 10, "2");
        sinkEvents = MgEventSinkFlowGenerator.generateRefundedFlow(sourceIdRefund2);
        sinkEvents.forEach(sinkEvent -> produceMessageToEventSink(MG_EVENT, sinkEvent));

        checkMessageInTopic(REFUND, RefundDeserializer.class, 2);

        String sourceChargeback = "source_chargeback";
        sinkEvents = MgEventSinkFlowGenerator.generateChargebackFlow(sourceChargeback);
        mockPayment(sourceChargeback);
        mockChargeback(sourceChargeback, 2, "1");
        mockChargeback(sourceChargeback, 3, "1");
        mockChargeback(sourceChargeback, 6, "1");
        sinkEvents.forEach(sinkEvent -> produceMessageToEventSink(MG_EVENT, sinkEvent));

        checkMessageInTopic(CHARGEBACK, ChargebackDeserializer.class, 1);

        //check exceptions retry
        sinkEvents = MgEventSinkFlowGenerator.generateSuccessFlow(SOURCE_ID);
        mockPaymentWithException(SOURCE_ID);
        sinkEvents.forEach(sinkEvent -> produceMessageToEventSink(MG_EVENT, sinkEvent));
        checkMessageInTopic(PAYMENT, PaymentDeserializer.class, 10);
    }

    @Test
    public void withdrawalStreamTest() throws TException, InterruptedException {
        when(fistfulClient.get(any(), any())).thenReturn(new WithdrawalState()
                .setBody(WithdrawalBeanUtils.createCash()));
        when(destinationClient.get(any(), any())).thenReturn(WithdrawalBeanUtils.createDestinationState());
        DomainObject domainObject = new DomainObject();
        domainObject.setWalletConfig(WithdrawalBeanUtils.createWalletConfigObject());
        when(dominantClient.checkoutObject(any(), any()))
                .thenReturn(WithdrawalBeanUtils.createVersionedObject(domainObject));

        List<SinkEvent> sinkEvents = WithdrawalFlowGenerator.generateSuccessFlow(SOURCE_ID);
        sinkEvents.forEach(sinkEvent -> produceMessageToEventSink(MG_WITHDRAWAL, sinkEvent));

        checkMessageInTopic(WITHDRAWAL, WithdrawalDeserializer.class, 2);
    }

    private void checkMessageInTopic(String topicName, Class<?> clazz, int size) throws InterruptedException {
        Thread.sleep(TIMEOUT);

        Consumer<String, Payment> consumer = createPaymentConsumer(clazz);
        try {
            consumer.subscribe(Collections.singletonList(topicName));
            ConsumerRecords<String, Payment> poll = consumer.poll(Duration.ofSeconds(10));
            Assertions.assertTrue(poll.iterator().hasNext());
            Assertions.assertEquals(size, poll.count());
            log.info("message: {}", poll.iterator().next().value());
        } catch (Exception e) {
            log.error("KafkaAbstractTest initialize e: ", e);
        }
        consumer.close();
    }

    private void mockPayment(String sourceId) throws TException, IOException {
        mockPayment(sourceId, 1);
        mockPayment(sourceId, 2);
        mockPayment(sourceId, 3);
        mockPayment(sourceId, 4);
        mockPayment(sourceId, 5);
        mockPayment(sourceId, 6);
    }

    private OngoingStubbing<Invoice> mockPayment(String sourceId, int i) throws TException, IOException {
        return when(invoicingClient.get(sourceId, eventRangeFactory.create(i)))
                .thenReturn(
                        BuildUtils.buildInvoice(i, MgEventSinkFlowGenerator.PARTY_ID, MgEventSinkFlowGenerator.SHOP_ID,
                                sourceId, "1", "1", "1",
                                InvoiceStatus.paid(new InvoicePaid()),
                                InvoicePaymentStatus.processed(new InvoicePaymentProcessed())));
    }

    private void mockPaymentWithException(String sourceId) throws TException, IOException {
        int eventNumber = 6;
        when(invoicingClient.get(sourceId, eventRangeFactory.create(eventNumber)))
                .thenThrow(new RuntimeException())
                .thenReturn(BuildUtils.buildInvoice(eventNumber, MgEventSinkFlowGenerator.PARTY_ID,
                        MgEventSinkFlowGenerator.SHOP_ID,
                        sourceId, "1", "1", "1",
                        InvoiceStatus.paid(new InvoicePaid()),
                        InvoicePaymentStatus.processed(new InvoicePaymentProcessed())));
    }

    private void mockRefund(String sourceId, int sequenceId, String refundId) throws TException, IOException {
        when(invoicingClient.get(sourceId, eventRangeFactory.create(sequenceId)))
                .thenReturn(BuildUtils.buildInvoice(sequenceId, MgEventSinkFlowGenerator.PARTY_ID,
                        MgEventSinkFlowGenerator.SHOP_ID,
                        sourceId, "1", refundId, "1",
                        InvoiceStatus.paid(new InvoicePaid()),
                        InvoicePaymentStatus.refunded(new InvoicePaymentRefunded())));
    }

    private void mockChargeback(String sourceId, int sequenceId, String chargebackId) throws TException, IOException {
        when(invoicingClient.get(sourceId, eventRangeFactory.create(sequenceId)))
                .thenReturn(BuildUtils.buildInvoice(sequenceId, MgEventSinkFlowGenerator.PARTY_ID,
                        MgEventSinkFlowGenerator.SHOP_ID,
                        sourceId, "1", "1", chargebackId,
                        InvoiceStatus.paid(new InvoicePaid()),
                        InvoicePaymentStatus.charged_back(new InvoicePaymentChargedBack())));
    }

}
