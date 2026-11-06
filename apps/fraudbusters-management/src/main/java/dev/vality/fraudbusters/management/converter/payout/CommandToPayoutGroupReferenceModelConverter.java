package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.GroupReference;
import dev.vality.fraudbusters.management.domain.payout.PayoutGroupReferenceModel;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CommandToPayoutGroupReferenceModelConverter implements Converter<Command, PayoutGroupReferenceModel> {

    @Override
    public PayoutGroupReferenceModel convert(Command command) {
        GroupReference reference = command.getCommandBody().getGroupReference();
        PayoutScope.validate(reference.getPartyId(), reference.getWalletId());
        PayoutGroupReferenceModel model = new PayoutGroupReferenceModel();
        model.setPartyId(PayoutScope.normalizeId(reference.getPartyId()));
        model.setWalletId(PayoutScope.normalizeId(reference.getWalletId()));
        model.setGroupId(reference.getGroupId());
        return model;
    }
}
