package dev.vality.fraudbusters.resource.payment.handler;

import dev.vality.damsel.base.InvalidRequest;
import dev.vality.damsel.domain.RiskScore;
import dev.vality.damsel.proxy_inspector.*;
import dev.vality.damsel.wb_list.*;
import dev.vality.fraudbusters.converter.CheckedResultToRiskScoreConverter;
import dev.vality.fraudbusters.converter.ContextToFraudRequestConverter;
import dev.vality.fraudbusters.converter.WithdrawalContextToModelConverter;
import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.domain.FraudRequest;
import dev.vality.fraudbusters.domain.FraudResult;
import dev.vality.fraudbusters.domain.PayoutResult;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.stream.TemplateVisitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class FraudInspectorHandler implements InspectorProxySrv.Iface {

    private final String resultTopic;
    private final CheckedResultToRiskScoreConverter checkedResultToRiskScoreConverter;
    private final ContextToFraudRequestConverter requestConverter;
    private final TemplateVisitor<PaymentModel, CheckedResultModel> templateVisitor;
    private final KafkaTemplate<String, FraudResult> kafkaFraudResultTemplate;
    private final WbListServiceSrv.Iface wbListServiceSrv;
    private final WithdrawalContextToModelConverter withdrawalContextToModelConverter;
    private final TemplateVisitor<PayoutModel, CheckedResultModel> payoutTemplateVisitor;
    private final String payoutResultTopic;
    private final KafkaTemplate<String, PayoutResult> kafkaPayoutResultTemplate;

    @Override
    public RiskScore inspectWithdrawal(WithdrawalContext context) throws InvalidRequest, TException {
        try {
            context.validate();
            PayoutModel model = withdrawalContextToModelConverter.convert(context);
            CheckedResultModel result = payoutTemplateVisitor.visit(model);
            kafkaPayoutResultTemplate.send(payoutResultTopic, model.getWithdrawalId(), new PayoutResult(model, result));
            return checkedResultToRiskScoreConverter.convert(result);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequest().setErrors(List.of(e.getMessage()));
        } catch (Exception e) {
            throw new TException("Unable to inspect withdrawal", e);
        }
    }

    @Override
    public RiskScore inspectPayment(Context context) throws TException {
        try {
            FraudRequest model = requestConverter.convert(context);
            if (model != null) {
                log.info("Check fraudRequest: {}", model);
                FraudResult fraudResult = new FraudResult(model, templateVisitor.visit(model.getFraudModel()));
                kafkaFraudResultTemplate.send(resultTopic, fraudResult);
                log.info("Checked fraudResult: {}", fraudResult);
                return checkedResultToRiskScoreConverter.convert(fraudResult.getResultModel());
            }
            return RiskScore.high;
        } catch (Exception e) {
            log.error("Error when inspectPayment() e: ", e);
            throw new TException("Error when inspectPayment() e: ", e);
        }
    }

    @Override
    public boolean isBlacklisted(BlackListContext blackListContext) throws InvalidRequest, TException {
        try {
            Row row = new Row()
                    .setId(IdInfo.payment_id(new PaymentId()
                            .setPartyId(blackListContext.first_id)
                            .setShopId(blackListContext.second_id)))
                    .setListName(blackListContext.field_name)
                    .setListType(ListType.black)
                    .setValue(blackListContext.getValue());
            return wbListServiceSrv.isExist(row);
        } catch (Exception e) {
            log.warn("FraudInspectorHandler error when isExistInBlackList e: ", e);
            return false;
        }
    }

    @Override
    public BlockedShops inspectUser(InspectUserContext context) throws InvalidRequest, TException {
        throw new UnsupportedOperationException();
    }
}
