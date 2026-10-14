package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandBody;
import dev.vality.swag.fraudbusters.management.model.GroupReference;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class PaymentGroupReferenceModelToCommandConverter implements Converter<GroupReference, Command> {

    @Override
    public Command convert(GroupReference groupReferenceModel) {
        return new Command()
                .setCommandBody(CommandBody.group_reference(
                        new dev.vality.damsel.fraudbusters.GroupReference()
                                .setShopId(groupReferenceModel.getShopId())
                                .setPartyId(groupReferenceModel.getPartyId())
                                .setGroupId(groupReferenceModel.getGroupId()))
                );
    }
}
