package dev.vality.fraudo.visitor;

import dev.vality.fraudo.exception.NotImplementedOperatorException;
import dev.vality.fraudo.model.BaseModel;
import dev.vality.fraudo.payout.visitor.PayoutFuncVisitor;
import dev.vality.fraudo.payment.visitor.*;
import dev.vality.fraudo.payment.visitor.impl.FirstFindVisitorImpl;
import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.utils.key.generator.HistoryKeyGenerator;

import java.util.List;

import static dev.vality.fraudo.FraudoRulesParser.*;
import static dev.vality.fraudo.constant.HistoryFunction.*;

public class FirstFindRulesWithPayoutVisitor<T extends BaseModel, U> extends FirstFindVisitorImpl<T, U> {

    private final PayoutFuncVisitor<T, U> payoutFuncVisitor;
    private final FieldResolver<T, U> fieldResolver;

    public FirstFindRulesWithPayoutVisitor(
            PaymentFuncVisitor<T> paymentFuncVisitor,
            ListVisitor<T> listVisitor,
            FieldResolver<T, U> fieldResolver,
            PayoutFuncVisitor<T, U> payoutFuncVisitor
    ) {
        super(paymentFuncVisitor, listVisitor, fieldResolver);
        this.payoutFuncVisitor = payoutFuncVisitor;
        this.fieldResolver = fieldResolver;
    }

    @Override
    public Integer visitCount_payouts(Count_payoutsContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException(COUNT_PAYOUTS);
        }
        return (Integer) cacheFunction(
                HistoryKeyGenerator.generate(COUNT_PAYOUTS, ctx.time_window(), ctx.group_by(),
                        List.of(ctx.STRING()), null, fieldResolver::resolveName),
                model -> payoutFuncVisitor.visitCountPayouts(ctx, model));
    }

    @Override
    public Integer visitCount_payouts_success(Count_payouts_successContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException(COUNT_PAYOUTS);
        }
        return (Integer) cacheFunction(
                HistoryKeyGenerator.generate(COUNT_PAYOUTS_SUCCESS, ctx.time_window(), ctx.group_by(),
                        List.of(ctx.STRING()), null, fieldResolver::resolveName),
                model -> payoutFuncVisitor.visitCountPayoutsSuccess(ctx, model));
    }

    @Override
    public Integer visitCount_payouts_error(Count_payouts_errorContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException(COUNT_PAYOUTS);
        }
        return (Integer) cacheFunction(
                HistoryKeyGenerator.generate(COUNT_PAYOUTS_ERROR, ctx.time_window(), ctx.group_by(),
                        List.of(ctx.STRING(0)), ctx.STRING().size() == 2 ? ctx.STRING(1) : null,
                        fieldResolver::resolveName),
                model -> payoutFuncVisitor.visitCountPayoutsError(ctx, model));
    }

    @Override
    public Double visitSum_payouts(Sum_payoutsContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException(SUM_PAYOUTS);
        }
        return (Double) cacheFunction(
                HistoryKeyGenerator.generate(SUM_PAYOUTS, ctx.time_window(), ctx.group_by(),
                        List.of(ctx.STRING()), null, fieldResolver::resolveName),
                model -> payoutFuncVisitor.visitSumPayouts(ctx, model));
    }

    @Override
    public Double visitSum_payouts_success(Sum_payouts_successContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException(SUM_PAYOUTS);
        }
        return (Double) cacheFunction(
                HistoryKeyGenerator.generate(SUM_PAYOUTS_SUCCESS, ctx.time_window(), ctx.group_by(),
                        List.of(ctx.STRING()), null, fieldResolver::resolveName),
                model -> payoutFuncVisitor.visitSumPayoutsSuccess(ctx, model));
    }

    @Override
    public Double visitSum_payouts_error(Sum_payouts_errorContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException(SUM_PAYOUTS);
        }
        return (Double) cacheFunction(
                HistoryKeyGenerator.generate(SUM_PAYOUTS_ERROR, ctx.time_window(), ctx.group_by(),
                        List.of(ctx.STRING(0)), ctx.STRING().size() == 2 ? ctx.STRING(1) : null,
                        fieldResolver::resolveName),
                model -> payoutFuncVisitor.visitSumPayoutsError(ctx, model));
    }

    @Override
    public Integer visitUnique_payouts(Unique_payoutsContext ctx) {
        if (payoutFuncVisitor == null) {
            throw new NotImplementedOperatorException(UNIQUE_PAYOUTS);
        }
        return (Integer) cacheFunction(
                HistoryKeyGenerator.generate(UNIQUE_PAYOUTS, ctx.time_window(), ctx.group_by(),
                        ctx.STRING(), null, fieldResolver::resolveName),
                model -> payoutFuncVisitor.visitUniquePayouts(ctx, model));
    }

}
