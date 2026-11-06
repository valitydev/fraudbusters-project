package dev.vality.fraudbusters.converter;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.ClientIdentity;
import dev.vality.damsel.domain.CountryCode;
import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.domain.DigitalWallet;
import dev.vality.damsel.domain.PartyConfigRef;
import dev.vality.damsel.domain.PaymentSystemRef;
import dev.vality.damsel.domain.WalletConfigRef;
import dev.vality.damsel.proxy_inspector.Party;
import dev.vality.damsel.proxy_inspector.WithdrawalContext;
import dev.vality.damsel.withdrawals.domain.ClientInfo;
import dev.vality.damsel.withdrawals.domain.ContactInfo;
import dev.vality.damsel.withdrawals.domain.Destination;
import dev.vality.damsel.withdrawals.domain.Withdrawal;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WithdrawalContextToModelConverterTest {

    private final WithdrawalContextToModelConverter converter = new WithdrawalContextToModelConverter();

    @Test
    void mapsEveryFieldThatPayoutRulesCanUse() {
        Withdrawal withdrawal = new Withdrawal()
                .setBody(new Cash(1500, new CurrencyRef("USD")))
                .setCreatedAt("2026-01-01T12:00:00Z")
                .setDestination(Destination.bank_card(new BankCard()
                        .setToken("synthetic-token")
                        .setBin("411111")
                        .setLastDigits("1111")
                        .setIssuerCountry(CountryCode.RUS)
                        .setPaymentSystem(new PaymentSystemRef("VISA"))
                        .setCategory("debit")))
                .setClientIdentity(new ClientIdentity()
                        .setCustomerId("synthetic-customer")
                        .setUserId("synthetic-user"))
                .setClientInfo(new ClientInfo()
                        .setIpAddress("192.0.2.1")
                        .setFingerprint("synthetic-fingerprint"))
                .setContactInfo(new ContactInfo()
                        .setEmail("synthetic@example.com")
                        .setPhoneNumber("+70000000000"));

        PayoutModel model = converter.convert(context(withdrawal));

        assertEquals("synthetic-withdrawal", model.getWithdrawalId());
        assertEquals("synthetic-party", model.getPartyId());
        assertEquals("synthetic-wallet", model.getWalletId());
        assertEquals(1500L, model.getAmount());
        assertEquals("USD", model.getCurrency());
        assertEquals(Instant.parse("2026-01-01T12:00:00Z").toEpochMilli(), model.getTimestamp());
        assertEquals("synthetic-customer", model.getCustomerId());
        assertEquals("synthetic-user", model.getUserId());
        assertEquals("192.0.2.1", model.getIp());
        assertEquals("synthetic-fingerprint", model.getFingerprint());
        assertEquals("synthetic@example.com", model.getEmail());
        assertEquals("+70000000000", model.getPhone());
        assertEquals("synthetic-token", model.getCardToken());
        assertEquals("411111", model.getBin());
        assertEquals("1111", model.getLastDigits());
        assertEquals("RUS", model.getBinCountryCode());
        assertEquals("VISA", model.getPaymentSystem());
        assertEquals("debit", model.getCardCategory());
    }

    @Test
    void optionalPartsStayEmptyAndMissingCreationTimeMeansNow() {
        Withdrawal withdrawal = new Withdrawal()
                .setBody(new Cash(100, new CurrencyRef("RUB")))
                .setDestination(Destination.digital_wallet(new DigitalWallet().setId("synthetic-destination")));
        long before = System.currentTimeMillis();

        PayoutModel model = converter.convert(context(withdrawal));

        assertTrue(model.getTimestamp() >= before && model.getTimestamp() <= System.currentTimeMillis());
        assertNull(model.getCustomerId());
        assertNull(model.getUserId());
        assertNull(model.getIp());
        assertNull(model.getEmail());
        assertNull(model.getCardToken());
        assertNull(model.getBin());
        assertNull(model.getBinCountryCode());
        assertNull(model.getPaymentSystem());
    }

    @Test
    void bankCardWithoutCountryOrPaymentSystemKeepsThemEmpty() {
        Withdrawal withdrawal = new Withdrawal()
                .setBody(new Cash(100, new CurrencyRef("RUB")))
                .setDestination(Destination.bank_card(new BankCard()
                        .setToken("synthetic-token")
                        .setBin("411111")
                        .setLastDigits("1111")));

        PayoutModel model = converter.convert(context(withdrawal));

        assertEquals("synthetic-token", model.getCardToken());
        assertNull(model.getBinCountryCode());
        assertNull(model.getPaymentSystem());
    }

    private WithdrawalContext context(Withdrawal withdrawal) {
        return new WithdrawalContext()
                .setParty(new Party(new PartyConfigRef("synthetic-party")))
                .setWalletRef(new WalletConfigRef("synthetic-wallet"))
                .setWithdrawalId("synthetic-withdrawal")
                .setWithdrawal(withdrawal);
    }
}
