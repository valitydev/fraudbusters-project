package dev.vality.fraudbusters.fraud.payout;

import dev.vality.fraudbusters.fraud.constant.CheckedField;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.fraud.resolver.OperationModelFieldResolver;
import dev.vality.fraudo.exception.UnresolvableFieldException;

public class PayoutModelFieldResolver extends OperationModelFieldResolver<PayoutModel> {

    @Override
    protected String resolveOperationValue(CheckedField field, String fieldName, PayoutModel payoutModel) {
        if (field == CheckedField.WALLET_ID) {
            return payoutModel.getWalletId();
        }
        throw new UnresolvableFieldException(fieldName);
    }

}
