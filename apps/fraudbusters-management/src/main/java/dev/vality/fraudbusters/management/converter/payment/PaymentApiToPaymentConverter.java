package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.fraudbusters.*;
import dev.vality.damsel.fraudbusters.Error;
import dev.vality.swag.fraudbusters.management.model.Payment;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class PaymentApiToPaymentConverter
        implements Converter<Payment, dev.vality.damsel.fraudbusters.Payment> {

    @NonNull
    @Override
    public dev.vality.damsel.fraudbusters.Payment convert(Payment payment) {
        return new dev.vality.damsel.fraudbusters.Payment()
                .setId(payment.getId())
                .setClientInfo(createClientInfo(payment))
                .setCost(createCash(payment))
                .setStatus(PaymentStatus.valueOf(payment.getStatus().getValue()))
                .setError(createError(payment))
                .setEventTime(payment.getEventTime() != null
                        ? payment.getEventTime().toString()
                        : null)
                .setMobile(Optional.ofNullable(payment.getMobile()).orElse(false))
                .setRecurrent(Optional.ofNullable(payment.getRecurrent()).orElse(false))
                .setPayerType(payment.getPayerType() != null
                        ? PayerType.valueOf(payment.getPayerType())
                        : PayerType.payment_resource)
                .setPaymentTool(createPaymentTool(payment))
                .setProviderInfo(createProviderInfo(payment))
                .setReferenceInfo(ReferenceInfo.merchant_info(createMerchantInfo(payment)));
    }

    private Cash createCash(Payment payment) {
        return new Cash()
                .setAmount(payment.getAmount())
                .setCurrency(new CurrencyRef()
                        .setSymbolicCode(payment.getCurrency()));
    }

    private PaymentTool createPaymentTool(Payment payment) {
        return PaymentTool.bank_card(new BankCard()
                .setBin(payment.getBin())
                .setLastDigits(payment.getLastDigits())
                .setToken(payment.getCardToken()));
    }

    private ProviderInfo createProviderInfo(Payment payment) {
        return payment.getProvider() != null
                ? new ProviderInfo()
                .setTerminalId(payment.getProvider().getProviderId())
                .setCountry(payment.getProvider().getCountry())
                .setProviderId(payment.getProvider().getProviderId())
                : new ProviderInfo();
    }

    private MerchantInfo createMerchantInfo(Payment payment) {
        return new MerchantInfo()
                .setShopId(payment.getMerchantInfo().getShopId())
                .setPartyId(payment.getMerchantInfo().getPartyId());
    }

    private Error createError(Payment payment) {
        return payment.getError() != null
                ? new Error()
                .setErrorCode(payment.getError().getErrorCode())
                .setErrorReason(payment.getError().getErrorReason())
                : null;
    }

    private ClientInfo createClientInfo(Payment payment) {
        return payment.getClientInfo() != null
                ? new ClientInfo()
                .setIp(payment.getClientInfo().getIp())
                .setEmail(payment.getClientInfo().getEmail())
                .setFingerprint(payment.getClientInfo().getFingerprint())
                : null;
    }
}
