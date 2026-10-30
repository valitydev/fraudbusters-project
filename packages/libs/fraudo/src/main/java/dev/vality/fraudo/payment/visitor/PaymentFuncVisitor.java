package dev.vality.fraudo.payment.visitor;

import dev.vality.fraudo.utils.TextUtil;
import lombok.RequiredArgsConstructor;

import static dev.vality.fraudo.FraudoRulesParser.*;

@RequiredArgsConstructor
public class PaymentFuncVisitor<T> {

    private final CountVisitor<T> countVisitor;
    private final SumVisitor<T> sumVisitor;
    private final CustomFuncVisitor<T> customFuncVisitor;
    private final IsTrustedFuncVisitor<T> isTrustedFuncVisitor;

    public Object visitIsTrusted(IsTrustedContext ctx, T model) {
        return isTrustedFuncVisitor.visitCheckTrusted(model);
    }

    public Object visitIsTrustedTemplateName(IsTrustedTemplateNameContext ctx, T model) {
        return isTrustedFuncVisitor.visitCheckTrusted(model, TextUtil.safeGetText(ctx.STRING()));
    }

    public Integer visitCount(CountContext ctx, T model) {
        return countVisitor.visitCount(ctx, model);
    }

    public Integer visitCountSuccess(Count_successContext ctx, T model) {
        return countVisitor.visitCountSuccess(ctx, model);
    }

    public Integer visitCountPending(Count_pendingContext ctx, T model) {
        return countVisitor.visitCountPending(ctx, model);
    }

    public Integer visitCountError(Count_errorContext ctx, T model) {
        return countVisitor.visitCountError(ctx, model);
    }

    public Integer visitCountChargeback(Count_chargebackContext ctx, T model) {
        return countVisitor.visitCountChargeback(ctx, model);
    }

    public Integer visitCountRefund(Count_refundContext ctx, T model) {
        return countVisitor.visitCountRefund(ctx, model);
    }

    public Double visitSum(SumContext ctx, T model) {
        return sumVisitor.visitSum(ctx, model);
    }

    public Double visitSumSuccess(Sum_successContext ctx, T model) {
        return sumVisitor.visitSumSuccess(ctx, model);
    }

    public Double visitSumError(Sum_errorContext ctx, T model) {
        return sumVisitor.visitSumError(ctx, model);
    }

    public Double visitSumChargeback(Sum_chargebackContext ctx, T model) {
        return sumVisitor.visitSumChargeback(ctx, model);
    }

    public Double visitSumRefund(Sum_refundContext ctx, T model) {
        return sumVisitor.visitSumRefund(ctx, model);
    }

    public String visitCountryBy(Country_byContext ctx, T model) {
        return customFuncVisitor.visitCountryBy(ctx, model);
    }

    public boolean visitLike(LikeContext ctx, T model) {
        return customFuncVisitor.visitLike(ctx, model);
    }

    public Integer visitUnique(UniqueContext ctx, T model) {
        return customFuncVisitor.visitUnique(ctx, model);
    }

    public Integer visitRand(RandContext ctx, T model) {
        return customFuncVisitor.visitRand(ctx, model);
    }

    public boolean visitCheckMobile(Is_mobileContext ctx, T model) {
        return customFuncVisitor.visitCheckMobile(ctx, model);
    }

    public boolean visitCheckRecurrent(Is_recurrentContext ctx, T model) {
        return customFuncVisitor.visitCheckRecurrent(ctx, model);
    }

}
