package dev.vality.fraudo.payment.resolver;

public interface CustomerTypeResolver<T> {

    Boolean isTrusted(T model);

    Boolean isTrusted(T model, String templateName);

}
