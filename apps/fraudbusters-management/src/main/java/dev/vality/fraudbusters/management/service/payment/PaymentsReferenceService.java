package dev.vality.fraudbusters.management.service.payment;

import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.fraudbusters.management.converter.payment.PaymentReferenceModelToCommandConverter;
import dev.vality.fraudbusters.management.converter.payment.ReferenceToCommandConverter;
import dev.vality.fraudbusters.management.dao.payment.reference.PaymentReferenceDao;
import dev.vality.fraudbusters.management.domain.payment.PaymentReferenceModel;
import dev.vality.fraudbusters.management.utils.CommandMapper;
import dev.vality.swag.fraudbusters.management.model.PaymentReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentsReferenceService {

    private final PaymentReferenceDao referenceDao;
    private final CommandMapper commandMapper;
    private final PaymentTemplateReferenceService paymentTemplateReferenceService;
    private final PaymentReferenceModelToCommandConverter paymentReferenceModelToCommandConverter;
    private final ReferenceToCommandConverter referenceToCommandConverter;

    public String removeReference(String id, String userName) {
        PaymentReferenceModel reference = referenceDao.getById(id);
        var command = paymentReferenceModelToCommandConverter.convert(reference);
        return paymentTemplateReferenceService
                .sendCommandSync(commandMapper.mapToConcreteCommand(userName, command, CommandType.DELETE));
    }

    public List<String> insertReferences(List<PaymentReference> paymentReference, String initiator) {
        return paymentReference.stream()
                .map(referenceToCommandConverter::convert)
                .map(command -> commandMapper.mapToConcreteCommand(initiator, command, CommandType.CREATE))
                .map(paymentTemplateReferenceService::sendCommandSync)
                .collect(Collectors.toList());
    }
}
