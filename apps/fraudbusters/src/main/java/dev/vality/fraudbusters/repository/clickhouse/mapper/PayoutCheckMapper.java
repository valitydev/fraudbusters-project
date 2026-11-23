package dev.vality.fraudbusters.repository.clickhouse.mapper;

import dev.vality.fraudbusters.constant.PayoutCheckField;
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
public class PayoutCheckMapper implements RowMapper<PayoutResult> {

    @Override
    public PayoutResult mapRow(ResultSet rs, int i) throws SQLException {
        PayoutModel model = new PayoutModel();
        model.setPayoutId(rs.getString(PayoutCheckField.ID.getValue()));
        model.setTimestamp(TimeUnit.SECONDS.toMillis(rs.getLong(PayoutCheckField.EVENT_TIME.getValue())));
        model.setPartyId(rs.getString(PayoutCheckField.PARTY_ID.getValue()));
        model.setWalletId(rs.getString(PayoutCheckField.WALLET_ID.getValue()));
        model.setAmount(rs.getLong(PayoutCheckField.AMOUNT.getValue()));
        model.setCurrency(rs.getString(PayoutCheckField.CURRENCY.getValue()));
        model.setCardToken(rs.getString(PayoutCheckField.CARD_TOKEN.getValue()));
        model.setBin(rs.getString(PayoutCheckField.BIN.getValue()));
        model.setLastDigits(rs.getString(PayoutCheckField.LAST_DIGITS.getValue()));
        model.setPaymentSystem(rs.getString(PayoutCheckField.PAYMENT_SYSTEM.getValue()));
        model.setBinCountryCode(rs.getString(PayoutCheckField.ISSUER_COUNTRY.getValue()));
        model.setCustomerId(rs.getString(PayoutCheckField.CUSTOMER_ID.getValue()));
        model.setUserId(rs.getString(PayoutCheckField.USER_ID.getValue()));
        model.setIp(rs.getString(PayoutCheckField.IP.getValue()));
        model.setFingerprint(rs.getString(PayoutCheckField.FINGERPRINT.getValue()));
        model.setEmail(rs.getString(PayoutCheckField.EMAIL.getValue()));
        model.setPhone(rs.getString(PayoutCheckField.PHONE.getValue()));
        CheckedResultModel result = new CheckedResultModel();
        result.setCheckedTemplate(rs.getString(PayoutCheckField.CHECKED_TEMPLATE.getValue()));
        result.setResultModel(new ConcreteResultModel(
                ResultStatus.valueOf(rs.getString(PayoutCheckField.RESULT_STATUS.getValue())),
                rs.getString(PayoutCheckField.CHECKED_RULE.getValue()),
                null));
        return new PayoutResult(model, result);
    }
}
