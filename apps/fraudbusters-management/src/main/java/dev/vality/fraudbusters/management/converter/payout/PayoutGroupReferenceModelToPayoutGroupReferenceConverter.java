package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.fraudbusters.management.domain.payout.PayoutGroupReferenceModel;
import dev.vality.fraudbusters.management.utils.DateTimeUtils;
import dev.vality.swag.fraudbusters.management.model.PayoutGroupReference;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Component
@RequiredArgsConstructor
public class PayoutGroupReferenceModelToPayoutGroupReferenceConverter
        implements Converter<PayoutGroupReferenceModel, PayoutGroupReference> {

    @Override
    public PayoutGroupReference convert(PayoutGroupReferenceModel model) {
        return new PayoutGroupReference()
                .id(model.getId())
                .groupId(model.getGroupId())
                .partyId(StringUtils.hasText(model.getPartyId()) ? model.getPartyId() : null)
                .walletId(StringUtils.hasText(model.getWalletId()) ? model.getWalletId() : null)
                .lastUpdateDate(DateTimeUtils.toDate(model.getLastUpdateDate()))
                .modifiedByUser(model.getModifiedByUser());
    }
}