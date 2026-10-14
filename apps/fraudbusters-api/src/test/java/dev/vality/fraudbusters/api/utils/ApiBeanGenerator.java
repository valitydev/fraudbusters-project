package dev.vality.fraudbusters.api.utils;

import dev.vality.swag.fraudbusters.model.*;
import dev.vality.swag.fraudbusters.model.Error;

import java.time.OffsetDateTime;
import java.util.UUID;

import static dev.vality.fraudbusters.api.constants.PaymentResourceTypeConstants.BANK_CARD;

public class ApiBeanGenerator {

    public static final String RUS = "RUS";
    public static final String TERMINAL_ID = "12321321313";
    public static final String BIN = "4242424242424242";
    public static final String LAST_DIGITS = "1234";
    public static final String VISA = "visa";
    public static final String IP = "192.168.0.1";
    public static final String PHONE = "+79651112233";
    public static final String ACCOUNT_ID = "123123123123";
    public static final String CHARGE_CODE = "CHARGE_CODE";
    public static final String ID = "ID";
    public static final String PAYMENT_ID = "PAYMENT_ID";
    public static final String COMMENT = "COMMENT";
    public static final String TYPE = "TYPE";
    public static final String EMAIL = "EMAIL";
    public static final String FINGERPRINT = "FINGERPRINT";
    public static final long AMOUNT = 1000L;
    public static final String CURRENCY = "RUB";
    public static final String BANK_NAME = "BANK_NAME";
    public static final String CARD_TOKEN = "CARD_TOKEN";
    public static final String PROVIDER_ID = "PROVIDER_ID";
    public static final String PARTY_ID = "PARTY_ID";
    public static final String SHOP_ID = "SHOP_ID";
    public static final String ERROR_CODE = "ERROR_CODE";
    public static final String ERROR_REASON = "ERROR_REASON";
    public static final String DESCRIPTION = "DESCRIPTION";

    public static Chargeback initChargeback() {
        return new Chargeback()
                .eventTime(OffsetDateTime.now())
                .id(ID)
                .status(ChargebackStatus.ACCEPTED)
                .payerType(PayerType.CUSTOMER)
                .category(ChargebackCategory.FRAUD)
                .merchant(initMerchant())
                .chargebackCode(CHARGE_CODE)
                .paymentId(PAYMENT_ID)
                .provider(initProvider())
                .paymentResource(initBankCard())
                .cash(initCash())
                .customer(initCustomer());
    }

    public static Refund initRefund() {
        return new Refund()
                .eventTime(OffsetDateTime.now())
                .id(ID)
                .status(RefundStatus.SUCCEEDED)
                .payerType(PayerType.CUSTOMER)
                .merchant(initMerchant())
                .paymentId(PAYMENT_ID)
                .provider(initProvider())
                .paymentResource(initBankCard())
                .cash(initCash())
                .customer(initCustomer());
    }

    public static Withdrawal initWithdrawal() {
        return new Withdrawal()
                .eventTime(OffsetDateTime.now())
                .id(ID)
                .status(WithdrawalStatus.SUCCEEDED)
                .provider(initProvider())
                .paymentResource(initBankCard())
                .cash(initCash())
                .account(new Account()
                        .id(ACCOUNT_ID)
                        .currency(CURRENCY));
    }

    public static PaymentChange initPaymentChange() {
        return new PaymentChange()
                .eventTime(OffsetDateTime.now())
                .paymentStatus(PaymentStatus.CAPTURED)
                .payment(initPayment());
    }

    public static Payment initPayment() {
        return new Payment()
                .id(ID)
                .payerType(PayerType.RECURRENT)
                .merchant(initMerchant())
                .provider(initProvider())
                .paymentResource(initBankCard())
                .cash(initCash())
                .customer(initCustomer())
                .description(DESCRIPTION)
                .createdAt(OffsetDateTime.now());
    }

    public static FraudPayment initFraudPayment() {
        return new FraudPayment()
                .eventTime(OffsetDateTime.now())
                .paymentId(PAYMENT_ID)
                .comment(COMMENT)
                .type(TYPE);
    }

    public static Customer initCustomer() {
        return new Customer()
                .name(randomString())
                .device(initDevice())
                .contact(initContact());
    }

    public static Device initDevice() {
        return new Device()
                .fingerprint(FINGERPRINT)
                .ip(IP);
    }

    public static Contact initContact() {
        return new Contact()
                .email(EMAIL)
                .phone(PHONE);
    }

    public static Cash initCash() {
        return new Cash()
                .amount(AMOUNT)
                .currency(CURRENCY);
    }

    public static BankCard initBankCard() {
        return new BankCard()
                .type(BANK_CARD)
                .bankName(BANK_NAME)
                .cardToken(CARD_TOKEN)
                .cardType(CardType.CREDIT)
                .bin(BIN)
                .countryCode(RUS)
                .lastDigits(LAST_DIGITS)
                .paymentSystem(VISA);
    }

    public static Provider initProvider() {
        return new Provider()
                .id(PROVIDER_ID)
                .country(RUS)
                .terminalId(TERMINAL_ID);
    }

    public static Merchant initMerchant() {
        return new Merchant()
                .id(PARTY_ID)
                .shop(initShop());
    }

    public static Shop initShop() {
        return new Shop()
                .id(SHOP_ID)
                .name(randomString())
                .category(randomString())
                .location(randomString());
    }

    public static Error initError() {
        return new Error()
                .errorCode(ERROR_CODE)
                .errorMessage(ERROR_REASON);
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

}
