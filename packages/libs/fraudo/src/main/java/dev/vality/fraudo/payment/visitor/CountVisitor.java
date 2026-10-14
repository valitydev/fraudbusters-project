package dev.vality.fraudo.payment.visitor;

import static dev.vality.fraudo.FraudoPaymentParser.*;

public interface CountVisitor<T> {

    Integer visitCount(CountContext ctx, T model);

    Integer visitCountSuccess(Count_successContext ctx, T model);

    Integer visitCountPending(Count_pendingContext ctx, T model);

    Integer visitCountError(Count_errorContext ctx, T model);

    Integer visitCountChargeback(Count_chargebackContext ctx, T model);

    Integer visitCountRefund(Count_refundContext ctx, T model);

}
