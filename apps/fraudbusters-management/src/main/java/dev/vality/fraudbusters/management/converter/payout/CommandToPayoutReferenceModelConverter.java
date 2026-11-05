package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.TemplateReference;
import dev.vality.fraudbusters.management.domain.payout.PayoutReferenceModel;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CommandToPayoutReferenceModelConverter implements Converter<Command, PayoutReferenceModel> {

    @Override
    public PayoutReferenceModel convert(Command command) {
        TemplateReference reference = command.getCommandBody().getReference();
        PayoutReferenceModel model = new PayoutReferenceModel();
        model.setIsGlobal(reference.isIsGlobal());
        model.setTemplateId(reference.getTemplateId());
        if (reference.isIsGlobal()) {
            model.setPartyId("");
            model.setWalletId("");
        } else {
            PayoutScope.validate(reference.getPartyId(), reference.getWalletId());
            model.setPartyId(PayoutScope.normalizeId(reference.getPartyId()));
            model.setWalletId(PayoutScope.normalizeId(reference.getWalletId()));
        }
        return model;
    }
}
