package dev.vality.fraudbusters.repository.clickhouse.mapper;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.CountryCode;
import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.domain.PaymentSystemRef;
import dev.vality.damsel.fraudbusters.Account;
import dev.vality.damsel.fraudbusters.ClientInfo;
import dev.vality.damsel.fraudbusters.Error;
import dev.vality.damsel.fraudbusters.GenericPaymentTool;
import dev.vality.damsel.fraudbusters.ProviderInfo;
import dev.vality.damsel.fraudbusters.Resource;
import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.damsel.fraudbusters.WithdrawalStatus;
import dev.vality.fraudbusters.constant.ClickhouseUtilsValue;
import dev.vality.fraudbusters.constant.PaymentField;
import dev.vality.fraudbusters.constant.WithdrawalField;
import dev.vality.fraudbusters.util.TimestampUtil;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Reads a row of the payout history query back into the Withdrawal it was written from. Only a bank card is stored
 * column by column, so any other destination comes back as an unknown generic tool.
 */
@Component
public class WithdrawalMapper implements RowMapper<Withdrawal> {

    private static final String ACCOUNT_ID = "accountId";
    private static final String ACCOUNT_CURRENCY = "accountCurrency";
    private static final String ISSUER_COUNTRY = "issuerCountry";

    @Override
    public Withdrawal mapRow(ResultSet rs, int i) throws SQLException {
        Withdrawal withdrawal = new Withdrawal()
                .setId(rs.getString(PaymentField.ID.getValue()))
                .setEventTime(TimestampUtil.getStringDate(rs.getLong(PaymentField.EVENT_TIME.getValue())))
                .setCost(new Cash()
                        .setAmount(rs.getLong(PaymentField.AMOUNT.getValue()))
                        .setCurrency(new CurrencyRef(rs.getString(PaymentField.CURRENCY.getValue()))))
                .setStatus(WithdrawalStatus.valueOf(rs.getString(PaymentField.STATUS.getValue())))
                .setAccount(new Account()
                        .setId(rs.getString(ACCOUNT_ID))
                        .setCurrency(new CurrencyRef(rs.getString(ACCOUNT_CURRENCY))))
                .setDestinationResource(mapResource(rs))
                .setPartyId(rs.getString(PaymentField.PARTY_ID.getValue()))
                .setWalletId(rs.getString(WithdrawalField.WALLET_ID.getValue()));
        String errorCode = known(rs.getString(PaymentField.ERROR_CODE.getValue()));
        if (errorCode != null) {
            withdrawal.setError(new Error()
                    .setErrorCode(errorCode)
                    .setErrorReason(known(rs.getString(PaymentField.ERROR_REASON.getValue()))));
        }
        String providerId = known(rs.getString(PaymentField.PROVIDER_ID.getValue()));
        String terminal = known(rs.getString(PaymentField.TERMINAL.getValue()));
        if (providerId != null || terminal != null) {
            withdrawal.setProviderInfo(new ProviderInfo()
                    .setProviderId(providerId != null ? providerId : ClickhouseUtilsValue.UNKNOWN)
                    .setTerminalId(terminal != null ? terminal : ClickhouseUtilsValue.UNKNOWN)
                    .setCountry(known(rs.getString(PaymentField.BANK_COUNTRY.getValue()))));
        }
        ClientInfo clientInfo = new ClientInfo()
                .setCustomerId(rs.getString(PaymentField.CUSTOMER_ID.getValue()))
                .setUserId(rs.getString(PaymentField.USER_ID.getValue()))
                .setIp(rs.getString(PaymentField.IP.getValue()))
                .setFingerprint(rs.getString(PaymentField.FINGERPRINT.getValue()))
                .setEmail(rs.getString(PaymentField.EMAIL.getValue()))
                .setPhone(rs.getString(PaymentField.PHONE.getValue()));
        if (Stream.of(ClientInfo._Fields.values()).anyMatch(clientInfo::isSet)) {
            withdrawal.setClientInfo(clientInfo);
        }
        return withdrawal;
    }

    private static Resource mapResource(ResultSet rs) throws SQLException {
        String cardToken = known(rs.getString(PaymentField.CARD_TOKEN.getValue()));
        if (cardToken == null) {
            return Resource.generic(new GenericPaymentTool(ClickhouseUtilsValue.UNKNOWN));
        }
        BankCard bankCard = new BankCard()
                .setToken(cardToken)
                .setBin(rs.getString(PaymentField.BIN.getValue()))
                .setLastDigits(rs.getString(PaymentField.LAST_DIGITS.getValue()));
        String paymentSystem = known(rs.getString(PaymentField.PAYMENT_SYSTEM.getValue()));
        if (paymentSystem != null) {
            bankCard.setPaymentSystem(new PaymentSystemRef(paymentSystem));
        }
        String issuerCountry = known(rs.getString(ISSUER_COUNTRY));
        Arrays.stream(CountryCode.values())
                .filter(country -> country.name().equals(issuerCountry))
                .findFirst()
                .ifPresent(bankCard::setIssuerCountry);
        return Resource.bank_card(bankCard);
    }

    private static String known(String value) {
        return StringUtils.hasLength(value) && !ClickhouseUtilsValue.UNKNOWN.equals(value) ? value : null;
    }
}
