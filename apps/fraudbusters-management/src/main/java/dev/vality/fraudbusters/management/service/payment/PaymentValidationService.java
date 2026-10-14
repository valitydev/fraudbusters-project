package dev.vality.fraudbusters.management.service.payment;

import dev.vality.damsel.fraudbusters.PaymentServiceSrv;
import dev.vality.damsel.fraudbusters.Template;
import dev.vality.damsel.fraudbusters.TemplateValidateError;
import dev.vality.fraudbusters.management.exception.ValidationException;
import dev.vality.fraudbusters.management.service.iface.ValidationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentValidationService implements ValidationTemplateService {

    private final PaymentServiceSrv.Iface paymentServiceSrv;

    @Override
    public List<TemplateValidateError> validateTemplate(Template template) {
        try {
            //todo proto refactoring
            return paymentServiceSrv.validateCompilationTemplate(List.of(template)).getErrors();
        } catch (TException e) {
            log.error("Error when validateTemplate");
            throw new ValidationException(e);
        }
    }

}
