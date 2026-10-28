package dev.vality.fraudo.payout.visitor;

import dev.vality.fraudo.aggregator.UniqueValueAggregator;
import dev.vality.fraudo.payout.aggregator.CountPayoutAggregator;
import dev.vality.fraudo.payout.aggregator.SumPayoutAggregator;
import dev.vality.fraudo.payment.resolver.PaymentGroupResolver;
import dev.vality.fraudo.payment.resolver.PaymentTimeWindowResolver;
import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.exception.NotImplementedOperatorException;
import dev.vality.fraudo.utils.TextUtil;
import lombok.RequiredArgsConstructor;

import static dev.vality.fraudo.FraudoPaymentParser.*;

@RequiredArgsConstructor
public class PayoutFuncVisitor<T, U> {

    private final CountPayoutAggregator<T, U> countPayoutAggregator;
    private final SumPayoutAggregator<T, U> sumPayoutAggregator;
    private final UniqueValueAggregator<T, U> uniquePayoutValueAggregator;
    private final FieldResolver<T, U> fieldResolver;
    private final PaymentGroupResolver<T, U> groupResolver;
    private final PaymentTimeWindowResolver timeWindowResolver;

    private <A> A aggregator(A aggregator) {
        if (aggregator == null) {
            throw new NotImplementedOperatorException("payout history");
        }
        return aggregator;
    }

    public Integer visitCountPayouts(Count_payoutsContext ctx, T model) {
        return aggregator(countPayoutAggregator).count(
                fieldResolver.resolveName(TextUtil.safeGetText(ctx.STRING())), model,
                timeWindowResolver.resolve(ctx.time_window()), groupResolver.resolve(ctx.group_by()));
    }

    public Integer visitCountPayoutsSuccess(Count_payouts_successContext ctx, T model) {
        return aggregator(countPayoutAggregator).countSuccess(
                fieldResolver.resolveName(TextUtil.safeGetText(ctx.STRING())), model,
                timeWindowResolver.resolve(ctx.time_window()), groupResolver.resolve(ctx.group_by()));
    }

    public Integer visitCountPayoutsError(Count_payouts_errorContext ctx, T model) {
        if (ctx.STRING().size() == 2) {
            return aggregator(countPayoutAggregator).countError(
                    fieldResolver.resolveName(TextUtil.safeGetText(ctx.STRING(0))), model,
                    timeWindowResolver.resolve(ctx.time_window()), TextUtil.safeGetText(ctx.STRING(1)),
                    groupResolver.resolve(ctx.group_by()));
        }
        return aggregator(countPayoutAggregator).countError(
                fieldResolver.resolveName(TextUtil.safeGetText(ctx.STRING(0))), model,
                timeWindowResolver.resolve(ctx.time_window()), groupResolver.resolve(ctx.group_by()));
    }

    public Double visitSumPayouts(Sum_payoutsContext ctx, T model) {
        return aggregator(sumPayoutAggregator).sum(
                fieldResolver.resolveName(TextUtil.safeGetText(ctx.STRING())), model,
                timeWindowResolver.resolve(ctx.time_window()), groupResolver.resolve(ctx.group_by()));
    }

    public Double visitSumPayoutsSuccess(Sum_payouts_successContext ctx, T model) {
        return aggregator(sumPayoutAggregator).sumSuccess(
                fieldResolver.resolveName(TextUtil.safeGetText(ctx.STRING())), model,
                timeWindowResolver.resolve(ctx.time_window()), groupResolver.resolve(ctx.group_by()));
    }

    public Double visitSumPayoutsError(Sum_payouts_errorContext ctx, T model) {
        if (ctx.STRING().size() == 2) {
            return aggregator(sumPayoutAggregator).sumError(
                    fieldResolver.resolveName(TextUtil.safeGetText(ctx.STRING(0))), model,
                    timeWindowResolver.resolve(ctx.time_window()), TextUtil.safeGetText(ctx.STRING(1)),
                    groupResolver.resolve(ctx.group_by()));
        }
        return aggregator(sumPayoutAggregator).sumError(
                fieldResolver.resolveName(TextUtil.safeGetText(ctx.STRING(0))), model,
                timeWindowResolver.resolve(ctx.time_window()), groupResolver.resolve(ctx.group_by()));
    }

    public Integer visitUniquePayouts(Unique_payoutsContext ctx, T model) {
        return aggregator(uniquePayoutValueAggregator).countUniqueValue(
                fieldResolver.resolveName(TextUtil.safeGetText(ctx.STRING(0))), model,
                fieldResolver.resolveName(TextUtil.safeGetText(ctx.STRING(1))),
                timeWindowResolver.resolve(ctx.time_window()), groupResolver.resolve(ctx.group_by()));
    }

}
