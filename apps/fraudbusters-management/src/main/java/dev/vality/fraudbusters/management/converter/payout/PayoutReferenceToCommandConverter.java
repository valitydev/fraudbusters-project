package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandBody;
import dev.vality.damsel.fraudbusters.TemplateReference;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import dev.vality.swag.fraudbusters.management.model.PayoutReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PayoutReferenceToCommandConverter implements Converter<PayoutReference, Command> {

    @Override
    public Command convert(PayoutReference reference) {
        if (!StringUtils.hasText(reference.getTemplateId())) {
            throw new IllegalArgumentException("templateId is required");
        }
        boolean global = Boolean.TRUE.equals(reference.getIsGlobal());
        if (global && (StringUtils.hasText(reference.getPartyId()) || StringUtils.hasText(reference.getWalletId()))) {
            throw new IllegalArgumentException("A global payout reference has no party and wallet");
        }
        if (!global && !StringUtils.hasText(reference.getPartyId())) {
            throw new IllegalArgumentException("A payout reference is global or has a party");
        }
        PayoutScope.requirePartyForWallet(reference.getPartyId(), reference.getWalletId());
        TemplateReference templateReference = new TemplateReference()
                .setTemplateId(reference.getTemplateId())
                .setIsGlobal(global);
        if (!global && StringUtils.hasText(reference.getPartyId())) {
            templateReference.setPartyId(reference.getPartyId());
            if (StringUtils.hasText(reference.getWalletId())) {
                templateReference.setWalletId(reference.getWalletId());
            }
        }
        return new Command()
                .setCommandBody(CommandBody.reference(templateReference));
    }
}
