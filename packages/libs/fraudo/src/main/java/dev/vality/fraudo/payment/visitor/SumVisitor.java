package dev.vality.fraudo.payment.visitor;

import static dev.vality.fraudo.FraudoRulesParser.*;

public interface SumVisitor<T> {

    Double visitSum(SumContext ctx, T model);

    Double visitSumSuccess(Sum_successContext ctx, T model);

    Double visitSumError(Sum_errorContext ctx, T model);

    Double visitSumChargeback(Sum_chargebackContext ctx, T model);

    Double visitSumRefund(Sum_refundContext ctx, T model);

}
