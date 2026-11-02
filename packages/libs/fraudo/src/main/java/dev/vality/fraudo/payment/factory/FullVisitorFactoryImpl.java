package dev.vality.fraudo.payment.factory;

import dev.vality.fraudo.model.BaseModel;
import dev.vality.fraudo.payment.visitor.ListVisitor;
import dev.vality.fraudo.payment.visitor.PaymentFuncVisitor;
import dev.vality.fraudo.payout.visitor.PayoutFuncVisitor;
import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.visitor.FullRulesVisitor;

public class FullVisitorFactoryImpl extends FraudVisitorFactoryImpl {

    @Override
    protected <T extends BaseModel, U> FullRulesVisitor<T, U> createRulesVisitor(
            PaymentFuncVisitor<T> paymentFuncVisitor,
            ListVisitor<T> listVisitor,
            FieldResolver<T, U> fieldResolver,
            PayoutFuncVisitor<T, U> payoutFuncVisitor
    ) {
        return new FullRulesVisitor<>(paymentFuncVisitor, listVisitor, fieldResolver, payoutFuncVisitor);
    }

}
