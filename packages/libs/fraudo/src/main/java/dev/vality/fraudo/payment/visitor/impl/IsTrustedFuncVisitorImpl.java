package dev.vality.fraudo.payment.visitor.impl;

import dev.vality.fraudo.model.TrustCondition;
import dev.vality.fraudo.payment.resolver.CustomerTypeResolver;
import dev.vality.fraudo.payment.visitor.IsTrustedFuncVisitor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class IsTrustedFuncVisitorImpl<T> implements IsTrustedFuncVisitor<T> {

    private final CustomerTypeResolver<T> customerTypeResolver;

    @Override
    public boolean visitCheckTrusted(T model) {
        return customerTypeResolver.isTrusted(model);
    }

    @Override
    public boolean visitCheckTrusted(T model, String templateName) {
        return customerTypeResolver.isTrusted(model, templateName);
    }

    @Override
    public boolean visitCheckTrusted(T model,
                                     List<TrustCondition> paymentsConditionsList,
                                     List<TrustCondition> withdrawalConditionsList) {
        return customerTypeResolver.isTrusted(model, paymentsConditionsList, withdrawalConditionsList);
    }

}
