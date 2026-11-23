package dev.vality.fraudbusters;

import dev.vality.damsel.base.InvalidRequest;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.domain.PartyConfigRef;
import dev.vality.damsel.domain.RiskScore;
import dev.vality.damsel.domain.WalletConfigRef;
import dev.vality.damsel.fraudbusters.Account;
import dev.vality.damsel.fraudbusters.Resource;
import dev.vality.damsel.fraudbusters.TemplateReference;
import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.damsel.fraudbusters.PayoutStatus;
import dev.vality.damsel.proxy_inspector.Context;
import dev.vality.damsel.proxy_inspector.InspectorProxySrv;
import dev.vality.damsel.proxy_inspector.Party;
import dev.vality.damsel.proxy_inspector.WithdrawalContext;
import dev.vality.damsel.withdrawals.domain.Destination;
import dev.vality.fraudbusters.config.MockExternalServices;
import dev.vality.fraudbusters.config.TestClickhouseConfig;
import dev.vality.fraudbusters.config.properties.KafkaTopics;
import dev.vality.fraudbusters.constant.PaymentField;
import dev.vality.fraudbusters.constant.SortOrder;
import dev.vality.fraudbusters.domain.PayoutResult;
import dev.vality.fraudbusters.extension.ClickHouseContainerExtension;
import dev.vality.fraudbusters.repository.clickhouse.impl.PayoutCheckRepository;
import dev.vality.fraudbusters.repository.clickhouse.impl.PayoutRepositoryImpl;
import dev.vality.fraudbusters.service.dto.FieldType;
import dev.vality.fraudbusters.service.dto.FilterDto;
import dev.vality.fraudbusters.service.dto.SearchFieldDto;
import dev.vality.fraudbusters.service.dto.SortDto;
import dev.vality.fraudbusters.util.BeanUtil;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.testcontainers.annotations.KafkaSpringBootTest;
import dev.vality.testcontainers.annotations.kafka.KafkaTestcontainer;
import dev.vality.testcontainers.annotations.kafka.config.KafkaProducer;
import dev.vality.woody.thrift.impl.http.THClientBuilder;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static dev.vality.fraudbusters.factory.TestObjectsFactory.createCommandTemplate;
import static dev.vality.fraudbusters.util.BeanUtil.crateCommandTemplateReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Payout inspection through the running service: payout templates arrive on their own Kafka topics, payout history
 * is read from ClickHouse, and payment rules can read the same payout history.
 */
@ActiveProfiles("full-prod")
@KafkaSpringBootTest
@KafkaTestcontainer(
        topicsKeys = {
                "kafka.topic.template",
                "kafka.topic.reference",
                "kafka.topic.payout-template",
                "kafka.topic.payout-reference",
                "kafka.topic.payout-group-list",
                "kafka.topic.payout-group-reference",
                "kafka.topic.payout-result"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {FraudBustersApplication.class, TestClickhouseConfig.class},
        properties = {
                "kafka.listen.result.concurrency=1",
                "spring.main.allow-bean-definition-overriding=true"
        })
@ExtendWith({ClickHouseContainerExtension.class})
@MockExternalServices
class PayoutEndToEndIntegrationTest {

    private static final String RUN = UUID.randomUUID().toString();
    private static final String PARTY = "payout-party-" + RUN;
    private static final String OTHER_PARTY = "payout-other-party-" + RUN;
    private static final String WALLET = "payout-wallet-" + RUN;
    private static final String CARD = "payout-card-" + RUN;
    private static final String OTHER_CARD = "payout-other-card-" + RUN;
    /** Payment and payout templates share the ID on purpose: their pools must stay apart. */
    private static final String SHARED_TEMPLATE_ID = "payout-shared-" + RUN;
    private static final String WALLET_TEMPLATE_ID = "payout-wallet-template-" + RUN;
    private static final long AWAIT_MILLIS = 60_000;

    @Autowired
    private KafkaProducer<TBase<?, ?>> testThriftKafkaProducer;
    @Autowired
    private KafkaTopics kafkaTopics;
    @Autowired
    private PayoutRepositoryImpl payoutRepository;
    @Autowired
    private PayoutCheckRepository payoutCheckRepository;

    @LocalServerPort
    private int serverPort;

    @Test
    void payoutAndPaymentRulesWorkTogetherThroughTheService() throws Exception {
        InspectorProxySrv.Iface client = new THClientBuilder()
                .withAddress(new URI(String.format("http://localhost:%s/fraud_inspector/v1", serverPort)))
                .withNetworkTimeout(300_000)
                .build(InspectorProxySrv.Iface.class);

        assertEquals(RiskScore.high, client.inspectWithdrawal(payout(PARTY, WALLET, CARD, 100)));

        publishPayoutTemplate(SHARED_TEMPLATE_ID, """
                rule:card_velocity: countPayoutsSuccess("card_token", 24, hours, "party_id") >= 2 -> decline;
                """);
        publishPayoutReference(new TemplateReference()
                .setTemplateId(SHARED_TEMPLATE_ID)
                .setIsGlobal(true));
        publishPayoutTemplate(WALLET_TEMPLATE_ID, """
                rule:big: amount() >= 1000 -> highRisk;
                rule:rest: 1 = 1 -> accept;
                """);
        publishPayoutReference(new TemplateReference()
                .setTemplateId(WALLET_TEMPLATE_ID)
                .setPartyId(PARTY)
                .setWalletId(WALLET));
        publishPaymentTemplate(SHARED_TEMPLATE_ID, """
                rule:payout_velocity: countPayouts("card_token", 24, hours) >= 2 -> decline;
                """);

        awaitRisk(RiskScore.low, () -> client.inspectWithdrawal(payout(PARTY, WALLET, CARD, 100)));
        assertEquals(RiskScore.high, client.inspectWithdrawal(payout(PARTY, WALLET, CARD, 5000)));
        assertEquals(RiskScore.high, client.inspectWithdrawal(payout(PARTY, "other-" + WALLET, CARD, 100)));

        Instant now = Instant.now();
        payoutRepository.insertBatch(List.of(
                history("succeeded-1", now.minusSeconds(60), PayoutStatus.succeeded),
                history("succeeded-2", now.minusSeconds(30), PayoutStatus.succeeded),
                history("failed", now.minusSeconds(20), PayoutStatus.failed)));

        assertEquals(RiskScore.fatal, client.inspectWithdrawal(payout(PARTY, WALLET, CARD, 100)));
        assertEquals(RiskScore.low, client.inspectWithdrawal(payout(PARTY, WALLET, OTHER_CARD, 100)));
        assertEquals(RiskScore.high, client.inspectWithdrawal(payout(OTHER_PARTY, WALLET, CARD, 100)));

        awaitRisk(RiskScore.fatal, () -> client.inspectPayment(payment(CARD)));
        assertEquals(RiskScore.high, client.inspectPayment(payment(OTHER_CARD)));

        InvalidRequest invalid = assertThrows(InvalidRequest.class,
                () -> client.inspectWithdrawal(payout(PARTY, "", CARD, 100)));
        assertFalse(invalid.getErrors().isEmpty());

        awaitPayoutCheck(ResultStatus.DECLINE, "card_velocity", SHARED_TEMPLATE_ID);
        awaitPayoutCheck(ResultStatus.HIGH_RISK, "big", WALLET_TEMPLATE_ID);
    }

    private void awaitPayoutCheck(ResultStatus status, String rule, String template) throws Exception {
        long deadline = System.currentTimeMillis() + AWAIT_MILLIS;
        boolean stored = isPayoutCheckStored(status, rule, template);
        while (!stored && System.currentTimeMillis() < deadline) {
            Thread.sleep(250);
            stored = isPayoutCheckStored(status, rule, template);
        }
        assertTrue(stored, "payout check " + status + " " + rule + " is not stored");
    }

    private boolean isPayoutCheckStored(ResultStatus status, String rule, String template) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        FilterDto filter = new FilterDto();
        filter.setTimeFrom(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(now.minusDays(1)));
        filter.setTimeTo(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(now.plusDays(1)));
        filter.setSize(100L);
        SortDto sort = new SortDto();
        sort.setOrder(SortOrder.DESC);
        filter.setSort(sort);
        filter.setSearchFields(Set.of(SearchFieldDto.builder()
                .field(PaymentField.PARTY_ID)
                .type(FieldType.STRING)
                .value(PARTY)
                .build()));
        List<PayoutResult> results = payoutCheckRepository.getByFilter(filter);
        return results.stream()
                .map(PayoutResult::getResultModel)
                .anyMatch(result -> result.getResultModel().getResultStatus() == status
                        && rule.equals(result.getResultModel().getRuleChecked())
                        && template.equals(result.getCheckedTemplate()));
    }

    private void publishPayoutTemplate(String id, String template) throws Exception {
        testThriftKafkaProducer.send(kafkaTopics.getPayoutTemplate(), id, createCommandTemplate(id, template));
    }

    private void publishPayoutReference(TemplateReference reference) throws Exception {
        testThriftKafkaProducer.send(kafkaTopics.getPayoutReference(), reference.getTemplateId(),
                crateCommandTemplateReference(reference));
    }

    private void publishPaymentTemplate(String id, String template) throws Exception {
        testThriftKafkaProducer.send(kafkaTopics.getTemplate(), id, createCommandTemplate(id, template));
        TemplateReference global = new TemplateReference()
                .setTemplateId(id)
                .setIsGlobal(true);
        testThriftKafkaProducer.send(kafkaTopics.getReference(), id, crateCommandTemplateReference(global));
    }

    private static void awaitRisk(RiskScore expected, ThriftCall call) throws Exception {
        long deadline = System.currentTimeMillis() + AWAIT_MILLIS;
        RiskScore actual = call.get();
        while (actual != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(250);
            actual = call.get();
        }
        assertEquals(expected, actual);
    }

    private static WithdrawalContext payout(String party, String wallet, String card, long amount) {
        return new WithdrawalContext()
                .setParty(new Party(new PartyConfigRef(party)))
                .setWalletRef(new WalletConfigRef(wallet))
                .setWithdrawalId(UUID.randomUUID().toString())
                .setWithdrawal(new dev.vality.damsel.withdrawals.domain.Withdrawal()
                        .setBody(new Cash(amount, new CurrencyRef("RUB")))
                        .setCreatedAt(Instant.now().toString())
                        .setDestination(Destination.bank_card(new BankCard(card, "400000", "0000"))));
    }

    private static Payout history(String id, Instant eventTime, PayoutStatus status) {
        return new Payout()
                .setId(id + "-" + RUN)
                .setEventTime(eventTime.toString())
                .setCost(new Cash(100, new CurrencyRef("RUB")))
                .setStatus(status)
                .setAccount(new Account()
                        .setId("payout-account-" + RUN)
                        .setCurrency(new CurrencyRef("RUB")))
                .setDestinationResource(Resource.bank_card(new BankCard(CARD, "400000", "0000")))
                .setPartyId(PARTY)
                .setWalletId(WALLET);
    }

    private static Context payment(String card) {
        Context context = BeanUtil.createContext("payout-payment-party-" + RUN);
        context.getPayment()
                .getPayment()
                .getPayer()
                .getPaymentResource()
                .getResource()
                .getPaymentTool()
                .getBankCard()
                .setToken(card);
        return context;
    }

    @FunctionalInterface
    private interface ThriftCall extends Supplier<RiskScore> {

        RiskScore call() throws TException;

        @Override
        default RiskScore get() {
            try {
                return call();
            } catch (TException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
