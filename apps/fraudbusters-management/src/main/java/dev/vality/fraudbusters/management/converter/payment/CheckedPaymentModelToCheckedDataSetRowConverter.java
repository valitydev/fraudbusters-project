package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.fraudbusters.management.domain.payment.CheckedPaymentModel;
import dev.vality.swag.fraudbusters.management.model.CheckedDataSetRow;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckedPaymentModelToCheckedDataSetRowConverter
        implements Converter<CheckedPaymentModel, CheckedDataSetRow> {

    private final PaymentModelToPaymentApiConverter paymentModelToPaymentApiConverter;

    @Override
    public CheckedDataSetRow convert(CheckedPaymentModel checkedPaymentModel) {
        var testPaymentModel = checkedPaymentModel.getPaymentModel();
        return new CheckedDataSetRow()
                .id(String.valueOf(checkedPaymentModel.getTestDataSetCheckingResultId()))
                .resultStatus(checkedPaymentModel.getResultStatus())
                .ruleChecked(checkedPaymentModel.getRuleChecked())
                .checkedTemplate(checkedPaymentModel.getCheckedTemplate())
                .notificationRule(checkedPaymentModel.getNotificationRule())
                .payment(paymentModelToPaymentApiConverter.convert(testPaymentModel));
    }

}
