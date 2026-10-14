package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandBody;
import dev.vality.damsel.fraudbusters.TemplateReference;
import dev.vality.swag.fraudbusters.management.model.PaymentReference;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ReferenceToCommandConverter implements Converter<PaymentReference, Command> {

    @NonNull
    @Override
    public Command convert(PaymentReference referenceModel) {
        var reference = new TemplateReference()
                .setShopId(referenceModel.getShopId())
                .setPartyId(referenceModel.getPartyId())
                .setTemplateId(referenceModel.getTemplateId());
        return new Command()
                .setCommandBody(CommandBody.reference(reference));
    }

}
