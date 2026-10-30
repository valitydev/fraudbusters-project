package dev.vality.fraudo.visitor;

import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.exception.UnknownResultException;
import dev.vality.fraudo.model.BaseModel;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.model.RuleResult;
import dev.vality.fraudo.payment.generator.RuleKeyGenerator;
import dev.vality.fraudo.payment.visitor.ListVisitor;
import dev.vality.fraudo.payment.visitor.PaymentFuncVisitor;
import dev.vality.fraudo.payout.visitor.PayoutFuncVisitor;
import dev.vality.fraudo.resolver.FieldResolver;

import java.util.ArrayList;
import java.util.List;

import static dev.vality.fraudo.FraudoRulesParser.Fraud_ruleContext;
import static dev.vality.fraudo.FraudoRulesParser.ParseContext;

public class FullRulesVisitor<T extends BaseModel, U> extends FirstFindRulesVisitor<T, U> {

    public FullRulesVisitor(
            PaymentFuncVisitor<T> paymentFuncVisitor,
            ListVisitor<T> listVisitor,
            FieldResolver<T, U> fieldResolver,
            PayoutFuncVisitor<T, U> payoutFuncVisitor
    ) {
        super(paymentFuncVisitor, listVisitor, fieldResolver, payoutFuncVisitor);
    }

    @Override
    public ResultModel visitParse(ParseContext ctx) {
        List<RuleResult> results = new ArrayList<>();
        for (int i = 0; i < ctx.fraud_rule().size(); i++) {
            Fraud_ruleContext fraudRuleContext = ctx.fraud_rule().get(i);
            ResultStatus result = (ResultStatus) visit(fraudRuleContext);
            String key = RuleKeyGenerator.generateRuleKey(fraudRuleContext, i);
            if (result == null) {
                throw new UnknownResultException(fraudRuleContext.getText());
            } else if (result != ResultStatus.NORMAL) {
                results.add(new RuleResult(result, key));
            }
        }
        return new ResultModel(results);
    }

}
