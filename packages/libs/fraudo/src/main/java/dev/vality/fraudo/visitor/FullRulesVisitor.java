package dev.vality.fraudo.visitor;

import dev.vality.fraudo.converter.TrustConditionConverter;
import dev.vality.fraudo.exception.NotImplementedOperatorException;
import dev.vality.fraudo.model.BaseModel;
import dev.vality.fraudo.payout.visitor.PayoutFuncVisitor;
import dev.vality.fraudo.payment.visitor.*;
import dev.vality.fraudo.payment.visitor.impl.FullVisitorImpl;
import dev.vality.fraudo.resolver.FieldResolver;

import static dev.vality.fraudo.FraudoRulesParser.*;

public class FullRulesVisitor<T extends BaseModel, U> extends FullVisitorImpl<T, U> {

    private final PayoutFuncVisitor<T, U> payoutFuncVisitor;

    public FullRulesVisitor(
            CountVisitor<T> countVisitor,
            SumVisitor<T> sumVisitor,
            ListVisitor<T> listVisitor,
            CustomFuncVisitor<T> customFuncVisitor,
            IsTrustedFuncVisitor<T> isTrustedFuncVisitor,
            FieldResolver<T, U> fieldResolver,
            TrustConditionConverter trustConditionConverter,
            PayoutFuncVisitor<T, U> payoutFuncVisitor
    ) {
        super(countVisitor, sumVisitor, listVisitor, customFuncVisitor, isTrustedFuncVisitor, fieldResolver,
                trustConditionConverter);
        this.payoutFuncVisitor = payoutFuncVisitor;
    }

    @Override
    public Integer visitCount_payouts(Count_payoutsContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException("countPayouts");
        }
        return (Integer) cacheFunction(
                ctx.getText(), model -> payoutFuncVisitor.visitCountPayouts(ctx, model));
    }

    @Override
    public Integer visitCount_payouts_success(Count_payouts_successContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException("countPayouts");
        }
        return (Integer) cacheFunction(
                ctx.getText(), model -> payoutFuncVisitor.visitCountPayoutsSuccess(ctx, model));
    }

    @Override
    public Integer visitCount_payouts_error(Count_payouts_errorContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException("countPayouts");
        }
        return (Integer) cacheFunction(
                ctx.getText(), model -> payoutFuncVisitor.visitCountPayoutsError(ctx, model));
    }

    @Override
    public Double visitSum_payouts(Sum_payoutsContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException("sumPayouts");
        }
        return (Double) cacheFunction(
                ctx.getText(), model -> payoutFuncVisitor.visitSumPayouts(ctx, model));
    }

    @Override
    public Double visitSum_payouts_success(Sum_payouts_successContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException("sumPayouts");
        }
        return (Double) cacheFunction(
                ctx.getText(), model -> payoutFuncVisitor.visitSumPayoutsSuccess(ctx, model));
    }

    @Override
    public Double visitSum_payouts_error(Sum_payouts_errorContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException("sumPayouts");
        }
        return (Double) cacheFunction(
                ctx.getText(), model -> payoutFuncVisitor.visitSumPayoutsError(ctx, model));
    }

    @Override
    public Integer visitUnique_payouts(Unique_payoutsContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException("uniquePayouts");
        }
        return (Integer) cacheFunction(
                ctx.getText(), model -> payoutFuncVisitor.visitUniquePayouts(ctx, model));
    }

}
