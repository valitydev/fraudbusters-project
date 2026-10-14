package dev.vality.fraudbusters.mg.connector.mapper.initializer;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.fraudbusters.ClientInfo;
import dev.vality.damsel.fraudbusters.Error;
import dev.vality.damsel.fraudbusters.ProviderInfo;
import dev.vality.damsel.fraudbusters.ReferenceInfo;
import dev.vality.damsel.payment_processing.InvoicePayment;
import dev.vality.damsel.payment_processing.InvoicePaymentRefundStatusChanged;
import dev.vality.geck.serializer.kit.tbase.TErrorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundInfoInitiatorDecorator implements InfoInitializer<InvoicePaymentRefundStatusChanged> {

    public static final String OPERATION_TIMEOUT = "operation_timeout";

    private final GeneralInfoInitiator generalInfoInitiator;

    @Override
    public Error initError(InvoicePaymentRefundStatusChanged refundStatusChanged) {
        Error error = null;
        if (refundStatusChanged.getStatus().isSetFailed()) {
            error = new Error();
            OperationFailure operationFailure = refundStatusChanged.getStatus().getFailed().getFailure();
            if (operationFailure.isSetFailure()) {
                Failure failure = operationFailure.getFailure();
                error.setErrorCode(TErrorUtil.toStringVal(failure))
                        .setErrorReason(failure.getReason());
            } else if (refundStatusChanged.getStatus().getFailed().getFailure().isSetOperationTimeout()) {
                error.setErrorCode(OPERATION_TIMEOUT);
            } else {
                error.setErrorCode("unknown error");
            }
        }
        return error;
    }

    @Override
    public ClientInfo initClientInfo(Payer payer) {
        return generalInfoInitiator.initClientInfo(payer);
    }

    @Override
    public void initContactInfo(ClientInfo clientInfo, Payer payer) {
        generalInfoInitiator.initContactInfo(clientInfo, payer);
    }

    @NonNull
    @Override
    public ProviderInfo initProviderInfo(InvoicePayment invoicePayment) {
        return generalInfoInitiator.initProviderInfo(invoicePayment);
    }

    @Override
    public ReferenceInfo initReferenceInfo(Invoice invoice) {
        return generalInfoInitiator.initReferenceInfo(invoice);
    }

    @Override
    public PaymentTool initPaymentTool(Payer payer) {
        return generalInfoInitiator.initPaymentTool(payer);
    }

}
