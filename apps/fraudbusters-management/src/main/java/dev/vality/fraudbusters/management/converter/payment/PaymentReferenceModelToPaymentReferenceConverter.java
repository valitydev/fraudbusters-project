package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.fraudbusters.management.domain.payment.PaymentReferenceModel;
import dev.vality.fraudbusters.management.utils.DateTimeUtils;
import dev.vality.swag.fraudbusters.management.model.PaymentReference;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class PaymentReferenceModelToPaymentReferenceConverter
        implements Converter<PaymentReferenceModel, PaymentReference> {

    @NonNull
    @Override
    public PaymentReference convert(PaymentReferenceModel paymentReferenceModel) {
        return new PaymentReference()
                .templateId(paymentReferenceModel.getTemplateId())
                .id(paymentReferenceModel.getId())
                .partyId(paymentReferenceModel.getPartyId())
                .shopId(paymentReferenceModel.getShopId())
                .lastUpdateDate(DateTimeUtils.toDate(paymentReferenceModel.getLastUpdateDate()))
                .modifiedByUser(paymentReferenceModel.getModifiedByUser());
    }

}
