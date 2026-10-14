package dev.vality.fraudo.payment.factory;

import dev.vality.fraudo.bundle.VisitorBundle;
import dev.vality.fraudo.model.BaseModel;
import dev.vality.fraudo.payment.visitor.impl.FirstFindVisitorImpl;

public interface FraudVisitorFactory {

    <T extends BaseModel, U> FirstFindVisitorImpl<T, U> createVisitor(VisitorBundle<T, U> dto);

}
