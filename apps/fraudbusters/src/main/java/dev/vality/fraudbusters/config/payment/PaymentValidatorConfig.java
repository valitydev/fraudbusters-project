package dev.vality.fraudbusters.config.payment;

import dev.vality.fraudbusters.fraud.InspectStorageRegistry;
import dev.vality.fraudbusters.fraud.ListTemplateValidator;
import dev.vality.fraudbusters.fraud.payment.resolver.DatabasePaymentFieldResolver;
import dev.vality.fraudbusters.fraud.payment.validator.PaymentHistoryFieldValidator;
import dev.vality.fraudbusters.fraud.validator.ErrorCodeFormatValidator;
import dev.vality.fraudbusters.fraud.validator.ListTemplateValidatorImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentValidatorConfig {

    @Bean
    public ListTemplateValidator paymentTemplatesValidator(InspectStorageRegistry inspectStorageRegistry,
                                                           DatabasePaymentFieldResolver databasePaymentFieldResolver) {
        return new ListTemplateValidatorImpl(inspectStorageRegistry, new ErrorCodeFormatValidator(),
                new PaymentHistoryFieldValidator(databasePaymentFieldResolver));
    }

}
