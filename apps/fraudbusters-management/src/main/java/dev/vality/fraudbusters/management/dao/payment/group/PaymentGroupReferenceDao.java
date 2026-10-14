package dev.vality.fraudbusters.management.dao.payment.group;

import dev.vality.fraudbusters.management.dao.GroupReferenceDao;
import dev.vality.fraudbusters.management.domain.payment.PaymentGroupReferenceModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;

import java.util.List;

public interface PaymentGroupReferenceDao extends GroupReferenceDao<PaymentGroupReferenceModel> {

    void remove(String partyId, String shopId);

    List<PaymentGroupReferenceModel> getByPartyIdAndShopId(String partyId, String shopId);

    List<PaymentGroupReferenceModel> filterReference(FilterRequest filterRequest);

    Integer countFilterReference(String filterValue);
}
