package dev.vality.fraudo.payment.resolver;

import dev.vality.fraudo.model.TrustCondition;

import java.util.List;

public interface CustomerTypeResolver<T> {

    Boolean isTrusted(T model);

    Boolean isTrusted(T model, String templateName);

    Boolean isTrusted(T model, List<TrustCondition> paymentsConditions, List<TrustCondition> withdrawalsConditions);

}
