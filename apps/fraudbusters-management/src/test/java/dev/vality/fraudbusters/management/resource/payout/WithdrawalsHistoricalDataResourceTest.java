package dev.vality.fraudbusters.management.resource.payout;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.CountryCode;
import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.domain.PaymentSystemRef;
import dev.vality.damsel.fraudbusters.Accept;
import dev.vality.damsel.fraudbusters.Account;
import dev.vality.damsel.fraudbusters.CheckResult;
import dev.vality.damsel.fraudbusters.ClientInfo;
import dev.vality.damsel.fraudbusters.ConcreteCheckResult;
import dev.vality.damsel.fraudbusters.Decline;
import dev.vality.damsel.fraudbusters.Error;
import dev.vality.damsel.fraudbusters.Filter;
import dev.vality.damsel.fraudbusters.GenericPaymentTool;
import dev.vality.damsel.fraudbusters.HistoricalData;
import dev.vality.damsel.fraudbusters.HistoricalDataResponse;
import dev.vality.damsel.fraudbusters.HistoricalDataServiceSrv;
import dev.vality.damsel.fraudbusters.HistoricalWithdrawalCheck;
import dev.vality.damsel.fraudbusters.Page;
import dev.vality.damsel.fraudbusters.ProviderInfo;
import dev.vality.damsel.fraudbusters.Resource;
import dev.vality.damsel.fraudbusters.ResultStatus;
import dev.vality.damsel.fraudbusters.Sort;
import dev.vality.damsel.fraudbusters.SortOrder;
import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.damsel.fraudbusters.WithdrawalStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.autoconfigure.JooqAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {FlywayAutoConfiguration.class, JooqAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class, SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class})
class WithdrawalsHistoricalDataResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistoricalDataServiceSrv.Iface iface;

    @Test
    void searchesWithdrawalHistoryAndMapsEachDestination() throws Exception {
        when(iface.getWithdrawals(any(), any(), any())).thenReturn(new HistoricalDataResponse()
                .setContinuationId("2")
                .setData(HistoricalData.withdrawals(List.of(cardWithdrawal(), otherWithdrawal()))));

        mockMvc.perform(get("/withdrawals-historical-data/withdrawals")
                        .queryParam("from", "2034-01-01 00:00:00")
                        .queryParam("to", "2034-01-02 00:00:00")
                        .queryParam("size", "2")
                        .queryParam("partyId", "party")
                        .queryParam("walletId", "wallet")
                        .queryParam("withdrawalId", "w1")
                        .queryParam("status", "succeeded"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"continuationId":"2","result":[
                          {"id":"w1","eventTime":"2034-01-01T10:05:00","partyId":"party","walletId":"wallet",
                           "amount":1000,"currency":"RUB","status":"succeeded","destinationType":"bank_card",
                           "cardToken":"token","bin":"411111","lastDigits":"1111","paymentSystem":"VISA",
                           "issuerCountry":"RUS","accountId":"account","accountCurrency":"RUB",
                           "clientInfo":{"userId":"user","customerId":"customer","email":"user@example.com"},
                           "provider":{"providerId":"provider","terminalId":"terminal","country":"RUS"}},
                          {"id":"w2","status":"failed","destinationType":"unknown",
                           "error":{"errorCode":"insufficient_funds","errorReason":"not enough money"}}
                        ]}"""));

        ArgumentCaptor<Filter> filter = ArgumentCaptor.forClass(Filter.class);
        ArgumentCaptor<Page> page = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<Sort> sort = ArgumentCaptor.forClass(Sort.class);
        verify(iface).getWithdrawals(filter.capture(), page.capture(), sort.capture());
        assertEquals("party", filter.getValue().getPartyId());
        assertEquals("wallet", filter.getValue().getWalletId());
        assertEquals("w1", filter.getValue().getPaymentId());
        assertEquals("succeeded", filter.getValue().getStatus());
        assertFalse(filter.getValue().isSetShopId());
        assertEquals("2034-01-01 00:00:00", filter.getValue().getInterval().getLowerBound().getBoundTime());
        assertEquals(2, page.getValue().getSize());
        assertEquals(SortOrder.DESC, sort.getValue().getOrder());
    }

    @Test
    void returnsAnEmptyPageWhenNothingIsFound() throws Exception {
        when(iface.getWithdrawals(any(), any(), any())).thenReturn(new HistoricalDataResponse()
                .setData(HistoricalData.withdrawals(List.of())));

        mockMvc.perform(get("/withdrawals-historical-data/withdrawals")
                        .queryParam("from", "2034-01-01 00:00:00")
                        .queryParam("to", "2034-01-02 00:00:00")
                        .queryParam("sortOrder", "ASC"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"result\":[]}"));

        ArgumentCaptor<Sort> sort = ArgumentCaptor.forClass(Sort.class);
        verify(iface).getWithdrawals(any(), any(), sort.capture());
        assertEquals(SortOrder.ASC, sort.getValue().getOrder());
    }

    @Test
    void rejectsAnUnknownStatus() throws Exception {
        mockMvc.perform(get("/withdrawals-historical-data/withdrawals")
                        .queryParam("from", "2034-01-01 00:00:00")
                        .queryParam("to", "2034-01-02 00:00:00")
                        .queryParam("status", "captured"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(iface);
    }

    @Test
    void searchesWithdrawalInspectResultsByResultAndRule() throws Exception {
        when(iface.getWithdrawalChecks(any(), any(), any())).thenReturn(new HistoricalDataResponse()
                .setContinuationId("2")
                .setData(HistoricalData.withdrawal_checks(List.of(
                        withdrawalCheck(base("w1", WithdrawalStatus.pending)
                                        .setDestinationResource(Resource.bank_card(new BankCard()
                                                .setToken("token")
                                                .setBin("411111")
                                                .setLastDigits("1111")
                                                .setPaymentSystem(new PaymentSystemRef("VISA"))
                                                .setIssuerCountry(CountryCode.RUS)))
                                        .setClientInfo(new ClientInfo()
                                                .setUserId("user")
                                                .setEmail("user@example.com")),
                                ResultStatus.decline(new Decline()), "card_velocity"),
                        withdrawalCheck(base("w2", WithdrawalStatus.pending)
                                        .setDestinationResource(Resource.generic(new GenericPaymentTool("-"))),
                                ResultStatus.accept(new Accept()), null)))));

        mockMvc.perform(get("/withdrawals-historical-data/inspect-results")
                        .queryParam("from", "2034-01-01 00:00:00")
                        .queryParam("to", "2034-01-02 00:00:00")
                        .queryParam("size", "2")
                        .queryParam("walletId", "wallet")
                        .queryParam("status", "decline")
                        .queryParam("rule", "card_velocity")
                        .queryParam("template", "template")
                        .queryParam("userId", "user"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"continuationId":"2","result":[
                          {"withdrawalId":"w1","eventTime":"2034-01-01T10:05:00","partyId":"party",
                           "walletId":"wallet","amount":1000,"currency":"RUB","destinationType":"bank_card",
                           "cardToken":"token","bin":"411111","lastDigits":"1111","paymentSystem":"VISA",
                           "issuerCountry":"RUS","clientInfo":{"userId":"user","email":"user@example.com"},
                           "resultStatus":"decline","checkedTemplate":"template","ruleChecked":"card_velocity"},
                          {"withdrawalId":"w2","destinationType":"unknown","resultStatus":"accept"}
                        ]}"""));

        ArgumentCaptor<Filter> filter = ArgumentCaptor.forClass(Filter.class);
        ArgumentCaptor<Page> page = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<Sort> sort = ArgumentCaptor.forClass(Sort.class);
        verify(iface).getWithdrawalChecks(filter.capture(), page.capture(), sort.capture());
        assertEquals("wallet", filter.getValue().getWalletId());
        assertEquals("decline", filter.getValue().getStatus());
        assertEquals("card_velocity", filter.getValue().getRule());
        assertEquals("template", filter.getValue().getTemplate());
        assertEquals("user", filter.getValue().getUserId());
        assertFalse(filter.getValue().isSetShopId());
        assertEquals(2, page.getValue().getSize());
        assertEquals(SortOrder.DESC, sort.getValue().getOrder());
    }

    @Test
    void returnsNoWithdrawalInspectResultsWhenNothingIsFound() throws Exception {
        when(iface.getWithdrawalChecks(any(), any(), any())).thenReturn(new HistoricalDataResponse()
                .setData(HistoricalData.withdrawal_checks(List.of())));

        mockMvc.perform(get("/withdrawals-historical-data/inspect-results")
                        .queryParam("from", "2034-01-01 00:00:00")
                        .queryParam("to", "2034-01-02 00:00:00"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"result\":[]}"));
    }

    private static HistoricalWithdrawalCheck withdrawalCheck(Withdrawal withdrawal, ResultStatus resultStatus,
                                                             String rule) {
        return new HistoricalWithdrawalCheck()
                .setWithdrawal(withdrawal)
                .setCheckResult(new CheckResult()
                        .setCheckedTemplate("template")
                        .setConcreteCheckResult(new ConcreteCheckResult()
                                .setResultStatus(resultStatus)
                                .setRuleChecked(rule)));
    }

    private static Withdrawal cardWithdrawal() {
        return base("w1", WithdrawalStatus.succeeded)
                .setDestinationResource(Resource.bank_card(new BankCard()
                        .setToken("token")
                        .setBin("411111")
                        .setLastDigits("1111")
                        .setPaymentSystem(new PaymentSystemRef("VISA"))
                        .setIssuerCountry(CountryCode.RUS)))
                .setProviderInfo(new ProviderInfo()
                        .setProviderId("provider")
                        .setTerminalId("terminal")
                        .setCountry("RUS"))
                .setClientInfo(new ClientInfo()
                        .setUserId("user")
                        .setCustomerId("customer")
                        .setEmail("user@example.com"));
    }

    private static Withdrawal otherWithdrawal() {
        return base("w2", WithdrawalStatus.failed)
                .setDestinationResource(Resource.generic(new GenericPaymentTool("UNKNOWN")))
                .setError(new Error()
                        .setErrorCode("insufficient_funds")
                        .setErrorReason("not enough money"));
    }

    private static Withdrawal base(String id, WithdrawalStatus status) {
        return new Withdrawal()
                .setId(id)
                .setEventTime("2034-01-01T10:05:00Z")
                .setCost(new Cash(1000, new CurrencyRef("RUB")))
                .setStatus(status)
                .setAccount(new Account()
                        .setId("account")
                        .setCurrency(new CurrencyRef("RUB")))
                .setPartyId("party")
                .setWalletId("wallet");
    }
}
