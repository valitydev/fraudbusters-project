package dev.vality.fraudo.payment.visitor.impl;

import dev.vality.fraudo.FraudoPaymentParser;
import dev.vality.fraudo.payment.aggregator.CountPaymentAggregator;
import dev.vality.fraudo.payment.resolver.PaymentGroupResolver;
import dev.vality.fraudo.payment.resolver.PaymentTimeWindowResolver;
import dev.vality.fraudo.payment.visitor.CountVisitor;
import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.utils.TextUtil;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CountVisitorImpl<T, U> implements CountVisitor<T> {

    private final CountPaymentAggregator<T, U> countPaymentAggregator;
    private final FieldResolver<T, U> fieldResolver;
    private final PaymentGroupResolver<T, U> groupResolver;
    private final PaymentTimeWindowResolver timeWindowResolver;

    @Override
    public Integer visitCount(FraudoPaymentParser.CountContext ctx, T model) {
        String countTarget = TextUtil.safeGetText(ctx.STRING());
        return countPaymentAggregator.count(
                fieldResolver.resolveName(countTarget),
                model,
                timeWindowResolver.resolve(ctx.time_window()),
                groupResolver.resolve(ctx.group_by())
        );
    }

    @Override
    public Integer visitCountSuccess(FraudoPaymentParser.Count_successContext ctx, T model) {
        String countTarget = TextUtil.safeGetText(ctx.STRING());
        return countPaymentAggregator.countSuccess(
                fieldResolver.resolveName(countTarget),
                model,
                timeWindowResolver.resolve(ctx.time_window()),
                groupResolver.resolve(ctx.group_by())
        );
    }

    @Override
    public Integer visitCountPending(FraudoPaymentParser.Count_pendingContext ctx, T model) {
        String countTarget = TextUtil.safeGetText(ctx.STRING());
        return countPaymentAggregator.countPending(
                fieldResolver.resolveName(countTarget),
                model,
                timeWindowResolver.resolve(ctx.time_window()),
                groupResolver.resolve(ctx.group_by())
        );
    }

    @Override
    public Integer visitCountError(FraudoPaymentParser.Count_errorContext ctx, T model) {
        String countTarget = TextUtil.safeGetText(ctx.STRING(0));
        if (ctx.STRING().size() == 2) {
            String errorCode = TextUtil.safeGetText(ctx.STRING(1));
            return countPaymentAggregator.countError(
                    fieldResolver.resolveName(countTarget),
                    model,
                    timeWindowResolver.resolve(ctx.time_window()),
                    errorCode,
                    groupResolver.resolve(ctx.group_by())
            );
        }
        return countPaymentAggregator.countError(
                fieldResolver.resolveName(countTarget),
                model,
                timeWindowResolver.resolve(ctx.time_window()),
                groupResolver.resolve(ctx.group_by())
        );
    }

    @Override
    public Integer visitCountChargeback(FraudoPaymentParser.Count_chargebackContext ctx, T model) {
        String countTarget = TextUtil.safeGetText(ctx.STRING());
        return countPaymentAggregator.countChargeback(
                fieldResolver.resolveName(countTarget),
                model,
                timeWindowResolver.resolve(ctx.time_window()),
                groupResolver.resolve(ctx.group_by())
        );
    }

    @Override
    public Integer visitCountRefund(FraudoPaymentParser.Count_refundContext ctx, T model) {
        String countTarget = TextUtil.safeGetText(ctx.STRING());
        return countPaymentAggregator.countRefund(
                fieldResolver.resolveName(countTarget),
                model,
                timeWindowResolver.resolve(ctx.time_window()),
                groupResolver.resolve(ctx.group_by())
        );
    }

}
