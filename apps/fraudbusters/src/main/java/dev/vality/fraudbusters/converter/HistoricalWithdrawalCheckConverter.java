package dev.vality.fraudbusters.converter;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.CountryCode;
import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.domain.PaymentSystemRef;
import dev.vality.damsel.fraudbusters.Account;
import dev.vality.damsel.fraudbusters.CheckResult;
import dev.vality.damsel.fraudbusters.ClientInfo;
import dev.vality.damsel.fraudbusters.ConcreteCheckResult;
import dev.vality.damsel.fraudbusters.GenericPaymentTool;
import dev.vality.damsel.fraudbusters.HistoricalWithdrawalCheck;
import dev.vality.damsel.fraudbusters.Resource;
import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.damsel.fraudbusters.WithdrawalStatus;
import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.domain.PayoutResult;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.util.TimestampUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class HistoricalWithdrawalCheckConverter
        implements Converter<PayoutResult, HistoricalWithdrawalCheck> {

    private static final String EMPTY_FIELD = "-";

    private final ResultStatusConverter resultStatusConverter;

    @Override
    public HistoricalWithdrawalCheck convert(PayoutResult payoutResult) {
        return new HistoricalWithdrawalCheck()
                .setWithdrawal(convertWithdrawal(payoutResult.getPayoutModel()))
                .setCheckResult(convertCheckResult(payoutResult.getResultModel()));
    }

    private static Withdrawal convertWithdrawal(PayoutModel model) {
        CurrencyRef currency = new CurrencyRef(model.getCurrency());
        Withdrawal withdrawal = new Withdrawal()
                .setId(model.getWithdrawalId())
                .setEventTime(TimestampUtil.getStringDate(TimeUnit.MILLISECONDS.toSeconds(model.getTimestamp())))
                .setCost(new Cash(model.getAmount(), currency))
                .setStatus(WithdrawalStatus.pending)
                .setAccount(new Account(EMPTY_FIELD, currency))
                .setDestinationResource(StringUtils.hasLength(model.getCardToken())
                        ? Resource.bank_card(convertBankCard(model))
                        : Resource.generic(new GenericPaymentTool(EMPTY_FIELD)))
                .setPartyId(model.getPartyId())
                .setWalletId(model.getWalletId());
        ClientInfo clientInfo = new ClientInfo()
                .setCustomerId(model.getCustomerId())
                .setUserId(model.getUserId())
                .setIp(model.getIp())
                .setFingerprint(model.getFingerprint())
                .setEmail(model.getEmail())
                .setPhone(model.getPhone());
        if (Stream.of(ClientInfo._Fields.values()).anyMatch(clientInfo::isSet)) {
            withdrawal.setClientInfo(clientInfo);
        }
        return withdrawal;
    }

    private CheckResult convertCheckResult(CheckedResultModel resultModel) {
        return new CheckResult()
                .setCheckedTemplate(resultModel.getCheckedTemplate())
                .setConcreteCheckResult(new ConcreteCheckResult()
                        .setResultStatus(resultStatusConverter.convert(resultModel.getResultModel().getResultStatus()))
                        .setRuleChecked(resultModel.getResultModel().getRuleChecked())
                        .setNotificationsRule(Collections.emptyList()));
    }

    private static BankCard convertBankCard(PayoutModel model) {
        BankCard bankCard = new BankCard()
                .setToken(model.getCardToken())
                .setBin(Objects.requireNonNullElse(model.getBin(), ""))
                .setLastDigits(Objects.requireNonNullElse(model.getLastDigits(), ""));
        if (StringUtils.hasLength(model.getPaymentSystem())) {
            bankCard.setPaymentSystem(new PaymentSystemRef(model.getPaymentSystem()));
        }
        Arrays.stream(CountryCode.values())
                .filter(country -> country.name().equals(model.getBinCountryCode()))
                .findFirst()
                .ifPresent(bankCard::setIssuerCountry);
        return bankCard;
    }
}
