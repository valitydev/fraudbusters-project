package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.fraudbusters.management.domain.payout.PayoutReferenceModel;
import dev.vality.fraudbusters.management.utils.DateTimeUtils;
import dev.vality.swag.fraudbusters.management.model.PayoutReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PayoutReferenceModelToPayoutReferenceConverter
        implements Converter<PayoutReferenceModel, PayoutReference> {

    @Override
    public PayoutReference convert(PayoutReferenceModel model) {
        return new PayoutReference()
                .id(model.getId())
                .templateId(model.getTemplateId())
                .partyId(emptyToNull(model.getPartyId()))
                .walletId(emptyToNull(model.getWalletId()))
                .isGlobal(Boolean.TRUE.equals(model.getIsGlobal()))
                .lastUpdateDate(DateTimeUtils.toDate(model.getLastUpdateDate()))
                .modifiedByUser(model.getModifiedByUser());
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
