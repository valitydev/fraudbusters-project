package dev.vality.fraudo.payment.visitor.impl;

import dev.vality.fraudo.payment.aggregator.SumPaymentAggregator;
import dev.vality.fraudo.resolver.impl.GroupFieldsResolverImpl;
import dev.vality.fraudo.resolver.impl.TimeWindowResolverImpl;
import dev.vality.fraudo.payment.visitor.SumVisitor;
import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.utils.TextUtil;
import lombok.RequiredArgsConstructor;

import static dev.vality.fraudo.FraudoRulesParser.*;

@RequiredArgsConstructor
public class SumVisitorImpl<T, U> implements SumVisitor<T> {

    private final SumPaymentAggregator<T, U> sumPaymentAggregator;
    private final FieldResolver<T, U> fieldResolver;
    private final GroupFieldsResolverImpl<T, U> groupResolver;
    private final TimeWindowResolverImpl timeWindowResolver;

    @Override
    public Double visitSum(SumContext ctx, T model) {
        String countTarget = TextUtil.safeGetText(ctx.STRING());
        return sumPaymentAggregator.sum(
                fieldResolver.resolveName(countTarget),
                model,
                timeWindowResolver.resolve(ctx.time_window()),
                groupResolver.resolve(ctx.group_by())
        );
    }

    @Override
    public Double visitSumSuccess(Sum_successContext ctx, T model) {
        String countTarget = TextUtil.safeGetText(ctx.STRING());
        return sumPaymentAggregator.sumSuccess(
                fieldResolver.resolveName(countTarget),
                model,
                timeWindowResolver.resolve(ctx.time_window()),
                groupResolver.resolve(ctx.group_by()));
    }

    @Override
    public Double visitSumError(Sum_errorContext ctx, T model) {
        String countTarget = TextUtil.safeGetText(ctx.STRING(0));
        if (ctx.STRING().size() == 2) {
            String errorCode = TextUtil.safeGetText(ctx.STRING(1));
            return sumPaymentAggregator.sumError(
                    fieldResolver.resolveName(countTarget),
                    model,
                    timeWindowResolver.resolve(ctx.time_window()),
                    errorCode,
                    groupResolver.resolve(ctx.group_by()));
        }
        return sumPaymentAggregator.sumError(
                fieldResolver.resolveName(countTarget),
                model,
                timeWindowResolver.resolve(ctx.time_window()),
                groupResolver.resolve(ctx.group_by()));
    }

    @Override
    public Double visitSumChargeback(Sum_chargebackContext ctx, T model) {
        String countTarget = TextUtil.safeGetText(ctx.STRING());
        return sumPaymentAggregator.sumChargeback(
                fieldResolver.resolveName(countTarget),
                model,
                timeWindowResolver.resolve(ctx.time_window()),
                groupResolver.resolve(ctx.group_by())
        );
    }

    @Override
    public Double visitSumRefund(Sum_refundContext ctx, T model) {
        String countTarget = TextUtil.safeGetText(ctx.STRING());
        return sumPaymentAggregator.sumRefund(
                fieldResolver.resolveName(countTarget),
                model,
                timeWindowResolver.resolve(ctx.time_window()),
                groupResolver.resolve(ctx.group_by())
        );
    }

}
