package dev.vality.trusted.tokens.utils;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.fraudbusters.ClientInfo;
import dev.vality.damsel.fraudbusters.*;
import dev.vality.testcontainers.annotations.util.ValuesGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionUtils {

    public static final String PARTY_ID = UUID.randomUUID().toString();
    public static final String SHOP_ID = UUID.randomUUID().toString();

    public static Payment createPayment() {
        return new Payment()
                .setId(ValuesGenerator.generateId())
                .setEventTime(String.valueOf(LocalDateTime.now()))
                .setReferenceInfo(ReferenceInfo.merchant_info(createMerchantInfo()))
                .setPaymentTool(PaymentTool.bank_card(createBankCard()))
                .setCost(new Cash()
                        .setCurrency(createCurrency())
                        .setAmount(1000))
                .setProviderInfo(createProviderInfo())
                .setStatus(PaymentStatus.processed)
                .setClientInfo(new ClientInfo());
    }

    public static Withdrawal createWithdrawal() {
        return new Withdrawal()
                .setId(ValuesGenerator.generateId())
                .setEventTime(String.valueOf(LocalDateTime.now()))
                .setDestinationResource(Resource.bank_card(createBankCard()))
                .setCost(new Cash()
                        .setCurrency(createCurrency())
                        .setAmount(1000))
                .setStatus(WithdrawalStatus.pending)
                .setAccount(createAccount());
    }

    public static BankCard createBankCard() {
        return new BankCard()
                .setToken("token")
                .setBin("4321")
                .setLastDigits("1234")
                .setPaymentSystem(new PaymentSystemRef("visa"));
    }

    public static Account createAccount() {
        return new Account()
                .setId("account_id")
                .setIdentity("account_identity")
                .setCurrency(createCurrency());

    }

    public static CurrencyRef createCurrency() {
        return new CurrencyRef().setSymbolicCode("RUB");
    }

    public static ProviderInfo createProviderInfo() {
        return new ProviderInfo()
                .setProviderId("provider_id")
                .setTerminalId("terminal_id");
    }

    public static MerchantInfo createMerchantInfo() {
        return new MerchantInfo()
                .setPartyId(PARTY_ID)
                .setShopId(SHOP_ID);
    }
}
