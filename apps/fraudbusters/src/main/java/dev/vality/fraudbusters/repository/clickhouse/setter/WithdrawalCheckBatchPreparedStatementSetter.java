package dev.vality.fraudbusters.repository.clickhouse.setter;

import dev.vality.fraudbusters.constant.EventSource;
import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.domain.PayoutResult;
import dev.vality.fraudbusters.domain.TimeProperties;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.util.TimestampUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class WithdrawalCheckBatchPreparedStatementSetter implements BatchPreparedStatementSetter {

    public static final String INSERT = String.format("""
            INSERT INTO %s
             (timestamp, eventTimeHour, eventTime, id, partyId, walletId, amount, currency, cardToken, bin,
             lastDigits, paymentSystem, issuerCountry, customerId, userId, ip, fingerprint, email, phone,
             resultStatus, checkedRule, checkedTemplate)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, EventSource.FRAUD_WITHDRAWAL_CHECK.getTable());

    private final List<PayoutResult> batch;

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        PayoutResult payoutResult = batch.get(i);
        PayoutModel model = payoutResult.getPayoutModel();
        CheckedResultModel result = payoutResult.getResultModel();
        TimeProperties timeProperties = TimestampUtil.generateTimePropertiesByInstant(
                TimestampUtil.instantFromModel(model));
        int l = 1;
        ps.setObject(l++, timeProperties.getTimestamp());
        ps.setLong(l++, timeProperties.getEventTimeHour());
        ps.setLong(l++, timeProperties.getEventTime());
        ps.setString(l++, model.getWithdrawalId());
        ps.setString(l++, model.getPartyId());
        ps.setString(l++, model.getWalletId());
        ps.setLong(l++, model.getAmount());
        ps.setString(l++, model.getCurrency());
        ps.setString(l++, model.getCardToken());
        ps.setString(l++, model.getBin());
        ps.setString(l++, model.getLastDigits());
        ps.setString(l++, model.getPaymentSystem());
        ps.setString(l++, model.getBinCountryCode());
        ps.setString(l++, model.getCustomerId());
        ps.setString(l++, model.getUserId());
        ps.setString(l++, model.getIp());
        ps.setString(l++, model.getFingerprint());
        ps.setString(l++, model.getEmail());
        ps.setString(l++, model.getPhone());
        ps.setString(l++, result.getResultModel().getResultStatus().name());
        ps.setString(l++, result.getResultModel().getRuleChecked());
        ps.setString(l, result.getCheckedTemplate());
    }

    @Override
    public int getBatchSize() {
        return batch.size();
    }
}
