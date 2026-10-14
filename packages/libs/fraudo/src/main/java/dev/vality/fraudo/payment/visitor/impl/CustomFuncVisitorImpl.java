package dev.vality.fraudo.payment.visitor.impl;

import dev.vality.fraudo.aggregator.UniqueValueAggregator;
import dev.vality.fraudo.model.Pair;
import dev.vality.fraudo.payment.resolver.PaymentGroupResolver;
import dev.vality.fraudo.payment.resolver.PaymentTimeWindowResolver;
import dev.vality.fraudo.payment.resolver.PaymentTypeResolver;
import dev.vality.fraudo.payment.visitor.CustomFuncVisitor;
import dev.vality.fraudo.resolver.CountryResolver;
import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.utils.TextUtil;
import lombok.RequiredArgsConstructor;

import java.util.Random;

import static dev.vality.fraudo.FraudoPaymentParser.*;

@RequiredArgsConstructor
public class CustomFuncVisitorImpl<T, U> implements CustomFuncVisitor<T> {

    private final UniqueValueAggregator<T, U> uniqueValueAggregator;
    private final CountryResolver<U> countryResolver;
    private final FieldResolver<T, U> fieldResolver;
    private final PaymentGroupResolver<T, U> groupResolver;
    private final PaymentTimeWindowResolver timeWindowResolver;
    private final PaymentTypeResolver<T> paymentTypeResolver;

    @Override
    public String visitCountryBy(Country_byContext ctx, T model) {
        String fieldName = TextUtil.safeGetText(ctx.STRING());
        Pair<U, String> resolve = fieldResolver.resolve(fieldName, model);
        return countryResolver.resolveCountry(resolve.getFirst(), resolve.getSecond());
    }

    @Override
    public boolean visitLike(LikeContext ctx, T model) {
        String fieldName = TextUtil.safeGetText(ctx.STRING(0));
        String fieldValue = fieldResolver.resolve(fieldName, model).getSecond();
        String pattern = TextUtil.safeGetText(ctx.STRING(1));
        if (fieldValue == null) {
            return false;
        }
        return fieldValue.matches(pattern);
    }

    @Override
    public Integer visitUnique(UniqueContext ctx, T model) {
        String field = TextUtil.safeGetText(ctx.STRING(0));
        String fieldBy = TextUtil.safeGetText(ctx.STRING(1));
        return uniqueValueAggregator.countUniqueValue(
                fieldResolver.resolveName(field),
                model,
                fieldResolver.resolveName(fieldBy),
                timeWindowResolver.resolve(ctx.time_window()),
                groupResolver.resolve(ctx.group_by())
        );
    }

    @Override
    public Integer visitRand(RandContext ctx, T model) {
        Integer bound = TextUtil.safeGetInteger(ctx.INTEGER());
        return new Random().nextInt(bound);
    }

    @Override
    public boolean visitCheckMobile(Is_mobileContext ctx, T model) {
        return paymentTypeResolver.isMobile(model);
    }

    @Override
    public boolean visitCheckRecurrent(Is_recurrentContext ctx, T model) {
        return paymentTypeResolver.isRecurrent(model);
    }

}
