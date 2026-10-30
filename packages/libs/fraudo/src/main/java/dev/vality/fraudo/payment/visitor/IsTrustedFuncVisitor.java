package dev.vality.fraudo.payment.visitor;

public interface IsTrustedFuncVisitor<T> {

    boolean visitCheckTrusted(T model);

    boolean visitCheckTrusted(T model, String templateName);
}
