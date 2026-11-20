package dev.vality.fraudbusters.repository.clickhouse.mapper;

import dev.vality.fraudbusters.constant.WithdrawalCheckField;
import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.domain.ConcreteResultModel;
import dev.vality.fraudbusters.domain.PayoutResult;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudo.constant.ResultStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Component
public class WithdrawalCheckMapper implements RowMapper<PayoutResult> {

    @Override
    public PayoutResult mapRow(ResultSet rs, int i) throws SQLException {
        PayoutModel model = new PayoutModel();
        model.setWithdrawalId(rs.getString(WithdrawalCheckField.ID.getValue()));
        model.setTimestamp(TimeUnit.SECONDS.toMillis(rs.getLong(WithdrawalCheckField.EVENT_TIME.getValue())));
        model.setPartyId(rs.getString(WithdrawalCheckField.PARTY_ID.getValue()));
        model.setWalletId(rs.getString(WithdrawalCheckField.WALLET_ID.getValue()));
        model.setAmount(rs.getLong(WithdrawalCheckField.AMOUNT.getValue()));
        model.setCurrency(rs.getString(WithdrawalCheckField.CURRENCY.getValue()));
        model.setCardToken(rs.getString(WithdrawalCheckField.CARD_TOKEN.getValue()));
        model.setBin(rs.getString(WithdrawalCheckField.BIN.getValue()));
        model.setLastDigits(rs.getString(WithdrawalCheckField.LAST_DIGITS.getValue()));
        model.setPaymentSystem(rs.getString(WithdrawalCheckField.PAYMENT_SYSTEM.getValue()));
        model.setBinCountryCode(rs.getString(WithdrawalCheckField.ISSUER_COUNTRY.getValue()));
        model.setCustomerId(rs.getString(WithdrawalCheckField.CUSTOMER_ID.getValue()));
        model.setUserId(rs.getString(WithdrawalCheckField.USER_ID.getValue()));
        model.setIp(rs.getString(WithdrawalCheckField.IP.getValue()));
        model.setFingerprint(rs.getString(WithdrawalCheckField.FINGERPRINT.getValue()));
        model.setEmail(rs.getString(WithdrawalCheckField.EMAIL.getValue()));
        model.setPhone(rs.getString(WithdrawalCheckField.PHONE.getValue()));
        CheckedResultModel result = new CheckedResultModel();
        result.setCheckedTemplate(rs.getString(WithdrawalCheckField.CHECKED_TEMPLATE.getValue()));
        result.setResultModel(new ConcreteResultModel(
                ResultStatus.valueOf(rs.getString(WithdrawalCheckField.RESULT_STATUS.getValue())),
                rs.getString(WithdrawalCheckField.CHECKED_RULE.getValue()),
                null));
        return new PayoutResult(model, result);
    }
}
