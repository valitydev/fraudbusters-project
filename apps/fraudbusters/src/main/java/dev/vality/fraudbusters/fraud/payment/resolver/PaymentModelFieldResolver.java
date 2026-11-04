package dev.vality.fraudbusters.fraud.payment.resolver;

import dev.vality.fraudbusters.fraud.constant.CheckedField;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.resolver.OperationModelFieldResolver;
import dev.vality.fraudo.exception.UnresolvableFieldException;

public class PaymentModelFieldResolver extends OperationModelFieldResolver<PaymentModel> {

    @Override
    protected String resolveOperationValue(CheckedField field, String fieldName, PaymentModel paymentModel) {
        if (field == CheckedField.SHOP_ID) {
            return paymentModel.getShopId();
        }
        throw new UnresolvableFieldException(fieldName);
    }

}
