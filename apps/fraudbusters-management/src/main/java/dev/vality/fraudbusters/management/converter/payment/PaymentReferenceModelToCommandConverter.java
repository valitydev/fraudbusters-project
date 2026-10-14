package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandBody;
import dev.vality.damsel.fraudbusters.TemplateReference;
import dev.vality.fraudbusters.management.domain.payment.PaymentReferenceModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class PaymentReferenceModelToCommandConverter implements Converter<PaymentReferenceModel, Command> {

    @NonNull
    @Override
    public Command convert(PaymentReferenceModel referenceModel) {
        TemplateReference reference = new TemplateReference();
        reference.setIsGlobal(referenceModel.getIsGlobal());
        if (!referenceModel.getIsGlobal()) {
            reference.setShopId(referenceModel.getShopId());
            reference.setPartyId(referenceModel.getPartyId());
        }
        reference.setTemplateId(referenceModel.getTemplateId());
        Command command = new Command();
        command.setCommandBody(CommandBody.reference(reference));
        return command;
    }

}
