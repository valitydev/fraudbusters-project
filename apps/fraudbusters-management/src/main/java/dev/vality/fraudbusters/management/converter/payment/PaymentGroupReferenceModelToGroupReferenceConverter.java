package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.fraudbusters.management.domain.payment.PaymentGroupReferenceModel;
import dev.vality.fraudbusters.management.utils.DateTimeUtils;
import dev.vality.swag.fraudbusters.management.model.GroupReference;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class PaymentGroupReferenceModelToGroupReferenceConverter implements
        Converter<PaymentGroupReferenceModel, GroupReference> {

    @Override
    public GroupReference convert(
            PaymentGroupReferenceModel groupReferenceModel) {
        return new GroupReference()
                .id(groupReferenceModel.getId())
                .shopId(groupReferenceModel.getShopId())
                .partyId(groupReferenceModel.getPartyId())
                .groupId(groupReferenceModel.getGroupId())
                .lastUpdateDate(DateTimeUtils.toDate(groupReferenceModel.getLastUpdateDate()))
                .modifiedByUser(groupReferenceModel.getModifiedByUser());
    }
}
