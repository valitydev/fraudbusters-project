package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.fraudbusters.management.domain.payment.PaymentModel;
import dev.vality.fraudbusters.management.utils.DateTimeUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;



@Component
public class PaymentToTestPaymentModelConverter
        implements Converter<dev.vality.damsel.fraudbusters.Payment, PaymentModel> {

    @NonNull
    @Override
    public PaymentModel convert(dev.vality.damsel.fraudbusters.Payment payment) {
        PaymentModel.PaymentModelBuilder builder = PaymentModel.builder();
        if (payment.isSetReferenceInfo()) {
            updateReferenceInfo(builder, payment.getReferenceInfo());
        }
        if (payment.getPaymentTool().isSetBankCard()) {
            updateBankCardData(builder, payment.getPaymentTool().getBankCard());
        }
        if (payment.isSetProviderInfo()) {
            updateProviderInfo(builder, payment.getProviderInfo());
        }
        if (payment.isSetClientInfo()) {
            updateClientInfo(builder, payment.getClientInfo());
        }
        if (payment.isSetError()) {
            updateError(builder, payment.getError());
        }
        return builder
                .currency(payment.getCost().getCurrency().getSymbolicCode())
                .amount(payment.getCost().getAmount())
                .eventTime(payment.getEventTime() != null
                        ? DateTimeUtils.toDate(payment.getEventTime())
                        : null)
                .paymentId(payment.getId())
                .status(payment.getStatus() != null
                        ? payment.getStatus().name()
                        : null)
                .paymentTool(payment.getPaymentTool().getFieldValue().toString())
                .payerType(payment.getPayerType().name())
                .build();
    }

    private void updateError(PaymentModel.PaymentModelBuilder builder, dev.vality.damsel.fraudbusters.Error error) {
        builder.errorCode(error.getErrorCode())
                .errorReason(error.getErrorReason());
    }

    private void updateClientInfo(PaymentModel.PaymentModelBuilder builder,
                                  dev.vality.damsel.fraudbusters.ClientInfo clientInfo) {
        builder.ip(clientInfo.getIp())
                .fingerprint(clientInfo.getFingerprint())
                .email(clientInfo.getEmail());
    }

    private void updateProviderInfo(PaymentModel.PaymentModelBuilder builder,
                                    dev.vality.damsel.fraudbusters.ProviderInfo providerInfo) {
        builder.terminalId(providerInfo.getTerminalId())
                .providerId(providerInfo.getProviderId())
                .country(providerInfo.getCountry());
    }

    private void updateBankCardData(PaymentModel.PaymentModelBuilder builder,
                                    dev.vality.damsel.domain.BankCard bankCard) {
        builder.paymentSystem(bankCard.isSetPaymentSystem() ? bankCard.getPaymentSystem().getId() : null)
                .paymentCountry(bankCard.isSetIssuerCountry() ? bankCard.getIssuerCountry().name() : null)
                .cardToken(bankCard.getToken())
                .bin(bankCard.getBin())
                .lastDigits(bankCard.getLastDigits());
    }

    private void updateReferenceInfo(PaymentModel.PaymentModelBuilder builder,
                                     dev.vality.damsel.fraudbusters.ReferenceInfo referenceInfo) {
        builder.partyId(referenceInfo.getMerchantInfo().getPartyId())
                .shopId(referenceInfo.getMerchantInfo().getShopId());
    }
}
